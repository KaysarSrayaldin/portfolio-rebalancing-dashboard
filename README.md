# Automated Portfolio Rebalancing Dashboard

A full-stack demo mimicking core functionality of a wealth-management trading platform:
an advisor views a client's portfolio, compares it against a target asset allocation model,
and generates a proposed list of trades to bring the account back into balance.

**Stack:** Java 17 + Spring Boot 3 + Spring Data JPA + MySQL (backend) / React + Axios (frontend)

<img width="1918" height="1092" alt="Adobe Express - Screen Recording 2026-08-14 212001" src="https://github.com/user-attachments/assets/625f9a59-686e-4c85-bf5a-721f196b667c" />

## Architecture

```
Client -> Account(s) -> Holdings (Security x Quantity)
Account -> ModelPortfolio -> ModelTarget (target weight per AssetClass)

POST /api/accounts/{id}/rebalance
  1. Sum current $ value per asset class (equity / fixed income / cash)
  2. Compare to target weights -> compute dollar drift per class
  3. Sell proportionally across held securities in overweight classes
  4. Buy proportionally across held securities in underweight classes,
     respecting a minimum cash buffer and a minimum trade dollar threshold
  5. Return proposed trades + before/after allocation breakdown
```

The rebalancing logic lives entirely in `RebalanceService.java` and is deliberately
framework-agnostic and unit-testable (pure BigDecimal math, no I/O inside the algorithm
itself besides the initial data fetch).

## Local setup (Windows 11)

### Prerequisites
Make sure you've installed: JDK 17, Maven, Node.js, MySQL Community Server, Git.
Verify with `java -version`, `mvn -version`, `node -version`, `mysql --version`.

### 1. Database
Open a MySQL shell (or MySQL Workbench) and just make sure the server is running —
the app will auto-create the `rebalance_db` schema on first run (see `application.properties`,
`createDatabaseIfNotExist=true`).

Edit `backend/src/main/resources/application.properties` and set your MySQL root password:
```
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE
```

### 2. Backend
```powershell
cd backend
mvn spring-boot:run
```
This starts the API on `http://localhost:8080` and auto-creates all tables via Hibernate
(`spring.jpa.hibernate.ddl-auto=update`).

Once it's up and tables exist, seed sample data:
```powershell
mysql -u root -p rebalance_db < ..\database\seed.sql
```

Sanity check: visit `http://localhost:8080/api/clients` in a browser — you should see
Jane Whitfield's client record as JSON.

### 3. Frontend
In a new terminal:
```powershell
cd frontend
npm install
npm start
```
This opens `http://localhost:3000`. Click **Run Rebalance** to see the seeded account
(intentionally drifted ~82% equity vs. a 60% target) generate SELL trades on equities and
BUY trades on fixed income to bring it back to the 60/35/5 target model.

## What to extend next (v2 ideas)
- Multiple accounts/clients with a picker UI instead of the hardcoded demo account
- Security-level (not just asset-class-level) target weights
- Tax-lot awareness (avoid selling lots at a loss unless tax-loss harvesting is the goal)
- Trade execution endpoint that actually updates `holdings` and `cash_balance`
- Deploy: Spring Boot -> AWS Elastic Beanstalk/EC2, MySQL -> RDS, React -> S3/CloudFront
