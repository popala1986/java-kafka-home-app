package com.homeapp.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.auth.ping.PingController;
import com.homeapp.auth.security.SecurityConfig;

@WebMvcTest(PingController.class)
@Import(SecurityConfig.class)
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/api/auth/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("user-auth-service"))
            .andExpect(jsonPath("$.status").value("OK"));
    }
}
