# Echo

Echo is a modern, full-stack social media platform and community-driven discussion app (inspired by Reddit). It enables users to create communities, share posts, engage in nested discussions, and vote on content.

![Demo GIF](<!-- TODO: Insert Demo GIF URL here -->)

## 🚀 Features

- **Communities**: Create and join topic-based communities (`e/communityName`).
- **Rich Discussions**: Create text posts and engage in deep, nested comment threads.
- **Voting System**: Upvote or downvote posts and comments to curate content.
- **User Profiles**: View user activities, past posts, and comments.
- **Powerful Search**: Full-text search powered by Elasticsearch.
- **Authentication**: Secure JWT-based authentication.
- **Modern UI**: A beautiful, responsive interface with Dark Mode support built with Tailwind CSS and Shadcn UI.

## 🛠️ Tech Stack

**Frontend:**
- React 18
- TypeScript
- Vite
- Tailwind CSS
- Shadcn UI (Radix UI primitives)
- React Router DOM
- TanStack Query (React Query)
- Lucide Icons

**Backend:**
- Java 25
- Spring Boot 3.4+
- Spring Security (JWT)
- Spring Data JPA & Hibernate
- Spring Data Elasticsearch

**Database & Infrastructure:**
- PostgreSQL (Relational Data)
- Elasticsearch (Search Engine)
- Docker & Docker Compose
- Liquibase (Database Migrations)

## 🏗️ Getting Started

The easiest way to run the entire application (Frontend, Backend, PostgreSQL, and Elasticsearch) is using Docker Compose.

### Prerequisites
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

### Running with Docker

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/echo.git
   cd echo
   ```

2. **Configure Environment Variables:**
   Navigate to the `server` directory and configure your `.env` file based on `.env.example`:
   ```bash
   cd server
   cp .env.example .env
   # Make sure JWT_SECRET is set in the .env file!
   ```

3. **Start the Backend & Databases:**
   ```bash
   docker compose up -d --build
   ```
   This will spin up:
   - PostgreSQL on port `5432`
   - Elasticsearch on port `9200`
   - Echo Spring Boot Backend on port `8080`

4. **Start the Frontend:**
   Open a new terminal window, navigate to the `client` directory, and start the development server:
   ```bash
   cd client
   npm install
   npm run dev
   ```

5. **Open the App!**
   Visit `http://localhost:5173` in your browser.

## 🧪 Testing

The backend includes a comprehensive suite of integration tests utilizing **Testcontainers** to spin up ephemeral PostgreSQL and Elasticsearch instances.

To run the backend tests:
```bash
cd server
./mvnw test
```

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.
