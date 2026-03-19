# Setup Guide

## 1. Configure Environment Variables

Create values from `.env.example`.

Required:

- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_USERNAME`
- `APP_PASSWORD`
- `JWT_SECRET` (32+ chars)

## 2. Start Local Dependencies

```bash
docker-compose up -d
```

Services:

- PostgreSQL (port 5432)
- Redis (port 6379)
- Prometheus (port 9090)
- Grafana (port 3000)

## 3. Start the App

```bash
mvn spring-boot:run
```

## 4. Authenticate

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"<APP_USERNAME>","password":"<APP_PASSWORD>"}'
```

Use returned token in `Authorization: Bearer <token>`.

## 5. Run the Test Script

Set token in shell:

```bat
set TOKEN=<jwt-token>
test.bat
```

## 6. Useful Endpoints

- Health: `GET /actuator/health`
- Prometheus Metrics: `GET /actuator/prometheus`
- Ingest: `POST /api/logs/ingest`
- Search: `POST /api/logs/search`
- Analyze: `POST /api/logs/analyze`
