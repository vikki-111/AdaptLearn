from flask import Flask, request, jsonify
from flask_cors import CORS
import google.generativeai as genai
import json
import re
import os
from dotenv import load_dotenv

load_dotenv()

API_KEY = os.getenv('GOOGLE_API_KEY')
if not API_KEY:
    raise ValueError("GOOGLE_API_KEY environment variable is not set. Please check your .env file.")

genai.configure(api_key=API_KEY)

working_model_name = None

def get_working_model():
    global working_model_name
    print("Trying gemini-2.5-flash as default model...")

    # First, try gemini-2.5-flash directly
    try:
        print("Trying: gemini-2.5-flash")
        test_model = genai.GenerativeModel('gemini-2.5-flash')
        working_model_name = 'gemini-2.5-flash'
        print("✓ Success: gemini-2.5-flash (default model)")
        return test_model
    except Exception as e:
        error_msg = str(e)
        print(f"✗ gemini-2.5-flash failed: {error_msg[:100]}")
        print("Falling back to model discovery...")

    print("Discovering available models...")

    try:
        all_models = genai.list_models()
        available_models = []

        for m in all_models:
            if 'generateContent' in m.supported_generation_methods:
                model_name = m.name
                if model_name.startswith('models/'):
                    model_name = model_name.replace('models/', '')
                available_models.append({
                    'full_name': m.name,
                    'short_name': model_name,
                    'display_name': m.display_name
                })
                print(f"  Found: {model_name}")

        if not available_models:
            raise Exception("No models available")

        free_tier_models = []
        experimental_models = []

        for model_info in available_models:
            model_name = model_info['short_name'].lower()
            full_name = model_info['full_name'].lower()
            if any(x in model_name or x in full_name for x in ['-exp', '-preview', 'experimental', 'deep-research']):
                experimental_models.append(model_info)
                print(f"  Skipping experimental: {model_info['short_name']}")
            else:
                free_tier_models.append(model_info)
        
        preferred_order = ['gemini-2.5-flash', 'gemini-1.5-flash', 'gemini-1.5-pro', 'gemini-pro', 'gemini-1.0-pro']

        for preferred in preferred_order:
            for model_info in free_tier_models:
                if model_info['short_name'] == preferred or model_info['full_name'].endswith(preferred):
                    try:
                        print(f"Trying: {model_info['full_name']}")
                        test_model = genai.GenerativeModel(model_info['full_name'])
                        working_model_name = model_info['full_name']
                        print(f"✓ Success: {model_info['full_name']}")
                        return test_model
                    except Exception as e:
                        error_msg = str(e)
                        print(f"✗ Failed: {error_msg[:100]}")
                        if "404" in error_msg or "not found" in error_msg.lower() or "quota" in error_msg.lower():
                            print("  Skipping...")
                        continue

        print("Trying other free-tier models...")
        for model_info in free_tier_models:
            if any(model_info['short_name'] == pref for pref in preferred_order):
                continue
            try:
                print(f"Trying: {model_info['full_name']}")
                test_model = genai.GenerativeModel(model_info['full_name'])
                working_model_name = model_info['full_name']
                print(f"✓ Success: {model_info['full_name']}")
                return test_model
            except Exception as e:
                error_msg = str(e)
                print(f"✗ Failed: {error_msg[:100]}")
                continue

        if not free_tier_models:
            raise Exception("No free-tier models available")
        else:
            raise Exception(f"Could not initialize any free-tier model")

    except Exception as e:
        print(f"Error discovering models: {e}")
        fallback_models = [
            'gemini-2.5-flash',
            'models/gemini-2.5-flash',
            'gemini-1.5-flash',
            'gemini-1.5-pro',
            'gemini-pro',
            'models/gemini-1.5-flash',
            'models/gemini-1.5-pro',
            'models/gemini-pro'
        ]
        print("Trying fallbacks...")
        for model_name in fallback_models:
            try:
                print(f"Trying: {model_name}")
                test_model = genai.GenerativeModel(model_name)
                working_model_name = model_name
                print(f"✓ Success: {model_name}")
                return test_model
            except Exception as e:
                print(f"✗ Failed: {str(e)[:100]}")
                continue

        raise Exception(f"Could not initialize any model")

try:
    model = get_working_model()
except Exception as e:
    print(f"FATAL ERROR: {e}")
    print("Flask app will start but API calls will fail")
    model = None

app = Flask(__name__)
CORS(app)

@app.route('/list-models', methods=['GET'])
def list_models():
    try:
        models = genai.list_models()
        available = []
        for m in models:
            if 'generateContent' in m.supported_generation_methods:
                available.append({
                    'name': m.name,
                    'display_name': m.display_name,
                    'description': m.description
                })
        return jsonify({"available_models": available})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/generate-questions', methods=['POST'])
def generate_questions():
    data = request.json
    topic = data.get('topic', 'Java')

    prompt = (
        f"Generate 5 multiple choice questions about {topic}. "
        "Return ONLY a raw JSON array. Do not include markdown formatting or backticks. "
        "Format: [{\"questionText\": \"...\", \"options\": [\"...\"], \"correctAnswer\": \"...\"}]"
    )

    if model is None:
        return jsonify({"error": "AI model not initialized"}), 500

    try:
        response = model.generate_content(prompt)
        content = response.text.strip()
        if content.startswith("```"):
            content = re.sub(r'^```[a-z]*\n|```$', '', content, flags=re.MULTILINE).strip()

        parsed_content = json.loads(content)
        return jsonify(parsed_content)
    except json.JSONDecodeError as e:
        print(f"JSON error: {e}")
        print(f"Response: {content[:200]}...")
        return jsonify({"error": f"Failed to parse AI response: {str(e)}"}), 500
    except Exception as e:
        error_msg = str(e)
        print(f"Error: {error_msg}")

        if "quota" in error_msg.lower() or "429" in error_msg or "ResourceExhausted" in str(type(e)):
            return jsonify({
                "error": "API quota exceeded. Please wait and try again.",
                "details": "Free tier has rate limits."
            }), 429

        return jsonify({"error": error_msg}), 500

@app.route('/generate-study-plan', methods=['POST'])
def generate_plan():
    data = request.json
    weak_areas = data.get('weakAreas', ["General Concepts"])
    areas_str = ", ".join(weak_areas)

    prompt = f"The student needs help with: {areas_str}. Create a concise 3-step study plan in Markdown."

    if model is None:
        return jsonify({"error": "AI model not initialized"}), 500

    try:
        response = model.generate_content(prompt)
        return jsonify({"plan_content": response.text})
    except Exception as e:
        error_msg = str(e)
        print(f"Plan error: {error_msg}")

        if "quota" in error_msg.lower() or "429" in error_msg:
            return jsonify({"error": "API quota exceeded. Wait and try again."}), 429

        return jsonify({"error": error_msg}), 500

@app.route('/generate-personalized-study-plan', methods=['POST'])
def generate_personalized_plan():
    data = request.json
    weak_areas = data.get('weakAreas', ["General Concepts"])
    detailed_analysis = data.get('detailedAnalysis', '')
    overall_score = data.get('overallScore', 0.0)

    prompt = f"""
Based on the following assessment analysis, create a personalized study plan:

{detailed_analysis}

Requirements:
1. Address each weak area with specific strategies
2. Include practice exercises and examples
3. Provide time estimates
4. Suggest resources
5. Create measurable goals

Format as Markdown with clear sections.
"""

    if model is None:
        return jsonify({"error": "AI model not initialized"}), 500

    try:
        response = model.generate_content(prompt)
        plan_content = response.text.strip()

        if len(plan_content) < 100:
            plan_content = generate_fallback_plan(weak_areas, overall_score)

        return jsonify({"plan_content": plan_content})
    except Exception as e:
        error_msg = str(e)
        print(f"Personalized plan error: {error_msg}")

        fallback_plan = generate_fallback_plan(weak_areas, overall_score)
        return jsonify({"plan_content": fallback_plan}), 200

def generate_fallback_plan(weak_areas, score):
    plan = f"""# Study Plan

## Score: {score:.1f}%

## Focus Areas:
{chr(10).join(f"- {area}" for area in weak_areas)}

## Plan:
1. Review fundamentals (2-3 hours)
2. Practice exercises (3-4 hours)
3. Build projects (4-6 hours)
4. Test knowledge (1-2 hours)
5. Study advanced topics (2-3 hours)

## Resources:
- Official docs
- Practice platforms
- Tutorials
- Community forums
"""
    return plan

@app.route('/chat', methods=['POST'])
def chat():
    data = request.json
    message = data.get('message', '')
    user_email = data.get('user_email', 'anonymous')

    if not message.strip():
        return jsonify({"error": "Message cannot be empty"}), 400

    prompt = f"""
You are an AI learning assistant for AdaptLearn, a programming education platform.
A student ({user_email}) asked: "{message}"

Provide a helpful, educational response about programming concepts. Keep your response:
- Concise but informative (under 300 words)
- Educational and encouraging
- Related to programming, algorithms, or computer science
- Include code examples when relevant
- Be friendly and supportive

If the question is not programming-related, gently redirect to programming topics.
"""

    if model is None:
        return jsonify({"error": "AI model not initialized"}), 500

    try:
        response = model.generate_content(prompt)
        reply = response.text.strip()

        # Clean up any markdown artifacts
        if reply.startswith("```"):
            reply = re.sub(r'^```[a-z]*\n|```$', '', reply, flags=re.MULTILINE).strip()

        return jsonify({"reply": reply})

    except Exception as e:
        error_msg = str(e)
        print(f"Chat error: {error_msg}")

        if "quota" in error_msg.lower() or "429" in error_msg:
            return jsonify({"error": "API quota exceeded. Please wait and try again."}), 429

        return jsonify({"error": "Failed to generate response"}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=False)