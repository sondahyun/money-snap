# Tripline 외부 API 및 SDK 정리

작성일: 2026-04-27  
기준 문서: [Tripline_기획서.md](/Users/sondahyun/money-snap/Tripline_기획서.md), [Tripline_기능명세서.md](/Users/sondahyun/money-snap/Tripline_기능명세서.md), [Tripline_API_명세서.md](/Users/sondahyun/money-snap/Tripline_API_명세서.md)

## 1. 문서 목적

Tripline에서 실제 개발 시 필요한 외부 연동을 정리한다.

- `필수`: MVP 개발에 바로 필요한 것
- `추천`: 기능 완성도와 UX를 높이기 위해 붙이는 것이 좋은 것
- `후순위`: 지금 당장 없어도 되지만, 이후 확장 시 유용한 것
- `불필요/로컬 처리 가능`: 외부 API 없이 앱 내부 처리나 서버 내부 로직으로 충분한 것

이 문서는 `API`와 `SDK`를 함께 다룬다.  
예를 들어 OCR, 걸음수 연동은 엄밀히는 외부 HTTP API가 아니라 SDK/플랫폼 연동에 가깝다.

---

## 2. 한눈에 보는 추천 결론

### 필수

1. Google Maps SDK for Android
2. Google Places SDK for Android
3. Google Routes API
4. 글로벌 날씨 API 1종
   - 추천: OpenWeather One Call 3.0
5. 날짜 기준 환율 API 1종
   - 추천: 한국수출입은행 현재환율 API
6. OCR 엔진 1종
   - 추천: Google ML Kit Text Recognition

### 추천

1. Health Connect
   - 홈 화면 걸음수 연동용
2. Google Place Details 확장 필드
   - 리뷰, 영업시간, 장소 특성 강화용

### 후순위

1. 항공편 조회 API
   - 편명 자동 입력/스케줄 보조용
2. 카드/결제사 로고/아이콘 연동용 별도 자산 체계

### 불필요 또는 지금은 미루기

1. 별도 PDF 외부 API
2. 별도 AI 챗봇 API
3. 나라별 날씨 API를 각각 따로 붙이는 구조

---

## 3. 필수 외부 API / SDK

### 3-1. Google Maps SDK for Android

구분:
- SDK

필요 이유:
- 일정 화면 상단 지도 미리보기
- 전면 지도 화면
- 장소 검색 화면의 지도 영역
- 마커, 폴리라인, 카메라 이동

Tripline에서 쓰이는 위치:
- 일정 탭
- 장소 검색
- 전면 지도

핵심 사용 기능:
- 지도 렌더링
- 마커 표시
- 폴리라인 표시
- 카메라 이동
- 클릭 이벤트

주의 사항:
- Android 앱에서는 단순 웹 URL 임베드보다 Maps SDK가 더 적합하다.
- API Key 관리 필요
- Places/Routes와 같은 Google Cloud 프로젝트에서 묶어서 관리하는 것이 좋다.

공식 문서:
- [Maps SDK for Android Overview](https://developers.google.com/maps/documentation/android-sdk/overview)

권장도:
- `필수`

---

### 3-2. Google Places SDK for Android

구분:
- SDK

필요 이유:
- 장소 자동완성 검색
- 장소명, 주소, 좌표, placeId 저장
- 장소 상세 정보 조회
- 영업시간, 리뷰 수, 평점, 장소 특성 표시

Tripline에서 쓰이는 위치:
- 장소 추가
- 장소 검색 화면
- 장소 상세 화면
- 일정에 삽입할 장소 선택

핵심 사용 기능:
- Autocomplete
- Place Details
- placeId 기반 상세 조회

주의 사항:
- 모든 필드를 한 번에 많이 가져오면 비용이 커질 수 있다.
- 장소 목록용 필드와 장소 상세용 필드를 분리해서 요청하는 것이 좋다.
- 리뷰는 Places 정책에 맞게 attribution 표시를 고려해야 한다.

공식 문서:
- [Places SDK for Android Overview](https://developers.google.com/maps/documentation/places/android-sdk/overview)
- [Autocomplete and search](https://developers.google.com/maps/documentation/places/android-sdk/autocomplete)
- [Place Details](https://developers.google.com/maps/documentation/places/android-sdk/details-place)

권장도:
- `필수`

---

### 3-3. Google Routes API

구분:
- HTTP API

필요 이유:
- 장소 간 거리
- 도보 시간
- 차량 시간
- 동선 재정렬 시 이동량 비교

Tripline에서 쓰이는 위치:
- 일정 화면의 장소 간 거리/시간 배지
- 전면 지도 동선
- 장소 순서 변경 시 이동 정보 갱신

핵심 사용 기능:
- `computeRoutes`
- 거리/시간 계산
- polyline

주의 사항:
- Places와 별개로 과금 구조 확인 필요
- 모든 장소 조합을 실시간으로 계산하면 비용이 증가할 수 있으므로, day 단위로 캐싱 전략을 두는 것이 좋다.

공식 문서:
- [Routes API Overview](https://developers.google.com/maps/documentation/routes)
- [Compute Routes](https://developers.google.com/maps/documentation/routes/reference/rest/v2/TopLevel/computeRoutes)

권장도:
- `필수`

---

### 3-4. 글로벌 날씨 API

추천:
- OpenWeather One Call 3.0

구분:
- HTTP API

필요 이유:
- 홈의 오늘 날씨
- 여행 안의 여행 기간 날씨
- 현지 시각/타임존 표시

Tripline에서 쓰이는 위치:
- 홈
- 여행 날씨 화면
- 준비물 보조 문구

핵심 사용 기능:
- 현재 날씨
- 일별 예보
- 타임존 정보

왜 국가별 API 대신 글로벌 API인가:
- Tripline은 여러 나라 여행을 다루므로, 나라별 API를 각각 붙이는 구조는 비효율적이다.
- 도시명/위경도 기반으로 전 세계 날씨를 가져오는 글로벌 API 1종이 더 적합하다.

OpenWeather를 추천하는 이유:
- 좌표 기반 사용이 쉬움
- 일별 예보와 타임존 정보를 같이 다루기 좋음
- 여행앱에 필요한 범위에 적합

주의 사항:
- “해당 국가의 날씨”는 실제로는 대표 도시 기준 데이터로 표기하는 것이 현실적이다.
- 장기 예보는 정확도 한계가 있다.

공식 문서:
- [OpenWeather API](https://openweathermap.org/api)
- [Pricing](https://openweathermap.org/price)

권장도:
- `필수`

---

### 3-5. 환율 API

추천:
- 한국수출입은행 현재환율 API

구분:
- HTTP API

필요 이유:
- 홈의 오늘 환율
- 환율 참고 화면
- 지출 저장 시 현지 통화 → 원화 환산 기준값 확보

Tripline에서 쓰이는 위치:
- 홈
- 환율 참고 화면
- 지출 저장/조회

핵심 사용 방식:
- 날짜별 환율 조회
- 지출 저장 시 해당 날짜 기준 환율 저장
- 전 영업일 대비 증감률은 API 원본값이 아닌 서버 계산

주의 사항:
- 전일 대비 값은 응답에서 직접 안 줄 수 있으므로, 선택일과 전 영업일 환율을 별도 조회해서 계산해야 한다.
- 비영업일/휴일 처리 필요
- `JPY(100)` 같이 단위가 100 기준인 통화 주의

공식 문서:
- [한국수출입은행 현재환율 API](https://www.koreaexim.go.kr/ir/HPHKIR020M01?apino=2&searchselect=&searchword=&viewtype=C)
- [공공데이터포털 한국수출입은행 환율 정보](https://www.data.go.kr/data/3068846/openapi.do)

권장도:
- `필수`

---

### 3-6. OCR 엔진

추천:
- Google ML Kit Text Recognition

구분:
- SDK

필요 이유:
- 패키지 일정표
- 항공권
- 예약 화면
- 여행 일정 스크린샷
에서 텍스트를 인식해 일정 후보를 생성

Tripline에서 쓰이는 위치:
- OCR 가져오기
- OCR 검수 화면
- 일정 편집 반영

핵심 사용 기능:
- 이미지 텍스트 추출
- 일정 후보 문자열 추출

권장 흐름:
- OCR → 후보 검수 → 일정 편집 반영
- 바로 저장하지 않고 항상 검수 단계를 거치는 구조 유지

주의 사항:
- OCR은 장소명/시간/날짜를 100% 정확히 파싱하지 못하므로 검수 UI가 필수다.

공식 문서:
- [ML Kit Text Recognition v2 for Android](https://developers.google.com/ml-kit/vision/text-recognition/v2/android)

권장도:
- `필수`

---

## 4. 추천 외부 API / SDK

### 4-1. Health Connect

구분:
- Android 플랫폼/SDK 연동

필요 이유:
- 홈의 오늘 걸음수
- 여행 중 걸음수/동선 보조 지표

Tripline에서 쓰이는 위치:
- 홈
- 추후 여행 통계

주의 사항:
- 권한 요청 필요
- Android 버전별 지원 차이 확인 필요

공식 문서:
- [Get started with Health Connect](https://developer.android.com/health-and-fitness/guides/health-connect/develop/get-started)
- [Track steps](https://developer.android.com/health-and-fitness/health-connect/features/steps)

권장도:
- `추천`

---

### 4-2. Places 확장 필드 활용

구분:
- Places SDK의 심화 사용

필요 이유:
- 장소 상세를 단순 이름/주소를 넘어서 더 풍부하게 보여줄 수 있다.

활용 가능 정보:
- 영업시간
- 현재 영업 여부
- 평점
- 리뷰 수
- 리뷰 일부
- 예약 가능 여부
- 야외 좌석
- 음식/카페 특성

주의 사항:
- 비용과 응답 크기가 증가할 수 있으므로 장소 상세 화면에서만 지연 로드하는 편이 좋다.

공식 문서:
- [Place Data Fields](https://developers.google.com/maps/documentation/places/android-sdk/data-fields)

권장도:
- `추천`

---

## 5. 후순위 외부 API

### 5-1. 항공편 조회 API

예시 후보:
- FlightAware AeroAPI
- Aviation Edge
- aviationstack

필요 이유:
- 편명 입력 시 항공사/출발공항/도착공항/시간 자동 채우기

Tripline에서 쓰일 수 있는 위치:
- 일정 생성 보조
- OCR 후 항공편 보정

지금 후순위인 이유:
- 현재 핵심은 장소/동선/지출/체크리스트
- 항공편 자동완성은 편의 기능이지 MVP 핵심은 아님

참고 링크:
- [FlightAware AeroAPI](https://www.flightaware.com/commercial/aeroapi)
- [Aviation Edge](https://aviation-edge.com/)
- [aviationstack](https://aviationstack.com/)

권장도:
- `후순위`

---

## 6. 지금은 외부 API가 꼭 필요하지 않은 것

### 6-1. PDF 생성

지금 판단:
- 외부 API 없이 서버/앱 내부 라이브러리로 처리 가능

이유:
- 일정 PDF 공유는 문서 렌더링 문제지 외부 데이터 의존이 아니다.
- 외부 PDF SaaS를 붙일 필요는 낮다.

권장도:
- `불필요`

---

### 6-2. AI 챗봇 API

지금 판단:
- 지금 당장 필수는 아님

이유:
- 현재 제품 핵심은 여행 실행 도구
- 먼저 일정/장소/지출/준비물 흐름이 완성되어야 함
- AI는 이후 `일정 초안 생성`, `OCR 보조`, `동선 재정렬`, `준비물 추천` 쪽으로 붙이는 것이 좋음

권장도:
- `후순위`

---

## 7. Tripline 화면별 외부 연동 매핑

### 홈

- 현재 선택된 여행 요약: 내부 데이터
- 오늘 날씨: 날씨 API
- 오늘 환율: 환율 API
- 오늘 걸음수: Health Connect(선택)

### 보관함

- 외부 API 필수 없음

### 일정

- 지도 미리보기: Google Maps SDK
- 장소 간 거리/시간: Routes API
- 장소 카드: Places 상세 데이터 일부
- 장소 하단 시트: Places 상세 데이터 일부
- OCR 가져오기: ML Kit

### 장소 검색

- 지도: Google Maps SDK
- 자동완성 검색: Places SDK
- 장소 선택 후 일정 삽입: Places SDK

### 장소 상세

- placeId 기반 상세 조회: Places SDK
- 평점/리뷰/영업시간/특성: Places SDK

### 전역 캘린더

- 지출 데이터: 내부 데이터
- 환산값 계산: 저장된 환율 정보 참조

### 여행 전용 캘린더

- 해당 여행 지출: 내부 데이터
- 현지 통화 + 원화: 저장된 환율 정보 참조

### 날씨 화면

- 국가/대표 도시 기준 일별 예보: 날씨 API
- 현지 시각/시차: 날씨 API 또는 타임존 정보

### 환율 참고 화면

- 날짜별 환율: 환율 API
- 전 영업일 대비: 내부 계산

### 준비물 체크리스트

- 필수 외부 API 없음
- 날씨와 연동한 추천 문구는 추후 가능

---

## 8. 구현 우선순위 추천

### 1차

1. Google Maps SDK for Android
2. Google Places SDK for Android
3. Google Routes API
4. 한국수출입은행 환율 API
5. Google ML Kit Text Recognition

### 2차

1. OpenWeather
2. Places 상세 필드 확장

### 3차

1. Health Connect
2. 항공편 조회 API

---

## 9. 최종 추천 스택

### 지도/장소/동선

- Google Maps SDK for Android
- Google Places SDK for Android
- Google Routes API

### 날씨

- OpenWeather One Call 3.0

### 환율

- 한국수출입은행 현재환율 API

### OCR

- Google ML Kit Text Recognition

### 걸음수

- Health Connect

---

## 10. 최종 요약

Tripline에서 지금 실제로 필요한 외부 연동은 많지 않다.  
핵심은 아래 5개다.

1. Google Maps SDK
2. Google Places SDK
3. Google Routes API
4. 글로벌 날씨 API 1종
5. 날짜 기준 환율 API 1종
6. OCR SDK 1종

즉 MVP 기준 외부 연동은:

- `지도/장소/동선`: Google
- `날씨`: OpenWeather
- `환율`: 한국수출입은행
- `OCR`: ML Kit

이 조합이 가장 현실적이고, 현재 Tripline 기획과도 가장 잘 맞는다.
