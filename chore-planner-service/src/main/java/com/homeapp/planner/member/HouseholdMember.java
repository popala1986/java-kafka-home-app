package com.homeapp.planner.member;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "household_members")
public class HouseholdMember {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    protected HouseholdMember() {
        // wymagany przez JPA
    }

    public HouseholdMember(UUID userId, UUID householdId, String firstName,
                           LocalDate birthDate, String role, Instant registeredAt) {
        this.userId = userId;
        this.householdId = householdId;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.role = role;
        this.registeredAt = registeredAt;
    }

    public static HouseholdMember from(UserRegisteredEvent event) {
        return new HouseholdMember(
            event.userId(),
            event.householdId(),
            event.firstName(),
            event.birthDate(),
            event.role(),
            event.occurredAt());
    }

    // Zmiana przyszła z user-auth-service (event user.updated).
    public void updateProfile(String firstName, LocalDate birthDate) {
        this.firstName = firstName;
        this.birthDate = birthDate;
    }

    public UUID getUserId() { return userId; }
    public UUID getHouseholdId() { return householdId; }
    public String getFirstName() { return firstName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getRole() { return role; }
    public Instant getRegisteredAt() { return registeredAt; }
}
