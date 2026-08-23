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
@Table(name = "PR_REVIEW_SOURCE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PrSourceEntity {

    @Id
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID")
    private ReviewEntity review;

    @Column(name = "PULL_NUMBER", nullable = false)
    private Integer pullNumber;

    private PrSourceEntity(
            ReviewEntity review,
            Integer pullNumber
    ) {
        this.review = review;
        this.pullNumber = pullNumber;
    }

    public static PrSourceEntity of(
            ReviewEntity review,
            Integer pullNumber
    ) {
        return new PrSourceEntity(
                review,
                pullNumber
        );
    }
}
