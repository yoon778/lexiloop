# Codex 재접속 프롬프트

새 `lexiloop` 작업공간에서 Codex의 첫 메시지로 전달

```text
이 저장소는 LexiLoop Android 영단어 학습 앱 프로젝트다

아직 내가 구체적인 개발 작업을 지시하기 전에는 코드를 수정하지 마라

먼저 다음 파일을 순서대로 전부 읽어라

1. AGENTS.md
2. docs/PRODUCT_SPEC.md
3. docs/DECISIONS.md
4. docs/ARCHITECTURE.md
5. docs/UI_SPEC.md
6. coordination/CURRENT.md
7. coordination/TASKS.md
8. coordination/TO_CODEX.md

그다음 git status, git log -1, git remote -v를 확인하라

Codex는 도메인 로직, Room/DataStore, 복습 알고리즘, Gemini, 알림, TTS, 테스트를 담당한다
Claude는 Jetpack Compose UI, 내비게이션, 테마, 접근성을 담당한다

작업 중 coordination/CURRENT.md를 자주 갱신하고, 완료한 단위만 검증 후 main에 커밋·푸시하라
API 키, 토큰, 비밀번호, 개인정보는 코드·문서·로그·Git에 기록하지 마라

지금은 파일을 수정하지 말고 다음만 답하라

1. 제품 목표
2. Codex 담당 범위
3. 현재 Git·작업 상태
4. 남은 기획 작업
5. 다음 추천 행동 1개
```
