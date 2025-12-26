# Parking Platform (Demo) - Mingrencha

> Tech: Spring Boot 3 + Maven + Docker Compose + SQLite + Redis + RabbitMQ  
> Services (5): parking-service, reservation-service, billing-service, payment-service, iot-service  
> Multi-tenant: JWT claim `tenantId` + Header `X-Tenant-Id` (both supported, must match if both present)  
> Billing rule: **15 RMB / 30 min**, round up  
> Reservation lock: **15 minutes**, with scheduled expiry release

## 0. Requirements (Mac)
- Docker Desktop
- JDK 17 (optional, only if you want to run without Docker)
- Maven (optional, only if you want to build without Docker)

## 1. Run (Docker Compose)
```bash
docker compose up --build
```

Ports:
- parking-service: 8081
- reservation-service: 8082
- billing-service: 8083
- payment-service: 8084
- iot-service: 8085
- RabbitMQ UI: http://localhost:15672 (guest/guest)
- Redis: localhost:6379

## 2. Quick Start (curl)
All requests require tenant header: `X-Tenant-Id: t1`.

### 2.1 Register + Login -> JWT
```bash
bash scripts/01_auth.sh
```

### 2.2 Query parking lot + spaces
```bash
bash scripts/02_query.sh
```

### 2.3 Create reservation (prefer EV)
```bash
bash scripts/03_reserve.sh
```

### 2.4 Enter -> Exit -> Pay -> Notify (mock) -> Gate Open event
```bash
bash scripts/04_billing_and_pay.sh
```

## 3. Notes / Simplifications
- Opening hours stored as JSON on parking_lot:
  - Mon-Fri: 08:00-22:00
  - Sat-Sun: 10:00-20:00
  Reservation service validates start/end are within the same day and within the range.
- User accounts stored in **parking-service** SQLite for demo convenience.
- Strong consistency for reservation uses Redis lock on (spaceId) if provided, otherwise locks the lot (demo-level).
- RabbitMQ delivery is "at least once" in practice; we made handlers idempotent where needed (payment notify, release).



## 网络问题：Maven Central 502/超时怎么办？
本项目在每个服务目录内内置了 `maven-settings.xml`，Docker 构建时会自动使用镜像源（HuaweiCloud）并启用重试。
如果你仍然遇到依赖下载失败，可在本机网络切换（例如手机热点）后重试 `docker compose build --no-cache`。

docker compose down -v
docker compose build --no-cache
docker compose up