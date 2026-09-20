package com.reviewer.github.webhook.model.service;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.reviewer.github.model.service.GithubClient;
import com.reviewer.github.webhook.model.dto.GithubWebhookReviewWork;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubPullRequestSummaryService {

    private static final Pattern COMMENT_ID_PATTERN =
            Pattern.compile("(?:issuecomment-|/comments/)(\\d+)(?:$|[?#])");
    private static final int LOCK_STRIPES = 64;

    private final GithubClient githubClient;
    private final GithubWebhookDeliveryService webhookDeliveryService;

    /*
     * 같은 애플리케이션 인스턴스에서 동일 PR의 두 리뷰가 동시에 완료되어
     * 둘 다 최초 POST를 실행하는 것을 막는다.
     */
    private final ReentrantLock[] pullRequestLocks = createLocks();

    public String publish(
            GithubWebhookReviewWork work,
            String summary
    ) {
        String lockKey = work.projectId() + ":" + work.pullNumber();
        ReentrantLock lock = pullRequestLocks[
                Math.floorMod(lockKey.hashCode(), pullRequestLocks.length)
        ];

        lock.lock();
        try {
            Optional<String> existingCommentUrl =
                    webhookDeliveryService.findSummaryCommentUrl(
                            work.projectId(),
                            work.pullNumber()
                    );

            /*
             * 더 최신 push가 이미 접수됐다면 오래된 리뷰 결과로 요약을
             * 덮어쓰지 않는다. 기존 URL은 delivery 이력에 그대로 연결한다.
             */
            if (!webhookDeliveryService.isLatestDelivery(
                    work.webhookDeliveryId(),
                    work.projectId(),
                    work.pullNumber()
            )) {
                log.info(
                        "최신 Webhook이 존재하여 오래된 PR 요약 게시를 건너뜁니다. deliveryId={}, projectId={}, pullNumber={}",
                        work.webhookDeliveryId(),
                        work.projectId(),
                        work.pullNumber()
                );
                return existingCommentUrl.orElse(null);
            }

            Optional<Long> existingCommentId = existingCommentUrl
                    .flatMap(this::extractCommentId);

            if (existingCommentId.isPresent()) {
                try {
                    return githubClient.updatePullRequestComment(
                            work.repositoryOwner(),
                            work.repositoryName(),
                            existingCommentId.get(),
                            summary
                    );
                } catch (HttpClientErrorException.NotFound exception) {
                    log.warn(
                            "기존 PR 요약 코멘트가 GitHub에서 삭제되어 새로 생성합니다. projectId={}, pullNumber={}, commentId={}",
                            work.projectId(),
                            work.pullNumber(),
                            existingCommentId.get()
                    );
                }
            } else if (existingCommentUrl.isPresent()) {
                log.warn(
                        "기존 PR 요약 코멘트 URL에서 commentId를 찾지 못해 새로 생성합니다. commentUrl={}",
                        existingCommentUrl.get()
                );
            }

            return githubClient.createPullRequestComment(
                    work.repositoryOwner(),
                    work.repositoryName(),
                    work.pullNumber(),
                    summary
            );
        } finally {
            lock.unlock();
        }
    }

    Optional<Long> extractCommentId(String commentUrl) {
        if (commentUrl == null || commentUrl.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = COMMENT_ID_PATTERN.matcher(commentUrl);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(Long.parseLong(matcher.group(1)));
    }

    private static ReentrantLock[] createLocks() {
        ReentrantLock[] locks = new ReentrantLock[LOCK_STRIPES];
        for (int index = 0; index < locks.length; index++) {
            locks[index] = new ReentrantLock();
        }
        return locks;
    }
}
