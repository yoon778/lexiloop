# 현재 상태

- 현재 단계: Claude Study 정답 노출 버그 수정 완료
- 현재 담당: 없음
- 현재 작업: 없음
- 마지막 완료: 한→영 객관식·철자·문장 단계에서 상단 영어 표현·발음·듣기 숨김(정답 노출 방지), 회귀 UI 테스트 3개 추가
- 기준 커밋: `7c46cd6`
- 검증: `testDebugUnitTest`, `assembleDebug`, `lintDebug` 통과. `connectedDebugAndroidTest` AVD Pixel_7 18개 통과(0 실패)
- 수정 파일: `presentation/theme`, `presentation/components`, `presentation/screens`, `presentation/sample`, `app/src/main/res/font/*`, `app/src/androidTest`, coordination 문서
- 미완료 부분: 복합 표현 정확 판별 플래그(`TO_CODEX.md` REQ-005 pending) — 현재는 표현 공백 휴리스틱 사용
- 다음 행동: Codex가 REQ-005 처리 시 휴리스틱을 실제 `ItemType` 플래그로 교체
- 브랜치: `main`, 원격 `origin` → `https://github.com/yoon778/lexiloop`
- 보류: 별도 `english` 루트 폴더가 다른 프로세스에 점유됨
- 블로커: 없음
- 참고: 실제 폰(R3KYB05QDFP)에서 Compose UI 테스트가 `No compose hierarchies found`로 실패하는 건 재현 필요 — 에뮬레이터에서는 전부 통과. 계측 테스트는 `ANDROID_SERIAL=emulator-5554`로 실행

## 작업공간

- 경로: `C:\Users\cys04\Desktop\APPS\lexiloop`
- JDK 21 (`D:\android studio\jbr`), Android SDK, AVD `Pixel_7`
- 비밀값은 `local.properties`에만 저장

## 복구 지침

다음 에이전트는 `AGENTS.md` 시작 순서대로 읽고 `git status`, `git log -1`, `git remote -v` 확인
