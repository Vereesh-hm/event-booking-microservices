package com.eventbooking.event.security;

public interface JwtService {

    String extractUsername(String token);

    String extractRole(String token);

    boolean isTokenValid(String token);
}