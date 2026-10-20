package com.homeapp.planner.plan;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.planner.chore.ChoreType;
import com.homeapp.planner.security.SecurityConfig;

@WebMvcTest(PlanController.class)
@Import(SecurityConfig.class)
class PlanControllerTest {

    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 26);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanService planService;

    private static JwtRequestPostProcessor loggedInAs(String role) {
        return jwt()
            .jwt(token -> token
                .subject(USER_ID.toString())
                .claim("householdId", HOUSEHOLD_ID.toString())
                .claim("role", role))
            .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static AssignmentResponse bed() {
        return new AssignmentResponse(UUID.randomUUID(), MONDAY, UUID.randomUUID(), "Pościel łóżko",
            ChoreType.PERSONAL, 1, USER_ID, "Ola", false);
    }

    @Test
    void adminShouldGeneratePlan() throws Exception {
        given(planService.generatePlan(HOUSEHOLD_ID, MONDAY)).willReturn(List.of(bed()));

        mockMvc.perform(post("/api/planner/plans/2026-10-26").with(loggedInAs("ADMIN")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].choreName").value("Pościel łóżko"));
    }

    @Test
    void userShouldNotGeneratePlan() throws Exception {
        mockMvc.perform(post("/api/planner/plans/2026-10-26").with(loggedInAs("USER")))
            .andExpect(status().isForbidden());

        verifyNoInteractions(planService);
    }

    @Test
    void userShouldSeeOwnAssignments() throws Exception {
        given(planService.getMyPlan(HOUSEHOLD_ID, USER_ID, MONDAY)).willReturn(List.of(bed()));

        mockMvc.perform(get("/api/planner/plans/2026-10-26/me").with(loggedInAs("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Ola"));
    }

    @Test
    void shouldReturn400WhenWeekDoesNotStartOnMonday() throws Exception {
        LocalDate tuesday = MONDAY.plusDays(1);
        given(planService.getPlan(HOUSEHOLD_ID, tuesday)).willThrow(new InvalidWeekStartException(tuesday));

        mockMvc.perform(get("/api/planner/plans/2026-10-27").with(loggedInAs("USER")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Zły początek tygodnia"));
    }

    @Test
    void userShouldNotReassign() throws Exception {
        mockMvc.perform(put("/api/planner/plans/2026-10-26/assignments/{id}", UUID.randomUUID())
                .with(loggedInAs("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\": \"" + UUID.randomUUID() + "\"}"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(planService);
    }
}
