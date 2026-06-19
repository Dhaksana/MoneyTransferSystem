# Banking Database Schema

## ER Diagram

```mermaid
erDiagram
    USERS {
        BIGINT id PK
        VARCHAR username
        VARCHAR full_name
        VARCHAR email
        VARCHAR password
        VARCHAR role
        VARCHAR status
        VARCHAR account_id
        VARCHAR display_name
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    ACCOUNTS {
        VARCHAR id PK
        VARCHAR account_number
        VARCHAR account_type
        VARCHAR holder_name
        DECIMAL balance
        VARCHAR status
        BIGINT user_id FK
        INT version
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    TRANSACTION_LOGS {
        BIGINT id PK
        VARCHAR reference_number
        VARCHAR from_account_id FK
        VARCHAR to_account_id FK
        DECIMAL amount
        VARCHAR status
        VARCHAR transaction_type
        VARCHAR failure_reason
        VARCHAR idempotency_key
        TIMESTAMP created_at
    }
    BENEFICIARIES {
        BIGINT id PK
        VARCHAR owner_account_id FK
        VARCHAR beneficiary_name
        VARCHAR beneficiary_account_number
        VARCHAR bank_name
        VARCHAR ifsc
        VARCHAR nickname
        BOOLEAN favorite
        TIMESTAMP created_at
    }
    REWARD_ACCOUNTS {
        BIGINT id PK
        BIGINT user_id FK
        INT current_points
        INT lifetime_points
        TIMESTAMP updated_at
    }
    REWARD_TRANSACTIONS {
        BIGINT id PK
        BIGINT reward_account_id FK
        BIGINT banking_transaction_id FK
        INT points_earned
        VARCHAR reason
        TIMESTAMP created_at
    }
    NOTIFICATIONS {
        BIGINT id PK
        BIGINT user_id FK
        TEXT message
        VARCHAR type
        BOOLEAN is_read
        TIMESTAMP created_at
    }
    AUDIT_LOGS {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR action
        TEXT details
        VARCHAR ip_address
        TIMESTAMP timestamp
    }

    USERS ||--o{ ACCOUNTS : owns
    ACCOUNTS ||--o{ TRANSACTION_LOGS : "sends from"
    ACCOUNTS ||--o{ TRANSACTION_LOGS : "receives to"
    ACCOUNTS ||--o{ BENEFICIARIES : "owns"
    USERS ||--o{ REWARD_ACCOUNTS : owns
    REWARD_ACCOUNTS ||--o{ REWARD_TRANSACTIONS : contains
    TRANSACTION_LOGS ||--|| REWARD_TRANSACTIONS : "one reward per transaction"
    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--o{ AUDIT_LOGS : generates
```

## Reward Rules

- Reward points are generated only when:
  - `status = 'SUCCESS'`
  - sender and receiver belong to different users
  - `amount >= 100`
- Calculation: `points = floor(amount / 100)`
- Reward history is maintained in `reward_transactions`
- A unique constraint on `banking_transaction_id` ensures rewards are generated once per transaction.

## SQL Schema Summary

The migration script `src/main/resources/db/migration/V1__bank_schema.sql` creates:

- `users`
- `accounts`
- `transaction_logs`
- `beneficiaries`
- `reward_accounts`
- `reward_transactions`
- `notifications`
- `audit_logs`

## Production migration strategy

- Flyway is enabled via `spring.flyway.enabled=true`
- Hibernate DDL auto is disabled with `spring.jpa.hibernate.ddl-auto=none`
- Migrations are applied from `classpath:db/migration`
- `spring.flyway.baseline-on-migrate=true` is enabled to support existing schema baselining
