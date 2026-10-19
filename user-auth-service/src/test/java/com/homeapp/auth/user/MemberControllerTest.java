package com.homeapp.auth.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.auth.household.HouseholdNotFoundException;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldAddMemberWithoutLogin() throws Exception {
        UserResponse response = new UserResponse(
            UUID.randomUUID(), HOUSEHOLD_ID, null, "Zosia",
            LocalDate.of(2025, 9, 1), null, Role.USER, false, Instant.now());
        given(userService.addMember(eq(HOUSEHOLD_ID), any())).willReturn(response);

        String body = """
                {
                  "firstName": "Zosia",
                  "birthDate": "2025-09-01"
                }
                """;

        mockMvc.perform(post("/api/households/{id}/members", HOUSEHOLD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Zosia"))
            .andExpect(jsonPath("$.canLogIn").value(false))
            .andExpect(jsonPath("$.householdId").value(HOUSEHOLD_ID.toString()));
    }

    @Test
    void shouldReturn400WhenEmailGivenWithoutPassword() throws Exception {
        String body = """
                {
                  "firstName": "Ola",
                  "birthDate": "2012-04-30",
                  "email": "ola@example.com"
                }
                """;

        mockMvc.perform(post("/api/households/{id}/members", HOUSEHOLD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.loginDataComplete").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn404WhenHouseholdDoesNotExist() throws Exception {
        given(userService.listMembers(HOUSEHOLD_ID)).willThrow(new HouseholdNotFoundException(HOUSEHOLD_ID));

        mockMvc.perform(get("/api/households/{id}/members", HOUSEHOLD_ID))
            .andExpect(status().isNotFound());
    }
}
