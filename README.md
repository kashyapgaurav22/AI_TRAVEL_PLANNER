# AI Travel Planner

A full-stack travel planner built with React, Spring Boot, PostgreSQL, and direct LLM integration.

## Architecture

- Frontend: React + Vite + Tailwind CSS
- Backend: Spring Boot REST API + Spring Security JWT + JPA + direct Gemini calls
- Database: PostgreSQL

Flow:

1. User logs in/registers in frontend.
2. Frontend calls backend APIs.
3. Backend calls Gemini directly to generate itinerary.
4. Backend stores trip and returns itinerary.
5. User can view history and regenerate trips.

## Project Structure

- `frontend/`
- `backend/`
- `database/schema.sql`

## Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 14+

## 1) Database Setup

Create DB:

```sql
CREATE DATABASE travel_planner;
```

Set DB and JWT values via environment variables before starting backend.
Use `backend/.env.example` as the template for your local `.env` file.

PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/travel_planner"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="<your_db_password>"
$env:JWT_SECRET="<base64_256bit_secret>"
```

Never commit real secrets to git. Keep keys only in local environment variables or your deployment secret manager.

## 2) Configure Gemini API

Set Gemini key and model before running backend.

Gemini (PowerShell):

```powershell
$env:GEMINI_API_KEY="<your_gemini_api_key>"
$env:GEMINI_MODEL="gemini-1.5-flash"
```

## 3) Run Backend

```bash
cd backend
mvn spring-boot:run
```

Runs on `http://localhost:8080`.

## 4) Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`.

## API Endpoints

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`

Travel:

- `POST /api/travel/plan`
- `GET /api/travel/history`
- `PUT /api/travel/update`

## Notes

- Passwords are stored using BCrypt.
- Backend auth uses JWT bearer tokens.
- Itinerary generation is fully backend-driven with real LLM API calls.
