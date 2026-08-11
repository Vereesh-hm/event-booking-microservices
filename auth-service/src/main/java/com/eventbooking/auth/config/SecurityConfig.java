package com.eventbooking.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth

				.requestMatchers("/api/auth/register", "/api/auth/login").permitAll().requestMatchers("/api/test/user")
				.hasRole("USER").requestMatchers("/api/test/admin").hasRole("ADMIN")

				.anyRequest().authenticated())

				.exceptionHandling(ex -> ex

						.authenticationEntryPoint((request, response, authException) -> {

							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType("application/json");

							ApiResponse<Void> responseBody = new ApiResponse<>(false,
									HttpServletResponse.SC_UNAUTHORIZED, "Authentication required", null);

							objectMapper.writeValue(response.getWriter(), responseBody);
						})

						.accessDeniedHandler((request, response, accessDeniedException) -> {

							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.setContentType("application/json");

							ApiResponse<Void> responseBody = new ApiResponse<>(false, HttpServletResponse.SC_FORBIDDEN,
									"Access denied", null);

							objectMapper.writeValue(response.getWriter(), responseBody);
						}))

				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}