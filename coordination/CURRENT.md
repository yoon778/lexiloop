# 현재 상태

- 현재 단계: Android 구현
- 현재 담당: Claude 인계 대기
- 현재 작업: `coordination/TO_CLAUDE.md`의 `REQ-001`
- 마지막 완료: 전체 빌드·lint·JVM·기기 통합·설치·실행 검증
- 수정 파일: `app/src/main`, `app/src/test`, coordination 문서
- 검증: JVM 27개, assembleDebug·lintDebug 통과, Pixel 7 API 37 Room 통합 4개 통과
- 다음 행동: Claude가 Compose UI·Navigation 3·테마·접근성·Preview·UI 테스트 구현
- 브랜치: `main`
- 원격: `origin` → `https://github.com/yoon778/lexiloop`
- 보류: 별도 `english` 루트 폴더가 다른 프로세스에 점유됨

## 작업공간

- 경로: `C:\Users\cys04\Desktop\APPS\lexiloop`
- Android Studio·SDK·JDK 확인 완료
- 비밀값은 `local.properties`에만 저장

## 복구 지침

다음 에이전트는 `AGENTS.md` 시작 순서대로 읽고 `git status`, `git log -1`, `git remote -v` 확인
