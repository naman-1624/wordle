[# Wordle+ 🎮

A feature-rich, full-stack Wordle application featuring server-side game logic and three unique, fast-paced game modes not found in the original game. Built with a robust **Spring Boot** backend, **PostgreSQL** database, and fully containerized with **Docker** for seamless deployment.

🔗 **Live Demo:** [Wordle+ on Render](https://wordle-db-n9b5.onrender.com/)  
*(Note: Hosted on Render's free tier. The first load after periods of inactivity may take 1–2 minutes to spin up the container. Subsequent loads are instant.)*

---

## 🚀 Features

Unlike standard front-end clones, **Wordle+** is engineered with production-grade backend rigor. 

*   **🔒 Secure Server-Side Logic:** All guess validations, word selections, and state tracking occur strictly on the server. The target word and remaining attempts cannot be exposed or manipulated via browser developer tools.
*   **📚 Expanded Vocabulary:** Upgraded the traditional 700-word dataset to a massive **13,000+ entry database**. This expansion reduces invalid word rejections ("false negatives") by roughly 40% during natural gameplay.
*   **🏆 Custom Game Modes:**
    *   **Normal:** The classic daily puzzle experience.
    *   **60 Seconds:** A high-pressure, race-against-the-clock challenge.
    *   **Sudden Death:** Ultimate high-stakes mode—one single incorrect guess ends the entire round.

---

## 🛠️ Tech Stack

| Layer | Tools & Technologies |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot, RESTful APIs |
| **Database** | PostgreSQL (Neon Serverless) |
| **Frontend** | HTML5, CSS3, JavaScript (Vanilla ES6) |
| **DevOps & Hosting** | Docker, Render |

---

## 📦 Local Installation & Setup

Follow these steps to clone, configure, and run the project locally on your machine.

### Prerequisites
*   Java 17 or higher Installed
*   Docker (Optional, for containerized execution)

### 1. Clone the Repository
```bash
git clone https://github.com/namanWordle.git
cd Wordle
```

### 2. Run via Maven Wrapper
Execute the application directly using the included Maven wrapper script:
```bash
./mvnw spring-boot:run
```

### 3. Run via Docker
Alternatively, you can build and run the application as a localized Docker container:
```bash
# Build the Docker image
docker build -t wordle-app .

# Run the container mapping to port 8080
docker run -p 8080:8080 wordle-app
```
Once started, open your browser and navigate to `http://localhost:8080`.

---

## 🚧 Known Limitations & Next Steps

*   **Caching Layer:** Word lookups currently hit the PostgreSQL database directly. Implementing a Redis caching layer for the word dictionary is a planned optimization.
*   **Testing Automation:** Introducing a comprehensive JUnit/Mockito automated test suite to handle core word-validation edge cases.
*   **Cold Starts:** Optimizing container footprint sizes to mitigate initial spin-up delays associated with Render's free tier tiers.

---

## 👤 Author

*   **Aditya Agrawal** – B.Tech in Information Technology, Medi-Caps University
*   **GitHub:** [@Aditya-Agrawal-Dev](https://github.com)
](https://github.com/naman-1624/wordle)
