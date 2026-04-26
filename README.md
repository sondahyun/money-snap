# Tripline

트립라인 (Tripline) - 여행 계획과 동선, 경비를 잇는 앱

## Repository Structure

```text
tripline/
  android/   # Android app (Kotlin + XML)
  backend/   # Spring Boot API server
```

## Android

안드로이드 앱은 [`android/`](/Users/sondahyun/money-snap/android) 아래에 있습니다.

주요 실행 경로:

- Gradle wrapper: [`android/gradlew`](/Users/sondahyun/money-snap/android/gradlew)
- App module: [`android/app/`](/Users/sondahyun/money-snap/android/app)

## Backend

백엔드는 [`backend/`](/Users/sondahyun/money-snap/backend) 아래에 있습니다.

주요 실행 경로:

- Gradle wrapper: [`backend/gradlew`](/Users/sondahyun/money-snap/backend/gradlew)
- Spring Boot entrypoint: [`backend/src/main/kotlin/com/example/triplinebackend/TriplineBackendApplication.kt`](/Users/sondahyun/money-snap/backend/src/main/kotlin/com/example/triplinebackend/TriplineBackendApplication.kt)
- Dockerfile: [`backend/Dockerfile`](/Users/sondahyun/money-snap/backend/Dockerfile)
