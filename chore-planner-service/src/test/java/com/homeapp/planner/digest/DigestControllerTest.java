package com.homeapp.planner.digest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.planner.security.SecurityConfig;

@WebMvcTest(DigestController.class)
@Import(SecurityConfig.class)
class DigestControllerTest {

    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DailyDigestService digestService;

    private static JwtRequestPostProcessor loggedInAs(String role) {
        return jwt()
            .jwt(token -> token
                .subject(UUID.randomUUID().toString())
                .claim("householdId", HOUSEHOLD_ID.toString())
                .claim("role", role))
            .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void adminShouldSendDigests() throws Exception {
        given(digestService.sendDigests(HOUSEHOLD_ID, LocalDate.of(2026, 10, 26))).willReturn(2);

        mockMvc.perform(post("/api/planner/digests/2026-10-26").with(loggedInAs("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sent").value(2));
    }

    @Test
    void userShouldNotSendDigests() throws Exception {
        mockMvc.perform(post("/api/planner/digests/2026-10-26").with(loggedInAs("USER")))
            .andExpect(status().isForbidden());

        verifyNoInteractions(digestService);
    }
}
