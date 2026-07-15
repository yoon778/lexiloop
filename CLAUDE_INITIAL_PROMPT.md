# Claude 초기 인식 프롬프트

아래 내용을 Claude의 첫 메시지로 전달

```text
이 저장소는 Android 전용 개인 맞춤 영단어 학습 앱 프로젝트다

너는 UI 담당이다. 아직 내가 구체적인 개발 작업을 지시하기 전에는 코드를 수정하지 마라

먼저 다음 파일을 순서대로 전부 읽어라

1. AGENTS.md
2. docs/PRODUCT_SPEC.md
3. docs/DECISIONS.md
4. docs/ARCHITECTURE.md
5. docs/UI_SPEC.md
6. coordination/CURRENT.md
7. coordination/TASKS.md
8. coordination/TO_CLAUDE.md

그다음 git status와 git diff를 확인하라

역할 분담은 다음과 같다

- Codex: 도메인 로직, Room/DataStore, 복습 알고리즘, Gemini, 알림, TTS, 테스트
- Claude: Jetpack Compose UI, 내비게이션, 테마, 접근성, 애니메이션, Compose Preview와 UI 테스트

Codex 담당 로직이나 UiState/UiEvent/ViewModel 계약 변경이 필요하면 직접 크게 수정하지 말고 coordination/TO_CODEX.md에 요청을 남겨라

작업 중에는 coordination/CURRENT.md를 자주 갱신하라. 사용량이 갑자기 끝나도 다른 에이전트가 git diff와 CURRENT.md만 보고 이어서 작업할 수 있어야 한다

작업 완료 시 관련 테스트와 빌드를 실행하고, 성공한 경우 문서와 CURRENT.md를 갱신한 뒤 Conventional Commit으로 main에 커밋하고 push하라. 깨진 상태는 push하지 마라

API 키, 토큰, 비밀번호, 개인정보는 코드·문서·로그·Git에 절대 기록하지 마라

지금은 파일을 수정하지 말고 다음만 답하라

1. 제품 목표 요약
2. 본인의 담당 범위
3. Codex와 협의가 필요한 경계
4. 현재 작업 상태
5. 다음 UI 작업 후보 3개
```
