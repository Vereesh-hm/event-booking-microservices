package com.eventbooking.booking.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
		String authorizationHeader = request.getHeader("Authorization");

		log.info("Authorization header present: {}", authorizationHeader != null);

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

			log.warn("JWT missing or invalid Authorization header format");
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);

		if (!jwtService.isTokenValid(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String userId = jwtService.extractUserId(token).toString();
			String role = jwtService.extractRole(token);

			log.info("Authenticated user ID: {}", userId);
			log.info("Authenticated user role: {}", role);

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
					List.of(new SimpleGrantedAuthority("ROLE_" + role)));

			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (Exception e) {

			log.error("Error while processing JWT", e);
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}