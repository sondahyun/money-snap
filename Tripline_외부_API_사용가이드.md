# Tripline 외부 API / SDK 사용 가이드

작성일: 2026-04-27  
관련 문서:
- [Tripline_기획서.md](/Users/sondahyun/money-snap/Tripline_기획서.md)
- [Tripline_기능명세서.md](/Users/sondahyun/money-snap/Tripline_기능명세서.md)
- [Tripline_API_명세서.md](/Users/sondahyun/money-snap/Tripline_API_명세서.md)
- [Tripline_외부_API_SDK_정리.md](/Users/sondahyun/money-snap/Tripline_외부_API_SDK_정리.md)

---

## 1. 문서 목적

이 문서는 Tripline에서 사용하는 외부 API/SDK를 **실제로 붙일 때 필요한 사용 설명서**다.

이 문서에서 다루는 내용:
- 각 API/SDK의 개념
- 왜 Tripline에 필요한지
- 어디 화면/기능에서 쓰는지
- 발급 및 준비 방법
- Android / Backend 어디에 붙이는지
- 기본 호출 흐름
- 주의사항

---

## 2. 먼저 정리할 원칙

### 2-1. Android에서 직접 붙일 것 vs Backend에서 붙일 것

Tripline은 외부 연동을 아래처럼 나누는 것이 가장 안전하다.

#### Android에서 직접 붙이는 것
- Google Maps SDK for Android
- Google Places SDK for Android
- Google ML Kit OCR
- Health Connect

이유:
- 지도 렌더링, 장소 자동완성, OCR, 걸음수는 디바이스/앱 UI와 밀접하다.

#### Backend에서 붙이는 것이 좋은 것
- Google Routes API
- 날씨 API
- 환율 API
- 항공편 API(추후)

이유:
- API 키 보호
- 캐싱
- 응답 정규화
- 여러 화면에서 재사용
- 호출량/비용 관리

---

### 2-2. API 키 관리 원칙

#### Android
- 키를 코드에 하드코딩하지 않는다.
- `local.properties` 또는 `gradle.properties`를 통해 주입한다.
- `BuildConfig` 또는 `manifestPlaceholders`로 연결한다.

#### Backend
- `.env` 또는 서버 환경변수로 넣는다.
- Git에 절대 커밋하지 않는다.

---

### 2-3. API를 붙이기 전 공통 준비

1. 공급자 콘솔에서 프로젝트 생성
2. API 활성화
3. 키 발급
4. 사용량/과금 정책 확인
5. 개발/운영 키 분리
6. Android, backend 각각에 주입

---

## 3. Google Maps SDK for Android

공식 문서:
- [Maps SDK for Android Overview](https://developers.google.com/maps/documentation/android-sdk/overview)

### 3-1. 이게 뭔가

안드로이드 앱 안에서 **Google 지도 자체를 화면에 렌더링**하는 SDK다.

Tripline에서는 이것으로:
- 일정 화면 상단 지도 미리보기
- 장소 검색 화면 지도
- 전면 지도 화면
를 만든다.

### 3-2. Tripline에서 쓰는 위치

- 일정 탭 지도 미리보기
- 장소 검색 화면
- 전면 경로 지도

### 3-3. 왜 필요한가

Tripline은 동선과 장소 배치가 핵심이라서, 단순 정적 이미지가 아니라 **사용자가 보는 지도 UI**가 필요하다.

### 3-4. 발급/설정 절차

1. Google Cloud 프로젝트 생성
2. `Maps SDK for Android` 활성화
3. Android API Key 발급
4. 키 제한 설정
   - Android app restriction
   - package name
   - SHA-1 fingerprint

### 3-5. Android 연결 위치

권장 위치:
- `android/local.properties`
- `android/app/build.gradle.kts`
- `AndroidManifest.xml`

예시 개념:
- `local.properties`에 `MAPS_API_KEY=...`
- Gradle에서 읽어서 manifest placeholder 주입
- manifest의 meta-data에 연결

### 3-6. 주요 사용 기능

- 지도 표시
- 마커 표시
- 폴리라인 표시
- 카메라 이동
- 마커 클릭
- 지도 클릭

### 3-7. Tripline 적용 흐름

#### 일정 화면
1. 여행의 장소 목록을 불러온다.
2. 각 장소 좌표로 마커를 찍는다.
3. day별 동선을 polyline으로 그린다.

#### 장소 검색 화면
1. 현재 화면 중앙 좌표 또는 선택 여행 도시 좌표 기준으로 지도 위치를 잡는다.
2. 추천/검색 장소와 기존 일정 장소를 함께 마커로 표시한다.

### 3-8. 주의사항

- 장소 검색은 Maps SDK가 아니라 Places SDK 역할이다.
- 지도는 렌더링용, 검색은 Places, 경로 계산은 Routes로 역할을 분리한다.

---

## 4. Google Places SDK for Android

공식 문서:
- [Places SDK for Android Overview](https://developers.google.com/maps/documentation/places/android-sdk/overview)
- [Autocomplete](https://developers.google.com/maps/documentation/places/android-sdk/autocomplete)
- [Place Details](https://developers.google.com/maps/documentation/places/android-sdk/details-place)

### 4-1. 이게 뭔가

Google 지도에 등록된 장소 정보를 **검색/자동완성/상세 조회**하는 SDK다.

### 4-2. Tripline에서 쓰는 위치

- 장소 검색
- 일정에 장소 추가
- 장소 상세

### 4-3. 왜 필요한가

Tripline은 사용자가 직접 장소 문자열을 막 입력하는 앱이 아니라,
- 정확한 장소명
- 주소
- 좌표
- placeId
를 기반으로 일정을 구성해야 한다.

### 4-4. 발급/설정 절차

1. Google Cloud에서 `Places API` 또는 `Places SDK for Android` 활성화
2. 같은 프로젝트의 API key 사용
3. 과금 활성화 여부 확인

### 4-5. Android 연결 위치

보통 Maps SDK와 같은 키를 사용한다.

초기화 흐름:
1. 앱 시작 시 `Places.initialize(...)`
2. 필요 화면에서 Autocomplete/Details 사용

### 4-6. 기본 사용 패턴

#### 장소 검색
1. 사용자가 검색창 입력
2. 자동완성 결과 표시
3. 장소 선택
4. `placeId`, 장소명, 주소, 좌표 저장

#### 장소 상세
1. 저장된 `placeId` 기준 조회
2. 평점, 리뷰 수, 영업시간, 특성, 주소 표시

### 4-7. Tripline에서 실제 저장해야 하는 필드

최소 저장 권장:
- `placeId`
- `displayName`
- `formattedAddress`
- `lat`
- `lng`
- `primaryType`

상세 조회용:
- `rating`
- `userRatingsCount`
- `openingHours`
- `currentOpeningHours`
- `websiteUri`
- `googleMapsUri`
- `reviews`

### 4-8. 비용 최적화 팁

- 장소 검색 목록과 장소 상세에서 요청 필드를 나눈다.
- 목록에서는 최소 필드만
- 상세 화면에서만 확장 필드 조회

### 4-9. 주의사항

- 모든 리뷰/모든 사진을 무조건 저장하려 하지 않는다.
- 상세는 필요할 때 조회하는 lazy load가 좋다.

---

## 5. Google Routes API

공식 문서:
- [Routes API Overview](https://developers.google.com/maps/documentation/routes)
- [Compute Routes](https://developers.google.com/maps/documentation/routes/reference/rest/v2/TopLevel/computeRoutes)

### 5-1. 이게 뭔가

두 지점 또는 여러 지점 사이의 **이동 거리/시간/경로**를 계산하는 API다.

### 5-2. Tripline에서 쓰는 위치

- 일정 화면 거리/시간 배지
- 장소 순서 변경 후 거리 재계산
- 전면 지도 동선 라인

### 5-3. 왜 backend로 붙이는 게 좋은가

- API 키 보호
- 캐싱하기 좋음
- 동일 요청 반복 비용 감소
- Android는 정규화된 값만 받으면 됨

### 5-4. Backend 기본 흐름

1. 클라이언트가 `fromPlace`, `toPlace`, `mode` 전달
2. backend가 Routes API 호출
3. 거리, 시간, polyline만 정리해서 응답

### 5-5. Tripline에서 저장/응답하면 좋은 값

- `distanceMeters`
- `duration`
- `travelMode`
- `polyline`

### 5-6. 주의사항

- 장소 순서가 바뀔 때마다 전부 호출하면 비용이 커진다.
- day 단위로 캐싱하거나, 임시 정렬 중에는 계산 지연 전략을 둘 수 있다.

---

## 6. OpenWeather

공식 문서:
- [OpenWeather API](https://openweathermap.org/api)
- [Pricing](https://openweathermap.org/price)

### 6-1. 이게 뭔가

전 세계 도시/좌표 기준으로
- 현재 날씨
- 일별 예보
- 타임존
등을 제공하는 글로벌 날씨 API다.

### 6-2. Tripline에서 쓰는 위치

- 홈의 오늘 날씨
- 여행 날씨 화면
- 시차/현지 시각

### 6-3. 왜 이걸 추천하나

- 여러 나라를 한 API로 처리 가능
- 좌표 기반이라 Places/Maps와 잘 맞음
- 여행 기간 예보에 적합

### 6-4. Backend 연결 권장 이유

- API 키 보호
- 캐싱 가능
- 국가/대표 도시 기준 응답 구조 통일 가능

### 6-5. Tripline 기준 사용 규칙

- “국가 날씨”라고 보여도 실제 조회 기준은 **대표 도시**다.
- 예: 중국 여행 → 상하이 기준

### 6-6. 기본 흐름

1. 여행에 저장된 대표 도시 또는 좌표 사용
2. backend가 OpenWeather 호출
3. 일별 예보/오늘 날씨/타임존 추출
4. 앱에 맞는 형식으로 재가공

### 6-7. 응답에서 주로 볼 값

- 날짜
- 최저/최고기온
- 날씨 코드
- 아이콘 코드
- 강수 확률
- timezone
- timezone_offset

### 6-8. 주의사항

- 너무 긴 기간 예보는 정확도 한계가 있다.
- 여행 기간 UI는 3~8일 수준이 가장 적절하다.

---

## 7. 한국수출입은행 환율 API

공식 문서:
- [한국수출입은행 현재환율 API](https://www.koreaexim.go.kr/ir/HPHKIR020M01?apino=2&searchselect=&searchword=&viewtype=C)
- [공공데이터포털 한국수출입은행 환율 정보](https://www.data.go.kr/data/3068846/openapi.do)

### 7-1. 이게 뭔가

날짜별 기준 환율을 제공하는 Open API다.

### 7-2. Tripline에서 쓰는 위치

- 홈의 오늘 환율
- 환율 참고 화면
- 지출 입력 시 원화 환산

### 7-3. 왜 backend로 붙여야 하나

- 날짜별 조회/전 영업일 비교 계산을 서버에서 일관되게 처리하기 좋음
- 앱에서는 정규화된 금액만 받는 구조가 더 안정적

### 7-4. 기본 사용 흐름

1. backend가 `searchdate` 기준 환율 조회
2. 지출 저장 시
   - 현지 통화 금액
   - 적용 환율
   - 원화 환산 금액
   저장
3. 전 영업일 환율과 비교해서 변화율 계산

### 7-5. 꼭 알아야 할 점

- 전일 대비 값은 직접 안 주는 경우가 있다.
- 전 영업일 데이터를 따로 가져와 계산해야 한다.
- 비영업일/휴일 처리 필요

### 7-6. 지출 저장 규칙

Tripline에서는 모든 지출에 대해 아래 3가지를 같이 저장하는 것이 좋다.

- `amount_local`
- `exchange_rate_to_krw`
- `amount_krw`

### 7-7. 주의사항

- `JPY(100)`처럼 100단위 통화가 있다.
- 숫자 파싱 시 문자열 콤마 제거 필요

---

## 8. Google ML Kit Text Recognition

공식 문서:
- [ML Kit Text Recognition v2](https://developers.google.com/ml-kit/vision/text-recognition/v2/android)

### 8-1. 이게 뭔가

이미지에서 텍스트를 읽는 on-device OCR SDK다.

### 8-2. Tripline에서 쓰는 위치

- OCR 일정 가져오기
- 일정표/예약 화면/항공권 인식

### 8-3. 왜 좋은가

- Android 앱에 바로 붙이기 좋음
- 서버 없이 1차 인식 가능
- OCR 결과를 검수 후 일정에 반영하는 구조와 잘 맞음

### 8-4. 기본 흐름

1. 사용자가 사진 촬영 또는 갤러리 선택
2. ML Kit로 텍스트 추출
3. backend 또는 앱 내부 파서가
   - 날짜
   - 시간
   - 편명
   - 장소명 후보
   추출
4. OCR 검수 화면에서 승인/수정
5. 일정 편집으로 반영

### 8-5. Tripline 구현 원칙

- OCR 결과를 바로 저장하지 않는다.
- 항상 검수 단계를 거친다.
- OCR 결과는 특수 아이템으로 남기지 않고, 일반 일정 아이템으로 반영한다.

### 8-6. 주의사항

- 장소명 오인식 가능
- 날짜/시간 인식 오류 가능
- 이미지 품질 영향 큼

---

## 9. Health Connect

공식 문서:
- [Get started with Health Connect](https://developer.android.com/health-and-fitness/guides/health-connect/develop/get-started)
- [Track steps](https://developer.android.com/health-and-fitness/health-connect/features/steps)

### 9-1. 이게 뭔가

Android에서 건강/활동 데이터(걸음수 등)를 읽는 공식 플랫폼 연동이다.

### 9-2. Tripline에서 쓰는 위치

- 홈의 오늘 걸음수
- 추후 여행 통계

### 9-3. 왜 필수는 아닌가

- Tripline 핵심은 일정/장소/지출/체크리스트
- 걸음수는 보조 지표라 후순위다

### 9-4. 기본 흐름

1. Health Connect 사용 가능 여부 확인
2. 권한 요청
3. 오늘 step count 조회
4. 홈에 표시

### 9-5. 주의사항

- 기기/OS 버전 차이
- 사용자 권한 거부 대응 필요

---

## 10. 항공편 API

후보:
- FlightAware AeroAPI
- Aviation Edge
- aviationstack

### 10-1. 언제 필요한가

- 편명 입력 시 항공사/출발도착시간 자동 입력
- 항공권 OCR 결과 보정

### 10-2. 왜 지금은 후순위인가

- 핵심 기능이 아니기 때문
- 현재 MVP는 장소/동선/지출/체크리스트가 우선

### 10-3. 도입 시 권장 방식

- backend에서만 사용
- 입력은 `편명 + 출발일`
- 후보가 여러 개면 사용자가 선택

---

## 11. 화면별 연결 요약

### 홈

- 오늘 날씨: OpenWeather
- 오늘 환율: 환율 API
- 오늘 걸음수: Health Connect(선택)

### 보관함

- 외부 API 필수 없음

### 일정

- 지도: Maps SDK
- 장소 추가: Places SDK
- 거리/시간: Routes API
- OCR: ML Kit

### 장소 상세

- Places 상세 조회

### 전역 캘린더

- 내부 지출 데이터
- 저장된 환율 기반 원화 환산

### 여행 전용 캘린더

- 내부 지출 데이터
- 장소별/상위 카테고리별 조회

### 날씨

- OpenWeather

### 환율 참고

- 환율 API

### 체크리스트

- 외부 API 필수 없음

---

## 12. 실제 적용 순서 추천

### 1차 개발

1. Google Maps SDK
2. Google Places SDK
3. Routes API
4. 환율 API
5. OCR(ML Kit)

### 2차 개발

1. OpenWeather
2. Places 확장 상세

### 3차 개발

1. Health Connect
2. 항공편 API

---

## 13. 최종 추천 조합

Tripline 기준 가장 현실적인 조합은 아래와 같다.

- 지도/장소/동선
  - Google Maps SDK for Android
  - Google Places SDK for Android
  - Google Routes API

- 날씨
  - OpenWeather

- 환율
  - 한국수출입은행 현재환율 API

- OCR
  - Google ML Kit Text Recognition

- 걸음수(선택)
  - Health Connect

---

## 14. 최종 정리

Tripline에서 외부 연동은 많아 보이지만, 실제로 핵심은 다음이다.

1. 장소 검색과 지도 표시
2. 동선 거리/시간 계산
3. 날씨
4. 환율
5. OCR

즉,

- 지도/장소/동선은 Google 계열
- 날씨는 OpenWeather
- 환율은 한국수출입은행
- OCR은 ML Kit

이 구조로 가면 기획과 가장 잘 맞고, 구현 난이도와 유지보수 균형도 가장 좋다.
