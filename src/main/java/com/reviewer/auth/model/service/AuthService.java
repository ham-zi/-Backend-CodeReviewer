package com.reviewer.auth.model.service;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.reviewer.auth.model.dto.LoginRequestDto;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.auth.model.vo.LoginResponse;
import com.reviewer.common.token.model.service.TokenService;
import com.reviewer.exception.auth.CustomAuthenticationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final TokenService tokenService;

	public LoginResponse login(LoginRequestDto lrd) {
		CustomUserDetails user = getCustomUserDetails(lrd);
		Map<String, String> tokens = tokenService.getTokens(user);
		return LoginResponse.builder()
				   	        .id(user.getUserId())
							.userId(user.getUsername())
							.userName(user.getMemberName())
							.role(user.getAuthorities().toString())
							.accessToken(tokens.get("accessToken"))
							.refreshToken(tokens.get("refreshToken"))
							.build();
	}

	private CustomUserDetails getCustomUserDetails(LoginRequestDto lrd){
		Authentication auth = null;
		try {
			auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(lrd.getLoginId(), lrd.getPassword()));
		} catch(AuthenticationException e) {
			throw new CustomAuthenticationException("아이디 또는 비밀번호가 올바르지 않습니다.");
		}
		 return (CustomUserDetails)auth.getPrincipal();
	}
	
	public Map<String, String> refresh(String refreshToken) {
		Map<String, String>tokens = tokenService.tokenRotation(refreshToken);

		return tokens;
	}

	public void logout(String refreshToken) {
		tokenService.deleteToken(refreshToken);
	}
	
	
	
}