package com.homeapp.notification.digest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.homeapp.notification.notification.NotificationService;

@Component
public class DailyDigestListener {

    private static final Logger log = LoggerFactory.getLogger(DailyDigestListener.class);

    private final NotificationService notificationService;

    public DailyDigestListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // auto.offset.reset=latest: przy PIERWSZYM starcie serwis nie czyta starych podsumowań (np. testowych z Kroku 9a),
    // tylko te, które przyjdą od teraz. Kontakty (ContactListener) czytamy od początku, bo są potrzebne wszystkie.
    @KafkaListener(
        topics = "chore.daily.digest",
        properties = {
            "spring.json.value.default.type=com.homeapp.notification.digest.DailyDigestEvent",
            "auto.offset.reset=latest"
        })
    public void onDailyDigest(@Payload DailyDigestEvent event) {
        log.info("Odebrano chore.daily.digest dla {} na {} ({} zadań)",
            event.firstName(), event.date(), event.tasks().size());
        notificationService.sendDigest(event);
    }
}
