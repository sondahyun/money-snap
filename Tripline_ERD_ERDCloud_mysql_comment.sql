-- Tripline ERDCloud용 MySQL SQL (속성/관계선 표시 강화 버전)
-- 목적:
--   1. 테이블 COMMENT는 한글 테이블명 참고용으로 유지
--   2. 컬럼 COMMENT는 ERDCloud 5번째 Comment 영역의 속성 표시용으로 사용
--   3. FOREIGN KEY를 관계선으로 인식
-- 주의:
--   - 실제 DB 실행용이 아니라 ERDCloud 업로드용
--   - 기준 스키마는 Tripline_ERD_mysql.sql
--   - 논리명은 Tripline_ERD_ERDCloud_logical_mapping.md 기준으로 수동 입력
--   - 컬럼 COMMENT에는 설명문 대신 UNIQUE 같은 속성만 최소한으로 표기
--   - trip_days / schedule_items / trip_route_segments / expenses / expense_photos / trip_expense_categories / checklist_sections / checklist_items 는 soft delete 대상
--   - soft delete 활성 UNIQUE 보조 generated column은 ERDCloud 호환을 위해 생략
--   - 컬럼 COMMENT와 테이블 COMMENT를 MySQL 정석 문법으로 분리
--   - PK/FK도 모두 테이블 하단 CONSTRAINT 구문으로 작성
--   - 파일 인코딩은 UTF-8, 테이블 기본 문자는 utf8mb4 기준
--   - default / check / enum / json / 복합 FK 일부는 ERDCloud 호환을 위해 단순화

SET NAMES utf8mb4;

CREATE TABLE users (
    id CHAR(36) NOT NULL COMMENT '',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'UNIQUE',
    password_hash VARCHAR(255) NOT NULL COMMENT '',
    nickname VARCHAR(50) NOT NULL COMMENT '',
    is_active BOOLEAN NOT NULL COMMENT '',
    last_login_at DATETIME COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_users PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';

CREATE TABLE user_settings (
    user_id CHAR(36) NOT NULL COMMENT '',
    base_currency_code CHAR(3) NOT NULL COMMENT '',
    default_pdf_scope VARCHAR(30) NOT NULL COMMENT '',
    default_route_transport_mode VARCHAR(30) NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    CONSTRAINT pk_user_settings PRIMARY KEY (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 설정';

CREATE TABLE auth_refresh_tokens (
    id CHAR(36) NOT NULL COMMENT '',
    user_id CHAR(36) NOT NULL COMMENT '',
    refresh_token_hash VARCHAR(255) NOT NULL UNIQUE COMMENT 'UNIQUE',
    user_agent TEXT COMMENT '',
    expires_at DATETIME NOT NULL COMMENT '',
    revoked_at DATETIME COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    CONSTRAINT pk_auth_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_auth_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='리프레시 토큰';

CREATE TABLE trips (
    id CHAR(36) NOT NULL COMMENT '',
    user_id CHAR(36) NOT NULL COMMENT '',
    title VARCHAR(100) NOT NULL COMMENT '',
    country_code CHAR(2) NOT NULL COMMENT '',
    country_name VARCHAR(100) NOT NULL COMMENT '',
    city_name VARCHAR(100) NOT NULL COMMENT '',
    start_date DATE NOT NULL COMMENT '',
    end_date DATE NOT NULL COMMENT '',
    base_currency_code CHAR(3) NOT NULL COMMENT '',
    one_line_description VARCHAR(255) COMMENT '',
    status VARCHAR(30) NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_trips PRIMARY KEY (id),
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행';

CREATE TABLE user_current_trip_selections (
    user_id CHAR(36) NOT NULL COMMENT '',
    trip_id CHAR(36) NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    CONSTRAINT pk_user_current_trip_selections PRIMARY KEY (user_id),
    CONSTRAINT fk_user_current_trip_selections_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_current_trip_selections_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='현재 선택 여행';

CREATE TABLE trip_days (
    id CHAR(36) NOT NULL COMMENT '',
    trip_id CHAR(36) NOT NULL COMMENT '',
    day_index INT NOT NULL COMMENT 'UNIQUE(trip_id, day_index)',
    calendar_date DATE NOT NULL COMMENT 'UNIQUE(trip_id, calendar_date)',
    title VARCHAR(100) COMMENT '',
    sort_order INT NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_trip_days PRIMARY KEY (id),
    CONSTRAINT fk_trip_days_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행 일차';

CREATE TABLE ocr_imports (
    id CHAR(36) NOT NULL COMMENT '',
    trip_id CHAR(36) NOT NULL COMMENT '',
    source_type VARCHAR(20) NOT NULL COMMENT '',
    source_image_url TEXT COMMENT '',
    status VARCHAR(30) NOT NULL COMMENT '',
    raw_text LONGTEXT COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    confirmed_at DATETIME COMMENT '',
    CONSTRAINT pk_ocr_imports PRIMARY KEY (id),
    CONSTRAINT fk_ocr_imports_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR 가져오기';

CREATE TABLE ocr_candidates (
    id CHAR(36) NOT NULL COMMENT '',
    ocr_import_id CHAR(36) NOT NULL COMMENT '',
    trip_day_id CHAR(36) COMMENT '',
    candidate_type VARCHAR(30) NOT NULL COMMENT '',
    status VARCHAR(30) NOT NULL COMMENT '',
    target_date DATE COMMENT '',
    target_time TIME COMMENT '',
    place_name VARCHAR(200) COMMENT '',
    hotel_name VARCHAR(200) COMMENT '',
    flight_number VARCHAR(30) COMMENT '',
    memo_content TEXT COMMENT '',
    source_text LONGTEXT COMMENT '',
    edited_payload LONGTEXT NOT NULL COMMENT '',
    sort_order INT NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    CONSTRAINT pk_ocr_candidates PRIMARY KEY (id),
    CONSTRAINT fk_ocr_candidates_import FOREIGN KEY (ocr_import_id) REFERENCES ocr_imports(id),
    CONSTRAINT fk_ocr_candidates_trip_day FOREIGN KEY (trip_day_id) REFERENCES trip_days(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR 후보';

CREATE TABLE trip_place_snapshots (
    id CHAR(36) NOT NULL COMMENT '',
    trip_id CHAR(36) NOT NULL COMMENT '',
    google_place_id VARCHAR(128) NOT NULL COMMENT 'UNIQUE(trip_id, google_place_id)',
    name VARCHAR(200) NOT NULL COMMENT '',
    localized_name VARCHAR(200) COMMENT '',
    primary_type VARCHAR(100) COMMENT '',
    secondary_text VARCHAR(255) COMMENT '',
    address VARCHAR(255) COMMENT '',
    latitude DECIMAL(10,7) NOT NULL COMMENT '',
    longitude DECIMAL(10,7) NOT NULL COMMENT '',
    rating DECIMAL(2,1) COMMENT '',
    review_count INT COMMENT '',
    saved_count INT COMMENT '',
    is_reservable BOOLEAN NOT NULL COMMENT '',
    is_open_now BOOLEAN COMMENT '',
    opening_hours_text TEXT COMMENT '',
    google_maps_url TEXT COMMENT '',
    photo_url TEXT COMMENT '',
    metadata LONGTEXT NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    CONSTRAINT pk_trip_place_snapshots PRIMARY KEY (id),
    CONSTRAINT fk_trip_place_snapshots_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행 장소 스냅샷';

CREATE TABLE schedule_items (
    id CHAR(36) NOT NULL COMMENT '',
    trip_id CHAR(36) NOT NULL COMMENT '',
    trip_day_id CHAR(36) NOT NULL COMMENT '',
    item_type VARCHAR(20) NOT NULL COMMENT '',
    trip_place_snapshot_id CHAR(36) COMMENT '',
    created_from_ocr_candidate_id CHAR(36) COMMENT '',
    title VARCHAR(200) COMMENT '',
    memo_content TEXT COMMENT '',
    scheduled_time TIME COMMENT '',
    sort_order INT NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_schedule_items PRIMARY KEY (id),
    CONSTRAINT fk_schedule_items_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    CONSTRAINT fk_schedule_items_trip_day FOREIGN KEY (trip_day_id) REFERENCES trip_days(id),
    CONSTRAINT fk_schedule_items_place FOREIGN KEY (trip_place_snapshot_id) REFERENCES trip_place_snapshots(id),
    CONSTRAINT fk_schedule_items_ocr_candidate FOREIGN KEY (created_from_ocr_candidate_id) REFERENCES ocr_candidates(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일정 아이템';

CREATE TABLE trip_route_segments (
    id CHAR(36) NOT NULL COMMENT '',
    trip_day_id CHAR(36) NOT NULL COMMENT '',
    from_schedule_item_id CHAR(36) NOT NULL COMMENT '',
    to_schedule_item_id CHAR(36) NOT NULL COMMENT 'UNIQUE(trip_day_id, from_schedule_item_id, to_schedule_item_id)',
    sort_order INT NOT NULL COMMENT '',
    distance_meters INT NOT NULL COMMENT '',
    walk_minutes INT COMMENT '',
    drive_minutes INT COMMENT '',
    polyline TEXT COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_trip_route_segments PRIMARY KEY (id),
    CONSTRAINT fk_trip_route_segments_trip_day FOREIGN KEY (trip_day_id) REFERENCES trip_days(id),
    CONSTRAINT fk_trip_route_segments_from_item FOREIGN KEY (from_schedule_item_id) REFERENCES schedule_items(id),
    CONSTRAINT fk_trip_route_segments_to_item FOREIGN KEY (to_schedule_item_id) REFERENCES schedule_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일정 이동 구간';

CREATE TABLE trip_expense_categories (
    id CHAR(36) NOT NULL COMMENT '',
    trip_id CHAR(36) NOT NULL COMMENT '',
    name VARCHAR(50) NOT NULL COMMENT 'UNIQUE(trip_id, name)',
    description VARCHAR(255) COMMENT '',
    color_hex CHAR(7) COMMENT '',
    is_default BOOLEAN NOT NULL COMMENT '',
    sort_order INT NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_trip_expense_categories PRIMARY KEY (id),
    CONSTRAINT fk_trip_expense_categories_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상위 지출 카테고리';

CREATE TABLE expenses (
    id CHAR(36) NOT NULL COMMENT '',
    trip_id CHAR(36) NOT NULL COMMENT '',
    schedule_item_id CHAR(36) COMMENT '',
    trip_place_snapshot_id CHAR(36) COMMENT '',
    top_category_id CHAR(36) NOT NULL COMMENT '',
    flow_type VARCHAR(20) NOT NULL COMMENT '',
    title VARCHAR(200) NOT NULL COMMENT '',
    expense_date DATE NOT NULL COMMENT '',
    expense_time TIME COMMENT '',
    local_currency_code CHAR(3) NOT NULL COMMENT '',
    amount_local DECIMAL(14,2) NOT NULL COMMENT '',
    amount_krw DECIMAL(14,2) NOT NULL COMMENT '',
    exchange_rate_to_krw DECIMAL(14,6) NOT NULL COMMENT '',
    payment_method VARCHAR(30) NOT NULL COMMENT '',
    note TEXT COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_expenses PRIMARY KEY (id),
    CONSTRAINT fk_expenses_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    CONSTRAINT fk_expenses_schedule_item FOREIGN KEY (schedule_item_id) REFERENCES schedule_items(id),
    CONSTRAINT fk_expenses_trip_place_snapshot FOREIGN KEY (trip_place_snapshot_id) REFERENCES trip_place_snapshots(id),
    CONSTRAINT fk_expenses_top_category FOREIGN KEY (top_category_id) REFERENCES trip_expense_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='지출';

CREATE TABLE expense_photos (
    id CHAR(36) NOT NULL COMMENT '',
    expense_id CHAR(36) NOT NULL COMMENT '',
    image_url TEXT NOT NULL COMMENT '',
    sort_order INT NOT NULL COMMENT 'UNIQUE(expense_id, sort_order)',
    created_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_expense_photos PRIMARY KEY (id),
    CONSTRAINT fk_expense_photos_expense FOREIGN KEY (expense_id) REFERENCES expenses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='지출 사진';

CREATE TABLE checklist_sections (
    id CHAR(36) NOT NULL COMMENT '',
    trip_id CHAR(36) NOT NULL COMMENT '',
    name VARCHAR(100) NOT NULL COMMENT '',
    sort_order INT NOT NULL COMMENT '',
    is_collapsed BOOLEAN NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_checklist_sections PRIMARY KEY (id),
    CONSTRAINT fk_checklist_sections_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='준비물 섹션';

CREATE TABLE checklist_items (
    id CHAR(36) NOT NULL COMMENT '',
    section_id CHAR(36) NOT NULL COMMENT '',
    title VARCHAR(100) NOT NULL COMMENT '',
    note VARCHAR(255) COMMENT '',
    is_checked BOOLEAN NOT NULL COMMENT '',
    checked_at DATETIME COMMENT '',
    sort_order INT NOT NULL COMMENT '',
    created_at DATETIME NOT NULL COMMENT '',
    updated_at DATETIME NOT NULL COMMENT '',
    deleted_at DATETIME COMMENT '',
    CONSTRAINT pk_checklist_items PRIMARY KEY (id),
    CONSTRAINT fk_checklist_items_section FOREIGN KEY (section_id) REFERENCES checklist_sections(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='준비물 아이템';

CREATE TABLE trip_weather_daily (
    trip_id CHAR(36) NOT NULL COMMENT '',
    weather_date DATE NOT NULL COMMENT '',
    country_name VARCHAR(100) NOT NULL COMMENT '',
    representative_city_name VARCHAR(100) NOT NULL COMMENT '',
    time_zone VARCHAR(60) NOT NULL COMMENT '',
    utc_offset_minutes INT NOT NULL COMMENT '',
    summary VARCHAR(50) NOT NULL COMMENT '',
    icon_code VARCHAR(30) NOT NULL COMMENT '',
    min_temp DECIMAL(5,2) COMMENT '',
    max_temp DECIMAL(5,2) COMMENT '',
    source VARCHAR(50) COMMENT '',
    fetched_at DATETIME NOT NULL COMMENT '',
    CONSTRAINT pk_trip_weather_daily PRIMARY KEY (trip_id, weather_date),
    CONSTRAINT fk_trip_weather_daily_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행 일별 날씨';

CREATE TABLE exchange_rates (
    rate_date DATE NOT NULL COMMENT '',
    currency_code CHAR(3) NOT NULL COMMENT '',
    country_name VARCHAR(100) COMMENT '',
    rate_to_krw DECIMAL(14,6) NOT NULL COMMENT '',
    previous_business_date DATE COMMENT '',
    change_amount DECIMAL(14,6) COMMENT '',
    change_percent DECIMAL(10,4) COMMENT '',
    source VARCHAR(50) COMMENT '',
    fetched_at DATETIME NOT NULL COMMENT '',
    CONSTRAINT pk_exchange_rates PRIMARY KEY (rate_date, currency_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='환율';
