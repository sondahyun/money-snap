# Tripline API 명세서

작성일: 2026-04-27  
기준 문서: [Tripline_기획서.md](/Users/sondahyun/money-snap/Tripline_기획서.md), [Tripline_기능명세서.md](/Users/sondahyun/money-snap/Tripline_기능명세서.md)  
API 버전: `v1`

## 1. 개요

Tripline API는 아래 도메인을 제공한다.

- 인증 / 사용자 설정
- 여행
- 홈 대시보드
- 일정 / 지도 / 장소
- 메모
- OCR
- 체크리스트
- 지출 / 상위 카테고리
- 전역 캘린더 / 여행 전용 캘린더
- 날씨 / 환율
- PDF 내보내기

기본 URL 예시:

```text
https://api.tripline.app/api/v1
```

## 2. 공통 규칙

### 2-1. 인증 방식

- 인증이 필요한 요청은 `Authorization: Bearer {accessToken}` 헤더를 사용한다.
- 로그인 후 `accessToken`, `refreshToken`을 발급한다.
- 비밀번호 저장은 **SHA-256 단독 해시가 아니라** `Argon2id` 또는 `bcrypt` 기반 해시를 사용한다.

### 2-2. 공통 헤더

```http
Content-Type: application/json
Authorization: Bearer {accessToken}
X-Timezone: Asia/Seoul
```

### 2-3. 날짜 / 시간 형식

- 날짜: `YYYY-MM-DD`
- 시간: `HH:mm`
- 월: `YYYY-MM`
- 타임스탬프: ISO-8601 (`2026-04-27T10:30:00+09:00`)

### 2-4. 통화 / 금액 규칙

- 통화는 ISO 4217 코드 사용 (`KRW`, `CNY`, `USD`)
- 금액은 모두 양수 저장
- 실제 화면의 `-지출`, `+환불/정산` 표시는 `flowType`으로 판별
- 모든 지출 응답은 가능하면 아래 두 값을 함께 준다.
  - `amountLocal`
  - `amountKrw`

### 2-5. 공통 응답 구조

성공:

```json
{
  "success": true,
  "data": {},
  "meta": null,
  "error": null
}
```

실패:

```json
{
  "success": false,
  "data": null,
  "meta": null,
  "error": {
    "code": "TRIP_NOT_FOUND",
    "message": "여행을 찾을 수 없습니다."
  }
}
```

### 2-6. 주요 에러 코드

| 코드 | 의미 |
| --- | --- |
| `AUTH_REQUIRED` | 인증 필요 |
| `AUTH_INVALID_TOKEN` | 토큰 만료 또는 위조 |
| `USER_ALREADY_EXISTS` | 이메일 중복 |
| `TRIP_NOT_FOUND` | 여행 없음 |
| `TRIP_REQUIRED` | 먼저 여행 생성 필요 |
| `PLACE_NOT_FOUND` | 장소 정보 없음 |
| `CATEGORY_REQUIRED` | 상위 카테고리 필수 |
| `EXPENSE_NOT_FOUND` | 지출 없음 |
| `OCR_PARSE_FAILED` | OCR 인식 실패 |
| `OCR_CANDIDATE_EMPTY` | 추출 후보 없음 |
| `WEATHER_UNAVAILABLE` | 날씨 정보 조회 불가 |
| `FX_UNAVAILABLE` | 환율 정보 조회 불가 |

## 3. 인증 / 사용자

### `POST /auth/signup`

회원가입

요청:

```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!",
  "nickname": "소녀아임"
}
```

응답:

```json
{
  "success": true,
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "nickname": "소녀아임"
  },
  "meta": null,
  "error": null
}
```

### `POST /auth/login`

로그인

응답:

```json
{
  "success": true,
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "user": {
      "userId": "uuid",
      "nickname": "소녀아임"
    }
  },
  "meta": null,
  "error": null
}
```

### `POST /auth/refresh`

Access Token 재발급

### `POST /auth/logout`

Refresh Token 폐기

### `GET /me`

내 정보 조회

### `GET /me/settings`

사용자 설정 조회

응답 예시:

```json
{
  "success": true,
  "data": {
    "baseCurrencyCode": "KRW",
    "defaultPdfScope": "trip_all",
    "defaultRouteTransportMode": "mixed"
  },
  "meta": null,
  "error": null
}
```

### `PATCH /me/settings`

사용자 설정 수정

## 4. 여행

### `GET /trips`

여행 목록 조회

쿼리 파라미터:

- `status=planned|active|completed`
- `q=검색어`

응답 예시:

```json
{
  "success": true,
  "data": [
    {
      "tripId": "uuid",
      "title": "상하이 여행",
      "countryName": "중국",
      "cityName": "상하이",
      "startDate": "2026-02-15",
      "endDate": "2026-02-18",
      "status": "active",
      "baseCurrencyCode": "CNY",
      "oneLineDescription": "부모님과"
    }
  ],
  "meta": {
    "total": 1
  },
  "error": null
}
```

### `POST /trips`

여행 생성

요청:

```json
{
  "title": "상하이 여행",
  "countryCode": "CN",
  "countryName": "중국",
  "cityName": "상하이",
  "startDate": "2026-02-15",
  "endDate": "2026-02-18",
  "baseCurrencyCode": "CNY",
  "oneLineDescription": "부모님과"
}
```

### `GET /trips/{tripId}`

여행 상세 조회

### `PATCH /trips/{tripId}`

여행 수정

### `DELETE /trips/{tripId}`

여행 삭제(소프트 삭제 권장)

## 5. 홈 대시보드

### `GET /home/dashboard`

홈 대시보드 데이터 조회

쿼리 파라미터:

- `date=YYYY-MM-DD` (옵션, 기본 오늘)

응답 예시:

```json
{
  "success": true,
  "data": {
    "hasActiveTrip": true,
    "activeTrip": {
      "tripId": "uuid",
      "title": "상하이 여행",
      "dayIndex": 2,
      "dateLabel": "2026-02-16"
    },
    "nextSchedule": {
      "title": "와이탄",
      "time": "14:00",
      "moveHint": "도보 12분"
    },
    "todayRoute": {
      "placeCount": 4,
      "distanceMeters": 3200,
      "walkMinutes": 46,
      "driveMinutes": 10
    },
    "todayWeather": {
      "summary": "맑음",
      "temperature": 18,
      "iconCode": "sun"
    },
    "todayFx": {
      "currencyCode": "CNY",
      "rateToKrw": 191.23,
      "changePercent": 0.22
    },
    "expenseSummary": {
      "todayKrw": 52300,
      "cumulativeKrw": 321400
    },
    "recentExpenses": [
      {
        "expenseId": "uuid",
        "title": "난샹만두 예원 점",
        "amountKrw": 11200,
        "flowType": "expense",
        "paymentMethod": "card"
      }
    ]
  },
  "meta": null,
  "error": null
}
```

## 6. 일정 / 지도

### `GET /trips/{tripId}/schedule`

여행 일정 조회

응답 예시:

```json
{
  "success": true,
  "data": {
    "trip": {
      "tripId": "uuid",
      "title": "상하이 여행",
      "startDate": "2026-02-15",
      "endDate": "2026-02-18"
    },
    "days": [
      {
        "tripDayId": "uuid",
        "dayIndex": 1,
        "date": "2026-02-15",
        "items": [
          {
            "scheduleItemId": "uuid",
            "itemType": "place",
            "sortOrder": 10,
            "scheduledTime": "10:00",
            "place": {
              "tripPlaceSnapshotId": "uuid",
              "googlePlaceId": "place-id",
              "name": "상하이 홍차오 국제공항",
              "primaryType": "관광명소",
              "secondaryText": "예약가능",
              "latitude": 31.1979,
              "longitude": 121.3363
            }
          },
          {
            "scheduleItemId": "uuid",
            "itemType": "memo",
            "sortOrder": 20,
            "scheduledTime": "17:30",
            "memo": {
              "content": "해 질 무렵 와이탄 쪽으로 이동하기"
            }
          }
        ]
      }
    ]
  },
  "meta": null,
  "error": null
}
```

### `POST /trips/{tripId}/schedule/places`

장소 일정 추가

요청:

```json
{
  "tripDayId": "uuid",
  "googlePlaceId": "google-place-id",
  "scheduledTime": "12:00",
  "sortOrder": 30
}
```

### `POST /trips/{tripId}/schedule/memos`

메모 일정 추가

```json
{
  "tripDayId": "uuid",
  "content": "야경 보기 전에 근처 카페에서 휴식",
  "scheduledTime": "18:00",
  "sortOrder": 35
}
```

### `PATCH /trips/{tripId}/schedule/items/{itemId}`

장소/메모 공통 수정

수정 가능 항목:

- `scheduledTime`
- `content` (메모)
- `sortOrder`
- `tripDayId`

### `PATCH /trips/{tripId}/schedule/items/reorder`

드래그 재정렬 / day 이동

요청:

```json
{
  "items": [
    {
      "scheduleItemId": "uuid-1",
      "tripDayId": "day-1",
      "sortOrder": 10
    },
    {
      "scheduleItemId": "uuid-2",
      "tripDayId": "day-2",
      "sortOrder": 10
    }
  ]
}
```

### `DELETE /trips/{tripId}/schedule/items/{itemId}`

일정 아이템 삭제

### `GET /trips/{tripId}/route-map`

전면 지도용 동선 데이터 조회

응답에는 아래를 포함한다.

- day별 마커 목록
- 장소 번호
- 동선 polyline
- 구간별 `distanceMeters`, `walkMinutes`, `driveMinutes`

## 7. 장소

### `GET /places/search`

Google Places 자동완성/검색 프록시

쿼리 파라미터:

- `tripId`
- `query`
- `type=food|sight|hotel`
- `lat`, `lng` (옵션)

### `GET /places/{googlePlaceId}`

장소 상세 조회

응답 예시:

```json
{
  "success": true,
  "data": {
    "googlePlaceId": "place-id",
    "name": "남상만두 예원 점",
    "localizedName": "南翔馒头店 豫园店",
    "rating": 4.1,
    "reviewCount": 110,
    "savedCount": 8518,
    "address": "인민 광장 주변",
    "isReservable": true,
    "openingHoursText": "08:30 - 21:00",
    "description": "100년이 넘는 전통을 간직한 만두 전문점",
    "photoUrls": [
      "https://..."
    ],
    "menuHighlights": [
      "샤오롱바오",
      "새우 만두"
    ],
    "googleMapsUrl": "https://maps.google.com/..."
  },
  "meta": null,
  "error": null
}
```

## 8. 지출 / 상위 카테고리

### `POST /trips/{tripId}/expenses`

지출 생성

요청:

```json
{
  "flowType": "expense",
  "title": "남상만두 예원 점",
  "expenseDate": "2026-02-16",
  "expenseTime": "13:20",
  "localCurrencyCode": "CNY",
  "amountLocal": 58.00,
  "amountKrw": 11122.34,
  "exchangeRateToKrw": 191.7645,
  "paymentMethod": "cash",
  "topCategoryId": "uuid",
  "tripPlaceSnapshotId": "uuid",
  "scheduleItemId": "uuid",
  "note": "점심",
  "photoUrls": [
    "https://..."
  ]
}
```

### `GET /expenses/{expenseId}`

지출 상세 조회

### `PATCH /expenses/{expenseId}`

지출 수정

### `DELETE /expenses/{expenseId}`

지출 삭제

### `GET /trips/{tripId}/expense-categories`

상위 카테고리 목록

### `POST /trips/{tripId}/expense-categories`

상위 카테고리 생성

요청:

```json
{
  "name": "환전",
  "description": "현지 현금 인출, 환전소 이용"
}
```

### `PATCH /trips/{tripId}/expense-categories/{categoryId}`

상위 카테고리 수정

### `DELETE /trips/{tripId}/expense-categories/{categoryId}`

상위 카테고리 삭제  
단, 기본 카테고리 `기타`는 삭제 불가

## 9. 캘린더

### `GET /calendar/summary`

전역 캘린더 월 요약

쿼리 파라미터:

- `month=YYYY-MM`

응답 예시:

```json
{
  "success": true,
  "data": {
    "month": "2026-04",
    "totalExpenseKrw": 877082,
    "topCategoryName": "식비",
    "days": [
      {
        "date": "2026-04-23",
        "expenseCount": 3,
        "expenseKrw": 148000,
        "refundKrw": 0
      }
    ]
  },
  "meta": null,
  "error": null
}
```

### `GET /calendar/days/{date}/expenses`

전역 캘린더에서 특정 날짜 지출 목록

- 모든 여행의 지출을 반환한다.
- 각 항목에는 반드시 `tripId`, `tripTitle`이 포함된다.

### `GET /trips/{tripId}/calendar`

여행 전용 캘린더 월 데이터

쿼리 파라미터:

- `month=YYYY-MM`
- `groupBy=place|topCategory`

### `GET /trips/{tripId}/calendar/days/{date}/expenses`

여행 전용 캘린더에서 특정 날짜 지출 목록

- `groupBy=place|topCategory`
- 현지 통화 / 원화 동시 반환

## 10. 체크리스트

### `GET /trips/{tripId}/checklist`

체크리스트 전체 조회

응답 구조:

- 섹션 목록
- 섹션별 아이템
- 체크 여부

### `POST /trips/{tripId}/checklist/sections`

섹션 생성

### `PATCH /trips/{tripId}/checklist/sections/{sectionId}`

섹션명 / 정렬 수정

### `DELETE /trips/{tripId}/checklist/sections/{sectionId}`

섹션 삭제

### `POST /trips/{tripId}/checklist/items`

아이템 생성

```json
{
  "sectionId": "uuid",
  "title": "여권",
  "note": "여권사본, 여권사진 여분",
  "sortOrder": 10
}
```

### `PATCH /trips/{tripId}/checklist/items/{itemId}`

아이템 수정 / 정렬 변경

### `PATCH /trips/{tripId}/checklist/items/{itemId}/check`

체크 토글

### `DELETE /trips/{tripId}/checklist/items/{itemId}`

아이템 삭제

## 11. OCR

### `POST /trips/{tripId}/ocr/imports`

OCR 업로드 생성

- 멀티파트 업로드 또는 이미지 URL 기준

응답:

```json
{
  "success": true,
  "data": {
    "ocrImportId": "uuid",
    "status": "reviewing"
  },
  "meta": null,
  "error": null
}
```

### `GET /trips/{tripId}/ocr/imports/{ocrImportId}`

OCR 추출 결과 조회

### `PATCH /trips/{tripId}/ocr/imports/{ocrImportId}/candidates/{candidateId}`

OCR 후보 수정 / 승인 / 거절

### `POST /trips/{tripId}/ocr/imports/{ocrImportId}/confirm`

검수 결과를 일정으로 반영

## 12. 날씨 / 환율

### `GET /trips/{tripId}/weather`

여행 기간 날씨 조회

응답 예시:

```json
{
  "success": true,
  "data": {
    "countryName": "중국",
    "representativeCityName": "상하이",
    "timeZone": "Asia/Shanghai",
    "utcOffsetMinutes": 480,
    "localTime": "2026-02-16T13:30:00+08:00",
    "days": [
      {
        "date": "2026-02-15",
        "summary": "맑음",
        "iconCode": "sun",
        "minTemp": 6,
        "maxTemp": 20
      }
    ]
  },
  "meta": null,
  "error": null
}
```

### `GET /trips/{tripId}/fx`

여행 기준 통화 환율 조회

쿼리 파라미터:

- `date=YYYY-MM-DD`

응답:

```json
{
  "success": true,
  "data": {
    "currencyCode": "CNY",
    "rateToKrw": 191.23,
    "previousBusinessDate": "2026-02-15",
    "changeAmount": 0.42,
    "changePercent": 0.22
  },
  "meta": null,
  "error": null
}
```

## 13. PDF

### `POST /trips/{tripId}/exports/pdf`

일정 PDF 생성

요청:

```json
{
  "scope": "trip_all",
  "tripDayId": null
}
```

응답:

```json
{
  "success": true,
  "data": {
    "downloadUrl": "https://..."
  },
  "meta": null,
  "error": null
}
```

## 14. 외부 API 연동 방침

### Google Places / Maps / Routes

- 클라이언트에서 직접 키를 쓰지 않고, 필요한 부분은 서버 프록시 또는 안전한 클라이언트 SDK 정책을 따른다.
- 일정에 들어간 장소는 필요한 핵심 필드만 스냅샷으로 저장한다.

### 날씨

- 글로벌 날씨 API 1종 사용
- 국가 날씨 화면은 대표 도시 기준 데이터를 사용한다.

### 환율

- 날짜 기준 환율 API 사용
- 전 영업일 증감은 서버에서 비교 계산 가능

### OCR

- 업로드된 이미지에서 텍스트를 추출하고, 검수 후 일정으로 반영

## 15. 구현 메모

- 일정/체크리스트/지출은 `여행`이 선행되어야 한다.
- 전역 캘린더는 예외적으로 모든 여행 지출을 통합해서 보여준다.
- 지출 추가 화면에서는 `장소`와 `상위 카테고리`가 동시에 선택 가능해야 한다.
- `수입`은 일반 지출 흐름에 포함하지 않고, `환불/정산`만 예외적으로 처리한다.
