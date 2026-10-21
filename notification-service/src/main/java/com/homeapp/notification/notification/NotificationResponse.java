package com.homeapp.notification.notification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID userId,
    LocalDate digestDate,
    Channel channel,
    String recipient,
    NotificationStatus status,
    String message,
    String error,
    Instant createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUserId(),
            notification.getDigestDate(),
            notification.getChannel(),
            notification.getRecipient(),
            notification.getStatus(),
            notification.getMessage(),
            notification.getError(),
            notification.getCreatedAt());
    }
}
