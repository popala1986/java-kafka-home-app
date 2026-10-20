package com.homeapp.planner.chore;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// "Ta osoba nie dostaje tego obowiązku", nawet jeśli wiekiem się łapie.
@Entity
@Table(name = "chore_exclusions")
public class ChoreExclusion {

    @Id
    private UUID id;

    @Column(name = "chore_id", nullable = false)
    private UUID choreId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected ChoreExclusion() {
        // wymagany przez JPA
    }

    public ChoreExclusion(UUID choreId, UUID userId) {
        this.id = UUID.randomUUID();
        this.choreId = choreId;
        this.userId = userId;
    }

    public UUID getId() { return id; }
    public UUID getChoreId() { return choreId; }
    public UUID getUserId() { return userId; }
}
