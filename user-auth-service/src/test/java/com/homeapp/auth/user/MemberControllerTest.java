package com.homeapp.auth.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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

import com.homeapp.auth.security.SecurityConfig;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
class MemberControllerTest {

    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();

    private static final String ZOSIA = """
            {
              "firstName": "Zosia",
              "birthDate": "2025-09-01"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // Udajemy zalogowanego użytkownika: token z householdId i rolą, bez prawdziwego logowania.
    private static JwtRequestPostProcessor loggedInAs(String role) {
        return jwt()
            .jwt(token -> token
                .subject(UUID.randomUUID().toString())
                .claim("householdId", HOUSEHOLD_ID.toString())
                .claim("role", role))
            .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void adminShouldAddMemberToOwnHousehold() throws Exception {
        UserResponse response = new UserResponse(
            UUID.randomUUID(), HOUSEHOLD_ID, null, "Zosia",
            LocalDate.of(2025, 9, 1), null, Role.USER, false, Instant.now());
        given(userService.addMember(eq(HOUSEHOLD_ID), any())).willReturn(response);

        mockMvc.perform(post("/api/households/me/members")
                .with(loggedInAs("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ZOSIA))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.householdId").value(HOUSEHOLD_ID.toString()));
    }

    @Test
    void userShouldNotAddMember() throws Exception {
        mockMvc.perform(post("/api/households/me/members")
                .with(loggedInAs("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ZOSIA))
            .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/households/me/members"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void anyMemberShouldListOwnHousehold() throws Exception {
        given(userService.listMembers(HOUSEHOLD_ID)).willReturn(List.of());

        mockMvc.perform(get("/api/households/me/members").with(loggedInAs("USER")))
            .andExpect(status().isOk());
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

        mockMvc.perform(post("/api/households/me/members")
                .with(loggedInAs("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.loginDataComplete").exists());
    }
}
