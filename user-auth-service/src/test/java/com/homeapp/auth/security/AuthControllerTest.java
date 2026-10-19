package com.homeapp.auth.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.auth.user.UserService;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String LOGIN_BODY = """
            {
              "email": "jan@example.com",
              "password": "Haslo123!"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnTokenForValidCredentials() throws Exception {
        given(authService.login(any())).willReturn(new LoginResponse("abc.def.ghi", "Bearer", 3600));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("abc.def.ghi"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturn401ForInvalidCredentials() throws Exception {
        given(authService.login(any())).willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.title").value("Logowanie nieudane"));
    }

    @Test
    void meShouldRequireToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }
}
