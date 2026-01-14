# Adapt-Learn

A personalized learning platform that uses AI to create custom assessments and study plans tailored to individual learning needs.

## What This Project Does

Adapt-Learn helps students learn more effectively by:
- Generating questions based on topics they want to study
- Creating personalized study plans based on their performance
- Providing an AI tutor for additional support through chat
- Tracking progress to show learning improvement over time

The platform adapts to each user's learning style and pace, making education more engaging and effective.

## Technology Used

### Backend
- Java with Spring Boot framework
- PostgreSQL database for storing user data and progress
- Secure authentication using JWT tokens
- RESTful APIs for communication between services

### AI Service
- Python with Flask web framework
- Google AI (Gemini) for generating questions and study recommendations
- Automatic model selection and fallback strategies

### Frontend
- Clean, responsive web interface using HTML, CSS, and JavaScript
- Dynamic content loading without page refreshes
- Mobile-friendly design

### Deployment
- Hosted on Render cloud platform
- Multi-service architecture for scalability
- Automated deployment with configuration management

## Getting Started

### Quick Local Setup

1. **Clone the project**
   ```bash
   git clone https://github.com/your-username/adapt-learn.git
   cd adapt-learn
   ```

2. **Set up the database**
   - Install PostgreSQL
   - Create a database called `adapt_learn`
   - Update database connection details in `src/main/resources/application.properties`

3. **Start the backend**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Start the AI service**
   ```bash
   cd ai_service
   pip install -r requirements.txt
   export GOOGLE_API_KEY=your-api-key-here
   python app.py
   ```

5. **Open your browser**
   - Go to `http://localhost:8080`
   - Register an account and start learning!

### Configuration

You'll need to set a few environment variables:

**For the backend:**
- Database connection details (URL, username, password)
- JWT secret key for authentication
- AI service URL (usually `http://localhost:5000` for local development)

**For the AI service:**
- Google AI API key (get this from Google AI Studio)

## Project Structure

```
adapt-learn/
├── src/main/java/          # Java backend code
│   └── com/projects/adaptlearn/
│       ├── controller/     # API endpoints
│       ├── service/        # Business logic
│       ├── security/       # Authentication & security
│       └── model/          # Data models
├── src/main/resources/     # Configuration and static files
├── ai_service/             # Python AI service
│   ├── app.py             # Flask application
│   └── requirements.txt   # Python dependencies
├── render.yaml            # Cloud deployment configuration
└── pom.xml               # Maven build configuration
```

## Key Features

### Personalized Assessments
- Choose topics you want to study
- Answer AI-generated questions
- Get immediate feedback and scoring

### Smart Study Plans
- Receive customized study recommendations
- Plans adapt based on your performance
- Focus on areas where you need improvement

### Learning Progress
- Track your improvement over time
- View detailed analytics
- Set learning goals and monitor progress

### AI Tutor Chat
- Ask questions about topics you're studying
- Get explanations and additional resources
- Interactive learning support

## Deployment

This project is designed to run on Render cloud platform. The `render.yaml` file contains the complete deployment configuration for:

- Java Spring Boot application
- Python AI service
- PostgreSQL database
- Automatic service discovery and networking

See `DEPLOYMENT_README.md` for detailed deployment instructions.

## API Reference

The backend provides REST APIs for:
- User authentication (register/login)
- Assessment management
- Progress tracking
- Chat functionality

All endpoints return JSON responses and use standard HTTP status codes.

## Contributing

We welcome contributions! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is open source and available under the MIT License.

## Support

If you run into any issues or have questions, please check the existing issues on GitHub or create a new one. We're here to help make learning more effective for everyone.
