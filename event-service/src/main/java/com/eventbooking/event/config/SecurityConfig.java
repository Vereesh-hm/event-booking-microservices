package com.eventbooking.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.eventbooking.event.security.JwtAuthenticationEntryPoint;
import com.eventbooking.event.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {

		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))

				.authorizeHttpRequests(auth -> auth

						// Anyone authenticated as USER or ADMIN
						// can view events
						.requestMatchers(HttpMethod.GET, "/api/events/**").hasAnyRole("USER", "ADMIN")

						// Only ADMIN can create
						.requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")

						// Only ADMIN can update
						.requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")

						// Only ADMIN can delete
						.requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")

						// Everything else requires authentication
						.anyRequest().authenticated())

				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}