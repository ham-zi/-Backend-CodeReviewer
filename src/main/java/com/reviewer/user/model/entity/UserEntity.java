package com.reviewer.user.model.entity;

import java.time.Instant;

import com.reviewer.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name="users",
       uniqueConstraints = {
    		   @UniqueConstraint(name = "uk_users_login_id", columnNames = "login_id")
       }
)
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;
	
	@Column(name = "login_id", nullable = false, length = 20)
	private String loginId;
	
	@Column(name = "password", nullable = false, length = 100)
	private String password;
	
	@Column(name = "name", nullable = false, length = 20)
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private UserRole role = UserRole.USER;

	@Column(name = "active", nullable = false)
	private boolean active = true;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
	
	@Column(name = "last_login_at")
	private Instant lastLoginAt;
	
	@Column(name ="image_url", nullable = false, length = 100)
	private String imageUrl = "basic_image.jpg";
	


	protected UserEntity() {
		
	}
	
	private UserEntity(String loginId, String password, String name) {
		this.loginId = loginId;
		this.password = password;
		this.name = name;
	}
	
	public static UserEntity of(String loginId, String password, String name) {
		return new UserEntity(loginId, password, name);
	}
	
    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
    public String getImageUrl() {
    	return imageUrl;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void markLoggedIn() {
        this.lastLoginAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }
	
	
}
