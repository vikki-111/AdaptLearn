// Global variables to track state
let questions = [];
let userAnswers = {};

/**
 * 1. Fetch questions from the Backend on page load
 */
async function loadDiagnosticTest() {
    const token = localStorage.getItem('token');
    
    if (!token) {
        alert("Please login first!");
        window.location.href = '/login';
        return;
    }

    const container = document.getElementById('quizContainer');
    if (container) {
        container.innerHTML = '<div style="text-align: center; padding: 50px;"><div class="loading"></div><p>Generating your personalized test...</p></div>';
    }

    try {
        const response = await fetch('/api/assessments/generate', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || "Failed to load questions");
        }

        questions = await response.json();
        
        if (!questions || questions.length === 0) {
            throw new Error("No questions received from server");
        }
        
        renderQuestions();
    } catch (error) {
        console.error("Error:", error);
        if (container) {
            container.innerHTML = `
                <div style="text-align: center; padding: 50px; color: red;">
                    <p><strong>Error:</strong> ${error.message}</p>
                    <p>Make sure the Python service is running on localhost:5000</p>
                    <button onclick="window.location.reload()">Retry</button>
                </div>
            `;
        } else {
            alert("Could not load test: " + error.message);
        }
    }
}

/**
 * 2. Render questions into the HTML
 */
function renderQuestions() {
    const container = document.getElementById('quizContainer');
    if (!container) {
        console.error('quizContainer element not found');
        return;
    }
    
    container.innerHTML = questions.map((q, index) => `
        <div class="question-card">
            <p><strong>Question ${index + 1}:</strong> ${q.questionText}</p>
            <div class="options">
                ${q.options.map((opt, optIndex) => `
                    <label class="option-label">
                        <input type="radio" name="question-${index}" value="${opt.replace(/'/g, "\\'")}"
                               onChange="saveAnswer(${index}, '${opt.replace(/'/g, "\\'")}')">
                        <span style="font-weight: 500;">${String.fromCharCode(65 + optIndex)}.</span> ${opt}
                    </label>
                `).join('')}
            </div>
        </div>
    `).join('');
    
    // Show submit button
    const submitArea = document.getElementById('submitArea');
    if (submitArea) {
        submitArea.style.display = 'block';
    }
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
    let detailedResults = [];

    // Analyze each question and build detailed results
    questions.forEach((q, index) => {
        const userAnswer = userAnswers[index];
        const isCorrect = userAnswer === q.correctAnswer;
        if (isCorrect) {
            correctCount++;
        }

        detailedResults.push({
            questionId: q.id,
            questionText: q.questionText,
            userAnswer: userAnswer,
            correctAnswer: q.correctAnswer,
            isCorrect: isCorrect,
            options: q.options,
            topic: q.topic ? q.topic.name : "General"
        });
    });

    const finalScore = (correctCount / questions.length) * 100;

    // Identify weak areas based on incorrect answers
    const weakAreas = [];
    const topicPerformance = {};

    detailedResults.forEach(result => {
        const topic = result.topic;
        if (!topicPerformance[topic]) {
            topicPerformance[topic] = { correct: 0, total: 0 };
        }
        topicPerformance[topic].total++;
        if (result.isCorrect) {
            topicPerformance[topic].correct++;
        }
    });

    // Find topics with less than 70% accuracy
    Object.keys(topicPerformance).forEach(topic => {
        const performance = topicPerformance[topic];
        const accuracy = (performance.correct / performance.total) * 100;
        if (accuracy < 70) {
            weakAreas.push({
                topic: topic,
                accuracy: accuracy.toFixed(1),
                incorrectCount: performance.total - performance.correct,
                totalQuestions: performance.total
            });
        }
    });

    const submissionData = {
        score: finalScore,
        detailedResults: detailedResults,
        weakAreas: weakAreas,
        totalQuestions: questions.length,
        correctAnswers: correctCount
    };

    const token = localStorage.getItem('token');

    try {
        const response = await fetch('/api/assessments/submit', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(submissionData)
        });

        if (response.ok) {
            const result = await response.json();
            alert(`Test Submitted! Your score: ${finalScore.toFixed(1)}%`);
            // Redirect to dashboard to see results/AI plan
            window.location.href = '/dashboard';
        } else {
            const errorData = await response.json().catch(() => ({}));
            alert('Failed to submit assessment: ' + (errorData.error || 'Unknown error'));
        }
    } catch (error) {
        console.error("Submission Error:", error);
        alert('Network error. Please try again.');
    }
}

// Initialize on load
document.addEventListener('DOMContentLoaded', loadDiagnosticTest);