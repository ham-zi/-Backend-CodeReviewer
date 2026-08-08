package com.reviewer.exception.user;

public class DuplicateLoginIdException extends RuntimeException{
	public DuplicateLoginIdException (String message) {
		super(message);
	}
}
