package com.eventbooking.auth.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.entity.User;
import com.eventbooking.auth.repository.UserRepository;
import com.eventbooking.auth.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, ObjectMapper objectMapper) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			String authHeader = request.getHeader("Authorization");

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				filterChain.doFilter(request, response);
				return;
			}

			String token = authHeader.substring(7);

			String username = jwtService.extractUsername(token);

			User user = userRepository.findByEmail(username).orElse(null);

			if (user != null && jwtService.isTokenValid(token, user)) {
				System.out.println("USER FOUND: " + user.getEmail());
				System.out.println("USER ROLE: " + user.getRole());
				System.out.println("TOKEN VALID: " + jwtService.isTokenValid(token, user));

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
						List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));

				SecurityContextHolder.getContext().setAuthentication(authentication);
				System.out.println("AUTHENTICATED: " + SecurityContextHolder.getContext().getAuthentication());

				System.out.println("AUTH AFTER SET: " + SecurityContextHolder.getContext().getAuthentication());
			}

			filterChain.doFilter(request, response);

		} catch (JwtException ex) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");

			ApiResponse<Void> responseBody = new ApiResponse<>(false, HttpServletResponse.SC_UNAUTHORIZED,
					"Invalid or expired token", null);

			objectMapper.writeValue(response.getWriter(), responseBody);
		}

	}
}
