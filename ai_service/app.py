from flask import Flask, request, jsonify
from flask_cors import CORS  # ADD THIS: pip install flask-cors
import google.generativeai as genai
import json
import re

API_KEY = "AIzaSyBS_336TtafApvnq_u3ehzrIG39vfhGAms"
genai.configure(api_key=API_KEY)
model = genai.GenerativeModel('gemini-1.5-flash') # Flash is faster for MCQs

app = Flask(__name__)
CORS(app) # This prevents "Blocked by CORS" errors during testing

@app.route('/generate-questions', methods=['POST'])
def generate_questions():
    data = request.json
    topic = data.get('topic', 'Java')

    # We add "Strict JSON" instructions to the prompt to stop Gemini from talking
    prompt = (
        f"Generate 5 multiple choice questions about {topic}. "
        "Return ONLY a raw JSON array. Do not include markdown formatting or backticks. "
        "Format: [{\"questionText\": \"...\", \"options\": [\"...\"], \"correctAnswer\": \"...\"}]"
    )

    try:
        response = model.generate_content(prompt)
        # Clean up the response in case Gemini includes ```json ... ```
        content = response.text.strip()
        if content.startswith("```"):
            content = re.sub(r'^```[a-z]*\n|```$', '', content, flags=re.MULTILINE).strip()

        return jsonify(json.loads(content))
    except Exception as e:
        print(f"Error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/generate-study-plan', methods=['POST'])
def generate_plan():
    data = request.json
    weak_areas = data.get('weakAreas', ["General Concepts"])
    areas_str = ", ".join(weak_areas)

    prompt = f"The student needs help with: {areas_str}. Create a concise 3-step study plan in Markdown."

    try:
        response = model.generate_content(prompt)
        return jsonify({"plan_content": response.text})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    # Threaded=True helps if Java makes multiple rapid calls
    app.run(port=5000, debug=True, threaded=True)