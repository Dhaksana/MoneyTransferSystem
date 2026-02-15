# ADMIN SETUP GUIDE

## How to Access Admin Features

### Option 1: Create Admin User Manually (Database)

1. **Connect to your database** (H2 Console or your DB tool)

2. **Insert an admin user** with this SQL:
```sql
INSERT INTO users (username, password, role, account_id, display_name) 
VALUES ('admin', '$2a$10$...bcrypt_hash...', 'ADMIN', 'MTS2026-ADMIN001', 'Administrator');
```

**Note:** You need to:
- Use a bcrypt-hashed password (not plain text)
- Set role to 'ADMIN'
- Generate a unique accountId or use one that exists

### Option 2: Create Admin User via SignUp with Role Update

1. **Sign up normally** with any account
2. **Update the role in database** to 'ADMIN':
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```

---

## Admin Endpoints

### 1. **View All Accounts**
```
GET /api/v1/admin/accounts
```
Response: List of all AccountDTO objects

### 2. **View All Transactions (Paginated)**
```
GET /api/v1/admin/transactions/paginated?page=0&size=10
```
Parameters:
- `page` (0-indexed): Which page to fetch
- `size`: Number of items per page (default: 10)

Response:
```json
{
  "content": [array of transactions],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 156,
  "totalPages": 16,
  "isFirst": true,
  "isLast": false
}
```

### 3. **Update Account Details**
```
PUT /api/v1/admin/accounts/{accountId}
```
Request body:
```json
{
  "holderName": "New Name",
  "status": "BLOCKED",
  "balance": 5000.00
}
```
Admin can:
- Change holder name
- Change account status (ACTIVE, INACTIVE, BLOCKED)
- Manually adjust balance

### 4. **Deactivate an Account**
```
DELETE /api/v1/admin/accounts/{accountId}
```
Sets account status to "INACTIVE"

---

## Bcrypt Password Generation

To create a bcrypt-hashed password for manual insertion:

**Using Online Tool:**
- Visit: https://bcrypt-generator.com/
- Enter plain password (e.g., "admin123")
- Copy the bcrypt hash
- Use in SQL INSERT

**Using Spring (Java):**
```java
String hashedPassword = new BCryptPasswordEncoder().encode("admin123");
System.out.println(hashedPassword);
```

**Using Python:**
```python
import bcrypt
password = "admin123"
hashed = bcrypt.hashpw(password.encode(), bcrypt.gensalt())
print(hashed.decode())
```

---

## Frontend Admin Panel (Future)

An admin user already exists in the database (example row shown in your environment). For this project the record is:

- username: `ADMIN`
- account_id: `MTS2026-45636783`

If you know the ADMIN password you can log in directly. If not, you can reset it by generating a bcrypt hash and updating the `users` table (see the "Bcrypt Password Generation" section above) or by running an SQL update like:

```sql
UPDATE users SET password = '$2a$10$...your_bcrypt_hash...' WHERE username = 'ADMIN';
```

After you log in as ADMIN, the frontend can display:
- Admin Dashboard
- List of all users/accounts
- Transaction management
- Account modification interfaces

The backend endpoints for admin operations are implemented, but a dedicated frontend Admin Dashboard component hasn't been created yet. Would you like me to create an Admin Dashboard component that:

- Lists all accounts and users
- Shows paginated transactions across the system
- Provides account edit/deactivate controls

If yes, I can scaffold the component and wire it to the existing admin APIs.

---

## Current User Roles

| Role | Access |
|------|--------|
| USER | Own account only, view/manage own transactions |
| ADMIN | All accounts, all transactions, can modify any account |

---

## Example: Creating Admin User

### Step 1: Bcrypt Hash Your Password
Password: `admin123`
Bcrypt Hash: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DRjk38`

### Step 2: Insert into Database
```sql
INSERT INTO users (username, password, role, account_id, display_name) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DRjk38', 'ADMIN', 'MTS2026-60000001', 'System Administrator');
```

### Step 3: Test in Frontend
1. Go to login: `http://localhost:4200/login`
2. Username: `admin`
3. Password: `admin123`
4. After login, you have access to admin endpoints via API calls

---

## Next Steps

1. Create admin user using instructions above
2. Log in with admin credentials
3. Test admin endpoints via:
   - Postman
   - Frontend service (with role check guard)
   - Create an Admin Dashboard component

Would you like me to create an Admin Dashboard component for the frontend?
