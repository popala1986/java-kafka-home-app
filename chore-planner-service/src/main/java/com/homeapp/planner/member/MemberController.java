package com.homeapp.planner.member;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homeapp.planner.security.CurrentUser;

// Tak jak w user-auth-service: rodzina z tokenu, nie z adresu.
@RestController
@RequestMapping("/api/planner/households/me/members")
public class MemberController {

    private final HouseholdMemberRepository memberRepository;
    private final Clock clock;

    public MemberController(HouseholdMemberRepository memberRepository, Clock clock) {
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @GetMapping
    public List<MemberResponse> listMembers(@AuthenticationPrincipal Jwt jwt) {
        LocalDate today = LocalDate.now(clock);
        return memberRepository.findAllByHouseholdIdOrderByBirthDateAsc(CurrentUser.householdId(jwt)).stream()
            .map(member -> MemberResponse.from(member, today))
            .toList();
    }
}
