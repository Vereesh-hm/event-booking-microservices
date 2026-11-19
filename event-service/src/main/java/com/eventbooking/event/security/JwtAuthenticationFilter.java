package com.eventbooking.event.security;

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

		String authorizationHeader = request.getHeader("Authorization");

		log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());

		log.info("Authorization header present: {}", authorizationHeader != null);

		log.info("Authorization format valid: {}",
				authorizationHeader != null && authorizationHeader.startsWith("Bearer "));

		log.info("Incoming JWT fingerprint: {}", getTokenFingerprint(authorizationHeader));

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);

		boolean valid = jwtService.isTokenValid(token);

		log.info("JWT validation result: {}", valid);

		if (!valid) {
			log.warn("JWT validation failed");
			filterChain.doFilter(request, response);
			return;
		}
		try {

			String username = jwtService.extractUsername(token);
			String role = jwtService.extractRole(token);

			SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null,
					List.of(authority));

			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (Exception e) {

			// Invalid token/claims.
			// Continue without authentication.
			log.error("Error while processing JWT claims", e);
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private String getTokenFingerprint(String authorizationHeader) {
		try {
			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				return "INVALID_HEADER";
			}

			String token = authorizationHeader.substring(7);

			byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
					.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));

			return java.util.HexFormat.of().formatHex(hash).substring(0, 16);

		} catch (Exception e) {
			return "FINGERPRINT_ERROR";
		}
	}

}