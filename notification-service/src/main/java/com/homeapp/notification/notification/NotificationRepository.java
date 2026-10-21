package com.homeapp.notification.notification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // "Czy ta osoba dostała już coś tym kanałem na ten dzień?"
    boolean existsByUserIdAndDigestDateAndChannel(UUID userId, LocalDate digestDate, Channel channel);

    List<Notification> findAllByHouseholdIdOrderByCreatedAtDesc(UUID householdId);
}
