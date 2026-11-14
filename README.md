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

## 🎯 Recommendation Engine

Skill-based matching between **users** and **projects**. Users store their skills, projects list their
technologies; both are normalized (trimmed, lowercase, de-duplicated) when saved, so `" Java "` and `"java"` are the same skill.

### Scoring formula

For a user's skill set `U` and a project's technology set `P`:

```
matchScore = |U ∩ P| / |P|          # how much of what the project needs the user covers
jaccard    = |U ∩ P| / |U ∪ P|      # overall similarity
finalScore = 0.7 * matchScore + 0.3 * jaccard
           + 0.05                   # only if the project was created in the last 14 days
```

- The final score is capped at `1.0` and returned as an integer **0–100**.
- Results are sorted by score (highest first); ties are broken alphabetically.
- Anything with **no skill in common is excluded** (score 0), even if the project is new.
- For *developer* recommendations the same formula is used (`U` = developer skills, `P` = project technologies), without the recency boost.
- Excluded when recommending projects: projects you own, projects you are a member of, and projects you already sent a join request to.
- Excluded when recommending developers: the owner and all existing members.
- Candidates are pre-filtered in MongoDB (`technologies IN userSkills`) so not every project is loaded; at most 1000 candidates are scored per request.

> Note: the match is an exact match on normalized names. Projects created before this feature may still store
> technologies with their original casing (e.g. `"Java"`) and won't be matched until they are re-saved in lowercase.

### Endpoints

All endpoints require a JWT (`Authorization: Bearer <token>`). Paths are relative to the context path (`/teamfinder`).

| Method | Path | Description |
|--------|------|-------------|
| `PUT` | `/api/user/skills` | Replace your own skills. Body: JSON array of strings. `400` if empty/blank (max 50 skills, 50 chars each). |
| `GET` | `/recommendations/projects?page=0&size=10` | Best projects for the logged-in user. `400` if you have no skills yet. |
| `GET` | `/recommendations/projects/{projectId}/developers?page=0&size=10` | Best developers for a project. **Owner only** (`403` otherwise, `404` if the project doesn't exist). |

`size` must be between 1 and 50. Responses never include email, password or roles.

### Sample

```http
PUT /teamfinder/api/user/skills
Authorization: Bearer <token>
Content-Type: application/json

["Java", "Spring Boot", "react"]
```
```json
{ "skills": ["java", "spring boot", "react"] }
```

```http
GET /teamfinder/recommendations/projects?page=0&size=10
Authorization: Bearer <token>
```
```json
{
  "content": [
    {
      "projectId": "65f1a2b3c4d5e6f708091a2b",
      "title": "Team Chat App",
      "score": 85,
      "matchedSkills": ["java", "spring boot", "react"],
      "missingSkills": ["mongodb"]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

Developer recommendations return `userName` instead of `projectId`/`title`:

```json
{ "userName": "alice", "score": 72, "matchedSkills": ["java", "mongodb"], "missingSkills": ["react"] }
```

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


```plaintext
TeamFinder/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/teamfinder/
│   │   │       ├── Controller/
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── ProjectController.java
│   │   │       │   └── ProjectMembershipController.java
│   │   │       ├── Entity/
│   │   │       │   ├── User.java
│   │   │       │   └── ProjectEntry.java
│   │   │       ├── Repository/
│   │   │       │   ├── UserRepository.java
│   │   │       │   └── ProjectRepository.java
│   │   │       ├── Security/
│   │   │       │   ├── JwtAuthFilter.java
│   │   │       │   ├── JwtUtil.java
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── Service/
│   │   │       │   ├── UserService.java
│   │   │       │   ├── ProjectService.java
│   │   │       │   └── ProjectMembershipService.java
│   │   │       └── TeamFinderApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   └── test/
│       └── java/
├── pom.xml
└── README.md

