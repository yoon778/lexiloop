# 영한 데이터 출처와 라이선스

상태: 확정

조사 기준일: 2026-07-15

법률 자문이 아닌 제품·개발 기준

## 결론

개인 베타의 영한 기준 데이터는 **한국어 위키낱말사전 원본 덤프를 Wiktextract로 구조화한 원시 JSONL** 사용

- 배포 경로: Kaikki.org의 Korean Wiktionary raw data
- 대상: `lang_code = "en"`인 영어 표제어의 텍스트 정보
- 라이선스: Wiktionary와 동일한 CC BY-SA·GFDL 중 CC BY-SA 조건 준수
- 런타임 API 호출 없음
- 빌드 전에 필요한 항목만 추출·검수
- 앱 코드와 사전 콘텐츠의 라이선스를 분리

Kaikki의 언어별 후처리 파일은 deprecated 상태이고 추가 데이터 병합 가능성이 있으므로 제품 입력으로 사용하지 않음

## 후보 비교

| 후보 | 범위 | 상업 사용 | 재배포 조건 | 결정 |
|---|---|---:|---|---|
| 한국어 위키낱말사전 raw dump + Wiktextract | 영어 표제어, 한국어 뜻, 품사, 일부 IPA·예문 | 가능 | 저작자 표시, 변경 표시, 동일조건 | 채택 |
| 한국어기초사전 Open API | 한국어 표제어 중심, 다국어 번역 | 가능 | 텍스트 CC BY-SA 2.0 KR, 저작자 표시·동일조건 | MVP 데이터에는 미혼합 |
| FreeDict | 다국어 양방향 사전 | 사전별 상이 | 사전별 확인 | 현재 공식 목록에 영한 없음 |
| Cambridge English–Korean | 편집 품질 높음 | 유료 계약 필요 | 계약별 | 제외 |
| 사전 웹사이트 크롤링 | 출처별 상이 | 불명확 | 불명확 | 금지 |
| 임의 GitHub·Hugging Face 묶음 | 출처 재가공 | 불명확 | 상위 출처 추적 어려움 | 제외 |

한국어기초사전은 한국어 표제어에서 영어 번역을 제공하므로 영어 학습 앱의 기본 표제어 자료로 방향이 맞지 않음. 향후 별도 라이선스 묶음으로 검토 가능

## 데이터 실측

2026-07-12 Kaikki 후처리 파일로 구조 품질만 표본 검사

| 항목 | 값 |
|---|---:|
| JSONL 엔트리 | 17,781 |
| 대소문자 무시 고유 표제어 | 16,427 |
| 뜻 항목 | 22,822 |
| 품사 식별 엔트리 | 6,816 |
| IPA 포함 엔트리 | 4,806 |
| 예문 포함 엔트리 | 4,124 |

품사·IPA·예문 누락률이 높으므로 원본 값을 그대로 학습 카드로 노출하지 않음

## 수집 규칙

1. 공식 Korean Wiktionary raw JSONL 스냅샷 다운로드
2. 덤프 날짜와 Wiktextract 커밋 기록
3. `lang_code = "en"` 필터
4. 허용 품사만 유지: 명사, 동사, 형용사, 부사, 구, 전치사, 접속사
5. 고유명사·기호·접사·불완전 표제어 제외
6. 표제어·품사·한국어 뜻·IPA 후보만 추출
7. HTML·위키 마크업 제거
8. 중복과 비정상 길이 제거
9. Gemini 생성 후보와 대조
10. 50개 배치 전체 검증 후 Room 저장

원본 덤프 전체를 앱에 포함하지 않음

## 기본 오프라인 단어장

- 일상 영어 300개
- raw dump에서 후보 추출 후 사람이 최종 검수
- 표제어, 품사, 목표 뜻만 사전 출처 사용
- 예문은 별도 생성·검수하고 출처를 분리
- IPA 누락 시 빈 값 허용
- 발음 파일 미포함, Android TextToSpeech 사용

## 출처 분리

각 `LearningItem`의 기존 출처 컬럼에 다음 값 보존

- 뜻: `meaningSourceName`, `meaningSourceUrl`, `meaningLicenseName`, `meaningLicenseUrl`
- 예문: `exampleSourceName`, `exampleSourceUrl`, `exampleLicenseName`, `exampleLicenseUrl`
- 덤프 날짜·Wiktextract 버전: 빌드 산출물의 전역 출처 메타데이터
- 정제·수정 여부: 앱의 오픈 데이터 라이선스 고지에서 전체 항목에 명시

사전 텍스트와 Gemini·수동 작성 텍스트를 한 필드에서 합성하지 않음

## 배포 준수

앱 공개 배포 전 필수

- 설정 화면에 `오픈 데이터 라이선스` 진입점
- 한국어 위키낱말사전과 Kaikki/Wiktextract 출처 표시
- CC BY-SA 라이선스 링크 포함
- 수정·정제 사실 표시
- 앱에 포함된 파생 사전 JSON을 같은 라이선스로 제공
- 앱 코드 자체에는 사전 콘텐츠 라이선스를 적용하지 않음
- 원본 페이지별 추가 저작자 표시가 있는 항목은 제외하거나 표시 유지
- 음성·이미지 등 멀티미디어 미포함

CC BY-SA 자료와 앱 코드를 구분 가능한 별도 콘텐츠로 유지하는 설계. 공개 출시 전 최종 라이선스 검토 필요

## 금지

- Naver·Daum·Oxford·Cambridge 화면 크롤링
- 출처 없는 단어 목록 재배포
- 개별 미디어 라이선스 확인 없는 발음 파일 포함
- CC BY-SA 뜻을 Gemini 문장에 섞고 출처 제거
- 원본보다 더 제한적인 이용약관이나 DRM을 사전 JSON에 적용

## 공식 근거

- [Kaikki Korean Wiktionary raw data](https://kaikki.org/kowiktionary/rawdata.html)
- [Kaikki Korean edition license](https://kaikki.org/kowiktionary/)
- [Wikimedia 이용 약관](https://foundation.wikimedia.org/wiki/Terms_of_Use/ko)
- [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)
- [Creative Commons 데이터 FAQ](https://creativecommons.org/faq/)
- [한국어기초사전 저작권 정책](https://krdict.korean.go.kr/kor/openApi/openApiRegister)
- [한국어기초사전 Open API](https://krdict.korean.go.kr/kor/openApi/openApiInfo)
- [FreeDict 공식 사전 저장소](https://github.com/freedict/fd-dictionaries)
- [Cambridge Dictionary API 라이선스 안내](https://dictionary-api.cambridge.org/)
