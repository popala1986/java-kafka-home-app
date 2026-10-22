package com.homeapp.notification.push;

// Treść powiadomienia. Jako JSON trafia do sw.js (zdarzenie "push"), a ten pokazuje je na ekranie.
// url = co otworzyć po kliknięciu w powiadomienie.
public record PushMessage(String title, String body, String url) {
}
