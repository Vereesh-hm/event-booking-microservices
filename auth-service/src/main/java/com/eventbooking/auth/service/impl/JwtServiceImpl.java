package com.eventbooking.auth.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eventbooking.auth.entity.User;
import com.eventbooking.auth.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String generateToken(User user) {

		return Jwts.builder().subject(user.getEmail()).claim("role", user.getRole().name()).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expiration)).signWith(getSigningKey()).compact();
	}

	@Override
	public String extractRole(String token) {

		return extractAllClaims(token).get("role", String.class);
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	@Override
	public String extractUsername(String token) {

		return extractAllClaims(token).getSubject();
	}

	private Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	private boolean isTokenExpired(String token) {

		return extractExpiration(token).before(new Date());
	}

	@Override
	public boolean isTokenValid(String token, User user) {

		String username = extractUsername(token);

		return username.equals(user.getEmail()) && !isTokenExpired(token);
	}

	@Override
	public long getExpirationInSeconds() {

		return expiration / 1000;
	}

}
