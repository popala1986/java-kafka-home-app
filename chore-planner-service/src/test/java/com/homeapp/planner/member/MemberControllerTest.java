package com.homeapp.planner.member;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.planner.security.SecurityConfig;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
class MemberControllerTest {

    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HouseholdMemberRepository memberRepository;

    // Stały "dzisiaj", żeby wiek w odpowiedzi zawsze wychodził taki sam.
    @MockitoBean
    private Clock clock;

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/planner/households/me/members"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListMembersOfHouseholdFromToken() throws Exception {
        given(clock.instant()).willReturn(Instant.parse("2026-10-19T08:00:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Europe/Warsaw"));
        HouseholdMember stas = new HouseholdMember(UUID.randomUUID(), HOUSEHOLD_ID, "Staś",
            LocalDate.of(2020, 5, 1), "USER", Instant.now());
        given(memberRepository.findAllByHouseholdIdOrderByBirthDateAsc(HOUSEHOLD_ID)).willReturn(List.of(stas));

        mockMvc.perform(get("/api/planner/households/me/members")
                .with(jwt().jwt(token -> token.claim("householdId", HOUSEHOLD_ID.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Staś"))
            .andExpect(jsonPath("$[0].ageGroup").value("AGE_4_7"));
    }
}
