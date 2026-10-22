package com.homeapp.notification.push;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.json.JsonMapper;

@Service
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private final PushSubscriptionRepository subscriptionRepository;
    private final PushSender pushSender;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public PushService(PushSubscriptionRepository subscriptionRepository,
                       PushSender pushSender,
                       JsonMapper jsonMapper,
                       Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.pushSender = pushSender;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    // Zapisz urządzenie albo przepisz je na osobę, która jest teraz zalogowana (upsert po endpoint).
    @Transactional
    public void subscribe(UUID userId, SubscriptionRequest request) {
        PushSubscription existing = subscriptionRepository.findByEndpoint(request.endpoint()).orElse(null);
        if (existing == null) {
            subscriptionRepository.save(new PushSubscription(userId, request.endpoint(),
                request.keys().p256dh(), request.keys().auth(), Instant.now(clock)));
            log.info("Nowe urządzenie z powiadomieniami push dla userId={}", userId);
        } else {
            // Obiekt z bazy w transakcji: JPA sam zapisze zmiany na końcu metody.
            existing.update(userId, request.keys().p256dh(), request.keys().auth());
        }
    }

    public void unsubscribe(UUID userId, String endpoint) {
        subscriptionRepository.deleteByEndpointAndUserId(endpoint, userId);
    }

    public boolean hasDevices(UUID userId) {
        return pushSender.isEnabled() && subscriptionRepository.existsByUserId(userId);
    }

    // Wysyła na wszystkie urządzenia osoby i zwraca, na ile dotarło. 0 = nigdzie.
    public int sendToUser(UUID userId, PushMessage message) {
        if (!pushSender.isEnabled()) {
            return 0;
        }
        String payload = jsonMapper.writeValueAsString(message);
        int delivered = 0;
        for (PushSubscription subscription : subscriptionRepository.findAllByUserId(userId)) {
            try {
                if (pushSender.send(subscription, payload)) {
                    delivered++;
                } else {
                    // 404/410 od Google/Apple: tego urządzenia już nie ma. Nie ma sensu wysyłać tam jutro.
                    subscriptionRepository.delete(subscription);
                    log.info("Urządzenie userId={} wygasło, usuwam subskrypcję", userId);
                }
            } catch (RuntimeException ex) {
                // Jedno urządzenie nie działa, ale pozostałe próbujemy dalej.
                log.error("Nie udało się wysłać push do userId={}", userId, ex);
            }
        }
        return delivered;
    }
}
