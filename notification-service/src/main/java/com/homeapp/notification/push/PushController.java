package com.homeapp.notification.push;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.homeapp.notification.security.CurrentUser;

// Powiadomienia push dla każdego zalogowanego (rodzica i dziecka). Kto, to wiemy z tokenu.
@RestController
@RequestMapping("/api/notifications/push")
public class PushController {

    private final PushService pushService;
    private final PushSender pushSender;

    public PushController(PushService pushService, PushSender pushSender) {
        this.pushService = pushService;
        this.pushSender = pushSender;
    }

    // Przeglądarka potrzebuje klucza publicznego, żeby zapisać się na powiadomienia właśnie od nas.
    @GetMapping("/public-key")
    public PublicKeyResponse publicKey() {
        return new PublicKeyResponse(pushSender.publicKey());
    }

    @PutMapping("/subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@AuthenticationPrincipal Jwt jwt, @RequestBody SubscriptionRequest request) {
        pushService.subscribe(CurrentUser.userId(jwt), request);
    }

    @DeleteMapping("/subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@AuthenticationPrincipal Jwt jwt, @RequestBody SubscriptionRequest request) {
        pushService.unsubscribe(CurrentUser.userId(jwt), request.endpoint());
    }

    // Przycisk "Wyślij próbne powiadomienie": sprawdzasz, czy działa, bez czekania na poranne podsumowanie.
    @PostMapping("/test")
    public TestPushResponse sendTest(@AuthenticationPrincipal Jwt jwt) {
        int delivered = pushService.sendToUser(CurrentUser.userId(jwt), new PushMessage(
            "Próbne powiadomienie", "Działa! Tak będą wyglądać poranne podsumowania zadań.", "/"));
        return new TestPushResponse(delivered);
    }
}
