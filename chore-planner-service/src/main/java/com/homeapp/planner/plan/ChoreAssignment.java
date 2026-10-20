package com.homeapp.planner.plan;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chore_assignments")
public class ChoreAssignment {

    @Id
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "chore_id", nullable = false)
    private UUID choreId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // true = admin zmienił osobę ręcznie
    @Column(nullable = false)
    private boolean manual;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ChoreAssignment() {
        // wymagany przez JPA
    }

    public ChoreAssignment(UUID householdId, LocalDate weekStart, PlannedAssignment planned, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.householdId = householdId;
        this.weekStart = weekStart;
        this.assignedDate = planned.date();
        this.choreId = planned.choreId();
        this.userId = planned.userId();
        this.manual = false;
        this.createdAt = createdAt;
    }

    public void reassignTo(UUID newUserId) {
        this.userId = newUserId;
        this.manual = true;
    }

    public UUID getId() { return id; }
    public UUID getHouseholdId() { return householdId; }
    public LocalDate getWeekStart() { return weekStart; }
    public LocalDate getAssignedDate() { return assignedDate; }
    public UUID getChoreId() { return choreId; }
    public UUID getUserId() { return userId; }
    public boolean isManual() { return manual; }
    public Instant getCreatedAt() { return createdAt; }
}
