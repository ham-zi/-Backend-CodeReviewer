package com.reviewer.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.exception.auth.CustomAuthenticationException;
import com.reviewer.exception.auth.NotFoundTokenException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(CustomAuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handlerCustomAuthentication(CustomAuthenticationException e) {
		return ResponseEntity.badRequest().body(ApiResponse.unAuthorized(e.getMessage(), null));
	}

	@ExceptionHandler(NotFoundTokenException.class)
	public ResponseEntity<ApiResponse<Void>> handlerNotFoundToken(NotFoundTokenException e) {
		return ResponseEntity.badRequest().body(ApiResponse.unAuthorized(e.getMessage(), null));
	}
	

}
