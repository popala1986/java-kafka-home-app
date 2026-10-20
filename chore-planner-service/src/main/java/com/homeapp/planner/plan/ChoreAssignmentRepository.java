package com.homeapp.planner.plan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoreAssignmentRepository extends JpaRepository<ChoreAssignment, UUID> {

    List<ChoreAssignment> findAllByHouseholdIdAndWeekStart(UUID householdId, LocalDate weekStart);

    boolean existsByHouseholdIdAndWeekStart(UUID householdId, LocalDate weekStart);

    void deleteAllByHouseholdIdAndWeekStart(UUID householdId, LocalDate weekStart);

    Optional<ChoreAssignment> findByIdAndHouseholdId(UUID id, UUID householdId);
}
