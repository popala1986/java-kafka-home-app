package com.homeapp.notification.contact;

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
import org.mockito.ArgumentCaptor;

class ContactListenerTest {

    private static final UUID OLA = UUID.randomUUID();
    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();
    private static final Instant MONDAY = Instant.parse("2026-10-19T10:00:00Z");

    private final ContactRepository repository = mock(ContactRepository.class);
    private final ContactListener listener = new ContactListener(repository);

    @Test
    void shouldCreateContactFromRegisteredUser() {
        given(repository.findById(OLA)).willReturn(Optional.empty());

        listener.onUserRegistered(new UserRegisteredEvent(UUID.randomUUID(), OLA, HOUSEHOLD_ID, "USER",
            "ola@opala.pl", "Ola", LocalDate.of(2020, 4, 30), null, MONDAY));

        ArgumentCaptor<Contact> saved = ArgumentCaptor.forClass(Contact.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ola@opala.pl");
        assertThat(saved.getValue().getPhoneNumber()).isNull();
    }

    @Test
    void shouldUpdateContactWithNewerData() {
        Contact ola = new Contact(OLA, HOUSEHOLD_ID, "Paweł", "ola@opala.pl", null, MONDAY);
        given(repository.findById(OLA)).willReturn(Optional.of(ola));

        listener.onUserUpdated(new UserUpdatedEvent(UUID.randomUUID(), OLA, HOUSEHOLD_ID, "Ola",
            "ola@opala.pl", "500600700", MONDAY.plusSeconds(60)));

        assertThat(ola.getFirstName()).isEqualTo("Ola");
        assertThat(ola.getPhoneNumber()).isEqualTo("500600700");
        verify(repository).save(ola);
    }

    @Test
    void shouldIgnoreOlderEvent() {
        Contact ola = new Contact(OLA, HOUSEHOLD_ID, "Ola", "ola@opala.pl", "500600700", MONDAY);
        given(repository.findById(OLA)).willReturn(Optional.of(ola));

        listener.onUserRegistered(new UserRegisteredEvent(UUID.randomUUID(), OLA, HOUSEHOLD_ID, "USER",
            "ola@opala.pl", "Paweł", LocalDate.of(2020, 4, 30), null, MONDAY.minusSeconds(3600)));

        assertThat(ola.getFirstName()).isEqualTo("Ola");
        verify(repository, never()).save(any());
    }
}
