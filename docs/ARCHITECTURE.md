# LexiLoop 기술 아키텍처 계획

## 기술 방향

- 대상: Android
- 언어: Kotlin
- UI: Jetpack Compose
- 로컬 DB: Room
- 설정: DataStore
- 음성: Android TextToSpeech, `Locale.US`
- 알림: 부정확 알람, 설정 시각 근처 하루 1회
- AI: Gemini API, 개인 베타에서만 앱 개발 설정을 통해 접근

아직 버전 번호와 최소 Android 버전은 확정하지 않음

## 계층

```text
Compose UI
  -> ViewModel / UiState / UiEvent
    -> Use Cases
      -> Repositories
        -> Room / DataStore / Gemini / TextToSpeech / Alarm
```

## 책임

### Presentation

- 화면과 내비게이션
- 사용자 이벤트 전달
- 상태 표시
- 접근성과 테마

### Domain

- 신규 학습 단계 전이
- `안다` 적응형 검증
- 정답 정규화와 판정
- 복습 간격 계산
- 복습 적체 잠금
- 연속 학습일 계산
- 추천 분야 비율 계산

### Data

- 단어와 의미 저장
- 예문과 출처 저장
- 사용자 학습 기록
- 복습 일정
- 추천 대기열
- 제외 목록
- 오류 메모
- 설정과 백업

## 주요 데이터 개념

- `LearningItem`: 단어·숙어·구동사·개발 용어
- `MeaningSense`: 목표 뜻 또는 보조 뜻
- `ExampleSentence`: 예문, 해석, 목표 형태
- `LearningProgress`: 학습 단계와 성공·실패 상태
- `ReviewSchedule`: 다음 복습일과 간격 단계
- `RecommendationProfile`: 관심 분야, 비율, 난이도, 제외 분야
- `RecommendationQueue`: 아직 학습하지 않은 추천 항목
- `ErrorNote`: 뜻·예문 오류 메모
- `DailySession`: 날짜별 신규·복습 세션

Room 구체화

- `LearningItem`에 목표 뜻과 예문 포함
- `LearningProgress`에 추천 순서, 복습 일정, 독립 제외 시각 포함
- 별도 추천 대기열·복습 일정·제외 테이블 없음
- 원시 문제 시도 이력 없이 현재 세션 복구 상태만 저장
- UUID 문자열 ID 사용
- 상세 스키마는 `docs/DATA_MODEL.md` 참조

## 정답 정규화

영어 입력 비교 전

1. 앞뒤 공백 제거
2. 모든 공백 제거
3. 소문자로 변환

그 외 철자 차이는 오답

## AI 생성 파이프라인

```text
사용자 자연어 입력
  -> 관심사·비율·난이도 분석
  -> 사용자 확인 팝업
  -> 50개 단위 추천 요청
  -> JSON 스키마 검증
  -> 중복·제외·기학습 항목 제거
  -> 사전 데이터 결합
  -> 예문 검증
  -> Room 저장
```

실패 원칙

- 50개 전체 검증 후 요청 단위 원자적 저장
- 최초 요청과 교정 요청을 합쳐 최대 2회
- 기존 추천 대기열 유지
- 내장 기본 단어장으로 대체 가능
- 상세 계약은 `docs/GEMINI_SCHEMA.md` 참조

## 오프라인 우선

- 화면은 Room 데이터를 기준으로 동작
- 네트워크 상태가 학습 세션을 막지 않음
- AI 생성 결과는 저장 후 재호출하지 않음
- TTS는 Android 시스템 엔진 사용

## 저장 구분

Room

- 학습 항목
- 의미와 예문
- 학습 진도
- 복습 일정
- 일일 세션
- 추천 대기열
- 제외 항목과 오류 메모

DataStore

- 하루 신규 단어 수
- 알림 시각과 활성 상태
- 관심 분야와 난이도
- 테마 설정
- 온보딩 완료 여부

## 백업

- JSON 내보내기·가져오기
- 스키마 버전 포함
- 가져오기 전 유효성 검증
- 전체 교체 전 사용자 확인
- 실패 시 기존 데이터 유지

## 보안

- `local.properties` Git 제외
- API 키 코드 포함 금지
- 로그에 프롬프트 전체와 비밀값 기록 금지
- 공개 APK에서 Gemini 키 제거
- 공개 버전은 백엔드 프록시, 인증, 호출 제한 필요

## 테스트 우선순위

- 정답 정규화
- 적응형 학습 상태 전이
- `안다` 검증 흐름
- 복습 간격 전이
- 복습 21개 이상 신규 잠금
- 날짜 변경과 연속 학습일
- 앱 종료 후 세션 복구
- AI JSON 검증과 중복 제거
- Room 마이그레이션
- 백업 가져오기 실패 시 원복

## 미확정 기술 조사

- 최소 Android SDK와 target SDK
- 오픈 라이선스 영한 데이터 출처
- Gemini 안정 모델과 정확한 호출 제한
- 알림 예약 API 세부 구현
- Room 스키마와 마이그레이션 정책
- 공개 버전 백엔드와 로그인 방식
