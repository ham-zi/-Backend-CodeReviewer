package com.reviewer.common.token.model.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.common.token.Util.JwtUtil;
import com.reviewer.common.token.model.dao.TokenRepository;
import com.reviewer.common.token.model.entity.TokenEntity;
import com.reviewer.exception.auth.CustomAuthenticationException;
import com.reviewer.exception.auth.NotFoundTokenException;
import com.reviewer.user.model.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
	private final TokenRepository tokenRepo;
	private final UserRepository userRepo;
	private final JwtUtil jwtUtil;

	public Map<String, String> getTokens(CustomUserDetails user) {
		
		Map<String, String> tokens = createTokens(user);
		saveToken(tokens.get("refreshToken"), user.getUserId());
		return tokens;
	}

	private Map<String, String> createTokens(CustomUserDetails user) {
		return Map.of("accessToken", jwtUtil.getAccessToken(user),
					  "refreshToken", jwtUtil.getRefreshToken(user));
	}

	private void saveToken(String token, Long userId) {
		TokenEntity tokenEntity = new TokenEntity(userRepo.findById(userId).orElseThrow(() -> new CustomAuthenticationException("존재하지 않는 아이디 입니다.")),
				                                  token,
				                                  Instant.now().plusSeconds(60 * 60 * 24 * 3));
		tokenRepo.save(tokenEntity);
	}
	
	public Map<String, String> tokenRotation(String refreshToken){
		TokenEntity token = tokenRepo.findByToken(refreshToken);
		Claims claims = null;
		try {
			claims = jwtUtil.parseJwt(token.getToken());
		} catch (ExpiredJwtException e) {
			throw new NotFoundTokenException("토큰만료");
		} catch (JwtException | IllegalArgumentException e) {
			throw new NotFoundTokenException("유효하지 않은 토큰입니다.");
		}
		String userId = claims.getSubject();
		String memberName = (String)claims.get("memberName");
		CustomUserDetails user = CustomUserDetails.builder().memberName(memberName).username(userId).build();
		hasRefreshToken(token);
		Map<String, String> tokens = createTokens(user);
		saveToken(tokens.get("refreshToken"), user.getUserId());
		deleteToken(refreshToken);
		return tokens;
	}
	
	private void hasRefreshToken(TokenEntity token) {
		if(token == null) {
			throw new CustomAuthenticationException("일치하는  토큰이 없습니다.");
		}
		if(!token.isExpired()) {
			deleteToken(token.getToken());
			throw new CustomAuthenticationException("유효하지않은 토큰입니다.");
		}
	}

	public void deleteToken(String refreshToken) {
		tokenRepo.deleteByToken(refreshToken);

	}
}	
	
	
	
	