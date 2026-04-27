-- Tripline ERDCloud용 MySQL 단순화 SQL
-- 목적: ERDCloud에서 테이블/관계도를 안정적으로 그리기 위한 스키마
-- 주의: 이 파일은 실제 MySQL 실행용이 아니라 ERD 시각화용이다.
-- 차이점:
-- 1. enum 제거 -> varchar
-- 2. default / on update / check 제거
-- 3. 복합 FK 제거
-- 4. json -> text
-- 5. index / unique(일부) 문 최소화

create table users (
    id varchar(36) primary key,
    email varchar(255) not null,
    password_hash varchar(255) not null,
    nickname varchar(50) not null,
    is_active boolean not null,
    last_login_at datetime,
    created_at datetime,
    updated_at datetime
);

create table user_settings (
    user_id varchar(36) primary key,
    base_currency_code varchar(3) not null,
    default_pdf_scope varchar(30) not null,
    default_route_transport_mode varchar(30) not null,
    created_at datetime,
    updated_at datetime,
    foreign key (user_id) references users(id)
);

create table auth_refresh_tokens (
    id varchar(36) primary key,
    user_id varchar(36) not null,
    refresh_token_hash varchar(255) not null,
    user_agent text,
    expires_at datetime,
    revoked_at datetime,
    created_at datetime,
    foreign key (user_id) references users(id)
);

create table trips (
    id varchar(36) primary key,
    user_id varchar(36) not null,
    title varchar(100) not null,
    country_code varchar(2) not null,
    country_name varchar(100) not null,
    city_name varchar(100) not null,
    start_date date not null,
    end_date date not null,
    base_currency_code varchar(3) not null,
    one_line_description varchar(255),
    status varchar(30) not null,
    created_at datetime,
    updated_at datetime,
    deleted_at datetime,
    foreign key (user_id) references users(id)
);

create table user_current_trip_selections (
    user_id varchar(36) primary key,
    trip_id varchar(36) not null,
    created_at datetime,
    updated_at datetime,
    foreign key (user_id) references users(id),
    foreign key (trip_id) references trips(id)
);

create table trip_days (
    id varchar(36) primary key,
    trip_id varchar(36) not null,
    day_index integer not null,
    calendar_date date not null,
    title varchar(100),
    sort_order integer not null,
    created_at datetime,
    updated_at datetime,
    foreign key (trip_id) references trips(id)
);

create table ocr_imports (
    id varchar(36) primary key,
    trip_id varchar(36) not null,
    source_type varchar(20) not null,
    source_image_url text,
    status varchar(30) not null,
    raw_text text,
    created_at datetime,
    updated_at datetime,
    confirmed_at datetime,
    foreign key (trip_id) references trips(id)
);

create table ocr_candidates (
    id varchar(36) primary key,
    ocr_import_id varchar(36) not null,
    trip_day_id varchar(36),
    candidate_type varchar(30) not null,
    status varchar(30) not null,
    target_date date,
    target_time time,
    place_name varchar(200),
    hotel_name varchar(200),
    flight_number varchar(30),
    memo_content text,
    source_text text,
    edited_payload text,
    sort_order integer not null,
    created_at datetime,
    updated_at datetime,
    foreign key (ocr_import_id) references ocr_imports(id),
    foreign key (trip_day_id) references trip_days(id)
);

create table trip_place_snapshots (
    id varchar(36) primary key,
    trip_id varchar(36) not null,
    google_place_id varchar(128) not null,
    name varchar(200) not null,
    localized_name varchar(200),
    primary_type varchar(100),
    secondary_text varchar(255),
    address varchar(255),
    latitude decimal(10,7) not null,
    longitude decimal(10,7) not null,
    rating decimal(2,1),
    review_count integer,
    saved_count integer,
    is_reservable boolean not null,
    is_open_now boolean,
    opening_hours_text text,
    google_maps_url text,
    photo_url text,
    metadata text,
    created_at datetime,
    updated_at datetime,
    foreign key (trip_id) references trips(id)
);

create table schedule_items (
    id varchar(36) primary key,
    trip_id varchar(36) not null,
    trip_day_id varchar(36) not null,
    item_type varchar(20) not null,
    trip_place_snapshot_id varchar(36),
    created_from_ocr_candidate_id varchar(36),
    title varchar(200),
    memo_content text,
    scheduled_time time,
    sort_order integer not null,
    created_at datetime,
    updated_at datetime,
    foreign key (trip_id) references trips(id),
    foreign key (trip_day_id) references trip_days(id),
    foreign key (trip_place_snapshot_id) references trip_place_snapshots(id),
    foreign key (created_from_ocr_candidate_id) references ocr_candidates(id)
);

create table trip_route_segments (
    id varchar(36) primary key,
    trip_day_id varchar(36) not null,
    from_schedule_item_id varchar(36) not null,
    to_schedule_item_id varchar(36) not null,
    sort_order integer not null,
    distance_meters integer not null,
    walk_minutes integer,
    drive_minutes integer,
    polyline text,
    created_at datetime,
    updated_at datetime,
    foreign key (trip_day_id) references trip_days(id),
    foreign key (from_schedule_item_id) references schedule_items(id),
    foreign key (to_schedule_item_id) references schedule_items(id)
);

create table trip_expense_categories (
    id varchar(36) primary key,
    trip_id varchar(36) not null,
    name varchar(50) not null,
    description varchar(255),
    color_hex varchar(7),
    is_default boolean not null,
    sort_order integer not null,
    created_at datetime,
    updated_at datetime,
    foreign key (trip_id) references trips(id)
);

create table expenses (
    id varchar(36) primary key,
    trip_id varchar(36) not null,
    schedule_item_id varchar(36),
    trip_place_snapshot_id varchar(36),
    top_category_id varchar(36) not null,
    flow_type varchar(20) not null,
    title varchar(200) not null,
    expense_date date not null,
    expense_time time,
    local_currency_code varchar(3) not null,
    amount_local decimal(14,2) not null,
    amount_krw decimal(14,2) not null,
    exchange_rate_to_krw decimal(14,6) not null,
    payment_method varchar(30) not null,
    note text,
    created_at datetime,
    updated_at datetime,
    deleted_at datetime,
    foreign key (trip_id) references trips(id),
    foreign key (schedule_item_id) references schedule_items(id),
    foreign key (trip_place_snapshot_id) references trip_place_snapshots(id),
    foreign key (top_category_id) references trip_expense_categories(id)
);

create table expense_photos (
    id varchar(36) primary key,
    expense_id varchar(36) not null,
    image_url text not null,
    sort_order integer not null,
    created_at datetime,
    foreign key (expense_id) references expenses(id)
);

create table checklist_sections (
    id varchar(36) primary key,
    trip_id varchar(36) not null,
    name varchar(100) not null,
    sort_order integer not null,
    is_collapsed boolean not null,
    created_at datetime,
    updated_at datetime,
    foreign key (trip_id) references trips(id)
);

create table checklist_items (
    id varchar(36) primary key,
    section_id varchar(36) not null,
    title varchar(100) not null,
    note varchar(255),
    is_checked boolean not null,
    checked_at datetime,
    sort_order integer not null,
    created_at datetime,
    updated_at datetime,
    foreign key (section_id) references checklist_sections(id)
);

create table trip_weather_daily (
    trip_id varchar(36) not null,
    weather_date date not null,
    country_name varchar(100) not null,
    representative_city_name varchar(100) not null,
    time_zone varchar(60) not null,
    utc_offset_minutes integer not null,
    summary varchar(50) not null,
    icon_code varchar(30) not null,
    min_temp decimal(5,2),
    max_temp decimal(5,2),
    source varchar(50),
    fetched_at datetime,
    primary key (trip_id, weather_date),
    foreign key (trip_id) references trips(id)
);

create table exchange_rates (
    rate_date date not null,
    currency_code varchar(3) not null,
    country_name varchar(100),
    rate_to_krw decimal(14,6) not null,
    previous_business_date date,
    change_amount decimal(14,6),
    change_percent decimal(10,4),
    source varchar(50),
    fetched_at datetime,
    primary key (rate_date, currency_code)
);
