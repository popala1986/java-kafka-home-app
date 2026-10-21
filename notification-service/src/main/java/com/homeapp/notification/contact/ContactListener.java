package com.homeapp.notification.contact;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// Buduje kopię kontaktów z dwóch topików. Oba eventy robią to samo: "zapisz albo popraw" (upsert).
// Dzięki temu user.updated wystarczy, żeby poznać osobę, której user.registered nigdy tu nie dotarł.
@Component
public class ContactListener {

    private static final Logger log = LoggerFactory.getLogger(ContactListener.class);

    private final ContactRepository contactRepository;

    public ContactListener(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @KafkaListener(
        topics = "user.registered",
        properties = "spring.json.value.default.type=com.homeapp.notification.contact.UserRegisteredEvent")
    public void onUserRegistered(@Payload UserRegisteredEvent event) {
        if (event.householdId() == null) {
            // Eventy z Kroku 4 (przed rodzinami) nie mają householdId: pomijamy je.
            log.warn("Pomijam event bez householdId (stary format) userId={}", event.userId());
            return;
        }
        save(event.userId(), event.householdId(), event.firstName(), event.email(), event.phoneNumber(),
            event.occurredAt());
    }

    @KafkaListener(
        topics = "user.updated",
        properties = "spring.json.value.default.type=com.homeapp.notification.contact.UserUpdatedEvent")
    public void onUserUpdated(@Payload UserUpdatedEvent event) {
        save(event.userId(), event.householdId(), event.firstName(), event.email(), event.phoneNumber(),
            event.occurredAt());
    }

    private void save(UUID userId, UUID householdId, String firstName, String email, String phoneNumber,
                      Instant occurredAt) {
        Contact contact = contactRepository.findById(userId).orElse(null);

        if (contact == null) {
            contact = new Contact(userId, householdId, firstName, email, phoneNumber, occurredAt);
        } else if (occurredAt.isBefore(contact.getUpdatedAt())) {
            // Dwa topiki = brak gwarancji kolejności między nimi. Starszy event nie nadpisuje nowszych danych.
            log.info("Pomijam starszy event dla {} z {}", userId, occurredAt);
            return;
        } else {
            contact.update(firstName, email, phoneNumber, occurredAt);
        }

        contactRepository.save(contact);
        log.info("Kontakt {}: email={}, telefon={}", firstName, email != null, phoneNumber != null);
    }
}
