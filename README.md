# ReviewMate Backend

Spring Boot 기반 코드 리뷰 API입니다. 수동 Quick/PR 리뷰와 함께 GitHub Webhook을 이용한 자동 PR 리뷰를 지원합니다.

## GitHub Webhook 자동 리뷰

다음 `pull_request` action을 수신하면 자동으로 현재 PR diff를 조회하고 AI 리뷰를 실행합니다.

- `opened`: PR이 새로 생성됨
- `reopened`: PR이 다시 열림
- `synchronize`: PR head 브랜치에 새 커밋이 push되거나 force-push됨

리뷰가 완료되면 일반 코드 리뷰와 팀 규칙 리뷰를 하나의 GitHub PR 타임라인 코멘트로 등록합니다. 동일한 repository/PR/head SHA는 한 번만 처리하며, 실패한 GitHub delivery가 재전송되면 이어서 다시 시도합니다.

## 필수 환경 변수

```env
AI_PROVIDER=openai
OPENAI_API_KEY=replace-with-openai-api-key
OPENAI_MODEL=gpt-5.4

GITHUB_TOKEN=replace-with-github-token
GITHUB_WEBHOOK_SECRET=replace-with-a-long-random-secret
GITHUB_MAX_DIFF_CHARACTERS=120000
```

`GITHUB_TOKEN`에는 대상 repository 접근 권한과 **Pull requests: Read and write** 권한이 필요합니다. PR diff 조회에는 read 권한이, PR 타임라인 코멘트 생성에는 write 권한이 사용됩니다.

`GITHUB_WEBHOOK_SECRET`은 GitHub Webhook 설정 화면의 Secret과 정확히 같아야 합니다. 서버는 원본 요청 body와 `X-Hub-Signature-256` 헤더를 HMAC-SHA256으로 검증합니다.

`GITHUB_MAX_DIFF_CHARACTERS`는 AI로 전송할 diff의 최대 문자 수이며 기본값은 `120000`입니다. 바이너리 파일이나 GitHub API가 patch를 생략한 파일은 리뷰 대상에서 제외됩니다.

## GitHub Webhook 등록

Repository의 **Settings → Webhooks → Add webhook**에서 다음처럼 설정합니다.

1. Payload URL: `https://YOUR_DOMAIN/api/webhooks/github`
2. Content type: `application/json`
3. Secret: 서버의 `GITHUB_WEBHOOK_SECRET`과 같은 값
4. Events: **Pull requests**
5. Active: 활성화

Webhook URL은 GitHub에서 접근 가능한 HTTPS 주소여야 합니다. `localhost`로 개발할 때는 터널링 도구 등으로 외부 HTTPS URL을 연결해야 합니다.

## 사전 데이터 설정

자동 리뷰 전에 ReviewMate에서 다음 항목이 준비되어 있어야 합니다.

1. Webhook을 설치한 GitHub owner/repository와 같은 프로젝트가 등록되어 있어야 합니다.
2. 프로젝트에 팀 규칙이 적용되어 있어야 합니다.
3. PR 타입의 시스템 프롬프트가 활성화되어 있어야 합니다.

저장소가 등록되어 있지 않거나 대상이 아닌 action이면 Webhook은 안전하게 무시됩니다.

## 실행과 테스트

```bash
./gradlew bootRun
./gradlew test
```

Windows에서는 각각 `gradlew.bat bootRun`, `gradlew.bat test`를 사용할 수 있습니다.

Webhook 서명 검증 테스트는 GitHub 공식 문서의 HMAC-SHA256 테스트 벡터를 사용합니다.
