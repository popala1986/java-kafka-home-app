package com.homeapp.notification.push;

// Dokładnie to, co daje przeglądarka w subscription.toJSON():
// { "endpoint": "https://fcm.googleapis.com/...", "keys": { "p256dh": "...", "auth": "..." } }
public record SubscriptionRequest(String endpoint, Keys keys) {

    public record Keys(String p256dh, String auth) {
    }
}
