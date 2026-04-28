-- Tripline ERD / MySQL 8 DDL
-- 작성일: 2026-04-28
-- 기준 문서:
--   - Tripline_기획서.md
--   - Tripline_기능명세서.md
--   - Tripline_API_명세서.md
--
-- 참고:
-- 1. UUID는 MySQL default function 대신 애플리케이션에서 생성하는 것을 권장한다.
-- 2. 모든 테이블은 utf8mb4 / InnoDB 기준으로 생성한다.
-- 3. trips.status 는 사용자가 직접 수정하지 않고 서버가 날짜 기준으로 계산/저장/동기화한다.
-- 4. expenses / expense_photos / trip_expense_categories / checklist_sections / checklist_items 는 deleted_at 기반 soft delete를 사용한다.

SET NAMES utf8mb4;

create table users (
    id char(36) not null,
    email varchar(255) not null,
    password_hash varchar(255) not null,
    nickname varchar(50) not null,
    is_active boolean not null default true,
    last_login_at datetime(6) null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    deleted_at datetime(6) null,
    primary key (id),
    unique key uk_users_email (email)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table user_settings (
    user_id char(36) not null,
    base_currency_code char(3) not null default 'KRW',
    default_pdf_scope enum('trip_all', 'selected_day', 'today') not null default 'trip_all',
    default_route_transport_mode enum('walk', 'drive', 'mixed') not null default 'mixed',
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (user_id),
    constraint fk_user_settings_user
        foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table auth_refresh_tokens (
    id char(36) not null,
    user_id char(36) not null,
    refresh_token_hash varchar(255) not null,
    user_agent text null,
    expires_at datetime(6) not null,
    revoked_at datetime(6) null,
    created_at datetime(6) not null default current_timestamp(6),
    primary key (id),
    unique key uk_auth_refresh_tokens_refresh_token_hash (refresh_token_hash),
    key ix_auth_refresh_tokens_user_id (user_id),
    key ix_auth_refresh_tokens_expires_at (expires_at),
    constraint fk_auth_refresh_tokens_user
        foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table trips (
    id char(36) not null,
    user_id char(36) not null,
    title varchar(100) not null,
    country_code char(2) not null,
    country_name varchar(100) not null,
    city_name varchar(100) not null,
    start_date date not null,
    end_date date not null,
    base_currency_code char(3) not null,
    one_line_description varchar(255) null,
    status enum('planned', 'active', 'completed') not null default 'planned',
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    deleted_at datetime(6) null,
    primary key (id),
    unique key uk_trips_user_id_id (user_id, id),
    key ix_trips_user_id_status_start_date (user_id, status, start_date),
    constraint fk_trips_user
        foreign key (user_id) references users(id) on delete cascade,
    constraint chk_trip_date_range check (start_date <= end_date)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table user_current_trip_selections (
    user_id char(36) not null,
    trip_id char(36) not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (user_id),
    key ix_user_current_trip_selections_trip_id (trip_id),
    constraint fk_user_current_trip_selections_user
        foreign key (user_id) references users(id) on delete cascade,
    constraint fk_user_current_trip_selections_trip
        foreign key (trip_id) references trips(id) on delete cascade,
    constraint fk_user_current_trip_selection_trip_owner
        foreign key (user_id, trip_id) references trips(user_id, id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table trip_days (
    id char(36) not null,
    trip_id char(36) not null,
    day_index int not null,
    calendar_date date not null,
    title varchar(100) null,
    sort_order int not null default 0,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_trip_days_id_trip_id (id, trip_id),
    unique key uk_trip_days_trip_id_day_index (trip_id, day_index),
    unique key uk_trip_days_trip_id_calendar_date (trip_id, calendar_date),
    key ix_trip_days_trip_id_calendar_date (trip_id, calendar_date),
    constraint fk_trip_days_trip
        foreign key (trip_id) references trips(id) on delete cascade,
    constraint chk_trip_day_index check (day_index > 0)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table ocr_imports (
    id char(36) not null,
    trip_id char(36) not null,
    source_type varchar(20) not null,
    source_image_url text null,
    status enum('uploaded', 'parsed', 'reviewing', 'confirmed', 'cancelled', 'failed') not null default 'uploaded',
    raw_text longtext null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    confirmed_at datetime(6) null,
    primary key (id),
    key ix_ocr_imports_trip_id_status (trip_id, status),
    constraint fk_ocr_imports_trip
        foreign key (trip_id) references trips(id) on delete cascade,
    constraint chk_ocr_import_source_type check (source_type in ('camera', 'gallery'))
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table ocr_candidates (
    id char(36) not null,
    ocr_import_id char(36) not null,
    trip_day_id char(36) null,
    candidate_type enum('flight', 'hotel', 'place', 'memo', 'transport', 'other') not null,
    status enum('pending', 'accepted', 'rejected', 'edited') not null default 'pending',
    target_date date null,
    target_time time null,
    place_name varchar(200) null,
    hotel_name varchar(200) null,
    flight_number varchar(30) null,
    memo_content text null,
    source_text longtext null,
    edited_payload json not null,
    sort_order int not null default 0,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    key ix_ocr_candidates_import_id_status (ocr_import_id, status),
    constraint fk_ocr_candidates_import
        foreign key (ocr_import_id) references ocr_imports(id) on delete cascade,
    constraint fk_ocr_candidates_trip_day
        foreign key (trip_day_id) references trip_days(id) on delete set null
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table trip_place_snapshots (
    id char(36) not null,
    trip_id char(36) not null,
    google_place_id varchar(128) not null,
    name varchar(200) not null,
    localized_name varchar(200) null,
    primary_type varchar(100) null,
    secondary_text varchar(255) null,
    address varchar(255) null,
    latitude decimal(10,7) not null,
    longitude decimal(10,7) not null,
    rating decimal(2,1) null,
    review_count int null,
    saved_count int null,
    is_reservable boolean not null default false,
    is_open_now boolean null,
    opening_hours_text text null,
    google_maps_url text null,
    photo_url text null,
    metadata json not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_trip_place_snapshots_id_trip_id (id, trip_id),
    unique key uk_trip_place_snapshots_trip_id_google_place_id (trip_id, google_place_id),
    key ix_trip_place_snapshots_trip_id_name (trip_id, name),
    constraint fk_trip_place_snapshots_trip
        foreign key (trip_id) references trips(id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table schedule_items (
    id char(36) not null,
    trip_id char(36) not null,
    trip_day_id char(36) not null,
    item_type enum('place', 'memo') not null,
    trip_place_snapshot_id char(36) null,
    created_from_ocr_candidate_id char(36) null,
    title varchar(200) null,
    memo_content text null,
    scheduled_time time null,
    sort_order int not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_schedule_items_id_trip_id (id, trip_id),
    unique key uk_schedule_items_id_trip_day_id (id, trip_day_id),
    key ix_schedule_items_day_sort_order (trip_day_id, sort_order),
    constraint fk_schedule_items_trip
        foreign key (trip_id) references trips(id) on delete cascade,
    constraint fk_schedule_items_trip_day
        foreign key (trip_day_id, trip_id) references trip_days(id, trip_id) on delete cascade,
    constraint fk_schedule_items_trip_place_snapshot
        foreign key (trip_place_snapshot_id, trip_id) references trip_place_snapshots(id, trip_id),
    constraint fk_schedule_items_ocr_candidate
        foreign key (created_from_ocr_candidate_id) references ocr_candidates(id) on delete set null,
    constraint chk_schedule_item_shape check (
        (item_type = 'place' and trip_place_snapshot_id is not null)
        or
        (item_type = 'memo' and trip_place_snapshot_id is null and memo_content is not null)
    )
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table trip_route_segments (
    id char(36) not null,
    trip_day_id char(36) not null,
    from_schedule_item_id char(36) not null,
    to_schedule_item_id char(36) not null,
    sort_order int not null default 0,
    distance_meters int not null default 0,
    walk_minutes int null,
    drive_minutes int null,
    polyline text null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (id),
    unique key uk_trip_route_segments_from_to (from_schedule_item_id, to_schedule_item_id),
    key ix_trip_route_segments_day_sort_order (trip_day_id, sort_order),
    constraint fk_trip_route_segments_trip_day
        foreign key (trip_day_id) references trip_days(id) on delete cascade,
    constraint fk_trip_route_segments_from_item
        foreign key (from_schedule_item_id, trip_day_id) references schedule_items(id, trip_day_id) on delete cascade,
    constraint fk_trip_route_segments_to_item
        foreign key (to_schedule_item_id, trip_day_id) references schedule_items(id, trip_day_id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table trip_expense_categories (
    id char(36) not null,
    trip_id char(36) not null,
    name varchar(50) not null,
    description varchar(255) null,
    color_hex char(7) null,
    is_default boolean not null default false,
    sort_order int not null default 0,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    deleted_at datetime(6) null,
    primary key (id),
    unique key uk_trip_expense_categories_id_trip_id (id, trip_id),
    unique key uk_trip_expense_categories_trip_id_name (trip_id, name),
    key ix_trip_expense_categories_trip_id_deleted_at_sort_order (trip_id, deleted_at, sort_order),
    constraint fk_trip_expense_categories_trip
        foreign key (trip_id) references trips(id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table expenses (
    id char(36) not null,
    trip_id char(36) not null,
    schedule_item_id char(36) null,
    trip_place_snapshot_id char(36) null,
    top_category_id char(36) not null,
    flow_type enum('expense', 'refund', 'settlement') not null default 'expense',
    title varchar(200) not null,
    expense_date date not null,
    expense_time time null,
    local_currency_code char(3) not null,
    amount_local decimal(14,2) not null,
    amount_krw decimal(14,2) not null,
    exchange_rate_to_krw decimal(14,6) not null,
    payment_method enum('cash', 'card', 'wallet', 'bank_transfer', 'other') not null,
    note text null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    deleted_at datetime(6) null,
    primary key (id),
    key ix_expenses_trip_id_deleted_at_expense_date (trip_id, deleted_at, expense_date),
    key ix_expenses_trip_place_snapshot_id (trip_place_snapshot_id),
    key ix_expenses_top_category_id (top_category_id),
    constraint fk_expenses_trip
        foreign key (trip_id) references trips(id) on delete cascade,
    constraint fk_expenses_schedule_item
        foreign key (schedule_item_id, trip_id) references schedule_items(id, trip_id),
    constraint fk_expenses_trip_place_snapshot
        foreign key (trip_place_snapshot_id, trip_id) references trip_place_snapshots(id, trip_id),
    constraint fk_expenses_top_category
        foreign key (top_category_id, trip_id) references trip_expense_categories(id, trip_id),
    constraint chk_expense_amounts check (
        amount_local >= 0
        and amount_krw >= 0
        and exchange_rate_to_krw > 0
    )
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table expense_photos (
    id char(36) not null,
    expense_id char(36) not null,
    image_url text not null,
    sort_order int not null default 0,
    created_at datetime(6) not null default current_timestamp(6),
    deleted_at datetime(6) null,
    primary key (id),
    unique key uk_expense_photos_expense_id_sort_order (expense_id, sort_order),
    key ix_expense_photos_expense_id_deleted_at_sort_order (expense_id, deleted_at, sort_order),
    constraint fk_expense_photos_expense
        foreign key (expense_id) references expenses(id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table checklist_sections (
    id char(36) not null,
    trip_id char(36) not null,
    name varchar(100) not null,
    sort_order int not null default 0,
    is_collapsed boolean not null default false,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    deleted_at datetime(6) null,
    primary key (id),
    key ix_checklist_sections_trip_id_deleted_at_sort_order (trip_id, deleted_at, sort_order),
    constraint fk_checklist_sections_trip
        foreign key (trip_id) references trips(id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table checklist_items (
    id char(36) not null,
    section_id char(36) not null,
    title varchar(100) not null,
    note varchar(255) null,
    is_checked boolean not null default false,
    checked_at datetime(6) null,
    sort_order int not null default 0,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    deleted_at datetime(6) null,
    primary key (id),
    key ix_checklist_items_section_id_deleted_at_sort_order (section_id, deleted_at, sort_order),
    constraint fk_checklist_items_section
        foreign key (section_id) references checklist_sections(id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table trip_weather_daily (
    trip_id char(36) not null,
    weather_date date not null,
    country_name varchar(100) not null,
    representative_city_name varchar(100) not null,
    time_zone varchar(60) not null,
    utc_offset_minutes int not null,
    summary varchar(50) not null,
    icon_code varchar(30) not null,
    min_temp decimal(5,2) null,
    max_temp decimal(5,2) null,
    source varchar(50) null,
    fetched_at datetime(6) not null default current_timestamp(6),
    primary key (trip_id, weather_date),
    constraint fk_trip_weather_daily_trip
        foreign key (trip_id) references trips(id) on delete cascade
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table exchange_rates (
    rate_date date not null,
    currency_code char(3) not null,
    country_name varchar(100) null,
    rate_to_krw decimal(14,6) not null,
    previous_business_date date null,
    change_amount decimal(14,6) null,
    change_percent decimal(10,4) null,
    source varchar(50) null,
    fetched_at datetime(6) not null default current_timestamp(6),
    primary key (rate_date, currency_code)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- 비고
-- 1. 일정 화면의 장소 번호는 별도 컬럼이 아니라 day 내 sort_order 및 place item 순번으로 계산한다.
-- 2. 메모는 schedule_items.item_type = 'memo'로 관리하며 숫자 배지를 갖지 않는다.
-- 3. 지출은 amount_local / amount_krw / exchange_rate_to_krw를 동시에 저장해 여행 전용 캘린더와 전역 캘린더에서 바로 활용한다.
-- 4. 상위 카테고리의 기본값 '기타'는 서비스 로직에서 여행 생성 시 자동 생성하는 것을 전제로 한다.
-- 5. 날씨 / 환율은 외부 API 결과를 캐시하는 용도로 테이블을 두었고, 캐시 미사용 시 생략 가능하다.
-- 6. 현재 선택된 여행은 user_current_trip_selections로 관리하며, 여행 삭제 시 함께 제거된다.
