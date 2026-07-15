# Codex → Claude 인수인계

## 기준

- 브랜치: `main`
- 기준 커밋: `2d0ee21`
- 원격: `origin/main`
- 작업 요청: `coordination/TO_CLAUDE.md`의 `REQ-001`

## 완료된 Codex 범위

- 기획 확정: `2d540d9`
- Android 프로젝트: `12eecb0`
- 도메인 학습·복습 로직: `d6c4c26`
- Room 저장소: `baab2a8`
- DataStore 설정: `82fea46`
- Gemini 엄격 JSON 계약: `c66244b`
- 알림·TTS·UI 계약: `2d0ee21`

세부 요구와 계약은 기존 문서를 기준으로 사용. 이 문서에 복사하지 않음

## 검증

- JVM 단위 테스트 27개 통과
- Pixel 7 API 37 계측 테스트 4개 통과
- `assembleDebug` 통과
- `lintDebug` 통과
- debug APK 설치와 `MainActivity` cold start 성공

## Claude 작업

- `presentation/LexiLoopApp.kt` 임시 UI를 확정 Compose UI로 교체
- `presentation/contract`와 `presentation/viewmodel`을 표시 계층의 고정 경계로 사용
- `docs/UI_CONTRACT.md`, `docs/UI_SPEC.md`의 화면 흐름과 접근성 조건 준수
- 계약 변경이 필요하면 구현 전에 `coordination/TO_CODEX.md`에 요청

## 보류

- 별도 `english` 루트 폴더 삭제: 외부 프로세스 점유 해제 필요
- 실제 Gemini 호출: 사용자 로컬 `GEMINI_API_KEY` 필요. 키는 공유·커밋 금지
