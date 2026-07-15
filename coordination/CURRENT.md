# 현재 상태

- 현재 단계: Claude Compose UI 표시 계층 구현 완료 (REQ-001 partially-completed)
- 현재 담당: 없음
- 현재 작업: 없음
- 마지막 완료: presentation 표시 계층(테마, 컴포넌트, 10개 route 화면, Nav3 호스트, Preview, UI 테스트) 구현 및 검증
- 기준 커밋: `d31f748`
- 검증: `testDebugUnitTest`, `assembleDebug`, `lintDebug` 통과. `connectedDebugAndroidTest` AVD Pixel_7에서 11개 통과(0 실패). debug APK 설치·콜드 스타트 렌더 확인
- 수정 파일: `app/src/main/.../presentation/**`, `app/src/androidTest/.../presentation/**`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, coordination 문서
- 미완료 부분: 데이터 구동 화면(Home/Study/WordManagement/Onboarding 분석)을 실제 ViewModel에 연결 — Codex 팩토리·필드 대기 (`coordination/TO_CODEX.md` REQ-002)
- 다음 행동: Codex가 REQ-002 처리 후 Claude가 `LexiLoopApp.kt`의 각 route를 실제 VM에 연결
- 브랜치: `main`, 원격 `origin` → `https://github.com/yoon778/lexiloop`
- 보류: 별도 `english` 루트 폴더가 다른 프로세스에 점유됨
- 블로커: 없음

## 작업공간

- 경로: `C:\Users\cys04\Desktop\APPS\lexiloop`
- JDK 21 (`D:\android studio\jbr`), Android SDK, AVD `Pixel_7`
- 비밀값은 `local.properties`에만 저장

## 복구 지침

다음 에이전트는 `AGENTS.md` 시작 순서대로 읽고 `git status`, `git log -1`, `git remote -v` 확인
