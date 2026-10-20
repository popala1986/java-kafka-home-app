package com.homeapp.planner.digest;

// Jedno zadanie w porannym podsumowaniu. Tylko to, co trafi do SMS-a i maila.
public record DigestTask(String choreName, int points) {
}
