package com.reviewer.review.model.entity;

import java.time.Instant;

import com.reviewer.enums.ReviewResultRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REVIEW_ITEM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private ReviewEntity review;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESULT", nullable = false, length = 30)
    private ReviewResultRole result;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "FILE_PATH", length = 500)
    private String filePath;

    @Column(name = "LOCATION", length = 300)
    private String location;

    @Column(name = "EVIDENCE", columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "DESCRIPTION", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "SUGGESTION", columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    private ReviewItemEntity(
            ReviewEntity review,
            ReviewResultRole result,
            String title,
            String filePath,
            String location,
            String evidence,
            String description,
            String suggestion) {

        this.review = review;
        this.result = result;
        this.title = title;
        this.filePath = filePath;
        this.location = location;
        this.evidence = evidence;
        this.description = description;
        this.suggestion = suggestion;
    }

    public static ReviewItemEntity of(
            ReviewEntity review,
            ReviewResultRole result,
            String title,
            String filePath,
            String location,
            String evidence,
            String description,
            String suggestion) {

        return new ReviewItemEntity(
                review,
                result,
                title,
                filePath,
                location,
                evidence,
                description,
                suggestion
        );
    }

    @PrePersist
    private void onCreated() {
        createdAt = Instant.now();
    }
}