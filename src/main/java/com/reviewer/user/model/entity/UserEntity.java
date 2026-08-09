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
@Table(name="USERS",
       uniqueConstraints = {
    		   @UniqueConstraint(name = "UK_USERS_LOGIN_ID", columnNames = "LOGIN_ID")
       }
)
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "USER_ID")
	private Long id;
	
	@Column(name = "LOGIN_ID", nullable = false, length = 20)
	private String loginId;
	
	@Column(name = "PASSWORD", nullable = false, length = 100)
	private String password;
	
	@Column(name = "NAME", nullable = false, length = 20)
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ROLE", nullable = false, length = 20)
	private UserRole role = UserRole.USER;

	@Column(name = "ACTIVE", nullable = false)
	private boolean active = true;
	
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Instant createdAt;
	
	@Column(name = "LAST_LOGIN_AT")
	private Instant lastLoginAt;
	
	@Column(name ="IMAGE_URL", nullable = false, length = 100)
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
