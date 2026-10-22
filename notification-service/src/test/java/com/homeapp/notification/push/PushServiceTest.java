package com.homeapp.notification.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import tools.jackson.databind.json.JsonMapper;

class PushServiceTest {

    private static final UUID OLA = UUID.randomUUID();
    private static final UUID TATA = UUID.randomUUID();
    private static final String PHONE = "https://fcm.googleapis.com/fcm/send/telefon-oli";

    private final PushSubscriptionRepository subscriptionRepository = mock(PushSubscriptionRepository.class);
    private final PushSender pushSender = mock(PushSender.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-20T04:30:00Z"), ZoneId.of("Europe/Warsaw"));
    private final PushService service = new PushService(subscriptionRepository, pushSender,
        JsonMapper.builder().build(), clock);

    private static SubscriptionRequest phoneRequest() {
        return new SubscriptionRequest(PHONE, new SubscriptionRequest.Keys("klucz-p256dh", "klucz-auth"));
    }

    private static PushSubscription phoneOf(UUID userId) {
        return new PushSubscription(userId, PHONE, "klucz-p256dh", "klucz-auth", Instant.now());
    }

    @Test
    void shouldSaveNewDevice() {
        given(subscriptionRepository.findByEndpoint(PHONE)).willReturn(Optional.empty());

        service.subscribe(OLA, phoneRequest());

        ArgumentCaptor<PushSubscription> saved = ArgumentCaptor.forClass(PushSubscription.class);
        verify(subscriptionRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(OLA);
        assertThat(saved.getValue().getEndpoint()).isEqualTo(PHONE);
    }

    @Test
    void shouldMoveDeviceToPersonWhoLoggedIn() {
        PushSubscription phone = phoneOf(TATA);
        given(subscriptionRepository.findByEndpoint(PHONE)).willReturn(Optional.of(phone));

        service.subscribe(OLA, phoneRequest());

        assertThat(phone.getUserId()).isEqualTo(OLA);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void shouldCountDeliveredAndRemoveExpiredDevices() {
        PushSubscription phone = phoneOf(OLA);
        PushSubscription oldLaptop = new PushSubscription(OLA, "https://fcm.googleapis.com/fcm/send/stary-laptop",
            "k", "a", Instant.now());
        given(pushSender.isEnabled()).willReturn(true);
        given(subscriptionRepository.findAllByUserId(OLA)).willReturn(List.of(phone, oldLaptop));
        given(pushSender.send(phone, "{\"title\":\"Tytuł\",\"body\":\"Treść\",\"url\":\"/\"}")).willReturn(true);
        given(pushSender.send(oldLaptop, "{\"title\":\"Tytuł\",\"body\":\"Treść\",\"url\":\"/\"}")).willReturn(false);

        int delivered = service.sendToUser(OLA, new PushMessage("Tytuł", "Treść", "/"));

        assertThat(delivered).isEqualTo(1);
        verify(subscriptionRepository).delete(oldLaptop);
        verify(subscriptionRepository, never()).delete(phone);
    }

    @Test
    void shouldTryOtherDevicesWhenOneFails() {
        PushSubscription phone = phoneOf(OLA);
        PushSubscription laptop = new PushSubscription(OLA, "https://updates.push.services.mozilla.com/laptop",
            "k", "a", Instant.now());
        given(pushSender.isEnabled()).willReturn(true);
        given(subscriptionRepository.findAllByUserId(OLA)).willReturn(List.of(phone, laptop));
        given(pushSender.send(any(), anyString())).willThrow(new IllegalStateException("Brak internetu"))
            .willReturn(true);

        int delivered = service.sendToUser(OLA, new PushMessage("Tytuł", "Treść", "/"));

        assertThat(delivered).isEqualTo(1);
        verify(subscriptionRepository, never()).delete(any());
    }

    @Test
    void shouldSendNothingWhenPushIsDisabled() {
        given(pushSender.isEnabled()).willReturn(false);

        assertThat(service.sendToUser(OLA, new PushMessage("Tytuł", "Treść", "/"))).isZero();
        assertThat(service.hasDevices(OLA)).isFalse();
        verify(pushSender, never()).send(any(), anyString());
    }
}
