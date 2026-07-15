# Android 기술 스택

상태: 확정

조사 기준일: 2026-07-15

## 로컬 환경

| 항목 | 확인값 |
|---|---|
| Android Studio | Quail 1, `2026.1.1` |
| 설치 경로 | `D:\android studio` |
| 번들 JDK | JetBrains Runtime `21.0.10` |
| `JAVA_HOME` | `D:\android studio\jbr` |
| Android SDK | `C:\Users\cys04\AppData\Local\Android\Sdk` |
| 설치 플랫폼 | `36`, `36.1`, `37.0` |
| 설치 Build Tools | `36.0.0`, `36.1.0`, `37.0.0` |

`java.exe`는 전역 `PATH`에 없지만 Gradle Wrapper는 `JAVA_HOME`을 사용하므로 문제없음

Android Studio Quail 2 `2026.1.2`가 최신 안정 패치지만 Quail 1도 AGP 9.2까지 지원. 즉시 업데이트 필수 아님

## SDK 기준

```text
minSdk = 26
compileSdk = 37
targetSdk = 36
```

선정 이유

- `minSdk 26`: Android 8.0 이상, `java.time` 기본 지원, 개인 베타 범위 단순화
- `compileSdk 37`: 최신 안정 AndroidX가 API 37로 컴파일되는 흐름 대응
- `targetSdk 36`: Android 16 안정 동작 적용
- Android 17 API 37은 현재 Beta이므로 컴파일만 하고 타깃 동작은 활성화하지 않음
- 2025년 Play 신규 앱 최소 요구 API 35보다 높음

`buildToolsVersion`은 명시하지 않고 AGP 기본값 사용

## 빌드 도구

| 항목 | 버전 | 선택 |
|---|---:|---|
| Android Gradle Plugin | `9.2.1` | Quail 1 지원 범위의 최신 안정 패치 |
| Gradle Wrapper | `9.4.1` | AGP 9.2 최소·기본 버전 |
| Gradle 실행 JDK | `21.0.10` | 설치된 Android Studio JBR |
| Java/Kotlin target | `17` | 소스·바이트코드 기준 고정 |
| Kotlin | AGP 내장 `2.3.10` | 별도 `org.jetbrains.kotlin.android` 플러그인 없음 |
| Compose compiler plugin | `2.3.10` | 내장 Kotlin과 동일 버전 |
| KSP | `2.3.9` | Room 코드 생성 |

AGP 9의 내장 Kotlin 사용. `kapt` 사용 금지

## 프로젝트 구조

- Kotlin DSL: `build.gradle.kts`
- Version Catalog: `gradle/libs.versions.toml`
- 단일 `app` 모듈
- 패키지 내부에서 `presentation`, `domain`, `data` 분리
- 다중 모듈은 실제 빌드 병목이나 독립 배포 필요가 생길 때만 검토

초기 버전

```text
versionCode = 1
versionName = "0.1.0"
```

## 안정 의존성

프리뷰·alpha·beta 의존성 사용 금지

### UI

| 용도 | 좌표 | 버전 |
|---|---|---:|
| Compose 호환 묶음 | `androidx.compose:compose-bom` | `2026.06.00` |
| Activity | `androidx.activity:activity-compose` | `1.13.0` |
| Material 3 | `androidx.compose.material3:material3` | BOM, 실제 안정 `1.4.0` |
| UI | `androidx.compose.ui:ui` | BOM |
| Foundation | `androidx.compose.foundation:foundation` | BOM |
| Preview | `androidx.compose.ui:ui-tooling-preview` | BOM |
| Debug tooling | `androidx.compose.ui:ui-tooling` | BOM, `debugImplementation` |
| ViewModel Compose | `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.11.0` |
| Lifecycle Compose | `androidx.lifecycle:lifecycle-runtime-compose` | `2.11.0` |
| Navigation | `androidx.navigation3:navigation3-ui` | `1.1.4` |
| Navigation ViewModel | `androidx.lifecycle:lifecycle-viewmodel-navigation3` | `2.11.0` |
| Android Core | `androidx.core:core` | `1.19.0` |

기존 Navigation Compose 2는 유지보수 모드이므로 새 Compose 앱은 안정 Navigation 3 사용

### 데이터·비동기

| 용도 | 좌표 | 버전 |
|---|---|---:|
| Room runtime | `androidx.room:room-runtime` | `2.8.4` |
| Room compiler | `androidx.room:room-compiler` | `2.8.4`, `ksp` |
| Room Gradle plugin | `androidx.room` | `2.8.4` |
| Typed DataStore | `androidx.datastore:datastore` | `1.2.1` |
| JSON | `org.jetbrains.kotlinx:kotlinx-serialization-json` | `1.9.0` |
| Coroutines Android | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | `1.11.0` |

DataStore는 설정 전체를 하나의 버전된 JSON 모델로 저장. Preferences와 Proto 추가 없음

### 테스트

| 용도 | 좌표 | 버전 |
|---|---|---:|
| JVM 단위 테스트 | `junit:junit` | `4.13.2` |
| Coroutine 테스트 | `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `1.11.0` |
| Room 테스트 | `androidx.room:room-testing` | `2.8.4` |
| Compose UI 테스트 | `androidx.compose.ui:ui-test-junit4` | BOM |
| Compose 테스트 매니페스트 | `androidx.compose.ui:ui-test-manifest` | BOM, debug |
| Espresso | `androidx.test.espresso:espresso-core` | `3.7.0` |

AndroidX Test runner 세부 버전은 Android Studio 생성 템플릿의 안정값을 사용하고 첫 빌드에서 고정

## 플랫폼 기능 우선

추가 의존성 없이 Android 기본 기능 사용

- 알림 예약: `AlarmManager`의 부정확 알람
- 재부팅·시간대 변경: `BroadcastReceiver`
- 음성: `TextToSpeech`, `Locale.US`
- 네트워크: `HttpURLConnection` + `kotlinx.serialization`
- 날짜: `java.time.LocalDate`
- 파일 내보내기·가져오기: Storage Access Framework

## 의도적으로 제외

- Hilt/Koin: 구현 하나뿐인 의존성은 수동 생성자 주입
- Retrofit/OkHttp: Gemini 호출 2종만 있어 플랫폼 HTTPS로 충분
- WorkManager: 하루 알림은 `AlarmManager`가 직접 요구사항에 맞음
- Firebase: 개인 베타 직접 Gemini 호출 결정 유지
- Room 3: 현재 RC이므로 안정 Room 2.8.4 사용
- Navigation Compose 2: 유지보수 모드
- 다중 모듈·Clean Architecture 프레임워크

## Gemini 개인 베타

- 공식 Android 전용 legacy Gemini SDK는 사용하지 않음
- 개인 베타는 Gemini REST 호출
- 키는 `local.properties`에서 BuildConfig로 주입
- 키는 Gemini API 전용으로 제한
- 공개 버전 전 앱 내 키·직접 호출 제거 후 백엔드 이전

## 공식 근거

- [Android Studio·AGP 호환](https://developer.android.com/build/releases/about-agp)
- [AGP 9.2 호환표](https://developer.android.com/build/releases/agp-9-2-0-release-notes)
- [AGP 내장 Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [Java/JDK 설정](https://developer.android.com/build/jdks)
- [Android 17 상태](https://developer.android.com/about/versions/17/release-notes)
- [Compose BOM](https://developer.android.com/develop/ui/compose/bom)
- [Compose compiler](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [AndroidX 안정 버전](https://developer.android.com/jetpack/androidx/versions)
- [Room](https://developer.android.com/jetpack/androidx/releases/room)
- [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore)
- [Navigation 3](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [Gemini 라이브러리](https://ai.google.dev/gemini-api/docs/libraries)
- [Play target API 정책](https://support.google.com/googleplay/android-developer/answer/11926878)

## 확정 결정

1. `minSdk 26`, `compileSdk 37`, `targetSdk 36`
2. AGP 9.2.1·Gradle 9.4.1·내장 Kotlin 2.3.10
3. JDK 21로 Gradle 실행, Java/Kotlin target 17
4. 단일 `app` 모듈과 수동 생성자 주입
5. Compose BOM 2026.06.00과 안정 AndroidX만 사용
6. Navigation 3 사용
7. Room 2.8.4·Typed DataStore 1.2.1
8. Gemini는 SDK 없이 REST + `HttpURLConnection`
9. AlarmManager·TTS·Storage Access Framework 등 플랫폼 API 우선
10. application ID: `com.yoon778.lexiloop`
