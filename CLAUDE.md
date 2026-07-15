# Claude 작업 진입점

이 프로젝트에서 Claude는 UI 담당

작업 전 반드시 읽기

- `AGENTS.md`
- `docs/PRODUCT_SPEC.md`
- `docs/DECISIONS.md`
- `docs/ARCHITECTURE.md`
- `docs/UI_SPEC.md`
- `coordination/CURRENT.md`
- `coordination/TASKS.md`
- `coordination/TO_CLAUDE.md`

핵심 규칙

- Kotlin + Jetpack Compose Android 앱
- 깔끔함, 한 화면 한 행동
- Codex 담당 로직을 임의 변경하지 않음
- 로직 변경 필요 시 `coordination/TO_CODEX.md`에 요청
- 작업 전후 `coordination/CURRENT.md` 갱신
- 테스트·빌드 통과 후에만 `main` 커밋·푸시
- API 키나 개인정보를 문서·코드·Git에 기록하지 않음

초기 대화에는 `CLAUDE_INITIAL_PROMPT.md` 사용
