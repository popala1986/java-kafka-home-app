package com.homeapp.planner.member;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planner/households/{householdId}/members")
public class MemberController {

    private final HouseholdMemberRepository memberRepository;
    private final Clock clock;

    public MemberController(HouseholdMemberRepository memberRepository, Clock clock) {
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @GetMapping
    public List<MemberResponse> listMembers(@PathVariable UUID householdId) {
        LocalDate today = LocalDate.now(clock);
        return memberRepository.findAllByHouseholdIdOrderByBirthDateAsc(householdId).stream()
            .map(member -> MemberResponse.from(member, today))
            .toList();
    }
}
