package com.reviewer.user.model.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reviewer.exception.user.DuplicateLoginIdException;
import com.reviewer.user.model.dto.UserDto;
import com.reviewer.user.model.entity.UserEntity;
import com.reviewer.user.model.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	
	public void signUp(@Valid UserDto user) {
		vaildateLoginId(user.getLoginId());
		userRepo.save(UserEntity.of(user.getLoginId(), passwordEncoder(user.getPassword()), user.getName()));
	}
	
	public String passwordEncoder(String password) {
		return passwordEncoder.encode(password);
	}
	
	public void vaildateLoginId(String loginId) {
		if(userRepo.existsByLoginId(loginId)) {
			throw new DuplicateLoginIdException("이미 존재하는 아이디입니다.");
		}
	}


}
