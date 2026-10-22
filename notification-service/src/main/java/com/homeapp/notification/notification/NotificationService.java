package com.homeapp.notification.notification;
import java.util.List;

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
import com.homeapp.notification.push.PushMessage;
import com.homeapp.notification.push.PushService;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ContactRepository contactRepository;
    private final NotificationRepository notificationRepository;
    private final PushService pushService;
    private final EmailSender emailSender;
    private final Clock clock;

    public NotificationService(ContactRepository contactRepository,
                               NotificationRepository notificationRepository,
                               PushService pushService,
                               EmailSender emailSender,
                               Clock clock) {
        this.contactRepository = contactRepository;
        this.notificationRepository = notificationRepository;
        this.pushService = pushService;
        this.emailSender = emailSender;
        this.clock = clock;
    }

    // Najpierw push na telefon. E-mail jest zapasem: idzie tylko wtedy, gdy osoba nie włączyła
    // powiadomień albo żadne jej urządzenie ich nie przyjęło. Każdy kanał najwyżej raz na dzień.
    public void sendDigest(DailyDigestEvent event) {
        if (pushService.hasDevices(event.userId())) {
            PushMessage message = new PushMessage(DigestMessages.pushTitle(event), DigestMessages.pushBody(event), "/");
            boolean pushed = send(event, Channel.PUSH, "aplikacja", message.body(), () -> {
                if (pushService.sendToUser(event.userId(), message) == 0) {
                    throw new IllegalStateException("Żadne urządzenie nie przyjęło powiadomienia");
                }
            });
            if (pushed) {
                return;
            }
        }

        // Do maila potrzebny jest kontakt (adres), który przychodzi z Kafki (user.registered, user.updated).
        Contact contact = contactRepository.findById(event.userId()).orElse(null);
        if (contact == null || contact.getEmail() == null) {
            log.info("{} nie ma powiadomień push ani maila, nic nie wysyłam", event.firstName());
            return;
        }
        String body = DigestMessages.emailBody(event);
        send(event, Channel.EMAIL, contact.getEmail(), body,
            () -> emailSender.send(contact.getEmail(), DigestMessages.emailSubject(event), body));
    }

    public List<NotificationResponse> history(UUID householdId) {
        return notificationRepository.findAllByHouseholdIdOrderByCreatedAtDesc(householdId).stream()
            .map(NotificationResponse::from)
            .toList();
    }

    // Zwraca true, gdy wiadomość tym kanałem dotarła: teraz albo już wcześniej tego dnia.
    private boolean send(DailyDigestEvent event, Channel channel, String recipient, String message, Runnable sending) {
        Notification earlier = notificationRepository
            .findByUserIdAndDigestDateAndChannel(event.userId(), event.date(), channel)
            .orElse(null);
        if (earlier != null) {
            log.info("{} na {} już wysłany do {}, pomijam duplikat", channel, event.date(), event.firstName());
            return earlier.getStatus() == NotificationStatus.SENT;
        }

        NotificationStatus status = NotificationStatus.SENT;
        String error = null;
        try {
            sending.run();
            log.info("Wysłano {} do {} ({})", channel, event.firstName(), recipient);
        } catch (RuntimeException ex) {
            // Np. Mailpit nie działa albo telefon wygasł. Zapisujemy porażkę w historii.
            status = NotificationStatus.FAILED;
            error = ex.getMessage();
            log.error("Nie udało się wysłać {} do {}", channel, recipient, ex);
        }

        notificationRepository.save(new Notification(event.userId(), event.householdId(), event.date(), channel,
            recipient, status, message, error, Instant.now(clock)));
        return status == NotificationStatus.SENT;
    }
}
