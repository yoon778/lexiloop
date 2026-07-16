# Gemini 요청·응답 계약

상태: 확정

## 구현

- 모델: `gemini-3.5-flash`
- API: Interactions API v1 `POST /v1/interactions`
- 응답: `response_format`의 `application/json` + JSON Schema
- 저장: `store=false`
- 인증: `local.properties`의 `GEMINI_API_KEY`를 BuildConfig로 주입
- 로그: 원문 프롬프트·응답·API 키 기록 금지
- 확인 기준일: 2026-07-15

## 공통 규칙

- `schemaVersion`: `1`
- 앱 생성 UUID `requestId`를 응답에서 동일하게 반환
- 정의하지 않은 JSON 필드 거부
- 빈 문자열, 제어 문자, HTML, Markdown 코드 펜스 거부
- 호출은 최초 1회 + 교정 1회, 최대 2회
- 두 번째 실패 시 전체 배치 폐기

## 목적 분석

입력: 학습 목적, 자가 난이도, 일일 신규 수, `ko-KR`, `en-US`

출력:

- 관심 분야 1~5개
- 분야 가중치 합계 정확히 100
- 제외 분야 0~10개
- 예시 항목 3~5개
- 예시의 분야명은 관심 분야 중 하나

## 추천 생성

앱이 분야별 개수를 계산해 요청함. Gemini가 비율을 재계산하지 않음

- 최초 단어장은 50개 배치 6회로 300개 생성
- 요청에 `coreWordCount=40`, `supplementaryExpressionCount=10` 포함
- 저장된 생성 배치 수를 확인해 중단 후 재시도 시 부족한 배치만 생성
- API 스키마에는 복잡도 제한 때문에 `items`의 50개 제한을 넣지 않고 앱 검증기에서 정확히 50개를 강제

출력:

- 항목 정확히 50개
- 공백 없는 핵심 단일 단어 정확히 40개, `WORD` 또는 `TECH_TERM`
- 공백이 있는 보조 복합 표현 정확히 10개, `IDIOM`, `PHRASAL_VERB`, `TECH_TERM`, `EXPRESSION` 중 하나
- 분야별 개수는 요청 할당과 정확히 일치
- 난이도는 요청과 일치
- `expression + partOfSpeech + targetMeaningKo` 중복 금지
- 대기열·기학습·제외 항목과 중복 금지
- 예문 `template`에 `{{target}}` 정확히 1개
- Android 정규식에서도 두 중괄호를 모두 리터럴로 검증
- AI가 DB ID, `contentKey`, 발음, 출처, 라이선스를 생성하지 않음

## 오류 코드

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

## 검증 순서

1. API 응답 envelope에서 모델 JSON 추출
2. 엄격 역직렬화
3. 요청 ID와 개수·분야 할당·중복·차단 항목·예문 검증
4. 실패 시 오류 코드와 필드 경로만 포함한 교정 요청
5. 전체 검증 성공 후에만 Room 트랜잭션 저장

HTTP 실패는 응답 원문 없이 상태 코드와 안전한 필드 경로만 사용자 진단에 사용
