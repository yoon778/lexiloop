# LexiLoop

개인 관심사에 맞는 Android 영단어 학습 앱

사용자가 자연어로 학습 목적을 입력하면 AI가 관심 분야와 난이도를 분석하고, 검수된 사전 데이터와 결합해 맞춤 단어장을 구성

## 현재 상태

- 기획 완료
- Android 프로젝트 생성·첫 빌드 완료
- Codex 담당 로직 구현 중
- 1개월 개인 베타 후 Play Store 출시 검토

## 기술 방향

- Kotlin
- Jetpack Compose
- Room
- DataStore
- Gemini API
- Android TextToSpeech

## 문서

- [제품 기획](docs/PRODUCT_SPEC.md)
- [기술 아키텍처](docs/ARCHITECTURE.md)
- [기술 스택](docs/TECH_STACK.md)
- [개발 환경과 명령](docs/DEVELOPMENT.md)
- [영한 데이터 출처](docs/DATA_SOURCES.md)
- [UI 기획](docs/UI_SPEC.md)
- [UI 계약과 와이어프레임](docs/UI_CONTRACT.md)
- [확정 결정](docs/DECISIONS.md)
- [에이전트 작업 규칙](AGENTS.md)
- [현재 작업 상태](coordination/CURRENT.md)

## 에이전트 역할

- Codex: 학습 로직, DB, 복습, AI, 알림, TTS, 테스트
- Claude: Jetpack Compose UI, 내비게이션, 테마, 접근성

한 번에 한 에이전트만 `main`에서 작업하며 완료된 단위만 검증 후 커밋·푸시

- Codex 재접속에는 [Codex 초기 프롬프트](CODEX_INITIAL_PROMPT.md) 사용
- Claude 첫 작업에는 [Claude 초기 프롬프트](CLAUDE_INITIAL_PROMPT.md) 사용

## 보안

- Gemini API 키는 `local.properties`에만 저장
- API 키, 서명 키, 개인정보는 Git에 커밋하지 않음
- 공개 버전은 Gemini 호출을 백엔드로 이전
