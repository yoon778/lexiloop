# 현재 상태

- 현재 단계: Android 구현
- 현재 담당자: Codex
- 현재 작업: 정답 정규화·학습·복습 순수 도메인 로직 구현
- 마지막 완료: 기본 Android 프로젝트 생성과 첫 debug 빌드
- 수정 중인 파일: Gradle 프로젝트, `app`, 개발 문서, coordination 문서
- 실행한 테스트: `.\gradlew.bat assembleDebug test` 성공
- 미완료 부분: 잠긴 빈 `english` 루트 폴더 삭제, Codex 담당 로직·테스트, Claude UI 인수인계
- 다음 행동: 순수 Kotlin 도메인 모델과 상태 전이 단위 테스트 작성
- Git 상태: `main`, 원격 `origin`
- 마지막 커밋: `git log -1` 기준
- 원격 푸시: `https://github.com/yoon778/lexiloop`의 `main`
- 블로커: 빈 `english` 루트 폴더는 다른 프로세스 점유 중

## 로컬 작업공간

- 새 기준 경로: `C:\Users\cys04\Desktop\APPS\lexiloop`
- Android Studio·SDK·JDK 경로와 버전 확인 완료
- 기존 폴더 내부는 모두 삭제되어 빈 상태
- 프로세스 명령줄 경로 검색 결과 0건이나 디렉터리 핸들 점유는 지속

## 복구 지침

다음 에이전트는 `AGENTS.md`의 시작 순서대로 읽고 `git status`, `git log -1`, `git remote -v` 확인
