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

    private static final String VALID_BODY = """
            {
              "householdName": "Kowalscy",
              "email": "jan@example.com",
              "password": "Haslo123!",
              "firstName": "Jan",
              "birthDate": "1986-05-20",
              "phoneNumber": "+48600100200"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldRegisterUserAsHouseholdAdmin() throws Exception {
        UUID householdId = UUID.randomUUID();
        UserResponse response = new UserResponse(
            UUID.randomUUID(), householdId, "jan@example.com", "Jan",
            LocalDate.of(1986, 5, 20), "+48600100200", Role.ADMIN, true, Instant.now());
        given(userService.register(any())).willReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("jan@example.com"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.householdId").value(householdId.toString()))
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
            .andExpect(jsonPath("$.errors.householdName").exists())
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

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isConflict());
    }
}
