package com.eventbooking.booking.security;

import java.util.UUID;

public interface JwtService {

    UUID extractUserId(String token);

    String extractRole(String token);

    boolean isTokenValid(String token);
}