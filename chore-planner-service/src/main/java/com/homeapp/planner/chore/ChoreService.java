package com.homeapp.planner.chore;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChoreService {

    private static final Logger log = LoggerFactory.getLogger(ChoreService.class);

    private final ChoreRepository choreRepository;
    private final Clock clock;

    public ChoreService(ChoreRepository choreRepository, Clock clock) {
        this.choreRepository = choreRepository;
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

    private Chore findChore(UUID householdId, UUID choreId) {
        return choreRepository.findByIdAndHouseholdId(choreId, householdId)
            .orElseThrow(() -> new ChoreNotFoundException(choreId));
    }
}
