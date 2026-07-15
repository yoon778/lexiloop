# Gemini 요청·응답 JSON 계약

상태: 확정

## 목표

- Gemini SDK·모델 버전과 독립된 앱 소유 계약 정의
- 목적 분석과 추천 생성 응답을 저장 전 검증
- 잘못된 응답의 부분 저장·무한 재시도 방지
- 프롬프트와 원시 응답을 로그에 남기지 않음

## 호출 종류

| 호출 | 시점 | 결과 |
|---|---|---|
| 목적 분석 | 온보딩·관심사 변경 | 확인 가능한 추천 프로필 초안 |
| 추천 생성 | 사용자 확인 후, 대기열 보충 승인 후 | 50개 추천 후보 |

초기 300개는 추천 생성 6회. 학습·복습 세션에서는 AI 호출 없음

## 공통 규칙

- JSON 계약 버전: `1`
- 요청 ID: 앱 생성 UUID
- 모든 응답은 요청의 `requestId`를 그대로 반환
- 모든 객체는 정의되지 않은 필드 금지
- 문자열은 앞뒤 공백 제거 후 검증
- 빈 문자열, 제어 문자, HTML, Markdown 코드 펜스 금지
- enum은 아래 영문 이름만 허용
- 앱이 ID, 정규화 값, `contentKey`, 출처 메타데이터 생성

공통 enum

```text
Difficulty = BEGINNER | INTERMEDIATE | ADVANCED
ItemType = WORD | IDIOM | PHRASAL_VERB | TECH_TERM | EXPRESSION
PartOfSpeech = NOUN | VERB | ADJECTIVE | ADVERB | PREPOSITION |
               CONJUNCTION | PRONOUN | DETERMINER | INTERJECTION |
               PHRASE | OTHER
```

## 목적 분석

### 요청

```json
{
  "schemaVersion": 1,
  "requestId": "3c56d3a0-68c1-4d52-8e2f-d7efaf4830c3",
  "learningPurpose": "일상생활과 개발 업무에서 자주 쓰는 단어를 공부하고 싶어",
  "selfAssessedDifficulty": "INTERMEDIATE",
  "dailyNewCount": 20,
  "contentLocale": "ko-KR",
  "learningLocale": "en-US"
}
```

제약

| 필드 | 제약 |
|---|---|
| `schemaVersion` | `1` |
| `requestId` | UUID |
| `learningPurpose` | 1..1000자 |
| `selfAssessedDifficulty` | `Difficulty` |
| `dailyNewCount` | 1..100 |
| `contentLocale` | `ko-KR` |
| `learningLocale` | `en-US` |

### 응답

```json
{
  "schemaVersion": 1,
  "requestId": "3c56d3a0-68c1-4d52-8e2f-d7efaf4830c3",
  "profile": {
    "topics": [
      { "name": "일상", "weightPercent": 50 },
      { "name": "개발", "weightPercent": 50 }
    ],
    "difficulty": "INTERMEDIATE",
    "excludedTopics": [],
    "exampleItems": [
      {
        "expression": "debug",
        "targetMeaningKo": "오류를 찾아 수정하다",
        "topicName": "개발"
      }
    ]
  }
}
```

제약

- `topics`: 1..5개
- `topics[].name`: 1..40자, 대소문자 무시 중복 금지
- `topics[].weightPercent`: 1..100 정수
- 모든 `weightPercent` 합: 정확히 100
- `excludedTopics`: 0..10개, 각 1..40자
- 관심 분야와 제외 분야 중복 금지
- `exampleItems`: 3..5개
- `expression`: 1..80자
- `targetMeaningKo`: 1..120자
- `topicName`: `topics[].name` 중 하나

앱은 사용자가 확인한 뒤 각 관심 분야에 UUID `topicId`를 부여하여 DataStore에 저장

## 추천 생성

### 요청

분야별 개수는 앱이 계산. Gemini에 비율 계산을 맡기지 않음

```json
{
  "schemaVersion": 1,
  "requestId": "1fa87335-a55c-435f-a9bc-75524099f7aa",
  "requestedCount": 50,
  "difficulty": "INTERMEDIATE",
  "topicAllocations": [
    {
      "topicId": "64b27fe3-4a03-48e6-aad6-f2c49490ce25",
      "name": "일상",
      "count": 25
    },
    {
      "topicId": "1af612db-10d5-4127-89e6-d189c5e74a56",
      "name": "개발",
      "count": 25
    }
  ],
  "excludedTopics": [],
  "blockedCards": [
    {
      "expression": "debug",
      "partOfSpeech": "VERB",
      "targetMeaningKo": "오류를 찾아 수정하다"
    }
  ]
}
```

제약

- `requestedCount`: 항상 50
- `topicAllocations`: 1..5개, `topicId`·이름 중복 금지
- `topicAllocations[].count`: 1..50
- 모든 `count` 합: 정확히 50
- `difficulty`: 최종 진단 반영 난이도
- `blockedCards`: 현재 대기열·기학습·제외 항목, 최대 1000개
- `blockedCards`는 동일 표현의 다른 뜻을 허용하기 위해 표현·품사·목표 뜻 조합 사용
- 1000개 초과 시 제외 항목 우선, 나머지는 최근 항목순. 전체 Room 데이터와의 로컬 중복 검증은 항상 수행

### 응답

```json
{
  "schemaVersion": 1,
  "requestId": "1fa87335-a55c-435f-a9bc-75524099f7aa",
  "items": [
    {
      "expression": "deploy",
      "baseForm": null,
      "itemType": "TECH_TERM",
      "partOfSpeech": "VERB",
      "targetMeaningKo": "배포하다",
      "auxiliaryMeaningsKo": ["전개하다"],
      "topicId": "1af612db-10d5-4127-89e6-d189c5e74a56",
      "difficulty": "INTERMEDIATE",
      "example": {
        "template": "We will {{target}} the update tonight.",
        "targetForm": "deploy",
        "translationKo": "오늘 밤 업데이트를 배포할 것이다."
      }
    }
  ]
}
```

항목 제약

| 필드 | 제약 |
|---|---|
| `expression` | 1..80자 |
| `baseForm` | null 또는 1..80자 |
| `itemType` | `ItemType` |
| `partOfSpeech` | `PartOfSpeech` |
| `targetMeaningKo` | 1..120자 |
| `auxiliaryMeaningsKo` | 0..3개, 각 1..120자, 목표 뜻과 중복 금지 |
| `topicId` | 요청의 `topicAllocations[].topicId` 중 하나 |
| `difficulty` | 요청의 `difficulty`와 일치 |
| `example.template` | 1..240자, `{{target}}` 정확히 1개 |
| `example.targetForm` | 1..80자 |
| `example.translationKo` | 1..240자 |

응답 전체 제약

- `items`: 정확히 50개
- 분야별 항목 수: 요청의 `topicAllocations`와 정확히 일치
- 응답 내부 `expression + partOfSpeech + targetMeaningKo` 중복 금지
- `blockedCards`와 동일 조합 금지
- 예문은 짧고 자연스러우며 `targetForm`이 문법상 맞아야 함
- `template`의 `{{target}}`를 `targetForm`으로 치환해 Room의 `exampleSentence` 생성
- AI는 발음기호·사전 출처·라이선스·DB ID·`contentKey`를 생성하지 않음

## 검증 파이프라인

```text
Gemini 응답
  → JSON 파싱
  → 스키마·길이·enum 검증
  → requestId 일치 검증
  → 분야 개수·비율 검증
  → 응답 내부 중복 검증
  → Room의 대기열·기학습·제외 항목과 중복 검증
  → 사전 데이터로 품사·뜻·발음기호 보완
  → 예문 placeholder·문법 규칙 검증
  → contentKey 생성
  → 50개 전체 Room 트랜잭션 저장
```

검증 실패 시 어떤 항목도 저장하지 않음

## 재시도와 실패

- 호출당 최대 2회: 최초 요청 1회 + 교정 요청 1회
- 교정 요청에는 오류 코드와 JSON 필드 경로만 전달
- 두 번째 실패: 요청 종료, 기존 추천 대기열 유지
- 자동 연속 재시도 없음
- 네트워크 오류도 동일한 2회 상한 적용
- 사용자가 나중에 명시적으로 다시 시도 가능

오류 코드

```text
INVALID_JSON
SCHEMA_MISMATCH
REQUEST_ID_MISMATCH
COUNT_MISMATCH
TOPIC_ALLOCATION_MISMATCH
DUPLICATE_ITEM
BLOCKED_ITEM
INVALID_EXAMPLE
DICTIONARY_MISMATCH
NETWORK_ERROR
```

## 보안과 로그

- API 키는 `local.properties`에서만 읽음
- 학습 목적 원문, 전체 프롬프트, 원시 응답 로그 금지
- 로그에는 요청 ID, 오류 코드, 필드 경로, 재시도 횟수만 저장
- 유효 응답은 필요한 필드만 Room에 저장
- 공개 버전은 동일 앱 계약을 유지하고 Gemini 호출 위치만 백엔드로 이동

## 구현 경계

- 도메인 계약은 Gemini SDK 타입을 노출하지 않음
- 공급자 어댑터가 이 계약과 Gemini structured output 설정을 연결
- Kotlin 구현 시 `kotlinx.serialization`의 알 수 없는 필드 거부 설정 사용
- 모델명·temperature·SDK 메서드는 구현 직전 공식 문서로 확정

## 후속 조사 연결부

사전 데이터에서 항목을 찾지 못했을 때 AI 초안을 허용할지는 영한 데이터 출처·라이선스 조사 후 확정

## 확정 결정

1. 목적 분석과 추천 생성을 별도 호출로 분리
2. 초기 300개는 동일한 50개 배치 계약을 6회 사용
3. 분야별 생성 개수는 앱이 계산하여 Gemini에 전달
4. 50개 중 하나라도 실패하면 배치 전체 미저장
5. 호출당 최대 2회만 시도
6. 예문은 `{{target}}` placeholder 계약 사용
7. 발음기호·출처·라이선스·ID·`contentKey`는 AI 응답에서 제외
8. 프롬프트와 원시 응답은 로그에 저장하지 않음
