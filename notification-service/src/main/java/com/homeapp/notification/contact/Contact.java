package com.homeapp.notification.contact;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Contact() {
        // wymagany przez JPA
    }

    public Contact(UUID userId, UUID householdId, String firstName, String email, String phoneNumber,
                   Instant updatedAt) {
        this.userId = userId;
        this.householdId = householdId;
        this.firstName = firstName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.updatedAt = updatedAt;
    }

    public void update(String firstName, String email, String phoneNumber, Instant updatedAt) {
        this.firstName = firstName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() { return userId; }
    public UUID getHouseholdId() { return householdId; }
    public String getFirstName() { return firstName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Instant getUpdatedAt() { return updatedAt; }
}
