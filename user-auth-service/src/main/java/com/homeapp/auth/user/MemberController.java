package com.homeapp.auth.user;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

// UWAGA: na razie każdy może wywołać te endpointy. W Kroku 6 (JWT) pozwolimy na to
// tylko adminowi tej konkretnej rodziny.
@RestController
@RequestMapping("/api/households/{householdId}/members")
public class MemberController {

    private final UserService userService;

    public MemberController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addMember(@PathVariable UUID householdId,
                                  @Valid @RequestBody AddMemberRequest request) {
        return userService.addMember(householdId, request);
    }

    @GetMapping
    public List<UserResponse> listMembers(@PathVariable UUID householdId) {
        return userService.listMembers(householdId);
    }
}
