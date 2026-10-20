package com.homeapp.planner.chore;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeapp.planner.member.HouseholdMemberRepository;
import com.homeapp.planner.member.MemberNotFoundException;

@Service
public class ChoreService {

    private static final Logger log = LoggerFactory.getLogger(ChoreService.class);

    private final ChoreRepository choreRepository;
    private final ChoreExclusionRepository exclusionRepository;
    private final HouseholdMemberRepository memberRepository;
    private final Clock clock;

    public ChoreService(ChoreRepository choreRepository, ChoreExclusionRepository exclusionRepository,
                        HouseholdMemberRepository memberRepository, Clock clock) {
        this.choreRepository = choreRepository;
        this.exclusionRepository = exclusionRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ChoreResponse> listChores(UUID householdId) {
        return choreRepository.findAllByHouseholdIdOrderByMinAgeAscNameAsc(householdId).stream()
            .map(ChoreResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ChoreResponse getChore(UUID householdId, UUID choreId) {
        return ChoreResponse.from(findChore(householdId, choreId));
    }

    @Transactional
    public ChoreResponse addChore(UUID householdId, ChoreRequest request) {
        Chore chore = new Chore(householdId, request, Instant.now(clock));
        return ChoreResponse.from(choreRepository.save(chore));
    }

    @Transactional
    public ChoreResponse updateChore(UUID householdId, UUID choreId, ChoreRequest request) {
        Chore chore = findChore(householdId, choreId);
        // Bez save(): encja jest śledzona w transakcji, więc JPA sam zapisze zmiany na końcu.
        chore.update(request);
        return ChoreResponse.from(chore);
    }

    @Transactional
    public void deleteChore(UUID householdId, UUID choreId) {
        choreRepository.delete(findChore(householdId, choreId));
    }

    // Wołane z listenera Kafki. Nowa rodzina dostaje 50 startowych obowiązków tylko raz.
    @Transactional
    public void addDefaultChoresIfMissing(UUID householdId) {
        if (choreRepository.existsByHouseholdId(householdId)) {
            return;
        }
        int copied = choreRepository.copyTemplatesToHousehold(householdId);
        log.info("Rodzina {} dostała {} startowych obowiązków", householdId, copied);
    }

    @Transactional(readOnly = true)
    public List<UUID> listExcludedMembers(UUID householdId, UUID choreId) {
        findChore(householdId, choreId);
        return exclusionRepository.findAllByChoreId(choreId).stream()
            .map(ChoreExclusion::getUserId)
            .toList();
    }

    // Wyłącz obowiązek jednej osobie. Drugie wywołanie niczego nie psuje (idempotentne).
    @Transactional
    public void excludeMember(UUID householdId, UUID choreId, UUID userId) {
        findChore(householdId, choreId);
        findMember(householdId, userId);
        if (!exclusionRepository.existsByChoreIdAndUserId(choreId, userId)) {
            exclusionRepository.save(new ChoreExclusion(choreId, userId));
        }
    }

    @Transactional
    public void includeMember(UUID householdId, UUID choreId, UUID userId) {
        findChore(householdId, choreId);
        findMember(householdId, userId);
        exclusionRepository.deleteByChoreIdAndUserId(choreId, userId);
    }

    private void findMember(UUID householdId, UUID userId) {
        memberRepository.findByUserIdAndHouseholdId(userId, householdId)
            .orElseThrow(() -> new MemberNotFoundException(userId));
    }

    private Chore findChore(UUID householdId, UUID choreId) {
        return choreRepository.findByIdAndHouseholdId(choreId, householdId)
            .orElseThrow(() -> new ChoreNotFoundException(choreId));
    }
}
