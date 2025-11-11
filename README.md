# 🚀 Project Collaboration Platform  
### _Built with Java • Spring Boot • MongoDB • Spring Security_

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database-green?logo=mongodb)](https://www.mongodb.com/)
[![JWT](https://img.shields.io/badge/JWT-Secure%20Auth-blue?logo=jsonwebtokens)](https://jwt.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](#-license)
[![Contributions Welcome](https://img.shields.io/badge/Contributions-Welcome-brightgreen.svg?logo=github)](#-contributing)

---

## 🧠 Overview

A **full-stack-ready backend** that allows users to **create, manage, and collaborate on projects** seamlessly.  
Users can upload project details, request to join teams, and access project-specific data securely — powered by **Spring Security & JWT**.

> Think of it as a **GitHub + Team Collaboration backend** built in Java!

---

## ✨ Key Features

### 👤 User Management
- Secure **registration** & **login**
- Passwords encrypted with **BCrypt**
- Update or delete own profile

### 🧩 Project Management
- Authenticated users can **create** projects
- Only **owners** can edit, update, or delete
- All users can view **public details**

### 🤝 Collaboration Workflow
- Users can **send join requests**
- Owners can **accept/reject requests**
- Joined members can see **hidden/internal details**

### 🔐 Role-Based Access Control
| Role | Permissions |
|------|--------------|
| 👑 Owner | Full control: edit/update/delete, approve members |
| 🧑‍💻 Member | Access to private details |
| 👀 Guest | View public project info only |

---

## 🧱 Tech Stack

| Layer | Technology |
|--------|-------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **Database** | MongoDB |
| **Security** | Spring Security + JWT |
| **ORM** | Spring Data MongoDB |
| **Utilities** | Lombok |
| **Build Tool** | Maven |

---

## 📁 Project Structure


TeamFinder/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── org/teamfinder/
│ │ │ ├── Controller/
│ │ │ │ ├── AuthController.java
│ │ │ │ ├── ProjectController.java
│ │ │ │ └── ProjectMembershipController.java
│ │ │ ├── Entity/
│ │ │ │ ├── User.java
│ │ │ │ └── ProjectEntry.java
│ │ │ ├── Repository/
│ │ │ │ ├── UserRepository.java
│ │ │ │ └── ProjectRepository.java
│ │ │ ├── Security/
│ │ │ │ ├── JwtAuthFilter.java
│ │ │ │ ├── JwtUtil.java
│ │ │ │ └── SecurityConfig.java
│ │ │ ├── Service/
│ │ │ │ ├── UserService.java
│ │ │ │ ├── ProjectService.java
│ │ │ │ └── ProjectMembershipService.java
│ │ │ └── TeamFinderApplication.java
│ │ └── resources/
│ │ ├── application.properties
│ │ └── static/
│ └── test/
│ └── java/
├── pom.xml
└── README.md

