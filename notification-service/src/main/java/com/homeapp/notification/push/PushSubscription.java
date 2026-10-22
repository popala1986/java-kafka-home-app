package com.homeapp.notification.push;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Jedno urządzenie (telefon albo przeglądarka na laptopie), które zgodziło się na powiadomienia.
@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 1000)
    private String endpoint;

    @Column(nullable = false)
    private String p256dh;

    @Column(nullable = false)
    private String auth;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PushSubscription() {
        // wymagany przez JPA
    }

    public PushSubscription(UUID userId, String endpoint, String p256dh, String auth, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.createdAt = createdAt;
    }

    // Ten sam telefon, ale np. zalogował się na nim ktoś inny: urządzenie przechodzi do tej osoby.
    public void update(UUID userId, String p256dh, String auth) {
        this.userId = userId;
        this.p256dh = p256dh;
        this.auth = auth;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getEndpoint() { return endpoint; }
    public String getP256dh() { return p256dh; }
    public String getAuth() { return auth; }
    public Instant getCreatedAt() { return createdAt; }
}
