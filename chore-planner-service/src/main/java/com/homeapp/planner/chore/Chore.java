package com.homeapp.planner.chore;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chores")
public class Chore {

    @Id
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    // Kod z listy startowej (np. K8-03). Obowiązki dodane przez admina nie mają kodu.
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChoreType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Frequency frequency;

    @Column(name = "min_age", nullable = false)
    private int minAge;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Chore() {
        // wymagany przez JPA
    }

    public Chore(UUID householdId, ChoreRequest request, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.householdId = householdId;
        this.createdAt = createdAt;
        update(request);
    }

    // Admin zmienia wszystko poza id, rodziną, kodem i datą utworzenia.
    public void update(ChoreRequest request) {
        this.name = request.name();
        this.type = request.type();
        this.frequency = request.frequency();
        this.minAge = request.minAge();
        this.durationMinutes = request.durationMinutes();
        this.points = request.points();
        this.active = request.active();
    }

    public UUID getId() { return id; }
    public UUID getHouseholdId() { return householdId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public ChoreType getType() { return type; }
    public Frequency getFrequency() { return frequency; }
    public int getMinAge() { return minAge; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getPoints() { return points; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
