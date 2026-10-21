package com.homeapp.notification.notification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Jeden wiersz historii: komu, którym kanałem, co i czy się udało.
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "digest_date", nullable = false)
    private LocalDate digestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Channel channel;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationStatus status;

    @Column(nullable = false)
    private String message;

    private String error;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Notification() {
        // wymagany przez JPA
    }

    public Notification(UUID userId, UUID householdId, LocalDate digestDate, Channel channel, String recipient,
                        NotificationStatus status, String message, String error, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.householdId = householdId;
        this.digestDate = digestDate;
        this.channel = channel;
        this.recipient = recipient;
        this.status = status;
        this.message = message;
        this.error = error;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getHouseholdId() { return householdId; }
    public LocalDate getDigestDate() { return digestDate; }
    public Channel getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public NotificationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public String getError() { return error; }
    public Instant getCreatedAt() { return createdAt; }
}
