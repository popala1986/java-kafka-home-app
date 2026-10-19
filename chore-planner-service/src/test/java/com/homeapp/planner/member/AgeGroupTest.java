package com.homeapp.planner.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeGroupTest {

    @ParameterizedTest
    @CsvSource({
        "0, TOO_YOUNG",
        "3, TOO_YOUNG",
        "4, AGE_4_7",
        "7, AGE_4_7",
        "8, AGE_8_10",
        "13, AGE_11_13",
        "14, AGE_14_17",
        "17, AGE_14_17",
        "18, ADULT",
        "40, ADULT"
    })
    void shouldMapAgeToGroup(int age, AgeGroup expected) {
        assertThat(AgeGroup.forAge(age)).isEqualTo(expected);
    }

    @Test
    void shouldCountAgeOnlyAfterBirthday() {
        LocalDate birthDate = LocalDate.of(2012, 4, 30);

        assertThat(AgeGroup.ageOn(birthDate, LocalDate.of(2026, 4, 29))).isEqualTo(13);
        assertThat(AgeGroup.ageOn(birthDate, LocalDate.of(2026, 4, 30))).isEqualTo(14);
    }
}
