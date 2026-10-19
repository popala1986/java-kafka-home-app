package com.homeapp.planner.member;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {

    List<HouseholdMember> findAllByHouseholdIdOrderByBirthDateAsc(UUID householdId);
}
