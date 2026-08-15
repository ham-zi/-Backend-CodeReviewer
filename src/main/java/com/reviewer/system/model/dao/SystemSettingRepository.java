package com.reviewer.system.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.system.model.Entity.SystemSettingEntity;

public interface SystemSettingRepository extends JpaRepository<SystemSettingEntity, ReviewTypeRole>{

}
