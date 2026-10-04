# Metro Ticket Reservation System - Backend

Spring Boot backend for the metro ticket reservation system.

## Technology

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Validation
- MyBatis-Plus
- JWT
- BCrypt
- DeepSeek function calling
- MySQL 8
- Redis 7
- Docker Compose
- Maven

## Database

Create the database and tables:

```bash
mysql -u root -p < database/00_create_database.sql
mysql -u root -p < database/01_schema.sql
mysql -u root -p < database/02_seed_data.sql
mysql -u root -p < database/03_verify.sql
```

The application uses:

```text
database: metro_ticket_reservation_system
port: 8088
```

## Environment variables

Copy `.env.example` values into your run configuration. Do not commit real
credentials.

```text
DB_URL
DB_USERNAME
DB_PASSWORD
MYSQL_ROOT_PASSWORD
MYSQL_PORT
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
REDIS_DATABASE
REDIS_KEY_PREFIX
REDIS_TICKET_SEARCH_TTL
REDIS_REFERENCE_DATA_TTL
JWT_SECRET
JWT_EXPIRATION
DEEPSEEK_API_KEY
DEEPSEEK_BASE_URL
DEEPSEEK_MODEL
```

Example PowerShell:

```powershell
$env:DB_PASSWORD="your-password"
$env:JWT_SECRET="replace-with-a-long-random-secret"
```

## Docker and Redis

`docker-compose.yml` starts three services:

1. MySQL 8.4
2. Redis 7.4
3. Spring Boot backend

Create a local `.env` file:

```powershell
Copy-Item .env.example .env
```

Change the default passwords in `.env`, then build and start the full stack:

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

The backend is available at:

```text
http://localhost:8088
http://localhost:8088/api/health
```

MySQL initialization scripts are mounted from `./database`. They only run
automatically when the MySQL data volume is empty.

Stop the services:

```bash
docker compose down
```

Delete the MySQL and Redis volumes and re-run database initialization:

```bash
docker compose down -v
docker compose up -d --build
```

The second command deletes all data stored in the Docker volumes. Use it only
when a full reset is intended.

### Run only MySQL and Redis

When the Spring Boot application runs from IntelliJ instead of Docker:

```bash
docker compose up -d mysql redis
```

Then keep these local environment values:

```text
DB_URL=jdbc:mysql://localhost:3306/metro_ticket_reservation_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=metro_app
DB_PASSWORD=the value assigned to DB_PASSWORD in .env
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=the value assigned to REDIS_PASSWORD in .env
```

### Redis cache design

Redis is used as an optional read cache:

- Metro line options: 10-minute TTL
- Station options: 10-minute TTL
- Ticket search results: 15-second TTL

MySQL remains the source of truth for stock and orders. Redis never performs
inventory deduction. Ticket creation, stock deduction, sold-out transitions,
and refund stock restoration remove ticket-search cache entries after the
database transaction commits.

If Redis is unavailable, cache reads fail open and requests continue against
MySQL. The cache is an optimization, not a dependency for core booking
correctness.

## Run

```bash
mvn test
mvn package
java -jar target/Metro_Ticket_Reservation_System_back-1.0-SNAPSHOT.jar
```

Health check:

```text
GET http://localhost:8088/api/health
```

## Main API groups

```text
/api/auth
/api/metro
/api/tickets
/api/orders
/api/refunds
/api/ai/chat
/api/admin/tickets
/api/admin/refunds
```
