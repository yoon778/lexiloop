# 현재 상태

- 현재 단계: 실제 데이터 배선
- 현재 담당: Claude 재인계 준비
- 현재 작업: Codex REQ-002 완료, Compose route 실제 ViewModel 연결 대기
- 마지막 완료: ViewModel provider, Room/DataStore/Gemini, 실제 학습 세션 로직 연결
- 기준 커밋: `bc4da61`
- 검증: `testDebugUnitTest`, `assembleDebug`, `lintDebug` 통과. `connectedDebugAndroidTest` AVD Pixel_7에서 12개 통과(0 실패)
- 수정 파일: `app/src/main/.../presentation/**`, `app/src/androidTest/.../presentation/**`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, coordination 문서
- 미완료 부분: `LexiLoopApp.kt` 각 route에서 `LexiLoopApplication.viewModels` 사용
- 다음 행동: Claude가 표시 계층에서 provider를 수집하고 effect·화면에 연결
- 브랜치: `main`, 원격 `origin` → `https://github.com/yoon778/lexiloop`
- 보류: 별도 `english` 루트 폴더가 다른 프로세스에 점유됨
- 블로커: 없음

## 작업공간

- 경로: `C:\Users\cys04\Desktop\APPS\lexiloop`
- JDK 21 (`D:\android studio\jbr`), Android SDK, AVD `Pixel_7`
- 비밀값은 `local.properties`에만 저장

## 복구 지침

다음 에이전트는 `AGENTS.md` 시작 순서대로 읽고 `git status`, `git log -1`, `git remote -v` 확인
