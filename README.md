# TeleMoodle
A small telegram bot that shows deadlines and assignments. Currently in early and heavy development stage.

This bot uses PostgreSQL as a database to store Moodle tokens and Telegram IDs and okHttp to interact with Moodle API.

To build this thing just import it in IntellijIdea and in Run/Debug configuration write down these env. variables:
1. DB_URL - Type in this format: jdbc:postgresql://<IP>:<PORT>/<DATABASE_NAME>
2. DB_USER - Your PostgreSQL username, usually it's just "postgres"
3. DB_PASS - Self-explanatory, password for your database
4. TG_BOT_TOKEN - Also self-explanatory, token for your Telegram bot.
