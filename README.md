# 🎭 Cultify – Cultural Interaction Platform

**Cultify** is a modular Java-based desktop application designed to promote cultural exploration, discussion, and interaction. Developed as a student project at **ESPRIT**, the platform brings together a wide array of cultural content such as events, philosophical discussions, scientific discoveries, and user expression through interactive canvases.

---

## 🧰 Tech Stack

- **Java Core 17**
- **JavaFX** for the GUI
- **Maven** for dependency management
- **FXML** for view definitions
- **MySQL** (depending on module) for data persistence

---

## 🚀 Features

### 👤 User Management
- Secure login/logout system
- Role-based access (user, admin, superadmin)
- Profile editing and password management

### 📚 Cultural Encyclopedia
- Browse and explore articles on philosophy, astronomy, and more
- Search and filter content by category

### 🗓️ Events & Offers
- View upcoming cultural events
- Propose and manage new event listings

### 📢 News & Updates
- Stay up-to-date with the latest cultural news
- Admins can publish articles and updates

### 📥 Reclamations
- Users can submit feedback or complaints
- Admins can respond through a dedicated dashboard

### 🖼️ Interactive Canvas [NEW]
- Users can create visual canvases to express ideas or start discussions
- Other users can **react** (like/dislike) and **comment** on these canvases
- Encourages collaborative creativity and conversation

---

## 🧑‍💻 Project Structure

```plaintext
Cultify/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controllers/
│   │   │   ├── models/
│   │   │   ├── services/
│   │   │   └── utils/
│   │   └── resources/
│   └── test/
├── pom.xml
└── README.md
