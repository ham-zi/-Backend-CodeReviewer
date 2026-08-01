package com.reviewer.auth.model.dao;

import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;


import com.reviewer.auth.model.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByLoginId(String loginId);
}
