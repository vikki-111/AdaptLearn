// Global variables to track state
let questions = [];
let userAnswers = {};

/**
 * 1. Fetch questions from the Backend on page load
 */
async function loadDiagnosticTest() {
    const token = localStorage.getItem('token'); // Assumes JWT is stored here

    try {
        const response = await fetch('/api/assessments/generate', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error("Failed to load questions");

        questions = await response.json();
        renderQuestions();
    } catch (error) {
        console.error("Error:", error);
        alert("Could not load test. Make sure you are logged in.");
    }
}

/**
 * 2. Render questions into the HTML
 */
function renderQuestions() {
    const container = document.getElementById('quiz-container');
    container.innerHTML = questions.map((q, index) => `
        <div class="question-card" style="margin-bottom: 20px; padding: 15px; border: 1px solid #ddd;">
            <p><strong>Q${index + 1}:</strong> ${q.questionText}</p>
            <div class="options">
                ${q.options.map(opt => `
                    <label style="display: block;">
                        <input type="radio" name="question-${index}" value="${opt}" 
                               onChange="saveAnswer(${index}, '${opt}')">
                        ${opt}
                    </label>
                `).join('')}
            </div>
        </div>
    `).join('') + '<button onclick="calculateAndSubmit()" class="btn-submit">Submit Assessment</button>';
}

/**
 * 3. Track selected answers
 */
function saveAnswer(questionIndex, selectedOption) {
    userAnswers[questionIndex] = selectedOption;
}

/**
 * 4. Calculate score and POST to Backend
 */
async function calculateAndSubmit() {
    let correctCount = 0;

    questions.forEach((q, index) => {
        if (userAnswers[index] === q.correctAnswer) {
            correctCount++;
        }
    });

    const finalScore = (correctCount / questions.length) * 100;
    const token = localStorage.getItem('token');

    try {
        const response = await fetch('/api/assessments/submit', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ score: finalScore })
        });

        const result = await response.json();

        if (response.ok) {
            alert(`Test Submitted! Your score: ${finalScore}%`);
            // Redirect to dashboard to see results/AI plan
            window.location.href = '/dashboard.html';
        }
    } catch (error) {
        console.error("Submission Error:", error);
    }
}

// Initialize on load
document.addEventListener('DOMContentLoaded', loadDiagnosticTest);