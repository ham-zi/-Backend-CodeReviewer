package com.reviewer.review.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "QUICK_REVIEW_SOURCE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class QuickSourceEntity {

    @Id
    @Column(name = "REVIEW_ID")
    private Long reviewId;
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID")
    private ReviewEntity review;
    @Column(name = "INPUT_CODE", nullable = false, columnDefinition = "TEXT")
    private String inputCode;
    
    private QuickSourceEntity(
            ReviewEntity review,
            String inputCode
    ) {
        this.review = review;
        this.inputCode = inputCode;
    }

    public static QuickSourceEntity of(
            ReviewEntity review,
            String inputCode
    ) {
        return new QuickSourceEntity(
                review,
                inputCode
        );
    }
}
