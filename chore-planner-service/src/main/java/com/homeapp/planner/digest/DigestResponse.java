package com.homeapp.planner.digest;

import java.time.LocalDate;

public record DigestResponse(LocalDate date, int sent) {
}
