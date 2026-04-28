# ERDCloud 논리명 입력용 매핑표

- 기준 물리 스키마: `Tripline_ERD_mysql.sql`
- 기준 논리명: 이 문서 기준 수동 입력
- ERDCloud 5열 코멘트: `Tripline_ERD_ERDCloud_mysql_comment.sql`의 컬럼 `COMMENT`
- 코멘트 정책: 설명문 없이 `UNIQUE` 같은 속성만 최소한으로 표기
- 삭제 정책: `trip_days`, `schedule_items`, `trip_route_segments`, `expenses`, `expense_photos`, `trip_expense_categories`, `checklist_sections`, `checklist_items`는 `deleted_at` 기반 soft delete
- 용도: ERDCloud에서 SQL import 후 한글 논리명 수동 입력
- 표기: `키`는 `PK`, `FK`, `UK`를 조합해서 표시

## 테이블 목록

| 테이블 논리명 | 테이블 물리명 |
|---|---|
| 사용자 | `users` |
| 사용자 설정 | `user_settings` |
| 리프레시 토큰 | `auth_refresh_tokens` |
| 여행 | `trips` |
| 현재 선택 여행 | `user_current_trip_selections` |
| 여행 일차 | `trip_days` |
| OCR 가져오기 | `ocr_imports` |
| OCR 후보 | `ocr_candidates` |
| 여행 장소 스냅샷 | `trip_place_snapshots` |
| 일정 아이템 | `schedule_items` |
| 일정 이동 구간 | `trip_route_segments` |
| 상위 지출 카테고리 | `trip_expense_categories` |
| 지출 | `expenses` |
| 지출 사진 | `expense_photos` |
| 준비물 섹션 | `checklist_sections` |
| 준비물 아이템 | `checklist_items` |
| 여행 일별 날씨 | `trip_weather_daily` |
| 환율 | `exchange_rates` |

## 사용자 (`users`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 이메일 | `email` | `VARCHAR(255)` | NOT NULL | UK |
| 비밀번호 해시 | `password_hash` | `VARCHAR(255)` | NOT NULL | - |
| 닉네임 | `nickname` | `VARCHAR(50)` | NOT NULL | - |
| 사용 가능 여부 | `is_active` | `BOOLEAN` | NOT NULL | - |
| 마지막 로그인 일시 | `last_login_at` | `DATETIME` | NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 탈퇴일시 | `deleted_at` | `DATETIME` | NULL | - |

## 사용자 설정 (`user_settings`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 사용자 아이디 | `user_id` | `CHAR(36)` | NOT NULL | PK/FK |
| 기준 통화 코드 | `base_currency_code` | `CHAR(3)` | NOT NULL | - |
| 기본 PDF 범위 | `default_pdf_scope` | `VARCHAR(30)` | NOT NULL | - |
| 기본 이동수단 기준 | `default_route_transport_mode` | `VARCHAR(30)` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |

## 리프레시 토큰 (`auth_refresh_tokens`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 사용자 아이디 | `user_id` | `CHAR(36)` | NOT NULL | FK |
| 리프레시 토큰 해시 | `refresh_token_hash` | `VARCHAR(255)` | NOT NULL | UK |
| 사용자 에이전트 | `user_agent` | `TEXT` | NULL | - |
| 만료일시 | `expires_at` | `DATETIME` | NOT NULL | - |
| 폐기일시 | `revoked_at` | `DATETIME` | NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |

## 여행 (`trips`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 사용자 아이디 | `user_id` | `CHAR(36)` | NOT NULL | FK |
| 여행 제목 | `title` | `VARCHAR(100)` | NOT NULL | - |
| 국가 코드 | `country_code` | `CHAR(2)` | NOT NULL | - |
| 국가명 | `country_name` | `VARCHAR(100)` | NOT NULL | - |
| 도시명 | `city_name` | `VARCHAR(100)` | NOT NULL | - |
| 시작일 | `start_date` | `DATE` | NOT NULL | - |
| 종료일 | `end_date` | `DATE` | NOT NULL | - |
| 기준 통화 코드 | `base_currency_code` | `CHAR(3)` | NOT NULL | - |
| 한 줄 소개 | `one_line_description` | `VARCHAR(255)` | NULL | - |
| 여행 상태 | `status` | `VARCHAR(30)` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## 현재 선택 여행 (`user_current_trip_selections`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 사용자 아이디 | `user_id` | `CHAR(36)` | NOT NULL | PK/FK |
| 현재 선택 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | FK |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |

## 여행 일차 (`trip_days`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | FK |
| 여행 일차 | `day_index` | `INT` | NOT NULL | - |
| 달력 날짜 | `calendar_date` | `DATE` | NOT NULL | - |
| 일차 제목 | `title` | `VARCHAR(100)` | NULL | - |
| 정렬 순서 | `sort_order` | `INT` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## OCR 가져오기 (`ocr_imports`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | FK |
| 가져오기 소스 | `source_type` | `VARCHAR(20)` | NOT NULL | - |
| 원본 이미지 URL | `source_image_url` | `TEXT` | NULL | - |
| OCR 상태 | `status` | `VARCHAR(30)` | NOT NULL | - |
| 추출 원문 | `raw_text` | `LONGTEXT` | NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 확정일시 | `confirmed_at` | `DATETIME` | NULL | - |

## OCR 후보 (`ocr_candidates`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| OCR 가져오기 아이디 | `ocr_import_id` | `CHAR(36)` | NOT NULL | FK |
| 연결 여행 일차 아이디 | `trip_day_id` | `CHAR(36)` | NULL | FK |
| 후보 유형 | `candidate_type` | `VARCHAR(30)` | NOT NULL | - |
| 후보 상태 | `status` | `VARCHAR(30)` | NOT NULL | - |
| 대상 날짜 | `target_date` | `DATE` | NULL | - |
| 대상 시간 | `target_time` | `TIME` | NULL | - |
| 장소명 | `place_name` | `VARCHAR(200)` | NULL | - |
| 숙소명 | `hotel_name` | `VARCHAR(200)` | NULL | - |
| 항공편 번호 | `flight_number` | `VARCHAR(30)` | NULL | - |
| 메모 내용 | `memo_content` | `TEXT` | NULL | - |
| 원문 텍스트 | `source_text` | `LONGTEXT` | NULL | - |
| 수정 데이터 | `edited_payload` | `LONGTEXT` | NOT NULL | - |
| 정렬 순서 | `sort_order` | `INT` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |

## 여행 장소 스냅샷 (`trip_place_snapshots`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | FK |
| 구글 장소 아이디 | `google_place_id` | `VARCHAR(128)` | NOT NULL | - |
| 장소명 | `name` | `VARCHAR(200)` | NOT NULL | - |
| 현지명 | `localized_name` | `VARCHAR(200)` | NULL | - |
| 대표 유형 | `primary_type` | `VARCHAR(100)` | NULL | - |
| 보조 설명 | `secondary_text` | `VARCHAR(255)` | NULL | - |
| 주소 | `address` | `VARCHAR(255)` | NULL | - |
| 위도 | `latitude` | `DECIMAL(10,7)` | NOT NULL | - |
| 경도 | `longitude` | `DECIMAL(10,7)` | NOT NULL | - |
| 평점 | `rating` | `DECIMAL(2,1)` | NULL | - |
| 리뷰 수 | `review_count` | `INT` | NULL | - |
| 저장 수 | `saved_count` | `INT` | NULL | - |
| 예약 가능 여부 | `is_reservable` | `BOOLEAN` | NOT NULL | - |
| 현재 영업 여부 | `is_open_now` | `BOOLEAN` | NULL | - |
| 영업시간 텍스트 | `opening_hours_text` | `TEXT` | NULL | - |
| 구글맵 URL | `google_maps_url` | `TEXT` | NULL | - |
| 대표 사진 URL | `photo_url` | `TEXT` | NULL | - |
| 확장 메타데이터 | `metadata` | `LONGTEXT` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |

## 일정 아이템 (`schedule_items`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | FK |
| 여행 일차 아이디 | `trip_day_id` | `CHAR(36)` | NOT NULL | FK |
| 일정 유형 | `item_type` | `VARCHAR(20)` | NOT NULL | - |
| 장소 스냅샷 아이디 | `trip_place_snapshot_id` | `CHAR(36)` | NULL | FK |
| OCR 후보 아이디 | `created_from_ocr_candidate_id` | `CHAR(36)` | NULL | FK |
| 제목 | `title` | `VARCHAR(200)` | NULL | - |
| 메모 내용 | `memo_content` | `TEXT` | NULL | - |
| 예정 시간 | `scheduled_time` | `TIME` | NULL | - |
| 정렬 순서 | `sort_order` | `INT` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## 일정 이동 구간 (`trip_route_segments`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 여행 일차 아이디 | `trip_day_id` | `CHAR(36)` | NOT NULL | FK |
| 출발 일정 아이디 | `from_schedule_item_id` | `CHAR(36)` | NOT NULL | FK |
| 도착 일정 아이디 | `to_schedule_item_id` | `CHAR(36)` | NOT NULL | FK |
| 정렬 순서 | `sort_order` | `INT` | NOT NULL | - |
| 거리(미터) | `distance_meters` | `INT` | NOT NULL | - |
| 도보 시간(분) | `walk_minutes` | `INT` | NULL | - |
| 차량 시간(분) | `drive_minutes` | `INT` | NULL | - |
| 경로 폴리라인 | `polyline` | `TEXT` | NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## 상위 지출 카테고리 (`trip_expense_categories`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | FK |
| 상위 카테고리명 | `name` | `VARCHAR(50)` | NOT NULL | - |
| 설명 | `description` | `VARCHAR(255)` | NULL | - |
| 색상 코드 | `color_hex` | `CHAR(7)` | NULL | - |
| 기본 여부 | `is_default` | `BOOLEAN` | NOT NULL | - |
| 정렬 순서 | `sort_order` | `INT` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## 지출 (`expenses`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | FK |
| 일정 아이템 아이디 | `schedule_item_id` | `CHAR(36)` | NULL | FK |
| 장소 스냅샷 아이디 | `trip_place_snapshot_id` | `CHAR(36)` | NULL | FK |
| 상위 카테고리 아이디 | `top_category_id` | `CHAR(36)` | NOT NULL | FK |
| 지출 흐름 유형 | `flow_type` | `VARCHAR(20)` | NOT NULL | - |
| 항목명 | `title` | `VARCHAR(200)` | NOT NULL | - |
| 지출 날짜 | `expense_date` | `DATE` | NOT NULL | - |
| 지출 시간 | `expense_time` | `TIME` | NULL | - |
| 현지 통화 코드 | `local_currency_code` | `CHAR(3)` | NOT NULL | - |
| 현지 통화 금액 | `amount_local` | `DECIMAL(14,2)` | NOT NULL | - |
| 원화 금액 | `amount_krw` | `DECIMAL(14,2)` | NOT NULL | - |
| 원화 환산 환율 | `exchange_rate_to_krw` | `DECIMAL(14,6)` | NOT NULL | - |
| 결제수단 | `payment_method` | `VARCHAR(30)` | NOT NULL | - |
| 메모 | `note` | `TEXT` | NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## 지출 사진 (`expense_photos`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 지출 아이디 | `expense_id` | `CHAR(36)` | NOT NULL | FK |
| 사진 URL | `image_url` | `TEXT` | NOT NULL | - |
| 정렬 순서 | `sort_order` | `INT` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## 준비물 섹션 (`checklist_sections`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | FK |
| 섹션명 | `name` | `VARCHAR(100)` | NOT NULL | - |
| 정렬 순서 | `sort_order` | `INT` | NOT NULL | - |
| 접힘 여부 | `is_collapsed` | `BOOLEAN` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## 준비물 아이템 (`checklist_items`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 아이디 | `id` | `CHAR(36)` | NOT NULL | PK |
| 섹션 아이디 | `section_id` | `CHAR(36)` | NOT NULL | FK |
| 아이템명 | `title` | `VARCHAR(100)` | NOT NULL | - |
| 메모 | `note` | `VARCHAR(255)` | NULL | - |
| 체크 여부 | `is_checked` | `BOOLEAN` | NOT NULL | - |
| 체크 일시 | `checked_at` | `DATETIME` | NULL | - |
| 정렬 순서 | `sort_order` | `INT` | NOT NULL | - |
| 생성일시 | `created_at` | `DATETIME` | NOT NULL | - |
| 수정일시 | `updated_at` | `DATETIME` | NOT NULL | - |
| 삭제일시 | `deleted_at` | `DATETIME` | NULL | - |

## 여행 일별 날씨 (`trip_weather_daily`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 여행 아이디 | `trip_id` | `CHAR(36)` | NOT NULL | PK/FK |
| 날씨 날짜 | `weather_date` | `DATE` | NOT NULL | PK |
| 국가명 | `country_name` | `VARCHAR(100)` | NOT NULL | - |
| 대표 도시명 | `representative_city_name` | `VARCHAR(100)` | NOT NULL | - |
| 타임존 | `time_zone` | `VARCHAR(60)` | NOT NULL | - |
| UTC 오프셋(분) | `utc_offset_minutes` | `INT` | NOT NULL | - |
| 날씨 요약 | `summary` | `VARCHAR(50)` | NOT NULL | - |
| 날씨 아이콘 코드 | `icon_code` | `VARCHAR(30)` | NOT NULL | - |
| 최저기온 | `min_temp` | `DECIMAL(5,2)` | NULL | - |
| 최고기온 | `max_temp` | `DECIMAL(5,2)` | NULL | - |
| 데이터 소스 | `source` | `VARCHAR(50)` | NULL | - |
| 조회일시 | `fetched_at` | `DATETIME` | NOT NULL | - |

## 환율 (`exchange_rates`)

| 논리명 | 물리명 | 타입 | NULL | 키 |
|---|---|---|---|---|
| 환율 기준일 | `rate_date` | `DATE` | NOT NULL | PK |
| 통화 코드 | `currency_code` | `CHAR(3)` | NOT NULL | PK |
| 국가명 | `country_name` | `VARCHAR(100)` | NULL | - |
| 원화 환율 | `rate_to_krw` | `DECIMAL(14,6)` | NOT NULL | - |
| 전 영업일 | `previous_business_date` | `DATE` | NULL | - |
| 변동 금액 | `change_amount` | `DECIMAL(14,6)` | NULL | - |
| 변동 비율 | `change_percent` | `DECIMAL(10,4)` | NULL | - |
| 데이터 소스 | `source` | `VARCHAR(50)` | NULL | - |
| 조회일시 | `fetched_at` | `DATETIME` | NOT NULL | - |
