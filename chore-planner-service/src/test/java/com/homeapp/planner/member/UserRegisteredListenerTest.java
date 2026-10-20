package com.homeapp.planner.member;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.homeapp.planner.chore.ChoreService;

class UserRegisteredListenerTest {

    private final HouseholdMemberRepository repository = mock(HouseholdMemberRepository.class);
    private final ChoreService choreService = mock(ChoreService.class);
    private final UserRegisteredListener listener = new UserRegisteredListener(repository, choreService);

    private UserRegisteredEvent event(UUID householdId) {
        return new UserRegisteredEvent(
            UUID.randomUUID(), UUID.randomUUID(), householdId, "USER",
            "Ola", LocalDate.of(2012, 4, 30), Instant.now());
    }

    @Test
    void shouldSaveNewMember() {
        UserRegisteredEvent event = event(UUID.randomUUID());
        given(repository.existsById(event.userId())).willReturn(false);

        listener.onUserRegistered(event, 0, 0L);

        verify(repository).save(any(HouseholdMember.class));
        verify(choreService).addDefaultChoresIfMissing(event.householdId());
    }

    @Test
    void shouldSkipDuplicateEvent() {
        UserRegisteredEvent event = event(UUID.randomUUID());
        given(repository.existsById(event.userId())).willReturn(true);

        listener.onUserRegistered(event, 0, 0L);

        verify(repository, never()).save(any());
        verifyNoInteractions(choreService);
    }

    @Test
    void shouldSkipEventWithoutHousehold() {
        listener.onUserRegistered(event(null), 0, 0L);

        verify(repository, never()).save(any());
    }
}
