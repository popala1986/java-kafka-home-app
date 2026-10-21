package com.homeapp.notification.notification;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.notification.security.SecurityConfig;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {

    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private static JwtRequestPostProcessor loggedInAs(String role) {
        return jwt()
            .jwt(token -> token
                .subject(UUID.randomUUID().toString())
                .claim("householdId", HOUSEHOLD_ID.toString())
                .claim("role", role))
            .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void adminShouldSeeHouseholdHistory() throws Exception {
        given(notificationService.history(HOUSEHOLD_ID)).willReturn(List.of(new NotificationResponse(
            UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2026, 10, 20), Channel.SMS, "500600700",
            NotificationStatus.SENT, "Ola, wtorek 20.10: ...", null, Instant.now())));

        mockMvc.perform(get("/api/notifications").with(loggedInAs("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].channel").value("SMS"));
    }

    @Test
    void userShouldNotSeeHistory() throws Exception {
        mockMvc.perform(get("/api/notifications").with(loggedInAs("USER")))
            .andExpect(status().isForbidden());

        verifyNoInteractions(notificationService);
    }
}
