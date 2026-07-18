# High-Frequency Log Aggregator & Search Engine

A Spring Boot + PostgreSQL project for high-write log ingestion, keyword search, semantic retrieval, and AI-assisted log analysis.

## What Was Improved

- JWT authentication for all API endpoints
- Input validation with DTOs and `@Valid`
- Unified API error responses with global exception handling
- SQL-safe partition management via parameterized function calls
- Async embedding indexing to avoid blocking ingestion
- Explicit HikariCP pool and leak detection configuration
- Optional caching with Redis (fallback to in-memory cache)
- Prometheus metrics + custom health indicators (DB + Ollama)
- Environment-based configuration (no hardcoded credentials)
- Multi-stage Dockerfile + Kubernetes manifest

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (or Docker)
- Ollama running at `OLLAMA_BASE_URL` (default `http://localhost:11434`)

## 1) Configure Environment

Copy `.env.example` values into your shell/session and replace secrets.

Minimum required variables:

- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_USERNAME`
- `APP_PASSWORD`
- `JWT_SECRET` (at least 32 chars)

## 2) Start Dependencies

```bash
docker-compose up -d
```

This starts PostgreSQL, Redis, Prometheus, and Grafana.

## 3) Run the Application

Make sure the required environment variables are set in the current shell before starting the app. If they are missing, Spring will fail at startup while connecting to PostgreSQL.

On PowerShell, for example:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:APP_USERNAME="admin"
$env:APP_PASSWORD="admin123"
$env:JWT_SECRET="replace-with-a-32-char-minimum-secret"
```

```bash
mvn spring-boot:run
```

## 4) Authenticate and Use APIs

### Login (get token)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"<APP_USERNAME>","password":"<APP_PASSWORD>"}'
```

Use the returned token as `Authorization: Bearer <token>`.

### Ingest Logs

```bash
curl -X POST http://localhost:8080/api/logs/ingest \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '[{"source":"app-1","level":"ERROR","message":"DB timeout"}]'
```

### Search Logs

```bash
curl -X POST http://localhost:8080/api/logs/search \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"searchText":"timeout","page":0,"size":20}'
```

## Monitoring

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Actuator health: `http://localhost:8080/actuator/health`
- Actuator metrics: `http://localhost:8080/actuator/prometheus`

## Production Artifacts

- Dockerfile: `Dockerfile`
- Production compose: `docker-compose.prod.yml`
- Kubernetes manifest: `k8s/log-aggregator.yaml`

## Quick Test

- Set `TOKEN` env variable from `/api/auth/login` response
- Run `test.bat`
