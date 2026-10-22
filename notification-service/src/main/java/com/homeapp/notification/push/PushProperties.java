package com.homeapp.notification.push;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Klucze VAPID z application.yaml (app.push.*), a tam ze zmiennych środowiskowych.
@ConfigurationProperties(prefix = "app.push")
public record PushProperties(String publicKey, String privateKey, String subject) {

    public boolean isConfigured() {
        return publicKey != null && !publicKey.isBlank() && privateKey != null && !privateKey.isBlank();
    }
}
