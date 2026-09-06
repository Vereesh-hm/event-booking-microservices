
package com.eventbooking.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.dto.LoginRequest;
import com.eventbooking.auth.dto.LoginResponse;
import com.eventbooking.auth.dto.RegisterRequest;
import com.eventbooking.auth.entity.Role;
import com.eventbooking.auth.entity.User;
import com.eventbooking.auth.exception.EmailAlreadyExistsException;
import com.eventbooking.auth.exception.InvalidCredentialsException;
import com.eventbooking.auth.repository.UserRepository;
import com.eventbooking.auth.service.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private AuthServiceImpl authService;

	// ---------------------------------------------------------
	// REGISTER TESTS
	// ---------------------------------------------------------

	@Test
	void register_shouldRegisterUserSuccessfully() {

		// Arrange
		RegisterRequest request = new RegisterRequest();
		request.setFullName("Test User");
		request.setEmail("test@example.com");
		request.setPassword("password123");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

		when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

		User savedUser = new User();

		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		// Act
		ApiResponse<Void> response = authService.register(request);

		// Assert
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		assertEquals("User registered successfully.", response.getMessage());

		verify(userRepository).findByEmail(request.getEmail());
		verify(passwordEncoder).encode(request.getPassword());
		verify(userRepository).save(any(User.class));
	}

	@Test
	void register_shouldThrowException_whenEmailAlreadyExists() {

		// Arrange
		RegisterRequest request = new RegisterRequest();
		request.setFullName("Test User");
		request.setEmail("test@example.com");
		request.setPassword("password123");

		User existingUser = new User();
		existingUser.setId(UUID.randomUUID());
		existingUser.setEmail("test@example.com");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

		// Act & Assert
		EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
				() -> authService.register(request));

		assertEquals("Email already exists", exception.getMessage());

		verify(userRepository).findByEmail(request.getEmail());

		verify(passwordEncoder, never()).encode(any());

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void register_shouldEncodePasswordBeforeSaving() {

		// Arrange
		RegisterRequest request = new RegisterRequest();
		request.setFullName("Test User");
		request.setEmail("test@example.com");
		request.setPassword("password123");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

		when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

		// Act
		authService.register(request);

		// Assert
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		verify(userRepository).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();

		assertEquals("encodedPassword", savedUser.getPassword());
	}

	@Test
	void register_shouldAssignUserRole() {

		// Arrange
		RegisterRequest request = new RegisterRequest();
		request.setFullName("Test User");
		request.setEmail("test@example.com");
		request.setPassword("password123");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

		when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

		// Act
		authService.register(request);

		// Assert
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		verify(userRepository).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();

		assertEquals(Role.USER, savedUser.getRole());
	}

	@Test
	void register_shouldSetIdAndTimestamps() {

		// Arrange
		RegisterRequest request = new RegisterRequest();
		request.setFullName("Test User");
		request.setEmail("test@example.com");
		request.setPassword("password123");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

		when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

		// Act
		authService.register(request);

		// Assert
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		verify(userRepository).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();

		assertNotNull(savedUser.getId());
		assertNotNull(savedUser.getCreatedAt());
		assertNotNull(savedUser.getUpdatedAt());

		assertEquals(savedUser.getCreatedAt(), savedUser.getUpdatedAt());
	}

	// ---------------------------------------------------------
	// LOGIN TESTS
	// ---------------------------------------------------------

	@Test
	void login_shouldLoginSuccessfully() {

		// Arrange
		LoginRequest request = new LoginRequest();
		request.setEmail("test@example.com");
		request.setPassword("password123");

		User user = new User();

		user.setId(UUID.randomUUID());
		user.setFullName("Test User");
		user.setEmail("test@example.com");
		user.setPassword("encodedPassword");
		user.setRole(Role.USER);

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

		when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

		when(jwtService.generateToken(user)).thenReturn("jwt-token");

		when(jwtService.getExpirationInSeconds()).thenReturn(3600L);

		// Act
		ApiResponse<LoginResponse> response = authService.login(request);

		// Assert
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		assertEquals("Login successful", response.getMessage());

		assertNotNull(response.getData());
		assertEquals("jwt-token", response.getData().getToken());
		assertEquals(3600L, response.getData().getExpiresIn());

		verify(userRepository).findByEmail(request.getEmail());

		verify(passwordEncoder).matches(request.getPassword(), user.getPassword());

		verify(jwtService).generateToken(user);

		verify(jwtService).getExpirationInSeconds();
	}

	@Test
	void login_shouldThrowException_whenEmailDoesNotExist() {

		// Arrange
		LoginRequest request = new LoginRequest();
		request.setEmail("unknown@example.com");
		request.setPassword("password123");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

		// Act & Assert
		InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
				() -> authService.login(request));

		assertEquals("Invalid email or password", exception.getMessage());

		verify(userRepository).findByEmail(request.getEmail());

		verify(passwordEncoder, never()).matches(any(), any());

		verify(jwtService, never()).generateToken(any());
	}

	@Test
	void login_shouldThrowException_whenPasswordIsIncorrect() {

		// Arrange
		LoginRequest request = new LoginRequest();
		request.setEmail("test@example.com");
		request.setPassword("wrongPassword");

		User user = new User();

		user.setId(UUID.randomUUID());
		user.setEmail("test@example.com");
		user.setPassword("encodedPassword");
		user.setRole(Role.USER);

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

		when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

		// Act & Assert
		InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
				() -> authService.login(request));

		assertEquals("Invalid email or password", exception.getMessage());

		verify(userRepository).findByEmail(request.getEmail());

		verify(passwordEncoder).matches(request.getPassword(), user.getPassword());

		verify(jwtService, never()).generateToken(any());
	}
}
