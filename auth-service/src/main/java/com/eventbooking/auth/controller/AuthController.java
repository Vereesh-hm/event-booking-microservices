package com.eventbooking.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.dto.LoginRequest;
import com.eventbooking.auth.dto.LoginResponse;
import com.eventbooking.auth.dto.RegisterRequest;
import com.eventbooking.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest registerRequest) {

		log.info("Sending Request Body : {}", registerRequest);

		return ResponseEntity.ok(authService.register(registerRequest));
	}

	@PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {

		return ResponseEntity.ok(authService.login(loginRequest));
	}

	@GetMapping("/test")
	public ResponseEntity<ApiResponse> test() {

		return ResponseEntity.ok(new ApiResponse(true, HttpStatus.OK.value(), "JWT authentication successful", null));
	}
}
