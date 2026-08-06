package com.eventbooking.auth.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.dto.RegisterRequest;
import com.eventbooking.auth.entity.Role;
import com.eventbooking.auth.entity.User;
import com.eventbooking.auth.exception.EmailAlreadyExistsException;
import com.eventbooking.auth.repository.UserRepository;
import com.eventbooking.auth.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public ApiResponse<Void> register(RegisterRequest request) {

		log.info("User registration request received for email: {}", request.getEmail());
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {

			log.warn("Registration failed. Email already exists: {}", request.getEmail());
			throw new EmailAlreadyExistsException("Email already exists");
		}

		User user = new User();

		user.setId(UUID.randomUUID());
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);

		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		user.setCreatedAt(now);
		user.setUpdatedAt(now);

		userRepository.save(user);
		log.info("User registered successfully with email: {}", request.getEmail());
		return new ApiResponse<>(true, HttpStatus.OK.value(), "User registered successfully.", null);
	}

}