# Expense Tracker App

A simple expense tracking application for managing personal finances.

## Technology Stack

**Backend:** Java 17, Spring Boot 3.2, PostgreSQL  
**Frontend:** React 18

## Quick Start

### 1. Start Backend + Database (Docker)

```bash
cd expense-tracker-app
docker-compose up -d --build
```

- **Backend API**: http://localhost:9024/api
- **Database**: localhost:7432 (PostgreSQL)

### 2. Start Frontend (Manual)

In a new terminal:

```bash
cd frontend
npm install
npm start
```

- **Frontend**: http://localhost:3000

That's it! Both are now running.