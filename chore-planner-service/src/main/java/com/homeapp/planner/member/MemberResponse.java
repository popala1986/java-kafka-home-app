package com.homeapp.planner.member;

import java.time.LocalDate;
import java.util.UUID;

public record MemberResponse(
    UUID userId,
    String firstName,
    LocalDate birthDate,
    String role,
    int age,
    AgeGroup ageGroup
) {

    public static MemberResponse from(HouseholdMember member, LocalDate today) {
        int age = AgeGroup.ageOn(member.getBirthDate(), today);
        return new MemberResponse(
            member.getUserId(),
            member.getFirstName(),
            member.getBirthDate(),
            member.getRole(),
            age,
            AgeGroup.forAge(age));
    }
}
