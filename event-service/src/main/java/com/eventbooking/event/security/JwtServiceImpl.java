package com.eventbooking.event.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {

	@Value("${jwt.secret}")
	private String secret;

	private SecretKey getSigningKey() {

		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	@Override
	public String extractUsername(String token) {

		return extractAllClaims(token).getSubject();
	}

	@Override
	public String extractRole(String token) {

		return extractAllClaims(token).get("role", String.class);
	}

	@Override
	public boolean isTokenValid(String token) {

		try {

			Claims claims = extractAllClaims(token);

			Date expiration = claims.getExpiration();

			return expiration != null && expiration.after(new Date());

		} catch (Exception e) {

			return false;
		}
	}
}