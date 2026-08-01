package com.reviewer.auth.model.vo;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CustomUserDetails implements UserDetails {
	private String username; 
	private String password;
	private Long userId;
	private String memberName;
	private Collection<? extends GrantedAuthority> authorities;
}
