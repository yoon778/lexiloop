# 현재 상태

- 현재 단계: 상세 로직 설계 진행, 개발 미시작
- 현재 담당자: 없음
- 현재 작업: Android 기술 스택 확정
- 마지막 완료: Android SDK·빌드 도구·의존성 버전 결정
- 수정 중인 파일: `docs/TECH_STACK.md`, `docs/DECISIONS.md`, `docs/ARCHITECTURE.md`, `coordination/CURRENT.md`, `coordination/TASKS.md`
- 실행한 테스트: API 36·37 `android.jar`, Build Tools 36 `aapt2.exe` 실재 확인, `git diff --check`
- 미완료 부분: 잠긴 빈 `english` 루트 폴더 삭제, 영한 데이터 출처·라이선스 조사, 나머지 상세 설계, Android 프로젝트 생성
- 다음 행동: 영한 데이터 출처와 라이선스 조사
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
