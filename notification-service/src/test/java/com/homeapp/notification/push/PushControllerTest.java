package com.homeapp.notification.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.notification.security.SecurityConfig;


@WebMvcTest(PushController.class)
@Import(SecurityConfig.class)
class PushControllerTest {

    private static final UUID OLA = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PushService pushService;

    @MockitoBean
    private PushSender pushSender;

    // Dziecko (USER) też może włączyć powiadomienia: to nie jest akcja tylko dla rodzica.
    private static JwtRequestPostProcessor ola() {
        return jwt()
            .jwt(token -> token
                .subject(OLA.toString())
                .claim("householdId", UUID.randomUUID().toString())
                .claim("role", "USER"))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void shouldReturnPublicKey() throws Exception {
        given(pushSender.publicKey()).willReturn("BPubliczny");

        mockMvc.perform(get("/api/notifications/push/public-key").with(ola()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publicKey").value("BPubliczny"));
    }

    @Test
    void shouldSaveSubscriptionOfLoggedInPerson() throws Exception {
        mockMvc.perform(put("/api/notifications/push/subscriptions").with(ola())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"endpoint": "https://fcm.googleapis.com/fcm/send/abc",
                     "expirationTime": null,
                     "keys": {"p256dh": "klucz", "auth": "sekret"}}
                    """))
            .andExpect(status().isNoContent());

        ArgumentCaptor<SubscriptionRequest> request = ArgumentCaptor.forClass(SubscriptionRequest.class);
        verify(pushService).subscribe(eq(OLA), request.capture());
        assertThat(request.getValue().endpoint()).isEqualTo("https://fcm.googleapis.com/fcm/send/abc");
        assertThat(request.getValue().keys().auth()).isEqualTo("sekret");
    }

    @Test
    void shouldSendTestPushToMyDevices() throws Exception {
        given(pushService.sendToUser(eq(OLA), any())).willReturn(2);

        mockMvc.perform(post("/api/notifications/push/test").with(ola()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.delivered").value(2));
    }

    @Test
    void shouldRejectWithoutToken() throws Exception {
        mockMvc.perform(post("/api/notifications/push/test"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(pushService);
    }
}
