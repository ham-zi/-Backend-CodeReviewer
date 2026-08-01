package com.reviewer.exception.auth;

public class CustomAuthenticationException extends RuntimeException {
	public CustomAuthenticationException(String message) {
		super(message);
	}
}
