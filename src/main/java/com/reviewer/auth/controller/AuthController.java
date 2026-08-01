package com.reviewer.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.auth.model.dto.LoginRequestDto;
import com.reviewer.auth.model.service.AuthService;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.auth.model.vo.LoginResponse;
import com.reviewer.auth.model.vo.LogoutResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
	
	private final AuthService authService;
	
	
	
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequestDto lrd){
		LoginResponse res = authService.login(lrd);
		return ResponseEntity.ok().body(ApiResponse.success("로그인 성공", res));
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody Map<String, String> token) {
		Map<String, String> tokens = authService.refresh(token.get("refreshToken"));
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(tokens));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Map<String, String>>> logout(@AuthenticationPrincipal CustomUserDetails user, @RequestBody LogoutResponse logout){
		authService.logout(logout.getRefreshToken());
		return ResponseEntity.status(200).body(ApiResponse.success("로그아웃 성공", null));
	}
}
