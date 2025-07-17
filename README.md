# Weather Service

A microservices weather application with React frontend and Spring Boot backend.

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Node.js (for frontend development)
- Java 17+ (for backend development)

### Backend Services
Start all backend services (API Gateway, Weather Service, PostgreSQL, Redis):

First time or after code changes:
```bash
docker-compose build
docker-compose up -d
```

Subsequent runs:
```bash
docker-compose up -d
```

Services will be available at:
- API Gateway: http://localhost:8080
- Weather Service: http://localhost:8081
- PostgreSQL: localhost:5432
- Redis: localhost:6379

### Frontend
Navigate to the frontend directory and start the development server:
```bash
cd weather-app
npm install
npm run dev
```

The React app will be available at: http://localhost:5173

## Environment Variables

Copy the example environment file and configure it:
```bash
cp weather-service/env.example .env
```

Then edit the `.env` file in the root directory with your values:
```env
WEATHER_OPENWEATHERMAP_API_KEY=your_openweathermap_api_key
JWT_SECRET=your_jwt_secret_key
```
