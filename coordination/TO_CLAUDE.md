# Codex에서 Claude로 보내는 요청

## REQ-001

- From: Codex
- To: Claude
- Status: pending
- 요청: Jetpack Compose 전체 화면·Navigation 3·테마·접근성·Preview·UI 테스트 구현
- 이유: 도메인, 저장소, 플랫폼 기능, route와 ViewModel UI 계약 준비 완료
- 기준 커밋: `2d0ee21`
- 관련 파일:
  - `docs/PRODUCT_SPEC.md`
  - `docs/UI_SPEC.md`
  - `docs/UI_CONTRACT.md`
  - `docs/ARCHITECTURE.md`
  - `app/src/main/java/com/yoon778/lexiloop/presentation/contract/`
  - `app/src/main/java/com/yoon778/lexiloop/presentation/viewmodel/`
  - `app/src/main/java/com/yoon778/lexiloop/presentation/LexiLoopApp.kt`
- 작업 경계:
  - `LexiLoopApp.kt`의 임시 화면 교체 가능
  - Compose 화면·컴포넌트·테마·내비게이션·Preview·UI 테스트만 수정
  - domain, data, platform, ViewModel 계약 변경 금지
  - 계약 문제가 있으면 이 문서에 별도 요청 기록
  - Gemini 키·프롬프트·응답 로그 금지
- 권장 스킬: `ponytail`, `accessibility`, `design-system`
- 완료 조건:
  - 확정된 10개 route와 모든 로딩·빈 상태·오류 상태 표시
  - `UiState` 표시와 `UiEvent` 전달 구조 준수
  - 시스템/밝게/어둡게 테마와 200% 글자 크기 대응
  - 최소 48dp 터치 영역, 아이콘 설명, 색상 외 정오답 표현
  - 핵심 화면 Preview와 내비게이션/UI 테스트 작성
  - `testDebugUnitTest`, `connectedDebugAndroidTest`, `assembleDebug`, `lintDebug` 통과
  - coordination 문서 갱신 후 커밋·푸시
- 답변:
