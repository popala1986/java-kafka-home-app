package com.homeapp.planner.member;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UserDeletedListenerTest {

    private final HouseholdMemberRepository repository = mock(HouseholdMemberRepository.class);
    private final UserDeletedListener listener = new UserDeletedListener(repository);

    @Test
    void shouldDeleteKnownMember() {
        UUID zosiaId = UUID.randomUUID();
        given(repository.existsById(zosiaId)).willReturn(true);

        listener.onUserDeleted(new UserDeletedEvent(UUID.randomUUID(), zosiaId, UUID.randomUUID(), Instant.now()),
            0, 0L);

        verify(repository).deleteById(zosiaId);
    }

    @Test
    void shouldSkipUnknownMember() {
        UUID unknownId = UUID.randomUUID();
        given(repository.existsById(unknownId)).willReturn(false);

        listener.onUserDeleted(new UserDeletedEvent(UUID.randomUUID(), unknownId, UUID.randomUUID(), Instant.now()),
            0, 0L);

        verify(repository, never()).deleteById(any());
    }
}
