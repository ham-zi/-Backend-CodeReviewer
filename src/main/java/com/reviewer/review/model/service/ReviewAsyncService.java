package com.reviewer.review.model.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.reviewer.github.model.dto.GithubFileResponse;
import com.reviewer.github.model.service.GithubClient;
import com.reviewer.review.model.dto.PrReviewProcessData;
import com.reviewer.review.model.dto.ReviewProcessData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAsyncService {

    private final ReviewTransactionService reviewTransactionService;
    private final TeamReviewProcessor teamReviewProcessor;
    private final GithubClient githubClient;

    @Async
    public void processQuick(Long reviewId) {

        try {
            ReviewProcessData data =
                    reviewTransactionService.startQuick(reviewId);

            teamReviewProcessor.process(
                    reviewId,
                    data
            );

            log.info(
                    "QUICK 비동기 코드 리뷰 완료. reviewId={}",
                    reviewId
            );

        } catch (Exception e) {
            handleFail(reviewId, "QUICK", e);
        }
    }

    @Async
    public void processPr(Long reviewId) {

        try {
            PrReviewProcessData data =
                    reviewTransactionService.startPr(reviewId);

            /*
             * GitHub REST API에서 PR에 포함된 변경 파일 목록을 조회한다.
             * 여기서 가져온 patch는 PR의 base와 head 사이에 실제로 변경된 diff이므로
             * 기존 BRANCH compare 결과처럼 LLM 리뷰 대상으로 사용할 수 있다.
             */
            List<GithubFileResponse> files =
                    githubClient.getPullRequestFiles(
                            data.gitRepoOwner(),
                            data.gitRepoName(),
                            data.pullNumber()
                    );

            if (files.isEmpty()) {
                throw new IllegalStateException(
                        "PR에 리뷰할 변경 파일이 존재하지 않습니다."
                );
            }

            String sourceCode = createPrSource(files);

            ReviewProcessData reviewData =
                    new ReviewProcessData(
                            sourceCode,
                            data.ruleContent(),
                            data.generalSystemPrompt(),
                            data.ruleSystemPrompt()
                    );

            teamReviewProcessor.process(
                    reviewId,
                    reviewData
            );

            log.info(
                    "PR 비동기 코드 리뷰 완료. reviewId={}, pullNumber={}",
                    reviewId,
                    data.pullNumber()
            );

        } catch (Exception e) {
            handleFail(reviewId, "PR", e);
        }
    }

    private String createPrSource(
            List<GithubFileResponse> files
    ) {

        StringBuilder sb = new StringBuilder();
        sb.append("## PR 변경 코드 ##\n");

        for (GithubFileResponse file : files) {
            sb.append("파일: ")
              .append(file.filename())
              .append("\n");

            sb.append("상태: ")
              .append(file.status())
              .append("\n");

            if (file.patch() == null || file.patch().isBlank()) {
                // binary 파일이나 GitHub가 patch를 생략한 큰 파일은 코드 리뷰 대상에서 제외한다.
                sb.append("변경사항: GitHub API에서 patch를 제공하지 않음\n\n");
                continue;
            }

            sb.append("변경사항:\n")
              .append(file.patch())
              .append("\n\n");
        }

        return sb.toString();
    }

    private void handleFail(
            Long reviewId,
            String reviewType,
            Exception e
    ) {

        log.error(
                "{} 리뷰 처리 실패. reviewId={}",
                reviewType,
                reviewId,
                e
        );

        try {
            reviewTransactionService.fail(reviewId);
        } catch (Exception failException) {
            log.error(
                    "리뷰 FAILED 상태 변경 중 예외 발생. reviewId={}",
                    reviewId,
                    failException
            );
        }
    }
}
