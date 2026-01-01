from pydantic import BaseModel

class Promt(BaseModel):
  text : str

class ChatMessage(BaseModel):
    message: str