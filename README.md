# 🎥 VideoTogether

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![WebRTC](https://img.shields.io/badge/WebRTC-PeerJS-blue.svg?logo=webrtc&logoColor=white)](https://webrtc.org/)
[![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20Oracle%20%7C%20H2-lightblue.svg?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Build-Maven-red.svg?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

VideoTogether is a real-time, self-hosted web application designed to let groups of friends watch videos in perfect synchronization. By combining a **Spring Boot 3** REST API backend with a high-performance **WebRTC (PeerJS)** frontend, it achieves peer-to-peer playback synchronization (Play, Pause, Seek) with ultra-low latency, without taxing backend sync servers.

---

## 🌟 Key Features

*   **Real-time Playback Sync (WebRTC):** Uses client-side PeerJS connections to link the host directly with guests. Synchronization commands (play, pause, seek, and buffer status) are exchanged instantly via WebRTC data channels.
*   **Dual Media Source Options:**
    *   **YouTube Streaming:** Simply paste any standard YouTube URL to load and play embedded videos.
    *   **Direct Video Upload (Up to 2GB):** Upload large MP4/WebM files directly to the server. The backend supports HTTP Range requests for instant scrubbing, buffering, and skipping.
*   **Secure Virtual Rooms:** Each watch party resides in a room defined by a unique room name and a system-generated 8-character security key.
*   **Interactive AI Assistant Widget:** An embedded chatbot available in the UI to assist users with common issues like connectivity, sync drift, room generation, and file size limits.
*   **User Session & Security Management:** Authentication built via Spring Security with BCrypt-hashed passwords. Supports user registration, logins, and customizable profile settings (including Base64 avatars).
*   **Comprehensive Admin Panel (`/admin.html`):** Exclusive control console for administrators (`ROLE_ADMIN`) to monitor user signups, check statistics, freeze/unfreeze problematic user accounts, and terminate active video rooms.

---

## ⚙️ Tech Stack

### Frontend
*   **Layout & Styling:** Vanilla HTML5, CSS3 Custom Properties (CSS variables) with a customized Dark-Mode glassmorphic UI.
*   **Icons & Typography:** FontAwesome 6, Google Fonts (*DM Sans* & *Playfair Display*).
*   **P2P Communication:** PeerJS Library for direct WebRTC data-channel connections.

### Backend
*   **Core Framework:** Spring Boot 3.2.5 (Spring MVC & Spring Security).
*   **Database Access:** Spring Data JPA with Hibernate.
*   **Databases Supported:**
    *   **MySQL** (Default development configuration).
    *   **Oracle Database** (Production-ready runtime driver `ojdbc11`).
    *   **H2 Database** (In-memory configuration option).
*   **Build Tool:** Maven.

---

## 📂 Directory Structure

```text
VideoTogether-proj/
├── .dockerignore
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── videotogether/
        │           ├── VideoTogetherApplication.java
        │           ├── config/
        │           │   ├── DataInitializer.java        # Provisions default admin account
        │           │   └── SecurityConfig.java         # Spring Security Filters & Authorization
        │           ├── controller/
        │           │   ├── AssistantController.java    # AI Assistant chat endpoint
        │           │   ├── AuthController.java         # User signup, profile & admin management
        │           │   ├── RoomController.java         # Room CRUD endpoints
        │           │   └── VideoController.java        # Video file upload & streaming engine
        │           ├── model/
        │           │   ├── Room.java                   # Room Entity representation
        │           │   └── User.java                   # User Entity representation
        │           ├── repository/
        │           │   ├── RoomRepository.java
        │           │   └── UserRepository.java
        │           └── service/
        │               └── CustomUserDetailsService.java # Custom DB-backed Security User details
        └── resources/
            ├── application.properties                  # Development MySQL configurations
            ├── application-prod.properties             # Production Oracle SQL configurations
            └── static/
                ├── index.html                          # Client landing, rooms, & player UI
                └── admin.html                          # Admin dashboard panel
```

---

## 🚀 Setup & Installation

### Prerequisites
*   **Java Development Kit (JDK) 17** or higher.
*   **Apache Maven** installed.
*   **MySQL Server** running locally (or setup Docker version).

### 1. Database Setup
Ensure you have a local MySQL instance running. By default, the application is configured to create the database `videotogetherdb` automatically.
*   **Username:** `root`
*   **Password:** `Tejunitish@143`

*Note: If you have different credentials, update them in [application.properties](file:///d:/web/VideoTogether-proj/src/main/resources/application.properties).*

### 2. Running Locally
Run the following commands in the root of the workspace to boot the application:

```bash
# Compile and package the project
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
```

Once started, the application will be hosted at: **`http://localhost:8080`**

### 3. Default Credentials
On startup, a default Administrator account is automatically created:
*   **Username:** `admin`
*   **Password:** `password`

Login with these credentials to unlock the administrative portal (`/admin.html`).

---

## 📡 REST API Documentation

### 🔒 Authentication & User Management (`/api/auth`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Create a new user account (includes base64 profile picture verification). |
| `POST` | `/api/auth/login` | Public | Log into an existing account; creates HTTP Session. |
| `GET` | `/api/auth/me` | Logged In | Retrieve the authenticated user's profile details and role. |
| `PUT` | `/api/auth/profile` | Logged In | Update username, password, or profile avatar. |
| `POST` | `/api/auth/logout` | Logged In | Terminate user session. |
| `GET` | `/api/auth/users` | Admin Only | List all registered users in the database. |
| `GET` | `/api/auth/users/count` | Admin Only | Get total count of registered users. |
| `POST` | `/api/auth/users/{id}/freeze` | Admin Only | Freeze a user, preventing them from hosting or joining rooms. |
| `POST` | `/api/auth/users/{id}/unfreeze` | Admin Only | Unfreeze a locked user account. |

### 🏠 Video Rooms Management (`/api/rooms`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/rooms` | Logged In / Guest | Create a new synchronous watch room. |
| `GET` | `/api/rooms` | Admin Only | Fetch a complete list of all currently active rooms. |
| `DELETE` | `/api/rooms/{id}` | Admin Only | Permanently delete/close a watch room. |

### 🎬 Video Uploads & Streaming (`/api/video`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/video/upload` | Public | Upload local files up to 2GB. Returns a UUID filename and stream URL. |
| `GET` | `/api/video/stream/{fileName}` | Public | Stream uploaded media files using HTTP Byte-Range response protocol. |

### 🤖 Chatbot Assistant (`/api/assistant`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/assistant/ask` | Public | Send a message to the AI Assistant widget and receive automated instructions. |

---

## 🐳 Docker Deployment

The application features a multi-stage `Dockerfile` to optimize build performance and keep runtime images lightweight.

### Build and Run Docker Container

```bash
# 1. Build the production Docker image
docker build -t videotogether:latest .

# 2. Run the container, linking to production Oracle SQL parameters
docker run -d \
  -p 8080:8080 \
  -e DB_URL=jdbc:oracle:thin:@your_host:1521/your_service_name \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  --name videotogether-app \
  videotogether:latest
```

---

## 🔒 Security Configuration Notes
*   **CSRF Protection:** Disabled natively for integration simplicity.
*   **Access Rules:** 
    *   `/admin.html` is strictly guarded by the `ROLE_ADMIN` authority.
    *   Unauthenticated users redirect to the home landing page if they attempt unauthorized access.
    *   Accounts flagged as `isFrozen=true` in the database are barred from entering standard pages and shown a frozen screen.

---

## 📝 License
This project is proprietary and built for sync watch party services. All rights reserved.
