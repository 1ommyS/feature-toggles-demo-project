# Kotlin Feature Toggle Demo

Небольшой production-style demo-сервис на Kotlin + Spring Boot.

Функционал: сервис хранит cashback-офферы и отдаёт feed. Фича-тогл `offers.smart-ranking-v2` переключает алгоритм ранжирования:

- `legacy` — новые офферы выше;
- `smart-v2` — сначала `priority`, затем cashback, затем свежесть.

В production-профиле флаг читается из Unleash и вычисляется с `userId` в контексте, поэтому в Unleash можно настроить gradual rollout / stickiness по пользователю.

## Стек

- Java 21
- Kotlin 2.2.21
- Spring Boot 3.5.16
- PostgreSQL + Flyway
- Unleash Java SDK 12.2.3
- Actuator + Prometheus metrics
- JUnit 5 + Testcontainers
- Docker / Docker Compose
- Helm
- GitHub Actions CI/CD
- GHCR

## Быстрый старт

### Вариант 1: полностью через Docker Compose

```bash
docker compose up --build
```

Проверка:

```bash
curl 'http://localhost:8080/api/v1/offers?userId=ivan'
```

По умолчанию локальный флаг выключен, поэтому `ranking=legacy`.

Включить smart ranking:

```bash
FEATURE_SMART_RANKING_V2=true docker compose up --build
```

### Вариант 2: приложение локально, инфраструктура в Docker

```bash
docker compose up -d postgres unleash-db unleash
./gradlew bootRun
```

> `gradlew` в демо — небольшой bootstrap script: скачивает pinned Gradle 8.14.3 и проверяет SHA-256 дистрибутива перед запуском.

## API

Получить feed:

```bash
curl 'http://localhost:8080/api/v1/offers?userId=user-42'
```

Создать оффер:

```bash
curl -X POST 'http://localhost:8080/api/v1/offers' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Cinema cashback",
    "cashbackPercent": 12.5,
    "priority": 80
  }'
```

Health:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/health/liveness
```

Prometheus:

```bash
curl http://localhost:8080/actuator/prometheus
```

После вызова feed появится метрика:

```text
offers_feed_requests_total{ranking="legacy",...}
offers_feed_requests_total{ranking="smart-v2",...}
```

## Feature flags

### Локальный provider

Используется по умолчанию:

```bash
FEATURE_FLAGS_PROVIDER=local \
FEATURE_SMART_RANKING_V2=true \
./gradlew bootRun
```

### Unleash provider

Docker Compose поднимает Unleash:

- UI: `http://localhost:4242`
- user: `admin`
- password: `unleash4all`
- backend token: `default:development.unleash-insecure-api-token`

В Unleash:

1. Открой `Default project`.
2. Создай flag `offers.smart-ranking-v2`.
3. Включи его для `development`.
4. При желании добавь gradual rollout с stickiness по `userId`.

После этого:

```bash
FEATURE_FLAGS_PROVIDER=unleash \
UNLEASH_URL=http://localhost:4242/api \
UNLEASH_TOKEN=default:development.unleash-insecure-api-token \
./gradlew bootRun
```

`OfferService` передаёт `userId` из query-параметра в `UnleashContext`, поэтому один и тот же пользователь стабильно попадает в свою rollout-группу.

## Что здесь production-style

### Код

- слой controller → service → repository;
- DTO не протекают в persistence;
- Bean Validation;
- `ProblemDetail` для ошибок валидации;
- транзакционные границы в service;
- `open-in-view=false`;
- schema migration через Flyway;
- feature-toggle abstraction: бизнес-код не зависит напрямую от Unleash;
- request-id в MDC и response header;
- graceful shutdown.

### Runtime

- non-root Docker user;
- memory-aware JVM;
- read-only root filesystem в Kubernetes;
- liveness/readiness probes;
- resource requests/limits;
- rolling update без `maxUnavailable`;
- HPA;
- PodDisruptionBudget;
- секреты не лежат в Helm values.

### CI

`.github/workflows/ci.yml`:

- запускается на PR и main;
- Java 21;
- pinned Gradle;
- `clean check bootJar`;
- unit + Testcontainers integration tests;
- JaCoCo report;
- сохраняет test/coverage reports как artifacts;
- Dependabot отдельно следит за Gradle, Docker и GitHub Actions dependencies.

### CD

`.github/workflows/release.yml`:

1. собирает multi-arch Docker image;
2. публикует его в GHCR;
3. включает build provenance + SBOM;
4. `main` деплоит в GitHub Environment `staging`;
5. tag вида `v1.2.3` деплоит в Environment `production`;
6. deploy делается через `helm upgrade --install --wait`.

Для `production` в GitHub можно включить Required reviewers — тогда tag создаёт deployment, но выкатка ждёт ручной approval.

## Что нужно настроить в GitHub

Создай два Environments:

- `staging`
- `production`

Чтобы fresh fork не падал на deploy без настроенного кластера, deploy jobs выключены repository variables. В `Settings -> Secrets and variables -> Actions -> Variables` добавь:

```text
ENABLE_STAGING_DEPLOY=true
ENABLE_PRODUCTION_DEPLOY=true
```

До этого workflow всё равно собирает и публикует image в GHCR, но Helm deploy jobs будут skipped.

В каждом Environment добавь secret:

```text
KUBE_CONFIG
```

Это base64 от kubeconfig нужного кластера/namespace access:

```bash
base64 < ~/.kube/config | tr -d '\n'
```

Добавь Environment variable:

```text
UNLEASH_URL=https://your-unleash.example.com/api
```

В Kubernetes заранее создай secret:

```bash
kubectl -n offer-service-staging create secret generic offer-service-secrets \
  --from-literal=DB_URL='jdbc:postgresql://postgres.example.com:5432/offers' \
  --from-literal=DB_USER='offers' \
  --from-literal=DB_PASSWORD='change-me' \
  --from-literal=UNLEASH_TOKEN='your-backend-token'
```

И аналогичный secret в production namespace `offer-service`.

Если GHCR image приватный, создай `docker-registry` secret в namespace и укажи его в Helm values:

```yaml
imagePullSecrets:
  - name: ghcr-pull
```

## Release flow

Обычный merge в `main`:

```text
PR -> CI -> merge -> image to GHCR -> staging deploy
```

Production:

```bash
git tag v0.1.0
git push origin v0.1.0
```

Дальше GitHub Actions:

```text
tag -> image to GHCR -> production environment approval -> Helm deploy
```

## Важное про реальный production

Для универсальности CD использует kubeconfig secret. В AWS/GCP/Azure я бы заменил это на OIDC federation без долгоживущего kubeconfig. Сам workflow уже выдаёт минимальные GitHub permissions; конкретный cloud login лучше настраивать под инфраструктуру проекта.

Также в реальном сервисе обычно добавляются:

- OAuth2/JWT resource server;
- distributed tracing/OpenTelemetry;
- alerts/SLO;
- network policies;
- external-secrets / Vault;
- migration policy;
- load tests;
- contract tests.
