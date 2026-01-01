from fastapi import FastAPI
from google import genai
from .schemas import Promt,ChatMessage
import os
import dotenv
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
from pydantic import BaseModel
dotenv.load_dotenv(os.path.join(os.path.dirname(__file__), '.env'))

app = FastAPI()

app.mount("/static", StaticFiles(directory="chat_bot/static"), name="static")

@app.get("/")
def get_chat_ui():
    return FileResponse("chat_bot/static/index.html")


conversation_history = []

@app.post('/chat')
async def chat(data: ChatMessage):
    global conversation_history
    user_message = data.message.strip()
    if not user_message:
        return {"error": "Message cannot be empty"}
    
    # Add user message to history
    conversation_history.append({"role": "user", "content": user_message})
    
    # Prepare context for AI (last few messages for context)
    context = "\n".join([f"{msg['role']}: {msg['content']}" for msg in conversation_history[-10:]])  # Keep last 10 messages
    
    try:
        client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=f"Continue this conversation:\n{context}\nassistant:"
        )
        ai_response = response.text.strip()
        
        # Clean the response: remove extra whitespace and newlines, but keep Markdown
        import re
        ai_response = re.sub(r'\s+', ' ', ai_response)  # Replace multiple whitespace with single space
        ai_response = ai_response.strip()
        
        # Add AI response to history
        conversation_history.append({"role": "assistant", "content": ai_response})
        
        return {"reply": ai_response}
    except Exception as e:
        return {"error": f"Failed to generate response: {str(e)}"}

@app.post('/reset')
def reset_conversation():
    global conversation_history
    conversation_history = []
    return {"message": "Conversation reset"}

stored_promt = None
@app.get("/hello")
def say_hello():
  return "hello world"

@app.post('/input')
def get_promt(data: Promt):
  global stored_promt
  stored_promt = data.text
  return {
    "promt" : stored_promt
  }

@app.get('/chat-old')
async def answer():
  if stored_promt is None:
    return {"error" : "No promt fount. Please call/ input first."}
  client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

  response = client.models.generate_content(
      model="gemini-2.5-flash",
      contents=(f"{stored_promt} in 50 words")
  )
 
  return {
    "reply" : response.text
  }
