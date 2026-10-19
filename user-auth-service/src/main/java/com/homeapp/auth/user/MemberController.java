package com.homeapp.auth.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.homeapp.auth.security.CurrentUser;

import jakarta.validation.Valid;

// Rodzina zawsze pochodzi z tokenu zalogowanego użytkownika, nigdy z adresu.
// Dzięki temu nie da się podejrzeć ani zmienić cudzej rodziny.
@RestController
@RequestMapping("/api/households/me/members")
public class MemberController {

    private final UserService userService;

    public MemberController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addMember(@AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody AddMemberRequest request) {
        return userService.addMember(CurrentUser.householdId(jwt), request);
    }

    @GetMapping
    public List<UserResponse> listMembers(@AuthenticationPrincipal Jwt jwt) {
        return userService.listMembers(CurrentUser.householdId(jwt));
    }
}
