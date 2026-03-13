# JINEN - Nursery Management Platform

Full-stack nursery management application with AI-powered child psychology tracking (Espace Enfant), intelligent chatbot, and vector search.

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Spring Boot | 3.2.12 |
| Runtime | Java (JDK) | **17** |
| Build | Maven Wrapper | included (`mvnw`) |
| Database | MongoDB Atlas | M0 (free tier) |
| Frontend | Angular | 19.2.0 |
| Node.js | Node.js | 18+ |
| LLM | Groq API | llama-3.1-8b-instant |
| Embeddings | HuggingFace Inference API | sentence-transformers/all-MiniLM-L6-v2 |

---

## Prerequisites

- **Java 17** — [Download JDK 17](https://adoptium.net/temurin/releases/?version=17)
- **Node.js 18+** and npm — [Download Node.js](https://nodejs.org/)
- **MongoDB Atlas** account (free M0 cluster) — [Create account](https://www.mongodb.com/cloud/atlas)
- **Groq API key** (free) — [Get key](https://console.groq.com/keys)
- **HuggingFace token** (free) — [Get token](https://huggingface.co/settings/tokens)

---

## Project Structure

```
Projet_Ing_Service/
├── backend-spring/              # Spring Boot REST API
│   ├── .env                     # (git-ignored)
│   ├── .env.example             # Template to copy
│   ├── pom.xml                  # Maven dependencies
│   ├── mvnw / mvnw.cmd          # Maven wrapper (no install needed)
│   └── src/main/
│       ├── java/com/nursery/
│       │   ├── controller/      # REST endpoints
│       │   ├── model/           # MongoDB documents
│       │   ├── repository/      # Data access + vector search
│       │   └── service/         # Business logic + AI services
│       └── resources/
│           └── application.properties  # Reads from .env automatically
├── frontend-spring/             # Angular 19 SPA
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/      # Standalone components
│   │   │   ├── services/        # HTTP services
│   │   │   ├── models/          # TypeScript interfaces
│   │   │   └── guards/          # Auth guards
│   │   └── environments/        # API URL config
│   ├── package.json
│   └── angular.json
└── database/                    # SQL schema (reference only)
```

---

## Setup Guide

### Step 1: Configure API Keys

```bash
cd backend-spring
```

Copy the example env file:

```bash
# Windows
copy .env.example .env

# Mac/Linux
cp .env.example .env
```

Open `backend-spring/.env` and fill in your values:

```env
# MongoDB Atlas connection string
MONGODB_URI=mongodb+srv://chahinchaaben_db_user:jEzOOUtah5mqoDEZ@nursery-cluster.w1n6yu6.mongodb.net/nursery_db?appName=nursery-cluster

# Server port
SERVER_PORT=3000

# Groq API key (https://console.groq.com/keys)
GROQ_API_KEY=gsk_your_key_here

# Groq model
GROQ_MODEL=llama-3.1-8b-instant

# HuggingFace token (https://huggingface.co/settings/tokens)
HUGGINGFACE_API_TOKEN=hf_your_token_here
```

> **Where to get each key:**
>
> | Key | Where | Cost |
> |-----|-------|------|
> | `MONGODB_URI` | [MongoDB Atlas](https://cloud.mongodb.com) → Database → Connect → Drivers | Free (M0) |
> | `GROQ_API_KEY` | [Groq Console](https://console.groq.com/keys) → API Keys → Create | Free |
> | `HUGGINGFACE_API_TOKEN` | [HuggingFace](https://huggingface.co/settings/tokens) → New token → Read | Free |

### Step 2: MongoDB Atlas Vector Search Index

In your MongoDB Atlas dashboard:

1. Go to your cluster → **Atlas Search** tab
2. Click **Create Search Index** → choose **Atlas Vector Search**
3. Select **JSON Editor** → pick the `daily_logs` collection
4. Paste this definition:

```json
{
  "fields": [
    {
      "type": "vector",
      "path": "embedding",
      "numDimensions": 384,
      "similarity": "cosine"
    },
    {
      "type": "filter",
      "path": "childId"
    }
  ]
}
```

5. Name: `daily_logs_vector_index` → **Create**

### Step 3: Start the Backend

```bash
cd backend-spring

# Windows
.\mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

The API starts at **http://localhost:3000/api**

> No need to install Maven — the wrapper (`mvnw`) downloads it automatically.

### Step 4: Install Frontend Dependencies & Start

```bash
cd frontend-spring
npm install
npm start
```

The app starts at **http://localhost:4201**

### Step 5 (Optional): Backfill Embeddings

If you already have daily logs in the database, generate vector embeddings for them:

```bash
curl -X POST http://localhost:3000/api/daily-logs/backfill-embeddings
```

---

## API Endpoints (Key)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/register` | Register |
| GET | `/api/nurseries` | List nurseries |
| POST | `/api/daily-logs` | Create observation log |
| GET | `/api/daily-logs/child/:id` | Get child logs (educator) |
| GET | `/api/daily-logs/child/:id/parent` | Get child logs (parent) |
| POST | `/api/daily-logs/backfill-embeddings` | Backfill vector embeddings |
| POST | `/api/child-chat` | Send message to AI chatbot |
| GET | `/api/child-chat/:childId/:userId/history` | Get chat history |

---

## Environment Files Summary

| File | Committed to Git? | Purpose |
|------|-------------------|---------|
| `backend-spring/.env` | **NO** (gitignored) | Your real API keys |
| `backend-spring/.env.example` | **YES** | Template with placeholder values |
| `backend-spring/src/main/resources/application.properties` | **YES** | Uses `${VAR}` placeholders that read from `.env` |
| `frontend-spring/src/environments/environment.ts` | YES | Frontend API URL (`http://localhost:3000/api`) |

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `java: command not found` | Install JDK 17 and add to PATH |
| MongoDB SSL/TLS error | Make sure your Atlas cluster is **not paused** (resume in Atlas UI) |
| Groq API returns empty | Check `GROQ_API_KEY` in `.env` — no quotes, no spaces |
| Vector search fails | Create the Atlas Vector Search index (Step 2) and backfill embeddings (Step 5) |
| Port 3000 in use | Change `SERVER_PORT` in `.env` or kill the process using that port |
| `npm start` fails | Run `npm install` first — check Node.js version is 18+ |
