package com.homeapp.notification.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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

import com.homeapp.notification.contact.Contact;
import com.homeapp.notification.contact.ContactRepository;
import com.homeapp.notification.digest.DailyDigestEvent;
import com.homeapp.notification.digest.DigestTask;
import com.homeapp.notification.push.PushMessage;
import com.homeapp.notification.push.PushService;

class NotificationServiceTest {

    private static final UUID OLA = UUID.randomUUID();
    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();
    private static final LocalDate DAY = LocalDate.of(2026, 10, 20);

    private final ContactRepository contactRepository = mock(ContactRepository.class);
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final PushService pushService = mock(PushService.class);
    private final EmailSender emailSender = mock(EmailSender.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-20T04:30:00Z"), ZoneId.of("Europe/Warsaw"));
    private final NotificationService service =
        new NotificationService(contactRepository, notificationRepository, pushService, emailSender, clock);

    private static DailyDigestEvent digest() {
        return new DailyDigestEvent(UUID.randomUUID(), OLA, HOUSEHOLD_ID, "Ola", DAY,
            List.of(new DigestTask("Pościel łóżko", 1)), 1, Instant.now());
    }

    private void olaHasContact(String email) {
        given(contactRepository.findById(OLA)).willReturn(Optional.of(
            new Contact(OLA, HOUSEHOLD_ID, "Ola", email, null, Instant.now())));
    }

    private List<Notification> savedHistory(int count) {
        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(count)).save(saved.capture());
        return saved.getAllValues();
    }

    @Test
    void shouldSendPushAndSkipEmail() {
        olaHasContact("ola@opala.pl");
        given(pushService.hasDevices(OLA)).willReturn(true);
        given(pushService.sendToUser(eq(OLA), any())).willReturn(2);

        service.sendDigest(digest());

        ArgumentCaptor<PushMessage> message = ArgumentCaptor.forClass(PushMessage.class);
        verify(pushService).sendToUser(eq(OLA), message.capture());
        assertThat(message.getValue().title()).isEqualTo("Ola, Twoje zadania na wtorek 20.10");
        verifyNoInteractions(emailSender);
        assertThat(savedHistory(1)).extracting(Notification::getChannel, Notification::getStatus)
            .containsExactly(tuple(Channel.PUSH, NotificationStatus.SENT));
    }

    @Test
    void shouldSendEmailWhenNoPushDevices() {
        olaHasContact("ola@opala.pl");
        given(pushService.hasDevices(OLA)).willReturn(false);

        service.sendDigest(digest());

        verify(pushService, never()).sendToUser(any(), any());
        verify(emailSender).send(eq("ola@opala.pl"), anyString(), anyString());
        assertThat(savedHistory(1)).extracting(Notification::getChannel).containsExactly(Channel.EMAIL);
    }

    @Test
    void shouldFallBackToEmailWhenNoDeviceAcceptedPush() {
        olaHasContact("ola@opala.pl");
        given(pushService.hasDevices(OLA)).willReturn(true);
        given(pushService.sendToUser(eq(OLA), any())).willReturn(0);

        service.sendDigest(digest());

        verify(emailSender).send(eq("ola@opala.pl"), anyString(), anyString());
        List<Notification> history = savedHistory(2);
        assertThat(history.get(0).getChannel()).isEqualTo(Channel.PUSH);
        assertThat(history.get(0).getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(history.get(0).getError()).contains("Żadne urządzenie");
        assertThat(history.get(1).getChannel()).isEqualTo(Channel.EMAIL);
        assertThat(history.get(1).getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void shouldNotSendTwiceTheSameDay() {
        olaHasContact("ola@opala.pl");
        given(pushService.hasDevices(OLA)).willReturn(true);
        Notification earlierPush = new Notification(OLA, HOUSEHOLD_ID, DAY, Channel.PUSH, "aplikacja",
            NotificationStatus.SENT, "...", null, Instant.now());
        given(notificationRepository.findByUserIdAndDigestDateAndChannel(OLA, DAY, Channel.PUSH))
            .willReturn(Optional.of(earlierPush));

        service.sendDigest(digest());

        verify(pushService, never()).sendToUser(any(), any());
        verifyNoInteractions(emailSender);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldSendPushEvenWithoutContact() {
        given(contactRepository.findById(OLA)).willReturn(Optional.empty());
        given(pushService.hasDevices(OLA)).willReturn(true);
        given(pushService.sendToUser(eq(OLA), any())).willReturn(1);

        service.sendDigest(digest());

        verifyNoInteractions(emailSender);
        assertThat(savedHistory(1)).extracting(Notification::getChannel).containsExactly(Channel.PUSH);
    }

    @Test
    void shouldSendNothingWithoutDevicesAndEmail() {
        olaHasContact(null);
        given(pushService.hasDevices(OLA)).willReturn(false);

        service.sendDigest(digest());

        verifyNoInteractions(emailSender);
        verify(notificationRepository, never()).save(any());
    }
}
