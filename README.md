## VoteOnline - Simple Online Voting Application

This project is a minimal, production-ready online voting system with a Spring Boot backend and a Vue 3 frontend.

### Tech Stack

- **Backend**: Spring Boot 3, Spring Web, Spring Data JPA, Spring Security
- **Frontend**: Vue 3 (Composition API) + Vite
- **Database**: H2 in-memory by default (with example MySQL configuration)

### Features

- Exactly **3 candidates (art performances)** configured in the database.
- Public users can:
  - Access the voting page via a URL (suitable for QR codes).
  - See the list of candidates (name only).
  - Select exactly **one** candidate and submit a vote.
  - See only a success / info message after voting.
  - Never see any vote results.
- Backend enforces **one vote per IP address**.
- Admin-only API for viewing aggregated results.

---

## Backend (Spring Boot)

Located in the `backend` directory.

### How to run

```bash
cd backend
mvn spring-boot:run
```

The backend will start at `http://localhost:8080`.

### Public API

- **GET** `/api/vote/candidates`  
  Returns a list of candidates:

  ```json
  [
    { "id": 1, "name": "Performance A" },
    { "id": 2, "name": "Performance B" },
    { "id": 3, "name": "Performance C" }
  ]
  ```

- **POST** `/api/vote`  
  Request body:

  ```json
  { "candidateId": 1 }
  ```

  Behavior:

  - Stores a vote for the provided `candidateId`, associated to the **client IP**.
  - If the same IP votes again, returns **409 Conflict** with a simple error message.

### Admin API (protected)

- **GET** `/api/admin/results`  
  Returns total votes per candidate in JSON.

  - Protected using **HTTP Basic** credentials:
    - Username: `admin`
    - Password: `admin123`

  Example with `curl`:

  ```bash
  curl -u admin:admin123 http://localhost:8080/api/admin/results
  ```

### Database

- Schema:
  - `candidates(id, name)`
  - `votes(id, candidate_id, ip_address, created_at)`
- Initial data:
  - Configured via `data.sql` with **exactly 3 candidates**.
- Default profile uses **H2 in-memory**.
  - Browse via H2 console at `http://localhost:8080/h2-console`.

To configure MySQL instead of H2, see the commented section in `backend/src/main/resources/application.yml`.

---

## Frontend (Vue 3 + Vite)

Located in the `frontend` directory.

### How to run

```bash
cd frontend
npm install
npm run dev
```

The app will start at `http://localhost:5173` and is configured to **proxy** `/api` calls to `http://localhost:8080`.

### UI Behavior

- Single-page voting UI.
- Displays candidates as **radio buttons**.
- One **"Vote"** button:
  - Disabled until a candidate is selected.
  - After successful vote:
    - Button is disabled.
    - Displays a **thank-you** message.
- No results, charts, or counts are shown to the public.

---

## QR Code Usage

For a production deployment, generate a QR code pointing to the public voting page URL (e.g., `https://your-domain.com/`).  
The Vue single-page app loads, calls `/api/vote/candidates`, and allows users to cast exactly one vote from their IP.

