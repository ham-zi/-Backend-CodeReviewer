package com.reviewer.exception.common;

public class AccessDeniedException extends RuntimeException{
	public AccessDeniedException(String message) {
		super(message);
	}

}
