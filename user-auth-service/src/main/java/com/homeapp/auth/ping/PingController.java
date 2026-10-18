package com.homeapp.auth.ping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class PingController {

    private final String serviceName;

    public PingController(@Value("${spring.application.name}") String serviceName) {
        this.serviceName = serviceName;
    }

    @GetMapping("/ping")
    public PingResponse ping() {
        return new PingResponse(serviceName, "OK", Instant.now());
    }
}
