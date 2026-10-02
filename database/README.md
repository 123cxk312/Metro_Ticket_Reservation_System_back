# Database setup

The project uses MySQL 8.x and the database name `metro_ticket_reservation_system`.

## File order

Run the scripts in this order:

1. `00_create_database.sql`
2. `01_schema.sql`
3. `02_seed_data.sql`
4. `03_verify.sql`

## Run locally with MySQL CLI

```powershell
mysql -u root -p < .\database\00_create_database.sql
mysql -u root -p < .\database\01_schema.sql
mysql -u root -p < .\database\02_seed_data.sql
mysql -u root -p < .\database\03_verify.sql
```

You can also open each file in MySQL Workbench and execute it in the same order.

## Move the scripts to a Linux virtual machine

From the Windows project directory:

```powershell
ssh your_user@your_vm_ip "mkdir -p /tmp/metro-database"
scp .\database\*.sql your_user@your_vm_ip:/tmp/metro-database/
```

Then log in to the virtual machine:

```bash
ssh your_user@your_vm_ip
mysql -u root -p < /tmp/metro-database/00_create_database.sql
mysql -u root -p < /tmp/metro-database/01_schema.sql
mysql -u root -p < /tmp/metro-database/02_seed_data.sql
mysql -u root -p < /tmp/metro-database/03_verify.sql
```

If MySQL requires a dedicated application account, create it with a strong
password and grant access only to this database:

```sql
CREATE USER 'metro_app'@'%' IDENTIFIED BY 'replace-with-a-strong-password';
GRANT SELECT, INSERT, UPDATE, DELETE ON metro_ticket_reservation_system.* TO 'metro_app'@'%';
FLUSH PRIVILEGES;
```

Adjust `'metro_app'@'%'` to `'metro_app'@'localhost'` when the backend and
MySQL run on the same machine.
