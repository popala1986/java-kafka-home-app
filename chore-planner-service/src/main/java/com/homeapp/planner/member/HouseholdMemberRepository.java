package com.homeapp.planner.member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {

    List<HouseholdMember> findAllByHouseholdIdOrderByBirthDateAsc(UUID householdId);

    // Domownik, ale tylko z tej rodziny: obcy domownik wygląda jak nieistniejący (404).
    Optional<HouseholdMember> findByUserIdAndHouseholdId(UUID userId, UUID householdId);

    // Wszystkie rodziny, które planner zna. Używa tego automat w niedzielę wieczorem.
    @Query("select distinct m.householdId from HouseholdMember m")
    List<UUID> findAllHouseholdIds();
}
