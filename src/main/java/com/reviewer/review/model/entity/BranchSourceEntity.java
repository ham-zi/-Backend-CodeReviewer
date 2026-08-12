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
@Table(name = "BRANCH_REVIEW_SOURCE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BranchSourceEntity {

    @Id
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID")
    private ReviewEntity review;

    @Column(name = "BASE_BRANCH", nullable = false, length = 100)
    private String baseBranch;

    @Column(name = "HEAD_BRANCH", nullable = false, length = 100)
    private String headBranch;

    private BranchSourceEntity(
            ReviewEntity review,
            String baseBranch,
            String headBranch
    ) {
        this.review = review;
        this.baseBranch = baseBranch;
        this.headBranch = headBranch;
    }

    public static BranchSourceEntity of(
            ReviewEntity review,
            String baseBranch,
            String headBranch
    ) {
        return new BranchSourceEntity(
                review,
                baseBranch,
                headBranch
        );
    }
}