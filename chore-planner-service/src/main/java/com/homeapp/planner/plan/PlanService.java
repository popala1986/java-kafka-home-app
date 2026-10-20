package com.homeapp.planner.plan;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeapp.planner.chore.Chore;
import com.homeapp.planner.chore.ChoreExclusion;
import com.homeapp.planner.chore.ChoreExclusionRepository;
import com.homeapp.planner.chore.ChoreRepository;
import com.homeapp.planner.member.HouseholdMember;
import com.homeapp.planner.member.HouseholdMemberRepository;
import com.homeapp.planner.member.MemberNotFoundException;

@Service
public class PlanService {

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final ChoreAssignmentRepository assignmentRepository;
    private final ChoreRepository choreRepository;
    private final ChoreExclusionRepository exclusionRepository;
    private final HouseholdMemberRepository memberRepository;
    private final WeeklyPlanner weeklyPlanner;
    private final Clock clock;

    public PlanService(ChoreAssignmentRepository assignmentRepository,
                       ChoreRepository choreRepository,
                       ChoreExclusionRepository exclusionRepository,
                       HouseholdMemberRepository memberRepository,
                       WeeklyPlanner weeklyPlanner,
                       Clock clock) {
        this.assignmentRepository = assignmentRepository;
        this.choreRepository = choreRepository;
        this.exclusionRepository = exclusionRepository;
        this.memberRepository = memberRepository;
        this.weeklyPlanner = weeklyPlanner;
        this.clock = clock;
    }

    @Transactional
    public List<AssignmentResponse> generatePlan(UUID householdId, LocalDate weekStart) {
        requireMonday(weekStart);
        if (assignmentRepository.existsByHouseholdIdAndWeekStart(householdId, weekStart)) {
            throw new PlanAlreadyExistsException(weekStart);
        }

        List<HouseholdMember> members = memberRepository.findAllByHouseholdIdOrderByBirthDateAsc(householdId);
        List<Chore> chores = choreRepository.findAllByHouseholdIdOrderByMinAgeAscNameAsc(householdId);
        Map<UUID, Set<UUID>> excluded = exclusionRepository
            .findAllByChoreIdIn(chores.stream().map(Chore::getId).toList()).stream()
            .collect(Collectors.groupingBy(ChoreExclusion::getChoreId,
                Collectors.mapping(ChoreExclusion::getUserId, Collectors.toSet())));

        Instant now = Instant.now(clock);
        List<ChoreAssignment> assignments = weeklyPlanner.plan(weekStart, members, chores, excluded).stream()
            .map(planned -> new ChoreAssignment(householdId, weekStart, planned, now))
            .toList();
        assignmentRepository.saveAll(assignments);
        log.info("Plan rodziny {} na tydzień od {}: {} zadań", householdId, weekStart, assignments.size());

        return toResponses(householdId, assignments);
    }

    // Wołane przez automat w niedzielę: nie rzuca 409, tylko pomija rodziny, które już mają plan.
    @Transactional
    public void generatePlanIfMissing(UUID householdId, LocalDate weekStart) {
        if (!assignmentRepository.existsByHouseholdIdAndWeekStart(householdId, weekStart)) {
            generatePlan(householdId, weekStart);
        }
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getPlan(UUID householdId, LocalDate weekStart) {
        requireMonday(weekStart);
        return toResponses(householdId, assignmentRepository.findAllByHouseholdIdAndWeekStart(householdId, weekStart));
    }

    // "Mój kalendarz": tylko zadania zalogowanej osoby.
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getMyPlan(UUID householdId, UUID userId, LocalDate weekStart) {
        return getPlan(householdId, weekStart).stream()
            .filter(assignment -> assignment.userId().equals(userId))
            .toList();
    }

    @Transactional
    public void deletePlan(UUID householdId, LocalDate weekStart) {
        requireMonday(weekStart);
        assignmentRepository.deleteAllByHouseholdIdAndWeekStart(householdId, weekStart);
    }

    // Admin ręcznie zmienia, kto robi dane zadanie. Ręczna zmiana wygrywa z automatem.
    @Transactional
    public AssignmentResponse reassign(UUID householdId, UUID assignmentId, UUID newUserId) {
        ChoreAssignment assignment = assignmentRepository.findByIdAndHouseholdId(assignmentId, householdId)
            .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        memberRepository.findByUserIdAndHouseholdId(newUserId, householdId)
            .orElseThrow(() -> new MemberNotFoundException(newUserId));

        assignment.reassignTo(newUserId);
        return toResponses(householdId, List.of(assignment)).getFirst();
    }

    private void requireMonday(LocalDate weekStart) {
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new InvalidWeekStartException(weekStart);
        }
    }

    // Dokleja nazwę obowiązku i imię domownika, żeby odpowiedź była czytelna dla człowieka.
    private List<AssignmentResponse> toResponses(UUID householdId, List<ChoreAssignment> assignments) {
        Map<UUID, Chore> chores = choreRepository.findAllByHouseholdIdOrderByMinAgeAscNameAsc(householdId).stream()
            .collect(Collectors.toMap(Chore::getId, Function.identity()));
        Map<UUID, HouseholdMember> members = memberRepository.findAllByHouseholdIdOrderByBirthDateAsc(householdId).stream()
            .collect(Collectors.toMap(HouseholdMember::getUserId, Function.identity()));

        return assignments.stream()
            .map(assignment -> {
                Chore chore = chores.get(assignment.getChoreId());
                HouseholdMember member = members.get(assignment.getUserId());
                return new AssignmentResponse(
                    assignment.getId(),
                    assignment.getAssignedDate(),
                    chore.getId(),
                    chore.getName(),
                    chore.getType(),
                    chore.getPoints(),
                    member.getUserId(),
                    member.getFirstName(),
                    assignment.isManual());
            })
            .sorted(Comparator.comparing(AssignmentResponse::date)
                .thenComparing(AssignmentResponse::firstName)
                .thenComparing(AssignmentResponse::choreName))
            .toList();
    }
}
