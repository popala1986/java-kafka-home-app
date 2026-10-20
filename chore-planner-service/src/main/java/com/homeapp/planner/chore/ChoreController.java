package com.homeapp.planner.chore;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.homeapp.planner.security.CurrentUser;

import jakarta.validation.Valid;

// Kto może co, ustawia SecurityConfig: czytać każdy domownik, zmieniać tylko ADMIN.
@RestController
@RequestMapping("/api/planner/chores")
public class ChoreController {

    private final ChoreService choreService;

    public ChoreController(ChoreService choreService) {
        this.choreService = choreService;
    }

    @GetMapping
    public List<ChoreResponse> listChores(@AuthenticationPrincipal Jwt jwt) {
        return choreService.listChores(CurrentUser.householdId(jwt));
    }

    @GetMapping("/{choreId}")
    public ChoreResponse getChore(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID choreId) {
        return choreService.getChore(CurrentUser.householdId(jwt), choreId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChoreResponse addChore(@AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody ChoreRequest request) {
        return choreService.addChore(CurrentUser.householdId(jwt), request);
    }

    @PutMapping("/{choreId}")
    public ChoreResponse updateChore(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable UUID choreId,
                                     @Valid @RequestBody ChoreRequest request) {
        return choreService.updateChore(CurrentUser.householdId(jwt), choreId, request);
    }

    @DeleteMapping("/{choreId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChore(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID choreId) {
        choreService.deleteChore(CurrentUser.householdId(jwt), choreId);
    }

    // Kto NIE robi tego obowiązku (lista userId).
    @GetMapping("/{choreId}/exclusions")
    public List<UUID> listExclusions(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID choreId) {
        return choreService.listExcludedMembers(CurrentUser.householdId(jwt), choreId);
    }

    @PutMapping("/{choreId}/exclusions/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excludeMember(@AuthenticationPrincipal Jwt jwt,
                              @PathVariable UUID choreId, @PathVariable UUID userId) {
        choreService.excludeMember(CurrentUser.householdId(jwt), choreId, userId);
    }

    @DeleteMapping("/{choreId}/exclusions/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void includeMember(@AuthenticationPrincipal Jwt jwt,
                              @PathVariable UUID choreId, @PathVariable UUID userId) {
        choreService.includeMember(CurrentUser.householdId(jwt), choreId, userId);
    }
}
