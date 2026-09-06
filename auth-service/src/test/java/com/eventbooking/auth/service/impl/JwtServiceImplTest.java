package com.eventbooking.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.eventbooking.auth.entity.Role;
import com.eventbooking.auth.entity.User;

class JwtServiceImplTest {

	private JwtServiceImpl jwtService;

	@BeforeEach
	void setUp() {

		jwtService = new JwtServiceImpl();

		ReflectionTestUtils.setField(jwtService, "secret", "my-super-secret-key-that-is-at-least-32-characters-long");

		ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);

	}

	@Test
	void generateToken_shouldGenerateTokenSuccessfully() {

		// Arrange
		User user = new User();

		user.setEmail("test@email.com");
		user.setRole(Role.USER);

		// Act

		String token = jwtService.generateToken(user);

		// Assert
		assertNotNull(token);
		assertFalse(token.isBlank());
	}

	@Test
	void extractUsername_shouldReturnUserEmail() {

		// Arrange

		User user = new User();
		user.setEmail("test@example.com");
		user.setRole(Role.USER);

		// Act

		String token = jwtService.generateToken(user);

		String username = jwtService.extractUsername(token);

		assertEquals("test@example.com", username);

	}

	@Test
	void extractRole_shouldReturnUserRole() {

		// Arrange
		User user = new User();
		user.setEmail("test@example.com");
		user.setRole(Role.USER);

		// Act
		String token = jwtService.generateToken(user);

		String role = jwtService.extractRole(token);

		// Assert
		assertEquals("USER", role);
	}

	@Test
	void isTokenValid_shouldReturnTrue_whenTokenIsValid() {

		// Arrange

		User user = new User();
		user.setEmail("test@example.com");
		user.setRole(Role.USER);

		String token = jwtService.generateToken(user);

		// Act
		boolean result = jwtService.isTokenValid(token, user);

		// Assert
		assertTrue(result);
	}

	@Test
	void isTokenValid_shouldReturnFalse_whenTokenBelongsToDifferentUser() {

		// Arrange
		User tokenUser = new User();
		tokenUser.setEmail("user1@example.com");
		tokenUser.setRole(Role.USER);

		User differentUser = new User();
		differentUser.setEmail("user2@example.com");
		differentUser.setRole(Role.USER);

		String token = jwtService.generateToken(tokenUser);

		// Act
		boolean result = jwtService.isTokenValid(token, differentUser);

		// Assert
		assertFalse(result);
	}

	@Test
	void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {

		// Arrange
		User user = new User();
		user.setEmail("test@example.com");
		user.setRole(Role.USER);

		// Temporarily configure an already-expired token
		ReflectionTestUtils.setField(jwtService, "expiration", -1000L);

		String token = jwtService.generateToken(user);

		// Act
		boolean result = jwtService.isTokenValid(token, user);

		// Assert
		assertFalse(result);
	}

	@Test
	void isTokenValid_shouldReturnFalse_whenTokenIsTampered() {

		// Arrange

		User user = new User();
		user.setEmail("test@example.com");
		user.setRole(Role.USER);

		String token = jwtService.generateToken(user);

		String tamperedToken = token + "tampered";

		boolean result = jwtService.isTokenValid(tamperedToken, user);

		assertFalse(result);
	}

	@Test
	void getExpirationInSeconds_shouldReturnExpirationInSeconds() {

		// Arrange
		ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);

		// Act
		long result = jwtService.getExpirationInSeconds();

		// Assert
		assertEquals(3600L, result);
	}
}
