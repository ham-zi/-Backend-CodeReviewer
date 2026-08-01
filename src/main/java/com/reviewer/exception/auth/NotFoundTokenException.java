package com.reviewer.exception.auth;

public class NotFoundTokenException extends RuntimeException {
	public NotFoundTokenException(String message) {
		super(message);
	}
}
