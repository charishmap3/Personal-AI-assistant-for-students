# 🎓 Personal AI Assistant for Students

A smart, AI-powered web application designed to help students manage their academic workload efficiently by providing automated summaries, flashcards, quizzes, and organized study tools — all in one unified platform.

---

## 📌 Project Overview

In today’s academic environment, students rely on multiple disconnected tools for reading PDFs, making notes, revising concepts, and practicing quizzes. This fragmentation leads to wasted time, poor organization, and reduced productivity.

**Personal AI Assistant for Students** solves this problem by offering a single, centralized system that integrates AI-driven academic features such as content summarization, flashcard generation, and MCQ-based quizzes. The application acts as a personalized digital study companion, simplifying learning and improving focus.

---

## ✨ Key Features

- 🔐 Secure Login & Signup (Spring Boot + MySQL)
- 📄 PDF / PPT Text Extraction
- 🧠 AI-Based Content Summarization
- 🃏 Automatic Flashcard (Q&A) Generation
- 📝 MCQ Quiz Generator for Exam Practice
- 🎨 Personalized Dashboard with Mood-Based Themes
- 🗂️ Centralized Storage of Study Materials
- ⚡ Fast and Responsive User Interface

---

## 🛠️ Technologies Used

### Backend
- Java
- Spring Boot
- Spring MVC
- Hibernate / JPA
- REST APIs

### Frontend
- HTML
- CSS
- JavaScript
- Thymeleaf

### Database
- MySQL

### AI & File Processing
- Natural Language Processing (AI API)
- Apache PDFBox (PDF extraction)
- Apache POI (PPT processing)

---

## 🏗️ System Architecture

The application follows a **three-tier architecture**:

1. **Presentation Layer**  
   User interface built using HTML, CSS, JavaScript, and Thymeleaf.

2. **Application Layer**  
   Spring Boot backend handling authentication, AI processing, business logic, and API requests.

3. **Data Layer**  
   MySQL database for storing user data, summaries, flashcards, quizzes, and history.

An external AI module is integrated to generate summaries, flashcards, and MCQs from extracted content.

---

## ⚙️ How It Works

1. User logs in or registers securely.
2. Uploads a PDF / PPT or enters text.
3. Backend extracts and processes content.
4. AI module generates:
   - Summaries
   - Flashcards
   - MCQ quizzes
5. Results are displayed on the dashboard and stored in the database.

---

## 🚀 How to Run the Project

### Prerequisites
- Java (JDK 8 or above)
- Maven
- MySQL
- IDE (VS Code / IntelliJ / Eclipse)

## 📸 Output Screenshots

### 🏠 Home / Dashboard
![Home Dashboard](screenshots/home.png)

### 📄 File Summary Generator
Upload PDF/PPT files and get AI-generated summaries instantly.
![Summary Generator](screenshots/summary.png)

### 🃏 Flashcard Generator
Automatically generates question–answer flashcards for quick revision.
![Flashcards](screenshots/flashcards.png)

### 📝 AI Quiz Generator
Generate MCQs from uploaded content and test your understanding.
![Quiz Page](screenshots/quiz.png)

### 📊 Quiz Result
Instant score evaluation with correct and wrong answers highlighted.
![Quiz Result](screenshots/quiz-result.png)
