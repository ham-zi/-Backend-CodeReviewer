package com.reviewer.common.token.Util;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.reviewer.auth.model.vo.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String secretKey;
	private SecretKey key;
	
	@PostConstruct
	public void init() {
		byte[] arr = Base64.getDecoder().decode(secretKey);
		this.key = Keys.hmacShaKeyFor(arr);
	}

	public String getAccessToken(CustomUserDetails user) {
		return Jwts.builder()
				   .subject(user.getUsername())
				   .issuedAt(new Date())
				   .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(15))))
				   .claim("name", user.getMemberName())
				   .claim("id",user.getUserId())
				   .signWith(key)
				   .compact();
	}

	public String getRefreshToken(CustomUserDetails user) {
		return Jwts.builder()
				   .subject(user.getUsername())
				   .issuedAt(new Date())
				   .expiration(Date.from(Instant.now().plusSeconds(60*60*24*3)))
				   .claim("name", user.getMemberName())
				   .claim("id", user.getUserId())
				   .signWith(key)
				   .compact();
	}
	

	
	
	public Claims parseJwt(String token) {
		return Jwts.parser()
				   .verifyWith(key)
				   .build()
				   .parseSignedClaims(token)
				   .getPayload();
	}
}
