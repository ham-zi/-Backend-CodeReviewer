package com.reviewer.common.token.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.common.token.model.entity.TokenEntity;

public interface TokenRepository extends JpaRepository<TokenEntity, Long>{

	TokenEntity findByToken(String refreshToken);
	void deleteByToken(String token);

}
