Wordle+
A full, working Wordle game — plus three game modes the original doesn't have. Built with a Spring Boot backend, PostgreSQL database, and Docker deployment.

Live demo: https://wordle-db-n9b5.onrender.com/ (Hosted on Render's free tier — the first load after inactivity takes 1-2 minutes to wake up. After that, it's fast.)

What it does
This isn't a stripped-down clone — it's a complete, playable game with everything the original has, plus three modes the original doesn't ship:

Normal – classic daily word
60 Seconds – solve before the timer runs out
Sudden Death – one wrong guess ends the round
All guess validation and word selection happen server-side, so the answer and remaining attempts can't be read or manipulated from the browser — the same backend rigor as a production game, not just a front-end demo.

Word dataset
The original word list had about 700 entries, which caused frequent false negatives — valid English words being rejected because they weren't in the list. I expanded the dataset to 13,000+ entries.

To check the impact, I manually tested a sample set of common 5-letter words against both versions of the list and counted incorrect rejections. The expanded list cut incorrect rejections by roughly 40% in that sample. This was a manual spot-check, not an automated benchmark, but the difference was large enough to be obvious in normal play — far fewer "that's a real word" complaints.

Tech stack
Layer	Tools
Backend	Java 17, Spring Boot, REST API
Database	PostgreSQL (Neon serverless)
Frontend	HTML, CSS, JavaScript
Deployment	Docker, Render
Running locally
git clone https://github.com/savitar007-droid/Wordle.git
cd Wordle
Run with Maven:

./mvnw spring-boot:run
Or with Docker:

docker build -t wordle-app .
docker run -p 8080:8080 wordle-app
Known limitations / next steps
No caching layer yet — word lookups hit the database directly
No automated test suite for the word-validation logic
Cold starts on the free Render tier (acceptable for a portfolio project, not for production traffic)
Author
Aditya Agrawal — B.Tech IT, Medi-Caps University
