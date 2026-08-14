package com.eventbooking.auth.service;

import com.eventbooking.auth.entity.User;

public interface JwtService {

	String generateToken(User user);

	String extractUsername(String token);

	boolean isTokenValid(String token, User user);
	
	long getExpirationInSeconds();
	
	String extractRole(String token);
}
