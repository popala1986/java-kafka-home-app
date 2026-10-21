package com.homeapp.notification.notification;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homeapp.notification.security.CurrentUser;

// Historia wysyłek Twojej rodziny, od najnowszych. Rodzina z tokenu, jak w pozostałych serwisach.
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> history(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.history(CurrentUser.householdId(jwt));
    }
}
