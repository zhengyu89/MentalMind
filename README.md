# MindWell - Digital Mental Health Literacy Hub 🧠💚

A comprehensive web-based mental health support platform designed for Malaysian university students. Built with Spring Boot MVC and Thymeleaf, featuring interactive learning modules, mood tracking, peer support forums, and counselor management tools.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-green)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-blue)
![TailwindCSS](https://img.shields.io/badge/TailwindCSS-CDN-cyan)

## ✨ Features

### For Students
| Feature | Description |
|---------|-------------|
| **Dashboard** | Personal overview with stats, quick access cards, and upcoming appointments |
| **Learning Modules** | Interactive mental health education with video content and quizzes |
| **Mood Tracker** | Daily mood logging with PSS-4 stress self-assessment |
| **Peer Support Forum** | Anonymous discussion forum with likes, comments, and topic filtering |
| **Resources Library** | Curated articles, videos, and guides with search and filtering |
| **Appointments** | Book counseling sessions with available counselors |
| **Emergency Help** | Quick access to crisis hotlines and immediate support |
| **Personalized Recommendations** | AI-suggested resources based on assessment results |
| **Feedback System** | Submit feedback, bug reports, and suggestions |

### For Counselors
| Feature | Description |
|---------|-------------|
| **Dashboard** | Overview of appointments, flagged students, and recent assessments |
| **Student Management** | View all students with risk levels and assessment history |
| **Appointment Management** | Accept/reject requests and manage daily schedule |
| **Resource Management** | Upload and manage mental health resources |
| **Forum Moderation** | Review flagged posts and moderate content |
| **Reports & Analytics** | Platform usage statistics and insights |
| **Settings** | Profile management and notification preferences |

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot 4.0.0, Spring MVC
- **Template Engine**: Thymeleaf
- **Styling**: TailwindCSS (CDN), Google Fonts (Outfit)
- **Icons**: Material Symbols
- **Build Tool**: Maven

## 📋 Prerequisites

- Java Development Kit (JDK) 21 or higher
- Maven 3.9+
- VS Code (recommended) or any Java IDE

## 🚀 Setup Instructions (VS Code)

### 1. Clone the Repository
```bash
git clone https://github.com/zhengyu89/MentalMind.git
```

### 2. Install Required VS Code Extensions

Open VS Code and install these extensions:

1. **Extension Pack for Java** (Microsoft)
   - Search: `vscjava.vscode-java-pack`
   - Includes: Language Support, Debugger, Test Runner, Maven, Project Manager

2. **Spring Boot Extension Pack** (VMware)
   - Search: `vmware.vscode-boot-dev-pack`
   - Includes: Spring Boot Tools, Spring Initializr, Spring Boot Dashboard

### 3. Open the Project
In VS Code: `File > Open Folder > Select MentalMind folder`

### 4. Wait for Dependencies
VS Code will automatically:
- Detect the Maven project
- Download dependencies
- Index the project

Check the bottom status bar for progress.

### 5. Run the Application

**Option A: Using Spring Boot Dashboard**
1. Open Spring Boot Dashboard (left sidebar, Spring icon)
2. Click ▶️ next to `MentalMindApplication`

**Option B: Using Terminal**
```bash
mvn spring-boot:run
```

**Option C: Using Run Button**
1. Open `MentalMindApplication.java`
2. Click `Run` above the `main` method

### 6. Access the Application
Open your browser and go to:
```
http://localhost:8080/login
```

## 📁 Project Structure

```
MentalMind/
├── src/main/java/com/example/MentalMind/
│   ├── MentalMindApplication.java      # Main entry point
│   └── controller/
│       ├── AuthController.java          # Login/logout handling
│       ├── StudentController.java       # Student routes
│       └── CounselorController.java     # Counselor routes
│
├── src/main/resources/
│   ├── templates/
│   │   ├── fragments/                   # Reusable components
│   │   │   ├── layout.html              # Base HTML head
│   │   │   ├── sidebar-student.html     # Student navigation
│   │   │   └── sidebar-counselor.html   # Counselor navigation
│   │   ├── student/                     # Student pages
│   │   │   ├── dashboard.html
│   │   │   ├── learning.html
│   │   │   ├── mood-tracker.html
│   │   │   ├── forum.html
│   │   │   ├── resources.html
│   │   │   ├── appointments.html
│   │   │   ├── emergency.html
│   │   │   ├── recommendations.html
│   │   │   └── feedback.html
│   │   ├── counselor/                   # Counselor pages
│   │   │   ├── dashboard.html
│   │   │   ├── appointments.html
│   │   │   ├── students.html
│   │   │   ├── resources.html
│   │   │   ├── forum.html
│   │   │   ├── reports.html
│   │   │   └── settings.html
│   │   └── login.html                   # Shared login page
│   └── application.properties
│
└── pom.xml                              # Maven dependencies
```

## 🔑 Usage

### Login
1. Visit `http://localhost:8080/login`
2. Toggle between **Student** or **Counselor** role
3. Enter any email/password (mock authentication)
4. Click **Login**

### Student Features
- **Forum**: Like posts (❤️), expand comments, create new posts
- **Mood Tracker**: Select emoji, take stress assessment quiz
- **Learning**: Watch videos, complete quizzes
- **Resources**: Search, filter by type, save to reading list

### Counselor Features
- **Dashboard**: View flagged students, manage appointments
- **Students**: Search and filter student list by risk level
- **Forum Moderation**: Review and moderate flagged posts

## 🎨 Design

- Modern, clean UI with dark mode support
- Gradient color scheme (Indigo/Purple primary)
- Responsive layout for all screen sizes
- Malaysian localization (names, language hints)

## 📝 License

This project is developed for educational purposes as part of the Internet Programming course (Y3S1) at Universiti Teknologi Malaysia (UTM).

## 👥 Contributors

- Tan Zheng Yu - Developer
- Chu Chen Qing - Developer
- Teow Zi Xian - Developer
- Tan Zhen Li - UI/UX Designer
- Benjamin Chew Jun Jie - Developer

---

Made with 💜 for student mental wellness
