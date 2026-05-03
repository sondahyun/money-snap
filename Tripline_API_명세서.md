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
- 모든 지출 응답은 아래 세 값을 항상 함께 준다.
  - `amountLocal`
  - `amountKrw`
  - `exchangeRateToKrw`

### 2-5. 공통 응답 구조

성공:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

실패:

```json
{
  "httpStatusCode": 404,
  "errorMessage": "여행을 찾을 수 없습니다."
}
```

- 성공 응답은 `errorMessage`를 내려주지 않는다.
- 실패 응답은 `responseMessage`와 `data`를 내려주지 않는다.

### 2-6. 주요 에러 코드

현재 백엔드 공통 응답 포맷은 `errorMessage`만 내려준다. 아래 코드는 서버 내부 분기, 로그, 추후 확장 기준으로 사용한다.

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

### 2-7. 삭제 정책

- 별도 언급이 없는 `DELETE` API는 hard delete가 아니라 `deletedAt` 기반 soft delete로 처리한다.
- soft delete된 데이터는 목록/상세/캘린더/체크리스트/일정 응답에서 기본적으로 제외한다.
- `expenses` 삭제 시 연결된 `expense_photos`도 함께 soft delete한다.
- `checklist_sections` 삭제 시 하위 `checklist_items`도 함께 soft delete한다.
- `trip_days`, `schedule_items`, `trip_lodging_groups`, `trip_route_segments`는 일정 편집 과정에서 soft delete될 수 있으며, 삭제된 데이터는 일정/지도 응답에서 제외한다.
- `ocr_candidates`는 삭제 API를 두지 않고 `pending/accepted/rejected/edited` 상태 전이로 관리한다.

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
  "httpStatusCode": 201,
  "responseMessage": "회원가입 성공",
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "nickname": "소녀아임"
  }
}
```

### `POST /auth/login`

로그인

응답:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "로그인 성공",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "user": {
      "userId": "uuid",
      "nickname": "소녀아임"
    }
  }
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
  "httpStatusCode": 200,
  "responseMessage": "사용자 설정 조회 성공",
  "data": {
    "baseCurrencyCode": "KRW",
    "defaultPdfScope": "trip_all",
    "defaultRouteTransportMode": "mixed",
    "currentTrip": {
      "tripId": "uuid",
      "title": "후쿠오카, 도쿄, 오사카 여행",
      "status": "active"
    }
  }
}
```

### `PATCH /me/settings`

사용자 설정 수정

### `GET /me/current-trip`

현재 선택된 여행 조회

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "현재 선택 여행 조회 성공",
  "data": {
    "tripId": "uuid",
    "title": "후쿠오카, 도쿄, 오사카 여행",
    "countryName": "일본",
    "cityName": "후쿠오카",
    "status": "active",
    "startDate": "2026-05-07",
    "endDate": "2026-05-11"
  }
}
```

### `PUT /me/current-trip`

현재 선택된 여행 변경

요청:

```json
{
  "tripId": "uuid"
}
```

규칙:

- 요청한 여행은 반드시 로그인 사용자 본인의 여행이어야 한다.
- 변경 성공 후 일정/준비물/날씨/여행 전용 캘린더는 이 여행을 기준으로 동작한다.

## 4. 여행

### `GET /trips`

여행 목록 조회

쿼리 파라미터:

- `status=planned|active|completed`
- `q=검색어`

규칙:

- `status`는 서버가 날짜 기준으로 계산해 저장한 값을 사용한다.
- 사용자는 상태를 직접 수정할 수 없다.

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "여행 목록 조회 성공",
  "data": {
    "trips": [
      {
        "tripId": "uuid",
        "title": "후쿠오카, 도쿄, 오사카 여행",
        "countryName": "일본",
        "cityName": "후쿠오카",
        "startDate": "2026-05-07",
        "endDate": "2026-05-11",
        "status": "active",
        "baseCurrencyCode": "JPY",
        "oneLineDescription": "연인과 · 유명 관광지는 필수"
      }
    ],
    "pagination": {
      "total": 1
    }
  }
}
```

### `POST /trips`

여행 생성

요청:

```json
{
  "destinations": [
    {
      "countryCode": "JP",
      "countryName": "일본",
      "cityName": "후쿠오카",
      "subCityNames": ["유후인", "벳푸", "기타큐슈"],
      "sortOrder": 1
    },
    {
      "countryCode": "JP",
      "countryName": "일본",
      "cityName": "도쿄",
      "subCityNames": ["하코네", "요코하마", "가마쿠라"],
      "sortOrder": 2
    },
    {
      "countryCode": "JP",
      "countryName": "일본",
      "cityName": "오사카",
      "subCityNames": ["교토", "고베", "나라"],
      "sortOrder": 3
    }
  ],
  "startDate": "2026-05-07",
  "endDate": "2026-05-11",
  "baseCurrencyCode": "JPY",
  "companionType": "couple",
  "travelStyleTags": ["famous_spots"]
}
```

규칙:

- 여행 생성은 `도시 선택 -> 순서 확인/변경 -> 기간 선택 -> 스타일 선택` 흐름을 기준으로 한다.
- `destinations`는 1개 이상이어야 하며, `sortOrder` 오름차순으로 여행 목적지 순서를 저장한다.
- 첫 번째 목적지는 `trips.city_name` 대표 도시로 저장한다.
- `title`은 서버가 목적지 기준으로 자동 생성한다.
  예: `후쿠오카, 도쿄, 오사카 여행`
- `companionType`, `travelStyleTags`는 선택값이며 비어 있을 수 있다.
- 여행 생성이 완료되면 생성된 여행은 자동으로 사용자의 `현재 선택된 여행`으로 저장된다.

### `GET /trips/{tripId}`

여행 상세 조회

### `PATCH /trips/{tripId}`

여행 수정

### `DELETE /trips/{tripId}`

여행 삭제(soft delete)

규칙:

- 삭제 대상 여행이 사용자의 현재 선택된 여행이면 `user_current_trip_selections`를 함께 비운다.
- 현재 선택된 여행이 비워진 뒤 `GET /me/current-trip`은 `data` 없이 성공 응답을 내려준다.

## 5. 홈 대시보드

### `GET /home/dashboard`

홈 대시보드 데이터 조회

쿼리 파라미터:

- `date=YYYY-MM-DD` (옵션, 기본 오늘)

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "홈 대시보드 조회 성공",
  "data": {
    "hasActiveTrip": true,
    "hasCurrentTrip": true,
    "currentTrip": {
      "tripId": "uuid",
      "title": "후쿠오카, 도쿄, 오사카 여행",
      "status": "active"
    },
    "activeTrip": {
      "tripId": "uuid",
      "title": "후쿠오카, 도쿄, 오사카 여행",
      "dayIndex": 2,
      "dateLabel": "2026-05-08"
    },
    "nextSchedule": {
      "title": "도쿄역",
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
      "currencyCode": "JPY",
      "rateToKrw": 9.12,
      "changePercent": 0.18
    },
    "expenseSummary": {
      "todayKrw": 52300,
      "cumulativeKrw": 321400
    },
    "recentExpenses": [
      {
        "expenseId": "uuid",
        "title": "이치란 라멘 하카타점",
        "amountKrw": 11674,
        "flowType": "expense",
        "paymentMethod": "card"
      }
    ]
  }
}
```

상태 규칙:

- `hasActiveTrip=true`면 실행형 대시보드 블록을 내려준다.
- `hasActiveTrip=false`이고 `hasCurrentTrip=true`면 현재 선택된 여행 진입용 요약 블록만 내려준다.
- 여행이 하나도 없으면 `hasActiveTrip=false`, `hasCurrentTrip=false`로 반환한다.

## 6. 일정 / 지도

### `GET /trips/{tripId}/schedule`

여행 일정 조회

- soft delete된 `tripDay`, `scheduleItem`은 응답에서 제외한다.

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "일정 조회 성공",
  "data": {
    "trip": {
      "tripId": "uuid",
      "title": "후쿠오카, 도쿄, 오사카 여행",
      "startDate": "2026-05-07",
      "endDate": "2026-05-11"
    },
    "days": [
      {
        "tripDayId": "uuid",
        "dayIndex": 1,
        "date": "2026-05-07",
        "items": [
          {
            "scheduleItemId": "uuid",
            "itemType": "place",
            "sortOrder": 10,
            "scheduledTime": "10:00",
            "place": {
              "tripPlaceSnapshotId": "uuid",
              "googlePlaceId": "place-id",
              "name": "후쿠오카 공항",
              "primaryType": "교통시설",
              "secondaryText": "하카타 주변",
              "latitude": 33.5859,
              "longitude": 130.4506
            }
          },
          {
            "scheduleItemId": "uuid",
            "itemType": "flight",
            "sortOrder": 15,
            "scheduledTime": "00:21",
            "flight": {
              "airlineName": "대한항공",
              "airlineCode": "KE",
              "flightNumber": "101",
              "departureAirportCode": "BCN",
              "departureAirportName": "엘프라트 국제공항",
              "departureDatetime": "2026-05-07T00:21:00",
              "arrivalAirportCode": "ATL",
              "arrivalAirportName": "하츠필드 잭슨 애틀랜타 국제공항",
              "arrivalDatetime": "2026-05-08T00:22:00"
            }
          },
          {
            "scheduleItemId": "uuid",
            "itemType": "lodging",
            "sortOrder": 18,
            "lodging": {
              "tripPlaceSnapshotId": "uuid",
              "name": "호텔 오리엔탈 익스프레스 후쿠오카 나카스 카와바타",
              "gradeText": "3성급",
              "areaText": "하카타",
              "checkInDate": "2026-05-09",
              "checkOutDate": "2026-05-11",
              "stayDate": "2026-05-09"
            }
          },
          {
            "scheduleItemId": "uuid",
            "itemType": "memo",
            "sortOrder": 20,
            "scheduledTime": "17:30",
            "memo": {
              "content": "저녁에는 나카스 강변 산책하기"
            }
          }
        ]
      }
    ]
  }
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

### `POST /trips/{tripId}/schedule/flights`

항공편 일정 추가

요청:

```json
{
  "tripDayId": "uuid",
  "airlineName": "대한항공",
  "airlineCode": "KE",
  "flightNumber": "101",
  "departureAirportCode": "BCN",
  "departureAirportName": "엘프라트 국제공항",
  "departureAirportCity": "바르셀로나",
  "departureDatetime": "2026-05-07T00:21:00",
  "arrivalAirportCode": "ATL",
  "arrivalAirportName": "하츠필드 잭슨 애틀랜타 국제공항",
  "arrivalAirportCity": "애틀랜타",
  "arrivalDatetime": "2026-05-08T00:22:00",
  "sortOrder": 15
}
```

규칙:

- 출발일은 여행 기간 내 day 중 하나를 선택한다.
- 항공사는 직접 입력하거나 추천 항공사 목록에서 선택할 수 있다.
- 항공사를 입력 또는 선택한 뒤 편명을 입력한다. 예: `KE101`이 아니라 항공사 `KE`와 편명 `101`을 분리 저장한다.
- 출발 공항과 도착 공항은 공항 검색 결과에서 선택한다.
- 도착일과 도착 시간은 출발 시간 이후여야 한다.

### `POST /trips/{tripId}/schedule/lodgings`

숙소 일정 추가

요청:

```json
{
  "googlePlaceId": "google-place-id",
  "checkInDate": "2026-05-09",
  "checkOutDate": "2026-05-11",
  "sortOrder": 10
}
```

규칙:

- 숙소는 Google Places 기반 검색 결과 또는 사용자가 직접 등록한 장소를 선택한다.
- 체크인/체크아웃 날짜는 현재 여행 기간 안에서만 선택한다.
- 저장 시 하나의 `lodgingGroupId`를 만들고, 체크인일부터 체크아웃 전날까지 각 여행 일차에 `lodging` 일정 아이템을 생성해 일정 타임라인에 표시한다.
- 숙소 일정은 장소 스냅샷을 참조하되, 일반 장소 아이템과 구분하기 위해 `itemType=lodging`으로 내려준다.

응답 예시:

```json
{
  "httpStatusCode": 201,
  "responseMessage": "숙소 일정 추가 성공",
  "data": {
    "lodgingGroupId": "uuid",
    "createdItems": [
      {
        "scheduleItemId": "uuid",
        "tripDayId": "uuid",
        "stayDate": "2026-05-09"
      },
      {
        "scheduleItemId": "uuid",
        "tripDayId": "uuid",
        "stayDate": "2026-05-10"
      }
    ]
  }
}
```

### `PATCH /trips/{tripId}/schedule/items/{itemId}`

장소/메모/항공편/숙소 공통 수정

수정 가능 항목:

- `scheduledTime`
- `content` (메모)
- 항공편 상세값 (항공사, 편명, 공항, 출발/도착 일시)
- 숙소 날짜 범위 (체크인/체크아웃)
- `sortOrder`
- `tripDayId`

숙소 수정 규칙:

- 숙소 날짜 범위를 수정하면 같은 `lodgingGroupId`에 속한 day별 숙소 아이템을 재생성한다.
- 기존 범위에서 빠진 숙소 아이템은 hard delete가 아니라 soft delete로 숨긴다.

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

일정 아이템 soft delete

- 연결된 동선 구간은 후속 조회에서 제외되도록 함께 정리한다.

### `GET /trips/{tripId}/route-map`

전면 지도용 동선 데이터 조회

- soft delete된 일정/동선 구간은 응답에서 제외한다.

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

### `GET /lodgings/search`

숙소 검색 / 인기 숙소 조회

쿼리 파라미터:

- `tripId`
- `query` (옵션, 없으면 여행 도시 기준 인기 숙소)
- `cityName`

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "숙소 검색 성공",
  "data": [
    {
      "googlePlaceId": "place-id",
      "name": "호텔 오리엔탈 익스프레스 후쿠오카 나카스 카와바타",
      "gradeText": "3성급",
      "areaText": "하카타",
      "photoUrl": "https://..."
    }
  ]
}
```

### `GET /airports/search`

공항 검색

쿼리 파라미터:

- `q=공항명 또는 도시명 또는 IATA 코드`

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "공항 검색 성공",
  "data": [
    {
      "airportCode": "ATL",
      "airportName": "하츠필드 잭슨 애틀랜타 국제공항",
      "englishName": "Hartsfield-Jackson Atlanta International Airport",
      "cityName": "애틀랜타",
      "countryCode": "US"
    },
    {
      "airportCode": "BCN",
      "airportName": "엘프라트 국제공항",
      "englishName": "Barcelona El Prat Airport",
      "cityName": "바르셀로나",
      "countryCode": "ES"
    }
  ]
}
```

### `GET /places/{googlePlaceId}`

장소 상세 조회

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "장소 상세 조회 성공",
  "data": {
    "googlePlaceId": "place-id",
    "name": "이치란 라멘 하카타점",
    "localizedName": "一蘭 博多店",
    "rating": 4.1,
    "reviewCount": 110,
    "savedCount": 8518,
    "address": "하카타 주변",
    "isReservable": true,
    "openingHoursText": "08:30 - 21:00",
    "description": "하카타 여행 중 들르기 좋은 라멘 전문점",
    "photoUrls": [
      "https://..."
    ],
    "menuHighlights": [
      "돈코츠 라멘",
      "반숙 계란"
    ],
    "googleMapsUrl": "https://maps.google.com/..."
  }
}
```

## 8. 지출 / 상위 카테고리

### `POST /trips/{tripId}/expenses`

지출 생성

요청:

```json
{
  "flowType": "expense",
  "title": "이치란 라멘 하카타점",
  "expenseDate": "2026-05-08",
  "expenseTime": "13:20",
  "localCurrencyCode": "JPY",
  "amountLocal": 1280.00,
  "amountKrw": 11673.60,
  "exchangeRateToKrw": 9.12,
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

지출 soft delete

- 연결된 `expense_photos`도 함께 soft delete한다.

### `GET /trips/{tripId}/expense-categories`

상위 카테고리 목록

- soft delete되지 않은 활성 카테고리만 반환한다.

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

상위 카테고리 soft delete

- 기본 카테고리 `기타`는 삭제 불가
- 이미 연결된 과거 지출은 유지하고, 삭제된 카테고리는 신규 선택 목록에서 제외한다.

## 9. 캘린더

### `GET /calendar/summary`

전역 캘린더 월 요약

쿼리 파라미터:

- `month=YYYY-MM`

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "캘린더 월 요약 조회 성공",
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
  }
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
- soft delete되지 않은 섹션/아이템만 포함

### `POST /trips/{tripId}/checklist/sections`

섹션 생성

### `PATCH /trips/{tripId}/checklist/sections/{sectionId}`

섹션명 / 정렬 수정

### `DELETE /trips/{tripId}/checklist/sections/{sectionId}`

섹션 soft delete

- 하위 아이템도 함께 soft delete한다.

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

아이템 soft delete

## 11. OCR

### `POST /trips/{tripId}/ocr/imports`

OCR 업로드 생성

- 멀티파트 업로드 또는 이미지 URL 기준
- OCR 업로드는 반드시 `여행 생성 후`, 해당 여행이 선택 또는 진입된 상태에서만 시작할 수 있다.

응답:

```json
{
  "httpStatusCode": 201,
  "responseMessage": "OCR 업로드 생성 성공",
  "data": {
    "ocrImportId": "uuid",
    "status": "reviewing"
  }
}
```

### `GET /trips/{tripId}/ocr/imports/{ocrImportId}`

OCR 추출 결과 조회

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "OCR 추출 결과 조회 성공",
  "data": {
    "ocrImportId": "uuid",
    "status": "reviewing",
    "candidates": [
      {
        "candidateId": "uuid",
        "candidateType": "flight",
        "status": "pending",
        "targetDate": "2026-05-07",
        "targetTime": "09:20",
        "candidatePayload": {
          "airlineName": "대한항공",
          "airlineCode": "KE",
          "flightNumber": "101",
          "departureAirportCode": "ICN",
          "arrivalAirportCode": "FUK"
        },
        "editedPayload": null,
        "sourceText": "KE101 ICN-FUK 09:20"
      }
    ]
  }
}
```

### `PATCH /trips/{tripId}/ocr/imports/{ocrImportId}/candidates/{candidateId}`

OCR 후보 수정 / 승인 / 거절

- OCR 후보는 별도 삭제 API 없이 상태 변경으로 관리한다.
- `status=accepted|rejected|edited` 중 하나로 변경한다.
- `status=edited`일 때만 `editedPayload`를 필수로 받는다.

요청 예시:

```json
{
  "status": "edited",
  "targetDate": "2026-05-07",
  "targetTime": "09:30",
  "editedPayload": {
    "candidateType": "flight",
    "airlineName": "대한항공",
    "airlineCode": "KE",
    "flightNumber": "101",
    "departureAirportCode": "ICN",
    "arrivalAirportCode": "FUK"
  }
}
```

### `POST /trips/{tripId}/ocr/imports/{ocrImportId}/confirm`

검수 결과를 일정으로 반영

요청 예시:

```json
{
  "acceptedCandidateIds": ["uuid-1", "uuid-2"]
}
```

응답 예시:

```json
{
  "httpStatusCode": 201,
  "responseMessage": "OCR 일정 반영 성공",
  "data": {
    "createdScheduleItemIds": ["uuid-1", "uuid-2"],
    "skippedCandidateIds": []
  }
}
```

## 12. 날씨 / 환율

### `GET /trips/{tripId}/weather`

여행 기간 날씨 조회

응답 예시:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "날씨 조회 성공",
  "data": {
    "countryName": "일본",
    "representativeCityName": "후쿠오카",
    "timeZone": "Asia/Tokyo",
    "utcOffsetMinutes": 540,
    "localTime": "2026-05-08T13:30:00+09:00",
    "days": [
      {
        "date": "2026-05-07",
        "summary": "맑음",
        "iconCode": "sun",
        "minTemp": 6,
        "maxTemp": 20
      }
    ]
  }
}
```

### `GET /trips/{tripId}/fx`

여행 기준 통화 환율 조회

쿼리 파라미터:

- `date=YYYY-MM-DD`

응답:

```json
{
  "httpStatusCode": 200,
  "responseMessage": "환율 조회 성공",
  "data": {
    "currencyCode": "JPY",
    "rateToKrw": 9.12,
    "previousBusinessDate": "2026-05-07",
    "changeAmount": 0.02,
    "changePercent": 0.18
  }
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

규칙:

- `scope`는 `trip_all | selected_day | today` 중 하나를 사용한다.
- `scope=selected_day`이면 `tripDayId`가 필수다.
- PDF는 일정 공유용이며 지출 상세 내역은 기본 범위에서 제외한다.

응답:

```json
{
  "httpStatusCode": 201,
  "responseMessage": "PDF 생성 성공",
  "data": {
    "downloadUrl": "https://..."
  }
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
