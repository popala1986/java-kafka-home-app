package com.homeapp.auth.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findAllByHouseholdIdOrderByBirthDateAsc(UUID householdId);

    // Użytkownik, ale tylko z tej rodziny: obcy wygląda jak nieistniejący (404).
    Optional<User> findByIdAndHouseholdId(UUID id, UUID householdId);
}
