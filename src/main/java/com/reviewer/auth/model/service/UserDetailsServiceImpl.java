package com.reviewer.auth.model.service;


import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.reviewer.auth.model.dao.UserRepository;
import com.reviewer.auth.model.entity.UserEntity;
import com.reviewer.auth.model.vo.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private final UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity user = userRepo.findByLoginId(username).orElseThrow(()->new UsernameNotFoundException("사용자가 존재하지 않습니다."));
		if(user == null) {
			throw new UsernameNotFoundException("요거 있다구요~");
		}
		if(!user.isActive()) {
		    throw new DisabledException("비활성화된 계정입니다.");
		}
		return CustomUserDetails.builder()
								.username(user.getLoginId())
								.password(user.getPassword())
								.userId(user.getId())
								.memberName(user.getName())
								.authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString())))
								.build();
	}
}