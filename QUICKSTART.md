# MentalMind - Quick Start Guide

## ✅ Application Status
The **MentalMind Spring Boot application is now running successfully** on **http://localhost:8080**

## 🚀 How to Access

Open your browser and navigate to:
```
http://localhost:8080
```

## 📋 Available Routes

### Authentication
- **Login Page**: `http://localhost:8080/login`
- **Register Page**: `http://localhost:8080/register`
- **Logout**: `http://localhost:8080/logout`

### Student Pages
- **Dashboard**: `/student/dashboard`
- **Learning Modules**: `/student/learning`
- **Mood Tracker**: `/student/mood-tracker`
- **Forum**: `/student/forum`
- **Appointments**: `/student/appointments`
- **Resources**: `/student/resources`
- **Emergency**: `/student/emergency`
- **Recommendations**: `/student/recommendations`
- **Feedback**: `/student/feedback`

### Counselor Pages
- **Dashboard**: `/counselor/dashboard`
- **Appointments**: `/counselor/appointments`
- **Students**: `/counselor/students`
- **Resources**: `/counselor/resources`
- **Forum**: `/counselor/forum`
- **Reports**: `/counselor/reports`
- **Settings**: `/counselor/settings`

## 🔐 Current Authentication
**Note**: The app is currently running in test mode without database integration.
- Any non-empty email/password combination will allow login
- Credentials are stored in session only

## 📝 Test Accounts (for future database integration)
- **Student**: `student@example.com` / `password123` (Role: student)
- **Counselor**: `counselor@example.com` / `password123` (Role: counselor)

## 🛠 To Stop the Server
Press `Ctrl+C` in the terminal where the application is running.

## 🔄 To Restart the Server
```bash
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"
cd C:\MentalMind
.\mvnw.cmd spring-boot:run
```

## 📚 Features Implemented

### Controllers
- ✅ **AuthController** - Login, Register, Logout functionality
- ✅ **StudentController** - All student endpoints with session validation
- ✅ **CounselorController** - All counselor endpoints with session validation

### Templates
- ✅ Login page
- ✅ Register page
- ✅ Student dashboard pages (8 pages)
- ✅ Counselor dashboard pages (7 pages)
- ✅ Responsive design with TailwindCSS

## 🔜 Next Steps

### To Integrate with Database
1. Set up MySQL database using the provided script
2. Re-add Spring Data JPA and Hibernate dependencies to pom.xml
3. Restore User entity, UserRepository, and AuthenticationService
4. Update application.properties with database credentials

### Security Improvements Needed
- ⚠️ Add password hashing (BCrypt)
- ⚠️ Implement CSRF protection
- ⚠️ Add input validation
- ⚠️ Use HTTPS in production
- ⚠️ Add role-based access control

## 📧 Form Submission

Currently, all POST endpoints accept form submissions and redirect with success/error query parameters:
- Success: `?success=<action>`
- Error: `?error=invalid`

These can be used to display user feedback in the templates.

---
**Last Updated**: December 23, 2025
**Java Version**: 21
**Spring Boot Version**: 4.0.0
