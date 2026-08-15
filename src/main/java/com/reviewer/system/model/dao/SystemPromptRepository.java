package com.reviewer.system.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.system.model.Entity.SystemPromptEntity;

public interface SystemPromptRepository extends JpaRepository<SystemPromptEntity, Long>{
    boolean existsByTypeAndVersion(
            ReviewTypeRole type,
            String version
        );
}
