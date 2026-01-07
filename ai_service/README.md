# AI Service Setup

This Flask service provides AI-powered assessment generation and study plan creation using Google's Gemini AI.

**Default Model:** `gemini-2.5-flash` (tries this first, falls back to others if unavailable)

## Environment Setup

1. **Install dependencies:**
   ```bash
   pip install -r ../requirements.txt
   ```

2. **Set up environment variables:**
   Create a `.env` file in this directory with your Google API key:

   ```env
   GOOGLE_API_KEY=your_actual_api_key_here
   FLASK_ENV=development
   FLASK_DEBUG=true
   ```

   **⚠️ Security Warning:** Never commit your `.env` file to version control!

3. **Get your Google API Key:**
   - Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Create a new API key
   - Copy it to your `.env` file

## Running the Service

```bash
python app.py
```

The service will start on `http://localhost:5000`

## Model Selection Logic

The service prioritizes models in this order:
1. **gemini-2.5-flash** (default - tried first)
2. **gemini-1.5-flash**
3. **gemini-1.5-pro**
4. **gemini-pro**
5. Other available models

If the primary model fails, it automatically falls back to the next available model.

## API Endpoints

- `GET /list-models` - List available Gemini models
- `POST /generate-questions` - Generate assessment questions
- `POST /generate-study-plan` - Create basic study plans
- `POST /generate-personalized-study-plan` - Create personalized study plans
- `POST /chat` - Interactive chat with AI assistant

## Security

- API keys are loaded from environment variables
- CORS is enabled for the main application
- Input validation is implemented for all endpoints
