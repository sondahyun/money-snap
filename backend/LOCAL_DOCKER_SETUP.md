# Backend Local Docker Setup

로컬에서 MySQL, Redis를 Docker로 띄우고 Spring Boot 백엔드를 연결하는 방법입니다.

## 1. 기본 사항

- Docker Desktop이 실행 중이어야 합니다.
- 작업 위치는 [backend](/Users/sondahyun/money-snap/backend) 입니다.
- 기본 프로필은 이미 `local`로 설정되어 있습니다.
- `.env.example`을 복사해 `.env`를 만들어야 Docker와 앱이 같은 값을 읽습니다.

```bash
cd /Users/sondahyun/money-snap/backend
cp .env.example .env
```

## 2. `.env` 파일 준비

먼저 `.env.example`을 복사해 `.env`를 만듭니다.

```bash
cd /Users/sondahyun/money-snap/backend
cp .env.example .env
```

복사한 뒤 `.env` 값은 로컬에서 사용할 실제 값으로 바꿔주세요.

기본 예시는 아래와 같습니다.

- MySQL host: `127.0.0.1`
- MySQL port: `3306`
- DB name: `.env`의 `MYSQL_DATABASE`
- DB user: `.env`의 `MYSQL_USER`
- DB password: `.env`의 `MYSQL_PASSWORD`
- Redis host: `127.0.0.1`
- Redis port: `6379`
- Redis password: `.env`의 `REDIS_PASSWORD`

## 3. Docker 실행

`.env`를 만든 뒤 아래 명령으로 실행합니다.

```bash
cd /Users/sondahyun/money-snap/backend
docker compose -f docker-compose.local.yml up -d
```

Docker Compose는 같은 디렉터리의 `.env`를 읽고, MySQL/Redis 컨테이너에 같은 값을 전달합니다.

## 4. 컨테이너 상태 확인

```bash
docker compose -f docker-compose.local.yml ps
```

로그를 확인하고 싶으면:

```bash
docker compose -f docker-compose.local.yml logs -f mysql
docker compose -f docker-compose.local.yml logs -f redis
```

## 5. Spring Boot 실행

백엔드는 `application-local.yml` 기준으로 아래 설정을 사용합니다.

- `spring.config.import=optional:file:.env[.properties]`
- `spring.datasource.url=jdbc:mysql://127.0.0.1:3306/${MYSQL_DATABASE}...`
- `spring.datasource.username=${MYSQL_USER}`
- `spring.datasource.password=${MYSQL_PASSWORD}`
- `spring.data.redis.password=${REDIS_PASSWORD}`

즉, 백엔드도 프로젝트 루트의 `.env`를 직접 읽습니다.

실행 예시는 아래와 같습니다.

```bash
cd /Users/sondahyun/money-snap/backend
./gradlew bootRun
```

## 6. 종료

컨테이너만 내리기:

```bash
docker compose -f docker-compose.local.yml down
```

볼륨까지 같이 지우기:

```bash
docker compose -f docker-compose.local.yml down -v
```

주의:

- `down -v`는 MySQL/Redis 데이터도 함께 삭제합니다.
- 로컬 테스트 데이터를 유지하려면 `down`만 사용하세요.

## 7. 자주 생기는 문제

### 3306 포트가 이미 사용 중인 경우

로컬 MySQL이 이미 떠 있으면 Docker MySQL이 실행되지 않을 수 있습니다.

- 기존 로컬 MySQL을 종료하거나
- `docker-compose.local.yml`에서 포트를 다른 값으로 바꿔야 합니다.

### 앱에서 DB 접속이 안 되는 경우

아래를 순서대로 확인하면 됩니다.

1. `docker compose -f docker-compose.local.yml ps`
2. MySQL 컨테이너가 `healthy` 상태인지 확인
3. 앱 실행 환경변수와 Compose 환경변수가 같은지 확인
4. `application-local.yml`의 host/port/db name이 현재 컨테이너와 맞는지 확인
