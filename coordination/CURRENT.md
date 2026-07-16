# 현재 상태

- 현재 단계: 실제 Gemini 호출 직전
- 현재 담당: Codex
- 현재 작업: 로컬 API 키 입력 대기
- 마지막 완료: 임시 route 제거, 실제 목적 분석, 추천 6×50 생성, Room 저장, 재시도 중복 방지
- 기준 커밋: `8eccae3`
- 검증: 단위 테스트, assemble, lint 통과. AVD Pixel_7 Android 테스트 13개 통과
- 수정 파일: Gemini 검증, DAO, presentation route·계약·ViewModel, Android 통합 테스트, 문서
- 미완료 부분: 실제 Gemini 네트워크 호출 확인
- 다음 행동: 사용자가 `local.properties`에 키 입력 → 재빌드·설치 → AVD 목적 분석·300개 저장 확인
- 브랜치: `main`, 원격 `origin` → `https://github.com/yoon778/lexiloop`
- 보류: 별도 `english` 루트 폴더가 다른 프로세스에 점유됨
- 블로커: `GEMINI_API_KEY` 미입력

## 작업공간

- 경로: `C:\Users\cys04\Desktop\APPS\lexiloop`
- JDK 21 (`D:\android studio\jbr`), Android SDK, AVD `Pixel_7`
- 비밀값은 `local.properties`에만 저장

## 복구 지침

다음 에이전트는 `AGENTS.md` 시작 순서대로 읽고 `git status`, `git log -1`, `git remote -v` 확인
