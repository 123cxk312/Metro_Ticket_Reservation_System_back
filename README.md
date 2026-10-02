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
- MySQL 8
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
JWT_SECRET
JWT_EXPIRATION
```

Example PowerShell:

```powershell
$env:DB_PASSWORD="your-password"
$env:JWT_SECRET="replace-with-a-long-random-secret"
```

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
/api/admin/tickets
/api/admin/refunds
```
