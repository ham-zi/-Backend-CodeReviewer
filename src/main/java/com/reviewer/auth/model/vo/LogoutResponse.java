package com.reviewer.auth.model.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LogoutResponse {
	private String refreshToken;
}
