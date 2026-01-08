# Deploying to Render

This project consists of two services that need to be deployed together:

1. **Java Spring Boot Backend** - Main application with REST API
2. **Python AI Service** - Flask application providing AI-powered features

## Prerequisites

- A Render account
- Google AI API key (for the Python AI service)

## Deployment Steps

### 1. Prepare Your Code

The following files have been configured for Render deployment:
- `render.yaml` - Multi-service blueprint configuration
- `pom.xml` - Updated for PostgreSQL and Java 21
- `src/main/resources/application.properties` - Updated for PostgreSQL

### 2. Deploy to Render

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click "New" → "Blueprint"
3. Connect your GitHub repository
4. Render will automatically detect the `render.yaml` file
5. Configure the services:
   - **adapt-learn-backend**: Java Spring Boot service
   - **adapt-learn-ai**: Python Flask service
   - **adapt-learn-db**: PostgreSQL database

### 3. Set Environment Variables

After deployment, set these environment variables in the Render dashboard:

#### For the AI Service (adapt-learn-ai):
- `GOOGLE_API_KEY`: Your Google AI API key (get from [Google AI Studio](https://makersuite.google.com/app/apikey))

#### For the Backend Service (adapt-learn-backend):
All database and service URLs are automatically configured via the blueprint.

### 4. Database Migration

The application uses Spring Boot's `spring.jpa.hibernate.ddl-auto=update` which will automatically create/update database tables on startup.

## Troubleshooting

### Database Connection Issues
- Check that the PostgreSQL service is running
- Verify environment variables are set correctly
- Check application logs for connection errors

### AI Service Communication
- Ensure the AI service is running and accessible
- Check that `GOOGLE_API_KEY` is set in the AI service
- Verify CORS settings allow communication between services

### Build Issues
- Java 21 is required and configured
- Ensure all dependencies are available in Maven Central
- Check build logs for specific error messages

## Architecture

```
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │
│   (Static)      │◄──►│   (Java)        │
│                 │    │                 │
└─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   AI Service    │
                       │   (Python)      │
                       └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Database      │
                       │  (PostgreSQL)   │
                       └─────────────────┘
```

## Local Development

To run locally:

1. **Database**: Set up PostgreSQL and update `application.properties`
2. **AI Service**: `cd ai_service && pip install -r requirements.txt && python app.py`
3. **Backend**: `./mvnw spring-boot:run`

Set `AI_SERVICE_URL=http://localhost:5000` for local development.
