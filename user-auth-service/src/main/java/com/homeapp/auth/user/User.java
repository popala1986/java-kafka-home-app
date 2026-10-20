package com.homeapp.auth.user;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.homeapp.auth.household.Household;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {
        // wymagany przez JPA
    }

    public User(Household household, String email, String passwordHash, String firstName,
                LocalDate birthDate, String phoneNumber, Role role) {
        this.id = UUID.randomUUID();
        this.household = household;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Household getHousehold() { return household; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhoneNumber() { return phoneNumber; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }

    // Admin poprawia dane domownika. Email, hasło i rola zostają bez zmian.
    public void updateProfile(String firstName, LocalDate birthDate, String phoneNumber) {
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
    }

    public boolean canLogIn() {
        return email != null && passwordHash != null;
    }
}
