# 현재 상태

- 현재 단계: 핵심 단어 중심 추천 실제 기기 검증 완료
- 현재 담당: Codex
- 현재 작업: Claude 보조 표현 구분 UI 인계 대기
- 마지막 완료: 실제 폰 Gemini 300개 생성 — 핵심 단어 240개·보조 표현 60개, 6개 배치 각각 40:10 확인
- 기준 커밋: `1a98099`
- 검증: `testDebugUnitTest`, `assembleDebug`, `lintDebug`, 실제 폰 로직 테스트 7개 통과. 실제 Gemini 일상 150·개발 150과 240:60 구성 확인
- 수정 파일: Gemini 계약·검증·프롬프트, DAO, 저장소, ViewModel, 단위·Android 통합 테스트, 제품·구조 문서
- 미완료 부분: Claude 보조 표현 배지 UI
- 다음 행동: `coordination/TO_CLAUDE.md` REQ-004 인계
- 브랜치: `main`, 원격 `origin` → `https://github.com/yoon778/lexiloop`
- 보류: 별도 `english` 루트 폴더가 다른 프로세스에 점유됨
- 블로커: 없음
- 참고: 실제 폰 전체 Android 테스트 중 기존 Compose UI 테스트 7개는 `No compose hierarchies found`로 실패, 변경 범위 로직 테스트는 별도 통과

## 작업공간

- 경로: `C:\Users\cys04\Desktop\APPS\lexiloop`
- JDK 21 (`D:\android studio\jbr`), Android SDK, AVD `Pixel_7`
- 비밀값은 `local.properties`에만 저장

## 복구 지침

다음 에이전트는 `AGENTS.md` 시작 순서대로 읽고 `git status`, `git log -1`, `git remote -v` 확인
