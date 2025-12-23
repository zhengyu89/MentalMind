# MentalMind Database Setup Guide

## Prerequisites
- MySQL Server installed and running
- MySQL Command Line Client or MySQL Workbench

## Setup Steps

### 1. Create Database
You have two options:

#### Option A: Using MySQL Command Line
```bash
mysql -u root -p < database-init.sql
```

#### Option B: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to your MySQL server
3. Open the `database-init.sql` file
4. Execute the script (Cmd + Enter or Ctrl + Enter)

### 2. Verify Database Creation
```bash
mysql -u root -p
mysql> USE mentalmind;
mysql> SHOW TABLES;
mysql> SELECT * FROM users;
```

You should see:
- ✓ Database `mentalmind` created
- ✓ `users` table created
- ✓ 2 sample users inserted (student@example.com, counselor@example.com)

### 3. Update Database Credentials (if needed)
If your MySQL credentials are different, update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mentalmind?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root          # Change this if needed
spring.datasource.password=root          # Change this if needed
```

### 4. Run the Application
```bash
# Set JAVA_HOME if needed
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"

# Run the application
.\mvnw.cmd spring-boot:run
```

### 5. Test Authentication
Open your browser and go to `http://localhost:8080`

**Test Credentials:**

Student Account:
- Email: `student@example.com`
- Password: `password123`
- Role: `student`

Counselor Account:
- Email: `counselor@example.com`
- Password: `password123`
- Role: `counselor`

## Authentication Features Implemented

✅ User Registration - Create new accounts with validation
✅ User Login - Authenticate with database credentials
✅ Session Management - Store user info in session
✅ Logout - Clear session data
✅ Role-based Redirects - Students and Counselors go to different dashboards

## Important Notes

⚠️ **Security Warning**: 
- Passwords are currently stored in plain text (FOR DEVELOPMENT ONLY)
- In production, implement BCrypt or similar password hashing
- Use HTTPS for all connections
- Implement CSRF protection
- Add proper input validation and SQL injection prevention

## Database Schema

```
users table:
- id (BIGINT, PRIMARY KEY)
- email (VARCHAR, UNIQUE)
- password (VARCHAR)
- role (VARCHAR) - 'student' or 'counselor'
- full_name (VARCHAR)
- phone_number (VARCHAR)
- is_active (BOOLEAN)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

## Next Steps

1. Add password hashing with BCrypt
2. Implement role-based access control (RBAC)
3. Add email verification
4. Create additional entities for Appointments, Forums, Resources, etc.
5. Add form validation
6. Implement error handling and logging
