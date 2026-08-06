package com.eventbooking.auth.service;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.dto.RegisterRequest;

public interface AuthService {

	ApiResponse<Void> register(RegisterRequest request);
}
