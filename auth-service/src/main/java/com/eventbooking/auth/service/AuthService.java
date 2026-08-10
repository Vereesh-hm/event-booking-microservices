package com.eventbooking.auth.service;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.dto.LoginRequest;
import com.eventbooking.auth.dto.LoginResponse;
import com.eventbooking.auth.dto.RegisterRequest;

public interface AuthService {

	ApiResponse<Void> register(RegisterRequest request);

	ApiResponse<LoginResponse> login(LoginRequest request);
}
