package com.homeapp.notification.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.interaso.webpush.VapidKeys;
import com.interaso.webpush.WebPush;
import com.interaso.webpush.WebPushService;

// "Listonosz" powiadomień: szyfruje treść kluczami urządzenia, podpisuje kluczem VAPID
// i wysyła do serwera push przeglądarki (Google dla Chrome i Androida, Apple dla iPhone'a).
// Resztę kodu nie obchodzi, jak to działa w środku: zna tylko send().
@Component
@EnableConfigurationProperties(PushProperties.class)
public class PushSender {

    private static final Logger log = LoggerFactory.getLogger(PushSender.class);

    // Poranne podsumowanie po 12 godzinach jest już nieaktualne. Telefon wyłączony dłużej go nie dostanie.
    private static final int TIME_TO_LIVE_SECONDS = 12 * 60 * 60;
    // Urgency.High = "obudź telefon od razu". Bez tego zablokowany telefon w trybie oszczędzania baterii
    // może odłożyć powiadomienie na później (Android tak robi z wiadomościami o normalnej ważności).

    private final WebPushService webPush;
    private final String publicKey;

    public PushSender(PushProperties properties) {
        if (properties.isConfigured()) {
            this.webPush = new WebPushService(properties.subject(),
                VapidKeys.fromUncompressedBytes(properties.publicKey(), properties.privateKey()));
            this.publicKey = properties.publicKey();
        } else {
            log.warn("Brak kluczy VAPID (VAPID_PUBLIC_KEY, VAPID_PRIVATE_KEY): powiadomienia push są wyłączone");
            this.webPush = null;
            this.publicKey = null;
        }
    }

    public boolean isEnabled() {
        return webPush != null;
    }

    public String publicKey() {
        return publicKey;
    }

    // true = dostarczone. false = urządzenie już nie istnieje (np. ktoś odinstalował apkę).
    // Każdy inny problem (brak internetu, zły klucz) to wyjątek.
    public boolean send(PushSubscription subscription, String payload) {
        try {
            WebPush.SubscriptionState state = webPush.send(payload, subscription.getEndpoint(),
                subscription.getP256dh(), subscription.getAuth(), TIME_TO_LIVE_SECONDS, null, WebPush.Urgency.High);
            return state == WebPush.SubscriptionState.ACTIVE;
        } catch (Exception ex) {
            // Biblioteka jest napisana w Kotlinie, który nie deklaruje wyjątków, więc łapiemy wszystkie.
            throw new IllegalStateException("Serwer push odrzucił powiadomienie: " + ex.getMessage(), ex);
        }
    }
}
