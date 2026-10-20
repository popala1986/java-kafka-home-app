package com.homeapp.planner.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UserUpdatedListenerTest {

    private final HouseholdMemberRepository repository = mock(HouseholdMemberRepository.class);
    private final UserUpdatedListener listener = new UserUpdatedListener(repository);

    @Test
    void shouldUpdateNameAndBirthDate() {
        UUID olaId = UUID.randomUUID();
        HouseholdMember ola = new HouseholdMember(olaId, UUID.randomUUID(), "Paweł",
            LocalDate.of(2020, 4, 30), "USER", Instant.now());
        given(repository.findById(olaId)).willReturn(Optional.of(ola));

        listener.onUserUpdated(new UserUpdatedEvent(UUID.randomUUID(), olaId, ola.getHouseholdId(),
            "Ola", LocalDate.of(2012, 4, 30), Instant.now()), 0, 0L);

        assertThat(ola.getFirstName()).isEqualTo("Ola");
        assertThat(ola.getBirthDate()).isEqualTo(LocalDate.of(2012, 4, 30));
        verify(repository).save(ola);
    }

    @Test
    void shouldSkipUnknownMember() {
        UUID unknownId = UUID.randomUUID();
        given(repository.findById(unknownId)).willReturn(Optional.empty());

        listener.onUserUpdated(new UserUpdatedEvent(UUID.randomUUID(), unknownId, UUID.randomUUID(),
            "Ola", LocalDate.of(2012, 4, 30), Instant.now()), 0, 0L);

        verify(repository, never()).save(any());
    }
}
