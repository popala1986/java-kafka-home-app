package com.homeapp.auth.user;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    List<User> findAllByHouseholdIdOrderByBirthDateAsc(UUID householdId);
}
