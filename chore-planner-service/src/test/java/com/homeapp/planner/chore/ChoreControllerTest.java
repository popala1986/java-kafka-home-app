package com.homeapp.planner.chore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.homeapp.planner.security.SecurityConfig;

@WebMvcTest(ChoreController.class)
@Import(SecurityConfig.class)
class ChoreControllerTest {

    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();

    private static final String PODLEJ_OGROD = """
            {
              "name": "Podlej ogród",
              "type": "SHARED",
              "frequency": "THREE_TIMES_A_WEEK",
              "minAge": 8,
              "durationMinutes": 15,
              "points": 1,
              "active": true
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChoreService choreService;

    private static JwtRequestPostProcessor loggedInAs(String role) {
        return jwt()
            .jwt(token -> token
                .subject(UUID.randomUUID().toString())
                .claim("householdId", HOUSEHOLD_ID.toString())
                .claim("role", role))
            .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static ChoreResponse podlejOgrod() {
        return new ChoreResponse(UUID.randomUUID(), null, "Podlej ogród", ChoreType.SHARED,
            Frequency.THREE_TIMES_A_WEEK, 8, 15, 1, true);
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/planner/chores"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void anyMemberShouldListChoresOfOwnHousehold() throws Exception {
        given(choreService.listChores(HOUSEHOLD_ID)).willReturn(List.of(podlejOgrod()));

        mockMvc.perform(get("/api/planner/chores").with(loggedInAs("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Podlej ogród"));
    }

    @Test
    void adminShouldAddChore() throws Exception {
        given(choreService.addChore(eq(HOUSEHOLD_ID), any())).willReturn(podlejOgrod());

        mockMvc.perform(post("/api/planner/chores")
                .with(loggedInAs("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PODLEJ_OGROD))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.type").value("SHARED"));
    }

    @Test
    void userShouldNotAddChore() throws Exception {
        mockMvc.perform(post("/api/planner/chores")
                .with(loggedInAs("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PODLEJ_OGROD))
            .andExpect(status().isForbidden());

        verifyNoInteractions(choreService);
    }

    @Test
    void shouldReturn400WhenPointsOutOfRange() throws Exception {
        String body = PODLEJ_OGROD.replace("\"points\": 1", "\"points\": 5");

        mockMvc.perform(post("/api/planner/chores")
                .with(loggedInAs("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.points").value("Punkty: od 1 do 3"));
    }

    @Test
    void shouldReturn404WhenChoreNotInHousehold() throws Exception {
        UUID choreId = UUID.randomUUID();
        given(choreService.getChore(HOUSEHOLD_ID, choreId)).willThrow(new ChoreNotFoundException(choreId));

        mockMvc.perform(get("/api/planner/chores/{id}", choreId).with(loggedInAs("USER")))
            .andExpect(status().isNotFound());
    }

    @Test
    void userShouldNotDeleteChore() throws Exception {
        mockMvc.perform(delete("/api/planner/chores/{id}", UUID.randomUUID()).with(loggedInAs("USER")))
            .andExpect(status().isForbidden());

        verifyNoInteractions(choreService);
    }
}
