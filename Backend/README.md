# Money Transfer System Backend - Detailed Concepts and Workflows

## Overview

This is a Spring Boot-based money transfer system that provides secure account management, money transfers, and administrative capabilities. The system uses JWT authentication, role-based access control, and follows RESTful API principles.

## Architecture Overview

### Technology Stack
- **Framework**: Spring Boot 3.4.0
- **Language**: Java 17
- **Security**: Spring Security with JWT
- **Database**: MySQL (production) / H2 (testing)
- **ORM**: Spring Data JPA with Hibernate
- **API Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven
- **Validation**: Jakarta Bean Validation

### Project Structure
```
Backend/
├── src/main/java/com/bd/
│   ├── controller/          # REST controllers
│   ├── dto/                 # Data Transfer Objects
│   ├── exception/           # Custom exceptions
│   ├── model/               # JPA entities
│   ├── repository/          # Data access layer
│   ├── security/            # Security configuration
│   └── service/             # Business logic
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## Core Concepts and Workflows

### 1. Authentication and Authorization

#### JWT Authentication Workflow
1. **Registration**: Users register with username, password, and holder name
2. **Login**: Username/password authentication generates JWT token
3. **Token Validation**: Each request validates JWT token in request headers
4. **Role-based Access**: Different endpoints accessible based on user roles (USER/ADMIN)

**Key Components:**
- `AuthController`: Handles login/register endpoints
- `AuthService`: Business logic for authentication
- `JwtUtil`: JWT token generation and validation
- `SecurityConfig`: Spring Security configuration with JWT filter

#### Security Configuration
- Stateless authentication using JWT
- CORS enabled for frontend integration
- Method-level security with `@PreAuthorize`
- Password encoding with BCrypt

### 2. Account Management

#### Account Entity Features
- **Validation**: Jakarta validation annotations for data integrity
- **Status Management**: ACTIVE/INACTIVE/BLOCKED states
- **Optimistic Locking**: Version field for concurrent updates
- **Automatic Timestamps**: `@PrePersist` and `@PreUpdate` hooks

**Key Methods:**
- `debit(amount)`: Reduces balance with validation
- `credit(amount)`: Increases balance
- `activate()` / `deactivate()`: Status changes

### 3. Money Transfer System

#### Transfer Workflow
1. **Validation**: Check account existence and status
2. **Idempotency**: Prevent duplicate transfers using idempotency keys
3. **Atomic Transaction**: Debit/credit operations in single transaction
4. **Logging**: Record all transfer attempts (success/failure)
5. **Error Handling**: Controlled failure responses instead of exceptions

**Key Features:**
- **Idempotency**: Same request multiple times only processes once
- **Failure Isolation**: Transfer failures don't affect logging
- **Balance Validation**: Insufficient funds checking
- **Same Account Prevention**: Cannot transfer to self

#### Transaction Logging
- **Success Logs**: Stored in `transaction_logs` table
- **Failure Logs**: Separate service for failed transactions
- **Audit Trail**: Complete history of all transfer attempts

### 4. Administrative Features

#### Admin Dashboard Capabilities
- **Account Management**: View/update all accounts
- **Transaction Monitoring**: Paginated view of all transactions
- **Balance Updates**: Admin can modify account balances
- **Account Deactivation**: Block/unblock accounts

**Security**: All admin endpoints protected with `@PreAuthorize("hasRole('ADMIN')")`

### 5. Data Access Layer

#### Repository Pattern
- **Spring Data JPA**: Automatic query generation
- **Custom Queries**: JPQL for complex transaction filtering
- **Pagination**: Efficient large dataset handling

#### Key Repositories
- `AccountRepository`: Account CRUD operations
- `TransactionLogRepository`: Transaction history queries
- `AppUserRepository`: User authentication data

### 6. API Design

#### RESTful Endpoints
- **Versioning**: `/api/v1/` prefix
- **HTTP Methods**: GET, POST, PUT, DELETE appropriately
- **Response Consistency**: Standardized response formats
- **Error Handling**: Meaningful error messages

#### Pagination Support
- **Page/Size Parameters**: Configurable result sets
- **Metadata**: Total elements, current page info
- **Efficient Queries**: Database-level pagination

### 7. Validation and Error Handling

#### Input Validation
- **Bean Validation**: `@NotBlank`, `@Size`, `@PositiveOrZero`
- **Custom Validators**: Business rule validation
- **Request DTOs**: Input sanitization

#### Exception Handling
- **Custom Exceptions**: `AccountNotFoundException`, `InsufficientBalanceException`
- **Controlled Responses**: No internal errors exposed to clients
- **Transaction Rollback**: Failed operations don't persist partial changes

### 8. Configuration Management

#### Environment Configuration
- **Dotenv Support**: Environment variables from `.env` file
- **Profile-based Config**: Different settings for dev/test/prod
- **Database Flexibility**: MySQL for production, H2 for testing

## Implementation Status of Requested Concepts

| Concept | Implemented | Location | Details |
|---------|-------------|----------|---------|
| Single response entity | ✅ | `dto/TransferResponseDTO.java`, `dto/LoginResponse.java` | Standardized response objects for API consistency |
| Basic authentication | ❌ | N/A | Uses JWT authentication instead of basic auth |
| AOP | ❌ | N/A | Not implemented in current codebase |
| Swagger - secured | ✅ | `pom.xml`, Controllers | OpenAPI documentation with springdoc-openapi-starter |
| Global Exception | ⚠️ | `exception/` | Custom exceptions exist but no global handler visible |
| DTO | ✅ | `dto/` package | Multiple DTOs for request/response data transfer |
| Validators | ✅ | `model/Account.java` | Jakarta validation annotations on entity fields |
| Database connectivity | ✅ | `application.properties`, `pom.xml` | MySQL (prod) and H2 (test) database support |
| Dev & test - jdbc | ✅ | `pom.xml`, `application.properties` | H2 for testing, MySQL for development |
| Custom queries | ✅ | `repository/TransactionLogRepository.java` | JPQL queries for transaction filtering |
| Composition (opt) | ✅ | `service/TransferService.java` | Services composed with repositories and other services |
| Functional interfaces | ✅ | `service/AuthService.java`, `controller/AuthController.java` | Optional.map, lambda expressions |
| Immutable collection | ❌ | N/A | Not implemented |
| Records | ❌ | N/A | Using traditional classes instead of records |
| Data and time - api | ✅ | `model/Account.java` | LocalDateTime for timestamp handling |
| Threads (opt) | ❌ | N/A | No multi-threading implemented |
| Coding standards - methods | ✅ | All classes | Standard Java naming and structure conventions |
| Add custom validators | ✅ | `model/Account.java` | Custom validation annotations and messages |
| Method references streams | ✅ | `service/TransferService.java` | Stream operations and method references in data processing |

## Key Workflows

### User Registration and Login
```java
// 1. User registers via /api/v1/auth/register
// 2. Account and AppUser entities created
// 3. Password hashed with BCrypt
// 4. Login generates JWT token
// 5. Subsequent requests include JWT in Authorization header
```

### Money Transfer Process
```java
// 1. Validate request and idempotency key
// 2. Check account existence and status
// 3. Verify sufficient balance
// 4. Atomic debit/credit operation
// 5. Log transaction result
// 6. Return success/failure response
```

### Admin Account Management
```java
// 1. Admin authentication required
// 2. View all accounts with pagination
// 3. Update account details (balance, status)
// 4. Monitor all transactions
// 5. Deactivate problematic accounts
```

## Database Schema

### Accounts Table
- `id` (VARCHAR): Primary key
- `holder_name` (VARCHAR): Account holder name
- `balance` (DOUBLE): Account balance
- `status` (VARCHAR): ACTIVE/INACTIVE/BLOCKED
- `version` (INT): Optimistic locking version
- `last_updated` (DATETIME): Auto-updated timestamp

### Transaction Logs Table
- `id` (BIGINT): Auto-generated ID
- `from_account_id` (VARCHAR): Sender account
- `to_account_id` (VARCHAR): Receiver account
- `amount` (DOUBLE): Transfer amount
- `status` (VARCHAR): SUCCESS/FAILED
- `idempotency_key` (VARCHAR): Idempotency identifier
- `failure_reason` (TEXT): Failure details if applicable

### App Users Table
- `id` (BIGINT): Auto-generated ID
- `username` (VARCHAR): Login username
- `password` (VARCHAR): Hashed password
- `role` (VARCHAR): USER/ADMIN role
- `account_id` (VARCHAR): Linked account ID

## Security Considerations

- **Password Security**: BCrypt hashing with salt
- **JWT Expiration**: 1-hour token validity
- **CORS Configuration**: Restricted to localhost:4200
- **Input Validation**: Comprehensive validation on all inputs
- **SQL Injection Prevention**: JPA parameterized queries
- **XSS Protection**: Proper encoding and validation

## Testing Strategy

- **Unit Tests**: Service layer testing with Mockito
- **Integration Tests**: Full API testing
- **Database Tests**: H2 in-memory database for isolation
- **Security Tests**: Authentication and authorization testing

## Deployment Considerations

- **Environment Variables**: Sensitive data via .env files
- **Database Migration**: Hibernate auto-update for development
- **CORS**: Configured for frontend integration
- **API Documentation**: Swagger UI available at `/swagger-ui.html`

## Future Enhancements

- Global exception handler implementation
- AOP for cross-cutting concerns (logging, security)
- Multi-threading for performance optimization
- Immutable collections for data safety
- Records adoption for data classes
- Advanced audit logging
- Rate limiting and throttling
- API versioning strategy</content>
<parameter name="filePath">/Users/dhaksana/Documents/GitHub/MoneyTransferSystem/Backend/README.md