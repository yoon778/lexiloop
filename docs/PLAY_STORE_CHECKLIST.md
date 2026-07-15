# Play Store 출시 전 체크리스트

상태: 사전 기준

정책 확인일: 2026-07-15

출시 직전 최신 정책을 다시 확인해야 함

## 출시 차단 조건

- [ ] 개인 베타 종료 조건 충족
- [ ] 앱 내 Gemini API 키 완전 제거
- [ ] Gemini 호출을 인증·호출 제한이 있는 백엔드로 이전
- [ ] 개인정보처리방침 공개 URL 준비
- [ ] 오픈 데이터 라이선스와 파생 사전 JSON 제공
- [ ] 릴리스 AAB에서 비밀값·디버그 로그 없음
- [ ] 치명적 충돌·데이터 손실 0건

## 계정·테스트

- [ ] Play Console 개발자 계정 확인 완료
- [ ] 개발자 본인 확인·연락처 공개 요구사항 확인
- [ ] 계정 생성일이 2023-11-13 이후 개인 계정인지 확인
- [ ] 해당 시 최소 12명, 연속 14일 closed test 완료
- [ ] production access 질문에 테스트 근거 작성
- [ ] 내부 테스트 → 비공개 테스트 → 프로덕션 순서

테스터 수·기간 정책은 출시 직전에 재확인

## 빌드·서명

- [ ] application ID `com.yoon778.lexiloop` 유지
- [ ] `versionCode` 증가, `versionName` 확정
- [ ] 최신 target API 정책 재확인
- [ ] 현재 `targetSdk 36`이 제출 요구사항 충족하는지 확인
- [ ] Android App Bundle 생성
- [ ] Play App Signing 설정
- [ ] 업로드 키 안전한 위치에 백업
- [ ] release 난독화 후 기능·Room 직렬화 테스트
- [ ] debug 전용 의존성·화면 제외
- [ ] 설치·업데이트·삭제 후 재설치 확인

## 데이터·개인정보

- [ ] Data safety 양식 작성
- [ ] 학습 목적이 백엔드·Gemini로 전송됨을 정확히 고지
- [ ] 앱·백엔드 로그 보존 범위 고지
- [ ] 계정·광고·분석 SDK 없음과 실제 앱 동작 일치
- [ ] 전송 데이터 TLS 적용
- [ ] 불필요한 사용자 데이터 저장 금지
- [ ] 데이터 삭제 방법을 앱과 개인정보처리방침에 기재
- [ ] 제3자 SDK의 데이터 처리 재검토

데이터를 수집하지 않는다고 판단해도 Data safety 양식과 개인정보처리방침 링크는 필요

## 앱 콘텐츠 선언

- [ ] 광고 포함 여부: 없음
- [ ] 앱 액세스: 로그인 없음
- [ ] 대상 연령 정확히 선택
- [ ] 어린이 대상이 아니면 스토어 표현과 기능도 일치
- [ ] IARC 콘텐츠 등급 설문 완료
- [ ] 뉴스·건강·금융 등 해당하지 않는 선언 확인
- [ ] 정부·공식 기관 연계로 오인할 표현 없음

## 권한

- [ ] `INTERNET` 사용 목적 확인
- [ ] Android 13 이상 `POST_NOTIFICATIONS` 런타임 요청
- [ ] 정확 알람 권한 미사용
- [ ] 저장소 광범위 권한 없이 Storage Access Framework 사용
- [ ] 사용하지 않는 권한 제거
- [ ] 권한 거부 후 핵심 학습 가능

## 오픈 데이터·법적 고지

- [ ] 한국어 위키낱말사전 출처 표시
- [ ] Kaikki/Wiktextract 가공 경로 표시
- [ ] CC BY-SA 라이선스 링크
- [ ] 정제·수정 사실 표시
- [ ] 파생 사전 JSON을 동일 라이선스로 접근 가능하게 제공
- [ ] 사전 데이터에 추가 이용 제한·DRM 미적용
- [ ] 발음 파일·이미지 등 개별 라이선스 자료 미포함
- [ ] 앱 아이콘·폰트·이미지 상업 사용권 확인

## 스토어 등록정보

- [ ] 앱 이름·짧은 설명·전체 설명
- [ ] 앱 아이콘 512×512
- [ ] 기능 그래픽 1024×500
- [ ] 휴대전화 스크린샷
- [ ] 지원 이메일
- [ ] 개인정보처리방침 URL
- [ ] 오픈소스·오픈 데이터 고지 URL
- [ ] 실제 제공 기능만 설명
- [ ] 오해를 부르는 AI·학습 효과 보장 표현 없음

## 품질

- [ ] pre-launch report 확인
- [ ] Android 8/API 26부터 최신 안정 버전 테스트
- [ ] 저사양·작은 화면·큰 글자 테스트
- [ ] 오프라인·느린 네트워크·Gemini 실패 테스트
- [ ] 백업 호환성과 Room migration 테스트
- [ ] ANR·시작 시간·배터리 과소비 확인
- [ ] 접근성 검사
- [ ] 릴리스 노트 작성
- [ ] 단계적 출시와 롤백 계획

## 공식 근거

- [Target API 정책](https://support.google.com/googleplay/android-developer/answer/11926878)
- [신규 개인 계정 테스트 요구사항](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469)
- [사용자 데이터 정책](https://support.google.com/googleplay/android-developer/answer/10144311)
- [콘텐츠 등급](https://support.google.com/googleplay/android-developer/answer/9859655)
- [테스트 트랙](https://support.google.com/googleplay/android-developer/answer/9845334)
