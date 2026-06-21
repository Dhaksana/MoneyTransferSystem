# Money Transfer System — Money-ger

## Architecture Documentation

---

## 1. System Overview

A full-stack private banking platform built with **Spring Boot 4.0.2** (Java 17) and **Angular 17.3** (TypeScript 5.4), backed by **MySQL 8.0**. The system supports user registration, JWT-authenticated money transfers, beneficiary management, reward points, real-time notifications, PDF document generation, analytics, and an admin portal with audit logging.

### High-Level Architecture

```
┌─────────────────────┐       HTTP/JSON        ┌──────────────────────┐       JDBC       ┌──────────┐
│   Angular 17 UI     │ ◄──────────────────────► │  Spring Boot 4.0    │ ◄──────────────► │  MySQL   │
│   (Standalone Comp) │    Authorization:       │  10 Controllers     │                  │  8.0 DB  │
│                     │    Bearer <JWT>         │  13 Services        │                  │  8 tables│
│   Chart.js · WebSocket                       │  8 JPA Repositories │                  └──────────┘
│   Material UI       │                         │  JWT + BCrypt       │
└─────────────────────┘                         │  WebSocket (STOMP)  │
       │                                        │  Event-Driven       │
       │ STOMP/WS                               └──────────────────────┘
       │                                                │
       ▼                                                ▼
  Real-time notifications                     TransferCompletedEvent
  (/user/queue/notifications)                  → Rewards + Notifications + Audit
```

---

## 2. Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend Framework | Spring Boot | 4.0.2 |
| Java | OpenJDK | 17 |
| ORM | Hibernate | 7.2.1.Final |
| Security | Spring Security + jjwt | 0.11.5 |
| Database | MySQL | 8.0.33 |
| DB Migration | Flyway | 11.14 (disabled in dev) |
| PDF Generation | OpenPDF | 2.0.3 |
| API Docs | SpringDoc OpenAPI | 2.3.0 |
| Frontend | Angular | 17.3 |
| TypeScript | | 5.4 |
| UI Framework | Angular Material | 17.3 |
| Charts | Chart.js | 4.4.9 |
| WebSocket Client | @stomp/stompjs | 7.0 |
| Layout | Bootstrap | 5.3 |
| Build (BE) | Maven Wrapper | — |
| Build (FE) | Angular CLI | 17.3 |
| Test (BE) | JUnit 5 + Mockito | — |
| Test (FE) | Karma + Jasmine | 6.4 / 5.1 |

---

## 3. Database Schema (8 tables)

### 3.1 `users`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| username | VARCHAR(100) | NOT NULL, UNIQUE |
| full_name | VARCHAR(150) | NOT NULL |
| email | VARCHAR(150) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL (BCrypt hash) |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' |
| account_id | VARCHAR(64) | nullable |
| display_name | VARCHAR(150) | nullable |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### 3.2 `accounts`
| Column | Type | Constraints |
|--------|------|-------------|
| id | VARCHAR(64) | PK |
| account_number | VARCHAR(64) | NOT NULL, UNIQUE |
| account_type | VARCHAR(50) | NOT NULL |
| holder_name | VARCHAR(50) | NOT NULL |
| balance | DECIMAL(19,2) | NOT NULL |
| status | VARCHAR(20) | NOT NULL |
| user_id | BIGINT | FK → users(id), NOT NULL |
| version | INT | Optimistic locking |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### 3.3 `transaction_logs`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| reference_number | VARCHAR(100) | NOT NULL, UNIQUE |
| from_account_id | VARCHAR(64) | FK → accounts(id) |
| to_account_id | VARCHAR(64) | FK → accounts(id) |
| amount | DECIMAL(19,2) | NOT NULL |
| status | VARCHAR(20) | NOT NULL |
| transaction_type | VARCHAR(50) | NOT NULL |
| failure_reason | VARCHAR(512) | nullable |
| idempotency_key | VARCHAR(128) | UNIQUE, nullable |
| created_at | TIMESTAMP | NOT NULL |

### 3.4 `beneficiaries`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| owner_account_id | VARCHAR(64) | FK → accounts(id) |
| beneficiary_name | VARCHAR(150) | NOT NULL |
| beneficiary_account_number | VARCHAR(64) | NOT NULL |
| bank_name | VARCHAR(150) | nullable |
| ifsc | VARCHAR(20) | nullable |
| nickname | VARCHAR(100) | nullable |
| favorite | BOOLEAN | DEFAULT FALSE |
| UNIQUE(owner_account_id, beneficiary_account_number) |

### 3.5 `reward_accounts`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → users(id), UNIQUE |
| current_points | INT | NOT NULL |
| lifetime_points | INT | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### 3.6 `reward_transactions`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| reward_account_id | BIGINT | FK → reward_accounts(id) |
| banking_transaction_id | BIGINT | FK → transaction_logs(id), UNIQUE |
| points_earned | INT | NOT NULL |
| reason | VARCHAR(255) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

### 3.7 `notifications`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → users(id) |
| message | TEXT | NOT NULL |
| type | VARCHAR(50) | NOT NULL |
| is_read | BOOLEAN | DEFAULT FALSE |
| created_at | TIMESTAMP | NOT NULL |

### 3.8 `audit_logs`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → users(id), nullable |
| action | VARCHAR(150) | NOT NULL |
| details | TEXT | nullable |
| ip_address | VARCHAR(45) | nullable |
| timestamp | TIMESTAMP | NOT NULL |

---

## 4. Entity Relationships

```
User ──1:N──► Account ──1:N──► TransactionLog (as sender/receiver)
  │              │
  │              └──1:N──► Beneficiary (via owner_account_id)
  │
  ├──1:1──► RewardAccount ──1:N──► RewardTransaction
  │
  ├──1:N──► Notification
  │
  └──1:N──► AuditLog
```

- **User (1) ↔ Account (N)**: One user can have multiple accounts
- **User (1) ↔ RewardAccount (1)**: Exactly one reward account per user
- **Account (1) ↔ TransactionLog (N)**: An account sends/receives many transactions
- **Account (1) ↔ Beneficiary (N)**: An account has many beneficiaries
- **RewardAccount (1) ↔ RewardTransaction (N)**: Tracks all points earned
- **TransactionLog (1) ↔ RewardTransaction (1)**: One reward per successful transfer
- **User (1) ↔ Notification (N)**: In-app notifications per user
- **User (1) ↔ AuditLog (N)**: All auditable actions recorded

---

## 5. Authentication & Security

### Flow
```
Client                    Server
  │                         │
  │  POST /auth/login       │
  │  {username, password}   │
  │────────────────────────►│  AuthenticationManager.authenticate()
  │                         │  UserDetailsServiceImpl.loadUserByUsername()
  │                         │  BCryptPasswordEncoder.matches()
  │                         │  JwtUtil.generateToken(username)
  │◄────────────────────────│
  │  {token, user, role}    │
  │                         │
  │  GET /api/...           │
  │  Authorization: Bearer  │
  │────────────────────────►│  JwtAuthenticationFilter.doFilter()
  │                         │  JwtUtil.validateToken()
  │                         │  SecurityContextHolder.set()
  │                         │  @PreAuthorize("hasRole('ADMIN')")
```

### JWT Structure
- **Algorithm**: HMAC-SHA (jjwt 0.11.5)
- **Expiry**: 1 hour (configurable via `jwt.expiry.ms`)
- **Payload**: `{ sub: username, iat, exp }`
- **Transport**: `Authorization: Bearer <token>`

### Security Config
- Stateless sessions (no HttpSession)
- CSRF disabled (REST API)
- CORS: `http://localhost:4200` allowed
- Public endpoints: `/api/v1/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`
- All other: authentication required
- Admin endpoints: `@PreAuthorize("hasRole('ADMIN')")`
- Password: BCrypt hashed, min 8 chars, 1 uppercase, 1 symbol

---

## 6. API Endpoints

### Public (No Auth)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | Login → JWT token |
| POST | `/api/v1/auth/register` | Register user + account |

### User (JWT Required)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/accounts/{id}` | Get account details |
| GET | `/api/v1/accounts/{id}/balance` | Get balance |
| POST | `/api/v1/transfers` | Execute transfer |
| GET | `/api/v1/transfers/history/{accountId}` | Transaction history |
| GET | `/api/v1/beneficiaries` | List beneficiaries |
| POST | `/api/v1/beneficiaries` | Add beneficiary |
| PUT | `/api/v1/beneficiaries/{id}` | Update beneficiary |
| DELETE | `/api/v1/beneficiaries/{id}` | Delete beneficiary |
| GET | `/api/v1/rewards/summary` | Reward summary |
| GET | `/api/v1/rewards/history` | Reward history |
| GET | `/api/v1/notifications` | List notifications |
| GET | `/api/v1/notifications/unread-count` | Unread count |
| PATCH | `/api/v1/notifications/{id}/read` | Mark read |
| GET | `/api/v1/analytics/me` | Personal analytics |
| GET | `/api/v1/documents/receipts/{id}` | Download receipt PDF |
| GET | `/api/v1/documents/statements/{accountId}` | Download statement PDF |

### Admin (ROLE_ADMIN Required)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/dashboard` | Dashboard KPIs |
| GET | `/api/v1/admin/users` | List all users |
| PATCH | `/api/v1/admin/users/{id}/status` | Block/unblock user |
| PUT | `/api/v1/admin/users/{id}` | Edit user details |
| GET | `/api/v1/admin/transactions` | All transactions |
| GET | `/api/v1/admin/audit-logs/search` | Search audit logs |
| GET | `/api/v1/admin/audit-logs/export` | Export audit CSV |
| DELETE | `/api/v1/admin/audit-logs/{id}` | Delete audit entry |

### WebSocket
| Endpoint | Protocol | Description |
|----------|----------|-------------|
| `/ws` | STOMP | Real-time notifications → `/user/queue/notifications` |

---

## 7. Core Features

### 7.1 Money Transfer
```
POST /api/v1/transfers  { fromAccountId, toAccountId, amount, idempotencyKey }

1. Validate both accounts exist and are ACTIVE
2. Check sender has sufficient balance
3. Prevent same-account transfer
4. Debit sender account (balance -= amount)
5. Credit receiver account (balance += amount)
6. Create TransactionLog with status SUCCESS
7. Check idempotency_key uniqueness (prevents double-spend)
8. Publish TransferCompletedEvent
9. Return TransferResponseDTO
```

### 7.2 Transfer Event Chain
```
TransferService (success)
       │
       ▼
TransferCompletedEvent (published via ApplicationEventPublisher)
       │
       ▼
TransferEventListener (@Transactional)
       │
       ├──► RewardService.awardForSuccessfulTransfer()
       │       └── 1 point per ₹100 (MANDATORY propagation)
       │
       ├──► NotificationService.create() × 2
       │       ├── TRANSFER_DEBIT (sender)
       │       └── TRANSFER_CREDIT (receiver)
       │              └── WebSocket push → /user/queue/notifications
       │
       └──► AuditService.log("TRANSFER_SUCCESS")
               └── Records IP + timestamp + details
```

### 7.3 Reward System
- **Rate**: 1 point per ₹100 transferred (integer division)
- **Account**: `RewardAccount` (current_points, lifetime_points) per user
- **Transaction**: `RewardTransaction` linked to each successful bank transfer
- **Propagation**: `@Transactional(MANDATORY)` — participates in transfer's transaction
- **Unique**: One reward per transfer (unique constraint on `banking_transaction_id`)

### 7.4 Notification System
- **Storage**: `notifications` table (message, type, is_read)
- **Types**: TRANSFER_DEBIT, TRANSFER_CREDIT
- **Real-time**: STOMP WebSocket via `/ws` → `/user/queue/notifications`
- **UI**: Unread count badge, notification menu (top 6), full list page
- **Actions**: Mark as read, delete

### 7.5 Analytics
| Metric | Scope |
|--------|-------|
| Money sent/received this month | Per user |
| Transaction count | Per user |
| Largest transaction | Per user |
| Most frequent beneficiary | Per user |
| Monthly transaction trend (Chart.js) | Per user |
| Status distribution (doughnut chart) | Per user |
| Total users, active users | Admin |
| Total transaction volume | Admin |
| Total rewards distributed | Admin |

### 7.6 PDF Documents
- **Receipts**: Per-transaction PDF with reference, amounts, timestamps
- **Statements**: Date-range filtered account statement PDF
- **Library**: OpenPDF 2.0.3 (LGPL)

### 7.7 Admin Portal
- **Dashboard**: KPI cards (users, volume, rewards)
- **User Management**: List, edit (name/email/role/status), block/unblock
- **Transaction Monitor**: All transactions with status
- **Audit Logs**: Search (action, username, date range), paginate, export CSV, delete

---

## 8. Frontend Architecture

### Component Tree
```
AppComponent (shell)
├── Public
│   ├── WelcomeComponent (landing + auth drawer)
│   ├── LoginComponent
│   ├── SignupComponent
│   └── UnauthorizedComponent
├── Protected (authGuard)
│   ├── ProfileComponent (dashboard)
│   ├── TransferMoneyComponent
│   ├── TransactionHistoryComponent
│   ├── BeneficiariesComponent
│   ├── RewardsComponent
│   ├── AnalyticsComponent
│   ├── StatementsComponent
│   └── NotificationsComponent
└── Admin (authGuard + adminGuard)
    └── AdminComponent
```

### Services
| Service | Responsibilities |
|---------|-----------------|
| `AuthService` | Login, register, logout, localStorage token/state, BehaviorSubject |
| `BankingApiService` | All HTTP calls to backend (accounts, transfers, admin, etc.) |
| `NotificationService` | Load notifications, STOMP WebSocket connection |

### Guards
| Guard | Logic |
|-------|-------|
| `authGuard` | `isLoggedInSync()` → true: allow; false: redirect to `/login?returnUrl=...` |
| `adminGuard` | `isLoggedInSync() && role === 'ADMIN'` → true: allow; false: redirect to `/unauthorized` |

### Routing Strategy
- **Public**: `/welcome`, `/login`, `/signup`, `/unauthorized` — no auth
- **User**: `/profile`, `/transfer`, `/transactions`, `/beneficiaries`, `/rewards`, `/analytics`, `/statements`, `/notifications` — authGuard
- **Admin**: `/admin` — authGuard + adminGuard
- **Default**: `/**` → redirect to `/welcome`

### State Management
- `AuthService._isLoggedIn` (BehaviorSubject) — reactive auth state
- `AuthService.currentUser$` (BehaviorSubject) — user profile
- `NotificationService.notifications$` (BehaviorSubject) — notification list
- localStorage: `auth_token`, `auth_flag`, `user_name`, `user_id`, `user_role`
- `AuthInterceptor` — auto-attaches `Authorization: Bearer` to all HTTP requests

---

## 9. Backend Architecture (package structure)

```
com.bd
├── MoneyTransferSystemApplication.java
├── config/
│   └── WebSocketConfig.java           — STOMP broker config
├── controller/ (10)
│   ├── AccountController.java
│   ├── AdminController.java
│   ├── AnalyticsController.java
│   ├── AuditController.java
│   ├── AuthController.java
│   ├── BeneficiaryController.java
│   ├── DocumentController.java
│   ├── NotificationController.java
│   ├── RewardController.java
│   └── TransferController.java
├── dto/ (16)
│   ├── AccountDTO.java / AdminAnalyticsDTO / AdminTransactionDTO
│   ├── AdminUserDTO / AnalyticsDTO / AuditLogDTO / BeneficiaryDTO
│   ├── ChartPointDTO / LoginRequest / LoginResponse / NotificationDTO
│   └── RewardHistoryDTO / RewardSummaryDTO / TransactionHistoryDTO
│   ├── TransferRequestDTO / TransferResponseDTO
├── event/
│   └── TransferCompletedEvent.java    — ApplicationEvent
├── exception/
│   ├── AccountNotFoundException       — 404
│   ├── GlobalExceptionHandler         — @RestControllerAdvice
│   ├── InactiveAccountException       — 400
│   └── InsufficientBalanceException   — 400
├── model/ (8)
│   ├── Account / AppUser / AuditLog / Beneficiary
│   ├── Notification / RewardAccount / RewardTransaction / TransactionLog
├── repository/ (8)
│   ├── AccountRepository / AppUserRepository / AuditLogRepository
│   ├── BeneficiaryRepository / NotificationRepository
│   ├── RewardAccountRepository / RewardTransactionRepository
│   └── TransactionLogRepository
├── security/
│   ├── JwtAuthenticationFilter.java   — OncePerRequestFilter
│   ├── JwtUtil.java                   — Token gen/validation
│   ├── SecurityConfig.java            — Spring Security chain
│   └── UserDetailsServiceImpl.java    — Load user from DB
└── service/ (13)
    ├── AccountService / AdminService / AnalyticsService
    ├── AuditService / AuthService / BeneficiaryService
    ├── CurrentUserService / DocumentService / FailureLogService
    ├── IAccountService / ITransferService (interfaces)
    ├── NotificationService / RewardService
    ├── TransferEventListener           — Event handler
    └── TransferService
```

---

## 10. Setup & Deployment

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0
- npm / Maven

### Backend Setup
```bash
cd Backend

# Configure environment
cp .env.example .env
# Edit .env with your MySQL credentials

# Run
./mvnw spring-boot:run
```

**`.env` example:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/mts
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=yourpassword
JWT_SECRET=your-256-bit-secret
```

### Frontend Setup
```bash
cd Frontend
npm install
ng serve
```

### Admin Access
```sql
-- Register via UI, then promote:
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';

-- Or create directly:
INSERT INTO users (username, full_name, email, password, role, status, account_id, display_name, created_at, updated_at)
VALUES ('admin', 'Administrator', 'admin@bank.com',
        '$2a$10$...BCryptHashOfPassword...',
        'ADMIN', 'ACTIVE', 'ACC0000001', 'Administrator', NOW(), NOW());
```

---

## 11. Testing

### Backend (JUnit 5 + Mockito)
```bash
cd Backend
./mvnw test
```
**44 tests** across 7 test classes:
- `AccountTest` (12) — Entity validation
- `AuthServiceTest` (10) — Login, register, edge cases
- `TransferServiceTest` (9) — Transfer logic
- `RewardServiceTest` (10) — Points calculation
- `SecurityConfigTest` (1) — Security bean loading
- `AuditLogRepositoryTest` (1) — Repository query
- `MoneyTransferSystemApplicationTests` (1) — Context load

### Frontend (Karma + Jasmine)
```bash
cd Frontend
ng test
```
**40 tests** across 8 spec files:
- `auth.service.spec.ts` (7) — Login/register/logout/state
- `banking-api.service.spec.ts` (16) — All API methods
- `app.guard.spec.ts` (5) — authGuard + adminGuard
- `app.component.spec.ts` (3) — Shell rendering
- `login/signup/welcome/profile/transfer/transaction` component specs

---

## 12. Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| JWT over Session | Stateless, scalable, cross-origin friendly |
| Event-Driven | Decouples transfer logic from rewards/notifications/audit |
| Idempotency Keys | Prevents double-spending on network retry |
| MANDATORY propagation | Guarantees reward + notification in same transaction as transfer |
| Optimistic Locking (version) | Prevents race conditions on balance updates |
| Flyway disabled in dev | Hibernate ddl-auto=update faster for development |
| Standalone Angular components | Simpler lazy loading, tree-shaking |
| BehaviorSubject for auth state | Reactive UI updates on login/logout |
| BCrypt cost 10 | Industry-standard password hashing |
| Material UI + Bootstrap | Rapid UI development + responsive grid |




1. logout redirecting to login page, redirect to wlcome page, 
2. save button should close the popup
3. dont render failed received transactions and receipients end
4. 2 transfer money buttons in dashboard of user not necessary
5. render sender and receibers full name on review transaction
6. after completing transaction route to dashboard or histroy page, whiever makes more logical sense
7. analytics success fail should reflect for the transactions sent by user not received
8. notification symbol in notificcation page being covered by text, check styling
9. sidebar components, going towards down start from top
10. dynamically adjust screen content based on sidebar collapsing or expanding
11. remove the lifetime points division in rewards page, fix styling of current point i think colour is white right now
12. remove details tht we dont have like bank and ifsc from beneficiary form