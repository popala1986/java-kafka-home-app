package com.homeapp.notification.push;

// Klucz publiczny VAPID dla przeglądarki. null = serwer nie ma kluczy i push jest wyłączony.
public record PublicKeyResponse(String publicKey) {
}
