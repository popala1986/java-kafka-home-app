package com.homeapp.planner.plan;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
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

// weekStart to poniedziałek w formacie 2026-10-26.
@RestController
@RequestMapping("/api/planner/plans/{weekStart}")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<AssignmentResponse> generatePlan(@AuthenticationPrincipal Jwt jwt,
                                                 @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return planService.generatePlan(CurrentUser.householdId(jwt), weekStart);
    }

    @GetMapping
    public List<AssignmentResponse> getPlan(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return planService.getPlan(CurrentUser.householdId(jwt), weekStart);
    }

    @GetMapping("/me")
    public List<AssignmentResponse> getMyPlan(@AuthenticationPrincipal Jwt jwt,
                                              @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return planService.getMyPlan(CurrentUser.householdId(jwt), CurrentUser.userId(jwt), weekStart);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlan(@AuthenticationPrincipal Jwt jwt,
                           @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        planService.deletePlan(CurrentUser.householdId(jwt), weekStart);
    }

    @PutMapping("/assignments/{assignmentId}")
    public AssignmentResponse reassign(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                                       @PathVariable UUID assignmentId,
                                       @Valid @RequestBody ReassignRequest request) {
        return planService.reassign(CurrentUser.householdId(jwt), assignmentId, request.userId());
    }
}
