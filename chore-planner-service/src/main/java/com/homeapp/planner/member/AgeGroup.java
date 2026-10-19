package com.homeapp.planner.member;

import java.time.LocalDate;
import java.time.Period;

// Grupy wiekowe z listy obowiązków (plik 01-lista-obowiazkow.md).
public enum AgeGroup {

    TOO_YOUNG(0),
    AGE_4_7(4),
    AGE_8_10(8),
    AGE_11_13(11),
    AGE_14_17(14),
    ADULT(18);

    private final int minAge;

    AgeGroup(int minAge) {
        this.minAge = minAge;
    }

    public int getMinAge() {
        return minAge;
    }

    public static int ageOn(LocalDate birthDate, LocalDate today) {
        return Period.between(birthDate, today).getYears();
    }

    public static AgeGroup forAge(int age) {
        AgeGroup result = TOO_YOUNG;
        for (AgeGroup group : values()) {
            if (age >= group.minAge) {
                result = group;
            }
        }
        return result;
    }
}
