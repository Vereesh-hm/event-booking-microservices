package com.eventbooking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

	@NotBlank(message = "Email is requried")
	@Email(message = "Invalid email format")
	private String email;

	@NotBlank(message = "password is required")
	private String password;
}
