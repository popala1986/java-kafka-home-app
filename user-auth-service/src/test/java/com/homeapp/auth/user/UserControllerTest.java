package com.homeapp.auth.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldRegisterUser() throws Exception {
        UserResponse response = new UserResponse(
            UUID.randomUUID(), "jan@example.com", "Jan",
            LocalDate.of(2011, 5, 20), "+48600100200", Role.USER, Instant.now());
        given(userService.register(any())).willReturn(response);

        String body = """
                {
                  "email": "jan@example.com",
                  "password": "Haslo123!",
                  "firstName": "Jan",
                  "birthDate": "2011-05-20",
                  "phoneNumber": "+48600100200"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("jan@example.com"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn400WhenDataIsInvalid() throws Exception {
        String body = """
                {
                  "email": "to-nie-email",
                  "password": "123",
                  "firstName": "",
                  "phoneNumber": "abc"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists())
            .andExpect(jsonPath("$.errors.firstName").exists())
            .andExpect(jsonPath("$.errors.birthDate").exists())
            .andExpect(jsonPath("$.errors.phoneNumber").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn409WhenEmailIsTaken() throws Exception {
        given(userService.register(any())).willThrow(new EmailAlreadyTakenException("jan@example.com"));

        String body = """
                {
                  "email": "jan@example.com",
                  "password": "Haslo123!",
                  "firstName": "Jan",
                  "birthDate": "2011-05-20",
                  "phoneNumber": "+48600100200"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict());
    }
}
