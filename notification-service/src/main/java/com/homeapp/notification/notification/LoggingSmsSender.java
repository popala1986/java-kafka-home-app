package com.homeapp.notification.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// SMS "na niby": zamiast wysyłać, zapisuje treść w logu. Darmowe i bezpieczne do nauki.
@Component
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String phoneNumber, String text) {
        log.info("SMS (testowy) do {}: {}", phoneNumber, text);
    }
}
