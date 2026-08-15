package com.reviewer.system.model.Entity;

import com.reviewer.enums.ReviewTypeRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="SYSTEM_PROMPT_SETTING")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SystemSettingEntity {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private ReviewTypeRole type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SYSTEM_PROMPT_ID", nullable = false)
    private SystemPromptEntity systemPrompt;
    
    public void changePrompt(SystemPromptEntity prompt) {
        this.systemPrompt = prompt;
    }
}
