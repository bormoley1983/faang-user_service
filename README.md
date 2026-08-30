# User Service

Service responsible for user profiles, subscriptions, mentorship, goals, skills, recommendations, events, premium access, and avatar management.

## Quick start

Prerequisites:

- Java 25+
- Docker
- PostgreSQL
- Redis
- Kafka
- S3-compatible object storage
- [faang-infra](https://github.com/bormoley1983/faang-infra) running locally or accessible

Run locally:

```sh
./gradlew bootRun
```

Run infrastructure-free unit tests and coverage verification:

```sh
./gradlew clean test jacocoTestCoverageVerification
```

Build and run in Docker:

```sh
./gradlew build
docker build -t user-service .
docker run -p 8080:8080 user-service
```

## Configuration

Main configuration: [src/main/resources/application.yaml](src/main/resources/application.yaml)

Important defaults and environment variables:

- Server port: `8080`
- Database: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`
- S3: `S3_ENDPOINT`, `S3_REGION`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET_NAME`
- DiceBear: `DICEBEAR_API_TOKEN`

## Main API areas

- Users and avatars: [`UserController`](src/main/java/school/faang/user_service/controller/user/UserController.java)
- Subscriptions: [`SubscriptionController`](src/main/java/school/faang/user_service/controller/subscription/SubscriptionController.java)
- Mentorship: [`MentorshipController`](src/main/java/school/faang/user_service/controller/mentorship/MentorshipController.java)
- Goals and invitations: [`controller/goal`](src/main/java/school/faang/user_service/controller/goal)
- Skills: [`SkillController`](src/main/java/school/faang/user_service/controller/skill/SkillController.java)
- Recommendations: [`controller/recommendation`](src/main/java/school/faang/user_service/controller/recommendation)
- Events: [`controller/event`](src/main/java/school/faang/user_service/controller/event)
- Premium access: [`PremiumController`](src/main/java/school/faang/user_service/controller/premium/PremiumController.java)

## External integrations

- Project Service and Payment Service through Feign clients
- Kafka for user-ban, deactivation, profile-view, and event-start events
- PostgreSQL and Liquibase for persistence
- Redis for caching and messaging
- S3-compatible storage and DiceBear for avatars

## CI and tests

[GitHub Actions](.github/workflows/ci.yml) builds the service and verifies JaCoCo coverage for pull requests and pushes to `dev-local`. Integration tests use JUnit tags and Testcontainers and are excluded from the default unit-test task.

## Repository files

- Build: [build.gradle.kts](build.gradle.kts)
- Docker image: [Dockerfile](Dockerfile)
- CI: [.github/workflows/ci.yml](.github/workflows/ci.yml)
- Application entry point: [UserServiceApplication.java](src/main/java/school/faang/user_service/UserServiceApplication.java)

**Note:** Base code structure and architecture patterns are based on the [FAANG School](https://github.com/faang-school) educational project.
