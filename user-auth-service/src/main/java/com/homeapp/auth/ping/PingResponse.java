package com.homeapp.auth.ping;

import java.time.Instant;

public record PingResponse(String service, String status, Instant timestamp) {
}
