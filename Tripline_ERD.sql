-- Tripline ERD / PostgreSQL DDL
-- 작성일: 2026-04-27
-- 기준 문서:
--   - Tripline_기획서.md
--   - Tripline_기능명세서.md
--   - Tripline_API_명세서.md

create extension if not exists pgcrypto;

create type trip_status_enum as enum ('planned', 'active', 'completed');
create type schedule_item_type_enum as enum ('place', 'memo');
create type payment_method_enum as enum ('cash', 'card', 'wallet', 'bank_transfer', 'other');
create type expense_flow_type_enum as enum ('expense', 'refund', 'settlement');
create type pdf_scope_enum as enum ('trip_all', 'selected_day', 'today');
create type route_transport_mode_enum as enum ('walk', 'drive', 'mixed');
create type ocr_import_status_enum as enum ('uploaded', 'parsed', 'reviewing', 'confirmed', 'cancelled', 'failed');
create type ocr_candidate_type_enum as enum ('flight', 'hotel', 'place', 'memo', 'transport', 'other');
create type ocr_candidate_status_enum as enum ('pending', 'accepted', 'rejected', 'edited');

create table users (
    id uuid primary key default gen_random_uuid(),
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    nickname varchar(50) not null,
    is_active boolean not null default true,
    last_login_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table user_settings (
    user_id uuid primary key references users(id) on delete cascade,
    base_currency_code char(3) not null default 'KRW',
    default_pdf_scope pdf_scope_enum not null default 'trip_all',
    default_route_transport_mode route_transport_mode_enum not null default 'mixed',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table auth_refresh_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    refresh_token_hash varchar(255) not null,
    user_agent text,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null default now()
);

create table trips (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    title varchar(100) not null,
    country_code char(2) not null,
    country_name varchar(100) not null,
    city_name varchar(100) not null,
    start_date date not null,
    end_date date not null,
    base_currency_code char(3) not null,
    one_line_description varchar(255),
    status trip_status_enum not null default 'planned',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz,
    constraint chk_trip_date_range check (start_date <= end_date),
    unique (user_id, id)
);

create table user_current_trip_selections (
    user_id uuid primary key references users(id) on delete cascade,
    trip_id uuid not null references trips(id) on delete cascade,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint fk_user_current_trip_selection_trip_owner
        foreign key (user_id, trip_id) references trips(user_id, id)
);

create table trip_days (
    id uuid primary key default gen_random_uuid(),
    trip_id uuid not null references trips(id) on delete cascade,
    day_index integer not null,
    calendar_date date not null,
    title varchar(100),
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_trip_day_index check (day_index > 0),
    unique (id, trip_id),
    unique (trip_id, day_index),
    unique (trip_id, calendar_date)
);

create table ocr_imports (
    id uuid primary key default gen_random_uuid(),
    trip_id uuid not null references trips(id) on delete cascade,
    source_type varchar(20) not null check (source_type in ('camera', 'gallery')),
    source_image_url text,
    status ocr_import_status_enum not null default 'uploaded',
    raw_text text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    confirmed_at timestamptz
);

create table ocr_candidates (
    id uuid primary key default gen_random_uuid(),
    ocr_import_id uuid not null references ocr_imports(id) on delete cascade,
    trip_day_id uuid references trip_days(id) on delete set null,
    candidate_type ocr_candidate_type_enum not null,
    status ocr_candidate_status_enum not null default 'pending',
    target_date date,
    target_time time,
    place_name varchar(200),
    hotel_name varchar(200),
    flight_number varchar(30),
    memo_content text,
    source_text text,
    edited_payload jsonb not null default '{}'::jsonb,
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table trip_place_snapshots (
    id uuid primary key default gen_random_uuid(),
    trip_id uuid not null references trips(id) on delete cascade,
    google_place_id varchar(128) not null,
    name varchar(200) not null,
    localized_name varchar(200),
    primary_type varchar(100),
    secondary_text varchar(255),
    address varchar(255),
    latitude numeric(10, 7) not null,
    longitude numeric(10, 7) not null,
    rating numeric(2, 1),
    review_count integer,
    saved_count integer,
    is_reservable boolean not null default false,
    is_open_now boolean,
    opening_hours_text text,
    google_maps_url text,
    photo_url text,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (id, trip_id),
    unique (trip_id, google_place_id)
);

create table schedule_items (
    id uuid primary key default gen_random_uuid(),
    trip_id uuid not null references trips(id) on delete cascade,
    trip_day_id uuid not null,
    item_type schedule_item_type_enum not null,
    trip_place_snapshot_id uuid,
    created_from_ocr_candidate_id uuid references ocr_candidates(id) on delete set null,
    title varchar(200),
    memo_content text,
    scheduled_time time,
    sort_order integer not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (id, trip_id),
    unique (id, trip_day_id),
    constraint fk_schedule_items_trip_day
        foreign key (trip_day_id, trip_id) references trip_days(id, trip_id) on delete cascade,
    constraint fk_schedule_items_trip_place_snapshot
        foreign key (trip_place_snapshot_id, trip_id) references trip_place_snapshots(id, trip_id),
    constraint chk_schedule_item_shape check (
        (item_type = 'place' and trip_place_snapshot_id is not null)
        or
        (item_type = 'memo' and trip_place_snapshot_id is null and memo_content is not null)
    )
);

create table trip_route_segments (
    id uuid primary key default gen_random_uuid(),
    trip_day_id uuid not null references trip_days(id) on delete cascade,
    from_schedule_item_id uuid not null,
    to_schedule_item_id uuid not null,
    sort_order integer not null default 0,
    distance_meters integer not null default 0,
    walk_minutes integer,
    drive_minutes integer,
    polyline text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint fk_trip_route_segments_from_item
        foreign key (from_schedule_item_id, trip_day_id) references schedule_items(id, trip_day_id) on delete cascade,
    constraint fk_trip_route_segments_to_item
        foreign key (to_schedule_item_id, trip_day_id) references schedule_items(id, trip_day_id) on delete cascade,
    unique (from_schedule_item_id, to_schedule_item_id)
);

create table trip_expense_categories (
    id uuid primary key default gen_random_uuid(),
    trip_id uuid not null references trips(id) on delete cascade,
    name varchar(50) not null,
    description varchar(255),
    color_hex char(7),
    is_default boolean not null default false,
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (id, trip_id)
);

create unique index ux_trip_expense_categories_trip_lower_name
    on trip_expense_categories (trip_id, lower(name));

create table expenses (
    id uuid primary key default gen_random_uuid(),
    trip_id uuid not null references trips(id) on delete cascade,
    schedule_item_id uuid,
    trip_place_snapshot_id uuid,
    top_category_id uuid not null,
    flow_type expense_flow_type_enum not null default 'expense',
    title varchar(200) not null,
    expense_date date not null,
    expense_time time,
    local_currency_code char(3) not null,
    amount_local numeric(14, 2) not null,
    amount_krw numeric(14, 2) not null,
    exchange_rate_to_krw numeric(14, 6) not null,
    payment_method payment_method_enum not null,
    note text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz,
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
);

create table expense_photos (
    id uuid primary key default gen_random_uuid(),
    expense_id uuid not null references expenses(id) on delete cascade,
    image_url text not null,
    sort_order integer not null default 0,
    created_at timestamptz not null default now()
);

create table checklist_sections (
    id uuid primary key default gen_random_uuid(),
    trip_id uuid not null references trips(id) on delete cascade,
    name varchar(100) not null,
    sort_order integer not null default 0,
    is_collapsed boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table checklist_items (
    id uuid primary key default gen_random_uuid(),
    section_id uuid not null references checklist_sections(id) on delete cascade,
    title varchar(100) not null,
    note varchar(255),
    is_checked boolean not null default false,
    checked_at timestamptz,
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table trip_weather_daily (
    trip_id uuid not null references trips(id) on delete cascade,
    weather_date date not null,
    country_name varchar(100) not null,
    representative_city_name varchar(100) not null,
    time_zone varchar(60) not null,
    utc_offset_minutes integer not null,
    summary varchar(50) not null,
    icon_code varchar(30) not null,
    min_temp numeric(5, 2),
    max_temp numeric(5, 2),
    source varchar(50),
    fetched_at timestamptz not null default now(),
    primary key (trip_id, weather_date)
);

create table exchange_rates (
    rate_date date not null,
    currency_code char(3) not null,
    country_name varchar(100),
    rate_to_krw numeric(14, 6) not null,
    previous_business_date date,
    change_amount numeric(14, 6),
    change_percent numeric(10, 4),
    source varchar(50),
    fetched_at timestamptz not null default now(),
    primary key (rate_date, currency_code)
);

create index ix_auth_refresh_tokens_user_id
    on auth_refresh_tokens (user_id);

create index ix_user_current_trip_selections_trip_id
    on user_current_trip_selections (trip_id);

create index ix_trips_user_id_status_start_date
    on trips (user_id, status, start_date);

create index ix_trip_days_trip_id_calendar_date
    on trip_days (trip_id, calendar_date);

create index ix_ocr_imports_trip_id_status
    on ocr_imports (trip_id, status);

create index ix_ocr_candidates_import_id_status
    on ocr_candidates (ocr_import_id, status);

create index ix_trip_place_snapshots_trip_id_name
    on trip_place_snapshots (trip_id, name);

create index ix_schedule_items_day_sort_order
    on schedule_items (trip_day_id, sort_order);

create index ix_trip_route_segments_day_sort_order
    on trip_route_segments (trip_day_id, sort_order);

create index ix_expenses_trip_id_expense_date
    on expenses (trip_id, expense_date);

create index ix_expenses_trip_place_snapshot_id
    on expenses (trip_place_snapshot_id);

create index ix_expenses_top_category_id
    on expenses (top_category_id);

create index ix_checklist_sections_trip_id_sort_order
    on checklist_sections (trip_id, sort_order);

create index ix_checklist_items_section_id_sort_order
    on checklist_items (section_id, sort_order);

-- 비고
-- 1. 일정 화면의 장소 번호는 별도 컬럼이 아니라 day 내 sort_order 및 place item 순번으로 계산한다.
-- 2. 메모는 schedule_items.item_type = 'memo'로 관리하며 숫자 배지를 갖지 않는다.
-- 3. 지출은 amount_local / amount_krw를 동시에 저장해 여행 전용 캘린더와 전역 캘린더에서 바로 활용한다.
-- 4. 상위 카테고리의 기본값 '기타'는 서비스 로직에서 여행 생성 시 자동 생성하는 것을 전제로 한다.
-- 5. 날씨 / 환율은 외부 API 결과를 캐시하는 용도로 테이블을 두었고, 캐시 미사용 시 생략 가능하다.
-- 6. 현재 선택된 여행은 user_current_trip_selections로 관리하며, 여행 삭제 시 함께 제거된다.
-- 7. trips.status는 사용자가 직접 수정하지 않고 서버가 날짜 기준으로 계산/저장/동기화한다.
