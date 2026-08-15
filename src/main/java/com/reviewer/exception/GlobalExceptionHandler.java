package com.reviewer.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.exception.auth.CustomAuthenticationException;
import com.reviewer.exception.auth.NotFoundTokenException;
import com.reviewer.exception.common.AccessDeniedException;
import com.reviewer.exception.common.DuplicateException;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.exception.user.DuplicateLoginIdException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	//400
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handlerNotFound(NotFoundException e) {
		return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage(), null));
	}
	
	
	
	//401
	
	@ExceptionHandler(CustomAuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handlerCustomAuthentication(CustomAuthenticationException e) {
		return ResponseEntity.badRequest().body(ApiResponse.unAuthorized(e.getMessage(), null));
	}

	@ExceptionHandler(NotFoundTokenException.class)
	public ResponseEntity<ApiResponse<Void>> handlerNotFoundToken(NotFoundTokenException e) {
		return ResponseEntity.badRequest().body(ApiResponse.unAuthorized(e.getMessage(), null));
	}
	
	//403
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> hanlderAccessDenied(AccessDeniedException e) {
		return ResponseEntity.badRequest().body(ApiResponse.forbidden(e.getMessage(), null));
	}
	
	//409
	
	@ExceptionHandler(DuplicateLoginIdException.class)
	public ResponseEntity<ApiResponse<Void>> handlerDuplicateLoginId(DuplicateLoginIdException e) {
		return ResponseEntity.badRequest().body(ApiResponse.conplict(e.getMessage(), null));
	}

	@ExceptionHandler(DuplicateException.class)
	public ResponseEntity<ApiResponse<Void>> handlerDuplicate(DuplicateException e) {
		return ResponseEntity.badRequest().body(ApiResponse.conplict(e.getMessage(), null));
	}
	


}
