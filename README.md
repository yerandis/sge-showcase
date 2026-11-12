# SGE Showcase

A full-stack employee management system built with **Spring Boot** and **React**. This repository contains a public showcase version of the project, with the backend and frontend maintained in a single repository.

## Technology Stack

### Backend
- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- Spring Security
- JWT authentication
- PostgreSQL
- WebSocket

### Frontend
- React
- TypeScript
- Vite
- React Router
- Axios

## Main Features

- Employee management.
- Role-based access control and permissions.
- JWT-based authentication.
- Real-time communication and notifications.
- Employee pagination.
- REST API integration between frontend and backend.

## Project Structure

```text
sge-showcase/
├── sge-showcase-backend/
│   └── src/
├── sge-showcase-frontend/
│   └── src/
├── .gitignore
└── README.md
```

## Requirements

- Java 21 or compatible version.
- Maven.
- Node.js and npm.
- PostgreSQL.

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/yerandis/sge-showcase.git
cd sge-showcase
```

### 2. Configure the database

Create a PostgreSQL database named:

```sql
CREATE DATABASE sge_showcase_db;
```

### 3. Configure the backend

Create the following private configuration file:

`sge-showcase-backend/config/application-dev-local.properties`

Add your local database credentials and application secrets:

```properties
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD

app.jwt.secret=YOUR_JWT_SECRET
app.seed.admin-password=YOUR_INITIAL_ADMIN_PASSWORD
```

Replace the placeholders with your own values. Do not commit this file or share its contents.

### 4. Run the backend

From the backend directory:

```bash
cd sge-showcase-backend
mvn spring-boot:run
```

The backend runs at:

`http://localhost:8080`

### 5. Configure and run the frontend

In another terminal:

```bash
cd sge-showcase-frontend
npm install
npm run dev
```

The frontend runs at:

`http://localhost:5173`

The frontend API URL can be configured through the `.env` file. Use `.env.example` as a reference.

## Security Notes

- Local credentials and environment files are excluded from version control.
- Configure production secrets through environment variables.
- Use your own credentials when running the application locally.
- The initial administrator account is created using the password configured in `app.seed.admin-password`.

## Author

**Ulises Yerandis Ramírez Heredia**

- GitHub: https://github.com/yerandis
- LinkedIn: https://www.linkedin.com/in/ulises-yerandis-ramirez-2516b72b6/