-- Tripline ERDCloud용 MySQL SQL (논리명/관계선 표시 강화 버전)
-- 목적:
--   1. ERDCloud에서 테이블/컬럼 COMMENT를 논리명으로 표시
--   2. FOREIGN KEY를 관계선으로 인식
-- 주의:
--   - 실제 DB 실행용이 아니라 ERDCloud 업로드용
--   - 기준 스키마는 Tripline_ERD_mysql.sql
--   - 컬럼 COMMENT와 테이블 COMMENT를 MySQL 정석 문법으로 분리
--   - PK/FK도 모두 테이블 하단 CONSTRAINT 구문으로 작성
--   - 파일 인코딩은 UTF-8, 테이블 기본 문자는 utf8mb4 기준
--   - default / check / enum / json / 복합 FK 일부는 ERDCloud 호환을 위해 단순화

SET NAMES utf8mb4;

CREATE TABLE users (
    id CHAR(36) NOT NULL COMMENT '아이디',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일',
    password_hash VARCHAR(255) NOT NULL COMMENT '비밀번호 해시',
    nickname VARCHAR(50) NOT NULL COMMENT '닉네임',
    is_active BOOLEAN NOT NULL COMMENT '활성 여부',
    last_login_at DATETIME COMMENT '마지막 로그인 일시',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_users PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';

CREATE TABLE user_settings (
    user_id CHAR(36) NOT NULL COMMENT '사용자 아이디',
    base_currency_code CHAR(3) NOT NULL COMMENT '기준 통화 코드',
    default_pdf_scope VARCHAR(30) NOT NULL COMMENT '기본 PDF 범위',
    default_route_transport_mode VARCHAR(30) NOT NULL COMMENT '기본 이동수단 기준',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_user_settings PRIMARY KEY (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 설정';

CREATE TABLE auth_refresh_tokens (
    id CHAR(36) NOT NULL COMMENT '아이디',
    user_id CHAR(36) NOT NULL COMMENT '사용자 아이디',
    refresh_token_hash VARCHAR(255) NOT NULL UNIQUE COMMENT '리프레시 토큰 해시',
    user_agent TEXT COMMENT '사용자 에이전트',
    expires_at DATETIME NOT NULL COMMENT '만료일시',
    revoked_at DATETIME COMMENT '폐기일시',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    CONSTRAINT pk_auth_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_auth_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='리프레시 토큰';

CREATE TABLE trips (
    id CHAR(36) NOT NULL COMMENT '아이디',
    user_id CHAR(36) NOT NULL COMMENT '사용자 아이디',
    title VARCHAR(100) NOT NULL COMMENT '여행 제목',
    country_code CHAR(2) NOT NULL COMMENT '국가 코드',
    country_name VARCHAR(100) NOT NULL COMMENT '국가명',
    city_name VARCHAR(100) NOT NULL COMMENT '도시명',
    start_date DATE NOT NULL COMMENT '시작일',
    end_date DATE NOT NULL COMMENT '종료일',
    base_currency_code CHAR(3) NOT NULL COMMENT '기준 통화 코드',
    one_line_description VARCHAR(255) COMMENT '한 줄 소개',
    status VARCHAR(30) NOT NULL COMMENT '여행 상태',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    CONSTRAINT pk_trips PRIMARY KEY (id),
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행';

CREATE TABLE user_current_trip_selections (
    user_id CHAR(36) NOT NULL COMMENT '사용자 아이디',
    trip_id CHAR(36) NOT NULL COMMENT '현재 선택 여행 아이디',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_user_current_trip_selections PRIMARY KEY (user_id),
    CONSTRAINT fk_user_current_trip_selections_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_current_trip_selections_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='현재 선택 여행';

CREATE TABLE trip_days (
    id CHAR(36) NOT NULL COMMENT '아이디',
    trip_id CHAR(36) NOT NULL COMMENT '여행 아이디',
    day_index INT NOT NULL COMMENT '여행 일차',
    calendar_date DATE NOT NULL COMMENT '달력 날짜',
    title VARCHAR(100) COMMENT '일차 제목',
    sort_order INT NOT NULL COMMENT '정렬 순서',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_trip_days PRIMARY KEY (id),
    CONSTRAINT fk_trip_days_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행 일차';

CREATE TABLE ocr_imports (
    id CHAR(36) NOT NULL COMMENT '아이디',
    trip_id CHAR(36) NOT NULL COMMENT '여행 아이디',
    source_type VARCHAR(20) NOT NULL COMMENT '가져오기 소스',
    source_image_url TEXT COMMENT '원본 이미지 URL',
    status VARCHAR(30) NOT NULL COMMENT 'OCR 상태',
    raw_text LONGTEXT COMMENT '추출 원문',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    confirmed_at DATETIME COMMENT '확정일시',
    CONSTRAINT pk_ocr_imports PRIMARY KEY (id),
    CONSTRAINT fk_ocr_imports_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR 가져오기';

CREATE TABLE ocr_candidates (
    id CHAR(36) NOT NULL COMMENT '아이디',
    ocr_import_id CHAR(36) NOT NULL COMMENT 'OCR 가져오기 아이디',
    trip_day_id CHAR(36) COMMENT '연결 여행 일차 아이디',
    candidate_type VARCHAR(30) NOT NULL COMMENT '후보 유형',
    status VARCHAR(30) NOT NULL COMMENT '후보 상태',
    target_date DATE COMMENT '대상 날짜',
    target_time TIME COMMENT '대상 시간',
    place_name VARCHAR(200) COMMENT '장소명',
    hotel_name VARCHAR(200) COMMENT '숙소명',
    flight_number VARCHAR(30) COMMENT '항공편 번호',
    memo_content TEXT COMMENT '메모 내용',
    source_text LONGTEXT COMMENT '원문 텍스트',
    edited_payload LONGTEXT NOT NULL COMMENT '수정 데이터',
    sort_order INT NOT NULL COMMENT '정렬 순서',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_ocr_candidates PRIMARY KEY (id),
    CONSTRAINT fk_ocr_candidates_import FOREIGN KEY (ocr_import_id) REFERENCES ocr_imports(id),
    CONSTRAINT fk_ocr_candidates_trip_day FOREIGN KEY (trip_day_id) REFERENCES trip_days(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR 후보';

CREATE TABLE trip_place_snapshots (
    id CHAR(36) NOT NULL COMMENT '아이디',
    trip_id CHAR(36) NOT NULL COMMENT '여행 아이디',
    google_place_id VARCHAR(128) NOT NULL COMMENT '구글 장소 아이디',
    name VARCHAR(200) NOT NULL COMMENT '장소명',
    localized_name VARCHAR(200) COMMENT '현지명',
    primary_type VARCHAR(100) COMMENT '대표 유형',
    secondary_text VARCHAR(255) COMMENT '보조 설명',
    address VARCHAR(255) COMMENT '주소',
    latitude DECIMAL(10,7) NOT NULL COMMENT '위도',
    longitude DECIMAL(10,7) NOT NULL COMMENT '경도',
    rating DECIMAL(2,1) COMMENT '평점',
    review_count INT COMMENT '리뷰 수',
    saved_count INT COMMENT '저장 수',
    is_reservable BOOLEAN NOT NULL COMMENT '예약 가능 여부',
    is_open_now BOOLEAN COMMENT '현재 영업 여부',
    opening_hours_text TEXT COMMENT '영업시간 텍스트',
    google_maps_url TEXT COMMENT '구글맵 URL',
    photo_url TEXT COMMENT '대표 사진 URL',
    metadata LONGTEXT NOT NULL COMMENT '확장 메타데이터',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_trip_place_snapshots PRIMARY KEY (id),
    CONSTRAINT fk_trip_place_snapshots_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행 장소 스냅샷';

CREATE TABLE schedule_items (
    id CHAR(36) NOT NULL COMMENT '아이디',
    trip_id CHAR(36) NOT NULL COMMENT '여행 아이디',
    trip_day_id CHAR(36) NOT NULL COMMENT '여행 일차 아이디',
    item_type VARCHAR(20) NOT NULL COMMENT '일정 유형',
    trip_place_snapshot_id CHAR(36) COMMENT '장소 스냅샷 아이디',
    created_from_ocr_candidate_id CHAR(36) COMMENT 'OCR 후보 아이디',
    title VARCHAR(200) COMMENT '제목',
    memo_content TEXT COMMENT '메모 내용',
    scheduled_time TIME COMMENT '예정 시간',
    sort_order INT NOT NULL COMMENT '정렬 순서',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_schedule_items PRIMARY KEY (id),
    CONSTRAINT fk_schedule_items_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    CONSTRAINT fk_schedule_items_trip_day FOREIGN KEY (trip_day_id) REFERENCES trip_days(id),
    CONSTRAINT fk_schedule_items_place FOREIGN KEY (trip_place_snapshot_id) REFERENCES trip_place_snapshots(id),
    CONSTRAINT fk_schedule_items_ocr_candidate FOREIGN KEY (created_from_ocr_candidate_id) REFERENCES ocr_candidates(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일정 아이템';

CREATE TABLE trip_route_segments (
    id CHAR(36) NOT NULL COMMENT '아이디',
    trip_day_id CHAR(36) NOT NULL COMMENT '여행 일차 아이디',
    from_schedule_item_id CHAR(36) NOT NULL COMMENT '출발 일정 아이디',
    to_schedule_item_id CHAR(36) NOT NULL COMMENT '도착 일정 아이디',
    sort_order INT NOT NULL COMMENT '정렬 순서',
    distance_meters INT NOT NULL COMMENT '거리(미터)',
    walk_minutes INT COMMENT '도보 시간(분)',
    drive_minutes INT COMMENT '차량 시간(분)',
    polyline TEXT COMMENT '경로 폴리라인',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_trip_route_segments PRIMARY KEY (id),
    CONSTRAINT fk_trip_route_segments_trip_day FOREIGN KEY (trip_day_id) REFERENCES trip_days(id),
    CONSTRAINT fk_trip_route_segments_from_item FOREIGN KEY (from_schedule_item_id) REFERENCES schedule_items(id),
    CONSTRAINT fk_trip_route_segments_to_item FOREIGN KEY (to_schedule_item_id) REFERENCES schedule_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일정 이동 구간';

CREATE TABLE trip_expense_categories (
    id CHAR(36) NOT NULL COMMENT '아이디',
    trip_id CHAR(36) NOT NULL COMMENT '여행 아이디',
    name VARCHAR(50) NOT NULL COMMENT '상위 카테고리명',
    description VARCHAR(255) COMMENT '설명',
    color_hex CHAR(7) COMMENT '색상 코드',
    is_default BOOLEAN NOT NULL COMMENT '기본 여부',
    sort_order INT NOT NULL COMMENT '정렬 순서',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_trip_expense_categories PRIMARY KEY (id),
    CONSTRAINT fk_trip_expense_categories_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상위 지출 카테고리';

CREATE TABLE expenses (
    id CHAR(36) NOT NULL COMMENT '아이디',
    trip_id CHAR(36) NOT NULL COMMENT '여행 아이디',
    schedule_item_id CHAR(36) COMMENT '일정 아이템 아이디',
    trip_place_snapshot_id CHAR(36) COMMENT '장소 스냅샷 아이디',
    top_category_id CHAR(36) NOT NULL COMMENT '상위 카테고리 아이디',
    flow_type VARCHAR(20) NOT NULL COMMENT '지출 흐름 유형',
    title VARCHAR(200) NOT NULL COMMENT '항목명',
    expense_date DATE NOT NULL COMMENT '지출 날짜',
    expense_time TIME COMMENT '지출 시간',
    local_currency_code CHAR(3) NOT NULL COMMENT '현지 통화 코드',
    amount_local DECIMAL(14,2) NOT NULL COMMENT '현지 통화 금액',
    amount_krw DECIMAL(14,2) NOT NULL COMMENT '원화 금액',
    exchange_rate_to_krw DECIMAL(14,6) NOT NULL COMMENT '원화 환산 환율',
    payment_method VARCHAR(30) NOT NULL COMMENT '결제수단',
    note TEXT COMMENT '메모',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    CONSTRAINT pk_expenses PRIMARY KEY (id),
    CONSTRAINT fk_expenses_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    CONSTRAINT fk_expenses_schedule_item FOREIGN KEY (schedule_item_id) REFERENCES schedule_items(id),
    CONSTRAINT fk_expenses_trip_place_snapshot FOREIGN KEY (trip_place_snapshot_id) REFERENCES trip_place_snapshots(id),
    CONSTRAINT fk_expenses_top_category FOREIGN KEY (top_category_id) REFERENCES trip_expense_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='지출';

CREATE TABLE expense_photos (
    id CHAR(36) NOT NULL COMMENT '아이디',
    expense_id CHAR(36) NOT NULL COMMENT '지출 아이디',
    image_url TEXT NOT NULL COMMENT '사진 URL',
    sort_order INT NOT NULL COMMENT '정렬 순서',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    CONSTRAINT pk_expense_photos PRIMARY KEY (id),
    CONSTRAINT fk_expense_photos_expense FOREIGN KEY (expense_id) REFERENCES expenses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='지출 사진';

CREATE TABLE checklist_sections (
    id CHAR(36) NOT NULL COMMENT '아이디',
    trip_id CHAR(36) NOT NULL COMMENT '여행 아이디',
    name VARCHAR(100) NOT NULL COMMENT '섹션명',
    sort_order INT NOT NULL COMMENT '정렬 순서',
    is_collapsed BOOLEAN NOT NULL COMMENT '접힘 여부',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_checklist_sections PRIMARY KEY (id),
    CONSTRAINT fk_checklist_sections_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='준비물 섹션';

CREATE TABLE checklist_items (
    id CHAR(36) NOT NULL COMMENT '아이디',
    section_id CHAR(36) NOT NULL COMMENT '섹션 아이디',
    title VARCHAR(100) NOT NULL COMMENT '아이템명',
    note VARCHAR(255) COMMENT '메모',
    is_checked BOOLEAN NOT NULL COMMENT '체크 여부',
    checked_at DATETIME COMMENT '체크 일시',
    sort_order INT NOT NULL COMMENT '정렬 순서',
    created_at DATETIME NOT NULL COMMENT '생성일시',
    updated_at DATETIME NOT NULL COMMENT '수정일시',
    CONSTRAINT pk_checklist_items PRIMARY KEY (id),
    CONSTRAINT fk_checklist_items_section FOREIGN KEY (section_id) REFERENCES checklist_sections(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='준비물 아이템';

CREATE TABLE trip_weather_daily (
    trip_id CHAR(36) NOT NULL COMMENT '여행 아이디',
    weather_date DATE NOT NULL COMMENT '날씨 날짜',
    country_name VARCHAR(100) NOT NULL COMMENT '국가명',
    representative_city_name VARCHAR(100) NOT NULL COMMENT '대표 도시명',
    time_zone VARCHAR(60) NOT NULL COMMENT '타임존',
    utc_offset_minutes INT NOT NULL COMMENT 'UTC 오프셋(분)',
    summary VARCHAR(50) NOT NULL COMMENT '날씨 요약',
    icon_code VARCHAR(30) NOT NULL COMMENT '날씨 아이콘 코드',
    min_temp DECIMAL(5,2) COMMENT '최저기온',
    max_temp DECIMAL(5,2) COMMENT '최고기온',
    source VARCHAR(50) COMMENT '데이터 소스',
    fetched_at DATETIME NOT NULL COMMENT '조회일시',
    CONSTRAINT pk_trip_weather_daily PRIMARY KEY (trip_id, weather_date),
    CONSTRAINT fk_trip_weather_daily_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행 일별 날씨';

CREATE TABLE exchange_rates (
    rate_date DATE NOT NULL COMMENT '환율 기준일',
    currency_code CHAR(3) NOT NULL COMMENT '통화 코드',
    country_name VARCHAR(100) COMMENT '국가명',
    rate_to_krw DECIMAL(14,6) NOT NULL COMMENT '원화 환율',
    previous_business_date DATE COMMENT '전 영업일',
    change_amount DECIMAL(14,6) COMMENT '변동 금액',
    change_percent DECIMAL(10,4) COMMENT '변동 비율',
    source VARCHAR(50) COMMENT '데이터 소스',
    fetched_at DATETIME NOT NULL COMMENT '조회일시',
    CONSTRAINT pk_exchange_rates PRIMARY KEY (rate_date, currency_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='환율';
