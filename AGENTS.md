# 공통 에이전트 작업 규칙

## 목적

Android 전용 개인 맞춤 영단어 학습 앱 개발
개인 베타 1개월 사용 후 수정·보완하여 Play Store 출시 검토

## 시작 순서

모든 에이전트는 작업 전에 다음 순서로 확인

1. `docs/PRODUCT_SPEC.md`
2. `docs/DECISIONS.md`
3. `docs/ARCHITECTURE.md`
4. `docs/UI_SPEC.md`
5. `coordination/CURRENT.md`
6. `coordination/TASKS.md`
7. 본인 대상 요청 파일
8. `git status`와 `git diff`

## 역할

### Codex

- 도메인 모델과 학습 로직
- Room, DataStore, 저장소 계층
- 복습 알고리즘
- Gemini 연동과 응답 검증
- 알림과 Android TextToSpeech
- 단위·통합 테스트

### Claude

- Jetpack Compose UI
- 화면 흐름과 내비게이션
- 테마, 레이아웃, 접근성
- UI 상태 표현과 애니메이션
- Compose Preview와 UI 테스트

### 공통 경계

- Codex가 도메인 계약과 ViewModel 동작을 관리
- Claude가 표시 계층을 관리
- `UiState`, `UiEvent`, ViewModel 계약 변경은 먼저 요청 문서에 기록
- 다른 에이전트 담당 파일의 대규모 수정 금지

## 문서 우선순위

충돌 시 우선순위

1. 사용자의 최신 지시
2. `docs/DECISIONS.md`
3. `docs/PRODUCT_SPEC.md`
4. `docs/ARCHITECTURE.md` 또는 `docs/UI_SPEC.md`
5. `coordination` 문서

결정 변경 시 관련 문서를 같은 작업에서 함께 갱신

## 체크포인트와 인수인계

- 작업 시작 전 `coordination/CURRENT.md` 갱신
- 의미 있는 작은 단위가 끝날 때마다 다시 갱신
- 긴 작업 전 현재 파일과 다음 행동 기록
- 교대 시 `coordination/handoffs/`에 새 문서 생성
- 기존 기획을 인수인계 문서에 복사하지 말고 파일 경로와 커밋을 참조
- API 키, 비밀번호, 토큰, 개인정보 기록 금지

## 에이전트 간 요청

- Codex 대상: `coordination/TO_CODEX.md`
- Claude 대상: `coordination/TO_CLAUDE.md`
- 상태: `pending`, `accepted`, `completed`, `rejected`
- 요청마다 목적, 관련 파일, 완료 조건 포함

## Git

- 한 번에 한 에이전트만 작업
- 단일 `main` 브랜치 사용
- 작업 시작: `git status`, `git pull --ff-only origin main`
- 깨진 빌드나 실패 테스트를 `main`에 푸시 금지
- 관련 파일만 스테이징, `git add .` 지양
- Conventional Commits 사용
- 강제 푸시와 공개 이력 재작성 금지
- 작업 완료 시 검증 후 커밋·푸시
- 작업 중단 시 로컬 diff를 보존하고 `CURRENT.md`에 상태 기록

## 보안

- 개인 베타 Gemini API 키는 `local.properties`에 저장
- `local.properties`는 Git 제외
- 앱 코드, 문서, 로그, 커밋에 비밀값 기록 금지
- 공개 배포 전 Gemini 호출을 백엔드로 이전

## 범위 제한

- 사용자 승인 없이 기능 범위 확대 금지
- 기획 단계에서는 코드 작성 금지
- 관계없는 리팩터링·파일 정리 금지
