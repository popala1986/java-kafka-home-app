package com.homeapp.planner.chore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChoreRepository extends JpaRepository<Chore, UUID> {

    List<Chore> findAllByHouseholdIdOrderByMinAgeAscNameAsc(UUID householdId);

    // Szukamy po id ORAZ rodzinie: obowiązek innej rodziny wygląda jak nieistniejący (404).
    Optional<Chore> findByIdAndHouseholdId(UUID id, UUID householdId);

    boolean existsByHouseholdId(UUID householdId);

    // Kopiuje 50 obowiązków ze wzoru (chore_templates) do jednej rodziny jednym zapytaniem SQL.
    @Modifying
    @Query(value = """
            INSERT INTO chores (id, household_id, code, name, type, frequency,
                                min_age, duration_minutes, points, active, created_at)
            SELECT gen_random_uuid(), :householdId, code, name, type, frequency,
                   min_age, duration_minutes, points, TRUE, now()
            FROM chore_templates
            """, nativeQuery = true)
    int copyTemplatesToHousehold(@Param("householdId") UUID householdId);
}
