package com.reviewer.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.user.model.dto.UserDto;
import com.reviewer.user.model.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {
	
	private final UserService userService;

	
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody @Valid UserDto user){
		userService.signUp(user);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("계정 생성 성공", null));
	}
}
