package com.reviewer.common.token.model.entity;


import java.time.Instant;

import com.reviewer.user.model.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name= "refresh_token")
@Getter
@NoArgsConstructor
public class TokenEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="token_id", nullable=false)
	private Long id;
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserEntity user;
	@Column(name="token", nullable=false, length=512, unique = true)
	private String token;
	@Column(name="expires_at", nullable=false)
	private Instant expiresAt;
	@Column(name="created_at", nullable=false)
	private Instant createdAt;
   
	public TokenEntity(
            UserEntity user,
            String token,
            Instant expiresAt
    ) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
	
	
}
