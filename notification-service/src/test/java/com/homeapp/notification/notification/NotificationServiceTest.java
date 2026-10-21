package com.homeapp.notification.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;

import com.homeapp.notification.contact.Contact;
import com.homeapp.notification.contact.ContactRepository;
import com.homeapp.notification.digest.DailyDigestEvent;
import com.homeapp.notification.digest.DigestTask;

class NotificationServiceTest {

    private static final UUID OLA = UUID.randomUUID();
    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();
    private static final LocalDate DAY = LocalDate.of(2026, 10, 20);

    private final ContactRepository contactRepository = mock(ContactRepository.class);
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final SmsSender smsSender = mock(SmsSender.class);
    private final EmailSender emailSender = mock(EmailSender.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-20T04:30:00Z"), ZoneId.of("Europe/Warsaw"));
    private final NotificationService service =
        new NotificationService(contactRepository, notificationRepository, smsSender, emailSender, clock);

    private static DailyDigestEvent digest() {
        return new DailyDigestEvent(UUID.randomUUID(), OLA, HOUSEHOLD_ID, "Ola", DAY,
            List.of(new DigestTask("Pościel łóżko", 1)), 1, Instant.now());
    }

    private void olaHasContact(String email, String phone) {
        given(contactRepository.findById(OLA)).willReturn(Optional.of(
            new Contact(OLA, HOUSEHOLD_ID, "Ola", email, phone, Instant.now())));
    }

    @Test
    void shouldSendSmsAndEmailAndSaveHistory() {
        olaHasContact("ola@opala.pl", "500600700");

        service.sendDigest(digest());

        verify(smsSender).send(eq("500600700"), anyString());
        verify(emailSender).send(eq("ola@opala.pl"), anyString(), anyString());
        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(Notification::getChannel)
            .containsExactly(Channel.SMS, Channel.EMAIL);
        assertThat(saved.getAllValues()).extracting(Notification::getStatus)
            .containsOnly(NotificationStatus.SENT);
    }

    @Test
    void shouldNotSendTwiceTheSameDay() {
        olaHasContact("ola@opala.pl", "500600700");
        given(notificationRepository.existsByUserIdAndDigestDateAndChannel(eq(OLA), eq(DAY), any()))
            .willReturn(true);

        service.sendDigest(digest());

        verifyNoInteractions(smsSender, emailSender);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldSendNothingWithoutContact() {
        given(contactRepository.findById(OLA)).willReturn(Optional.empty());

        service.sendDigest(digest());

        verifyNoInteractions(smsSender, emailSender, notificationRepository);
    }

    @Test
    void shouldSaveFailedEmailAndStillSendSms() {
        olaHasContact("ola@opala.pl", "500600700");
        willThrow(new MailSendException("Mailpit nie odpowiada"))
            .given(emailSender).send(anyString(), anyString(), anyString());

        service.sendDigest(digest());

        verify(smsSender).send(eq("500600700"), anyString());
        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(saved.capture());
        Notification email = saved.getAllValues().get(1);
        assertThat(email.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(email.getError()).contains("Mailpit nie odpowiada");
    }
}
