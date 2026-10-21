package com.homeapp.notification.notification;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.homeapp.notification.contact.Contact;
import com.homeapp.notification.contact.ContactRepository;
import com.homeapp.notification.digest.DailyDigestEvent;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ContactRepository contactRepository;
    private final NotificationRepository notificationRepository;
    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final Clock clock;

    public NotificationService(ContactRepository contactRepository,
                               NotificationRepository notificationRepository,
                               SmsSender smsSender,
                               EmailSender emailSender,
                               Clock clock) {
        this.contactRepository = contactRepository;
        this.notificationRepository = notificationRepository;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
        this.clock = clock;
    }

    // SMS, jeśli osoba ma telefon, i e-mail, jeśli ma adres. Każdy kanał najwyżej raz na dzień.
    public void sendDigest(DailyDigestEvent event) {
        Contact contact = contactRepository.findById(event.userId()).orElse(null);
        if (contact == null) {
            log.warn("Brak kontaktu do {} (userId={}), nic nie wysyłam", event.firstName(), event.userId());
            return;
        }
        if (contact.getPhoneNumber() == null && contact.getEmail() == null) {
            log.info("{} nie ma telefonu ani maila, nic nie wysyłam", event.firstName());
            return;
        }

        if (contact.getPhoneNumber() != null) {
            String text = DigestMessages.sms(event);
            send(event, Channel.SMS, contact.getPhoneNumber(), text,
                () -> smsSender.send(contact.getPhoneNumber(), text));
        }

        if (contact.getEmail() != null) {
            String body = DigestMessages.emailBody(event);
            send(event, Channel.EMAIL, contact.getEmail(), body,
                () -> emailSender.send(contact.getEmail(), DigestMessages.emailSubject(event), body));
        }
    }

    public List<NotificationResponse> history(UUID householdId) {
        return notificationRepository.findAllByHouseholdIdOrderByCreatedAtDesc(householdId).stream()
            .map(NotificationResponse::from)
            .toList();
    }

    private void send(DailyDigestEvent event, Channel channel, String recipient, String message, Runnable sending) {
        if (notificationRepository.existsByUserIdAndDigestDateAndChannel(event.userId(), event.date(), channel)) {
            log.info("{} na {} już wysłany do {}, pomijam duplikat", channel, event.date(), event.firstName());
            return;
        }

        NotificationStatus status = NotificationStatus.SENT;
        String error = null;
        try {
            sending.run();
            log.info("Wysłano {} do {} ({})", channel, event.firstName(), recipient);
        } catch (RuntimeException ex) {
            // Np. Mailpit nie działa. Zapisujemy porażkę w historii, a drugi kanał i tak próbujemy wysłać.
            status = NotificationStatus.FAILED;
            error = ex.getMessage();
            log.error("Nie udało się wysłać {} do {}", channel, recipient, ex);
        }

        notificationRepository.save(new Notification(event.userId(), event.householdId(), event.date(), channel,
            recipient, status, message, error, Instant.now(clock)));
    }
}
