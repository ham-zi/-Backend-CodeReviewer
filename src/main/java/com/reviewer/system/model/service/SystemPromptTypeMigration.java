package com.reviewer.system.model.service;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기존 BRANCH 리뷰를 PR 리뷰로 변경하면서
 * 이미 DB에 저장되어 있던 BRANCH 시스템 프롬프트 설정을 PR 설정으로 옮긴다.
 *
 * ReviewTypeRole.BRANCH 값 자체는 과거 리뷰 이력 조회를 위해 남겨두지만,
 * 앞으로 새 리뷰에서 사용하는 시스템 프롬프트와 현재 설정은 PR을 사용한다.
 *
 * ddl-auto=update는 컬럼/테이블 구조는 변경해주지만
 * 기존 데이터의 enum 문자열(BRANCH -> PR)까지 변경하지는 않기 때문에
 * 애플리케이션 시작 시 한 번 호환 마이그레이션을 수행한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemPromptTypeMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        List<Long> branchSettingPromptIds = jdbcTemplate.query(
                """
                SELECT SYSTEM_PROMPT_ID
                FROM SYSTEM_PROMPT_SETTING
                WHERE TYPE = 'BRANCH'
                """,
                (rs, rowNum) -> rs.getLong("SYSTEM_PROMPT_ID")
        );

        Integer branchPromptCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SYSTEM_PROMPT WHERE TYPE = 'BRANCH'",
                Integer.class
        );

        if (branchSettingPromptIds.isEmpty()
                && (branchPromptCount == null || branchPromptCount == 0)) {
            return;
        }

        String selectedVersion = null;

        if (!branchSettingPromptIds.isEmpty()) {
            Long branchPromptId = branchSettingPromptIds.get(0);

            List<String> versions = jdbcTemplate.query(
                    """
                    SELECT VERSION
                    FROM SYSTEM_PROMPT
                    WHERE SYSTEM_PROMPT_ID = ?
                    """,
                    (rs, rowNum) -> rs.getString("VERSION"),
                    branchPromptId
            );

            if (!versions.isEmpty()) {
                selectedVersion = versions.get(0);
            }
        }

        /*
         * BRANCH용 프롬프트 버전을 PR용으로 변경한다.
         * 같은 버전의 PR 프롬프트가 이미 존재한다면 UNIQUE(TYPE, VERSION)
         * 충돌을 피하기 위해 기존 BRANCH 행은 그대로 보존한다.
         * 이 행은 과거 리뷰가 참조할 수도 있으므로 삭제하지 않는다.
         */
        int migratedPromptCount = jdbcTemplate.update(
                """
                UPDATE SYSTEM_PROMPT AS branch_prompt
                SET TYPE = 'PR'
                WHERE branch_prompt.TYPE = 'BRANCH'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM SYSTEM_PROMPT AS pr_prompt
                      WHERE pr_prompt.TYPE = 'PR'
                        AND pr_prompt.VERSION = branch_prompt.VERSION
                  )
                """
        );

        if (selectedVersion != null) {
            List<Long> prPromptIds = jdbcTemplate.query(
                    """
                    SELECT SYSTEM_PROMPT_ID
                    FROM SYSTEM_PROMPT
                    WHERE TYPE = 'PR'
                      AND VERSION = ?
                    ORDER BY SYSTEM_PROMPT_ID DESC
                    LIMIT 1
                    """,
                    (rs, rowNum) -> rs.getLong("SYSTEM_PROMPT_ID"),
                    selectedVersion
            );

            if (!prPromptIds.isEmpty()) {
                Long targetPromptId = prPromptIds.get(0);

                Integer prSettingCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM SYSTEM_PROMPT_SETTING WHERE TYPE = 'PR'",
                        Integer.class
                );

                if (prSettingCount != null && prSettingCount > 0) {
                    // PR 설정이 이미 있다면 기존 BRANCH에서 선택했던 프롬프트로 맞춘다.
                    jdbcTemplate.update(
                            """
                            UPDATE SYSTEM_PROMPT_SETTING
                            SET SYSTEM_PROMPT_ID = ?
                            WHERE TYPE = 'PR'
                            """,
                            targetPromptId
                    );

                    jdbcTemplate.update(
                            "DELETE FROM SYSTEM_PROMPT_SETTING WHERE TYPE = 'BRANCH'"
                    );
                } else {
                    // 기존 BRANCH 설정 행 자체를 PR 설정 행으로 변경한다.
                    jdbcTemplate.update(
                            """
                            UPDATE SYSTEM_PROMPT_SETTING
                            SET TYPE = 'PR',
                                SYSTEM_PROMPT_ID = ?
                            WHERE TYPE = 'BRANCH'
                            """,
                            targetPromptId
                    );
                }
            }
        }

        log.info(
                "BRANCH 시스템 프롬프트를 PR용으로 변경했습니다. migratedPromptCount={}",
                migratedPromptCount
        );
    }
}
