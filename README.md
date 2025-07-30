
# 🧠 SmartDesk: Employee Tracking and Performance Evaluation Collaboration System

SmartDesk is an intelligent enterprise solution that integrates employee hiring, tracking, performance evaluation, and collaboration into one seamless system. Designed as a hybrid (web + desktop) application, it empowers organizations to manage their workforce more effectively with AI and automation.

---

## 📌 Key Modules

### 🔹 1. Web Portal (Hiring System)
- Resume analysis using ATS (Applicant Tracking System)
- Document verification and candidate screening
- Admin Dashboard for hiring decision
- Dynamic offer letter generation
- Django-based backend with Bootstrap frontend

### 🔹 2. Desktop App (Main System)
- Secure login using facial recognition
- Role-based access: Developer, Team Lead, Company Admin
- Real-time activity monitoring (applications used, code quality, time management)
- Performance scoring system with credit points
- ML-based project recommendations
- Local restrictions (USB/data sharing blocking)
- Built with JavaFX UI and Spring Boot backend

### 🔹 3. Real-Time Communication
- Video conferencing and project meeting scheduling
- Chat and alert systems (WebSocket)
- Jitsi integration for video calls

---

## 🧠 Technologies Used

| Layer        | Tech Stack                                      |
|--------------|--------------------------------------------------|
| Frontend     | JavaFX, Bootstrap, HTML/CSS, JavaScript          |
| Backend      | Spring Boot, Django, Flask                       |
| Database     | MySQL, Hibernate                                 |
| AI/ML        | Python, Flask, `face_recognition`, sklearn       |
| Real-Time    | WebSocket, Jitsi Meet                            |
| DevOps/Git   | GitHub, Git CLI                                  |

---

## 🛡️ Security Features

- Facial recognition-based login (Python + OpenCV)
- External device restriction (USB, drives)
- Controlled file sharing
- Encrypted data transmission

---

## 🎯 Roles in the System

1. **Company Owner/Admin**  
   - Full access to performance data, hiring, and user control

2. **Team Leads (My Team)**  
   - Assign tasks, schedule meetings, monitor team performance

3. **Employees (Developers)**  
   - Work dashboard, feedback, and performance history

---

## 📂 Project Structure
<pre> ```
SmartDesk/
├── web-portal/
│   ├── compWeb/                  # Django web portal
│   ├── templates/                # Frontend templates (AdminDashboard, login, etc.)
│   └── members/                  # ATS, hiring logic
├── desktop-app/
│   ├── src/                      # JavaFX UI
│   ├── backend/                  # Spring Boot services
│   └── websocket/                # Real-time module
├── ai-services/
│   ├── face_recognition/         # Facial recognition API
│   └── ml_engine/                # Performance recommendation engine
└── README.md

```</pre>

---

## 🛠️ Setup Instructions

### 1. Web Portal (Django)
```bash
cd web-portal/compWeb
pip install -r requirements.txt
python manage.py makemigrations
python manage.py migrate
python manage.py runserver
