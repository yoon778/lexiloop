# 개발 환경과 명령

## 환경

- Android Studio Quail 1 `2026.1.1`
- Android SDK `C:\Users\cys04\AppData\Local\Android\Sdk`
- JDK `D:\android studio\jbr` (`21.0.10`)
- Gradle Wrapper `9.4.1`

`local.properties`

```properties
sdk.dir=C\:\\Users\\cys04\\AppData\\Local\\Android\\Sdk
```

Gemini 개인 베타 키는 구현 후 다음 로컬 값으로만 주입

```properties
GEMINI_API_KEY=...
```

비밀값은 Git에 추가하지 않음

## 명령

PowerShell, 저장소 루트 기준

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

전체 로컬 검증

```powershell
.\gradlew.bat clean test assembleDebug lintDebug
```

실기기·에뮬레이터가 없으면 `connectedDebugAndroidTest` 제외

## 산출물

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- JVM 테스트: `app/build/reports/tests/testDebugUnitTest/`
- Lint: `app/build/reports/lint-results-debug.html`
- Room schema: `app/schemas/`

## Android Studio

1. 저장소 루트 열기
2. Gradle JDK를 `D:\android studio\jbr`로 선택
3. SDK sync
4. API 26 이상 기기 선택
5. `app` 실행

## 첫 검증 기록

2026-07-15

- `assembleDebug`: 성공
- `test`: 성공, 테스트 소스 추가 전이라 `NO-SOURCE`
- Gradle configuration cache: 성공

2026-07-15 Room 구현 후

- JVM 도메인 테스트 17개: 성공
- Pixel 7 API 37 Room 통합 테스트 4개: 성공
