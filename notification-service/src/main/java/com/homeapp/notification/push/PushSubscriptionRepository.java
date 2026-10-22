package com.homeapp.notification.push;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {

    List<PushSubscription> findAllByUserId(UUID userId);

    Optional<PushSubscription> findByEndpoint(String endpoint);

    boolean existsByUserId(UUID userId);

    // Usuwanie po polach (nie po id) potrzebuje transakcji, więc dopisujemy ją od razu tutaj.
    @Transactional
    void deleteByEndpointAndUserId(String endpoint, UUID userId);

    @Transactional
    void deleteAllByUserId(UUID userId);
}
