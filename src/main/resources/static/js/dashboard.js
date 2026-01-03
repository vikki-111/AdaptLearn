async function loadDashboard() {
    const token = localStorage.getItem('token');

    if (!token) {
        window.location.href = '/login';
        return;
    }

    try {
        const response = await fetch('/api/progress/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error("Failed to fetch progress");

        const data = await response.json();

        // 1. Display Username
        document.getElementById('user-welcome').innerText = `Welcome, ${data.username}!`;

        // 2. Display Latest Score
        const latestScore = data.assessmentHistory.length > 0
            ? data.assessmentHistory[0].overallScore + "%"
            : "No tests taken yet";
        document.getElementById('latest-score').innerText = latestScore;

        // 3. Display AI Study Plan
        const planContainer = document.getElementById('study-plan-container');
        if (data.latestPlans && data.latestPlans.length > 0) {
            const latestPlan = data.latestPlans[0];
            const planContent = latestPlan.aiRecommendations || latestPlan.content || 'No plan content available';
            const createdDate = latestPlan.createdAt ? new Date(latestPlan.createdAt).toLocaleDateString() : 'Recently';
            // Using innerHTML to render Markdown content from AI
            planContainer.innerHTML = `
                <div class="study-plan-card">
                    <h3>🎯 Your AI Learning Path</h3>
                    <p style="color: #666; font-size: 14px; margin-bottom: 15px;">Generated on ${createdDate}</p>
                    <div class="content">${planContent}</div>
                </div>
            `;
        } else {
            planContainer.innerHTML = `
                <div class="study-plan-card">
                    <h3>📚 No Study Plan Yet</h3>
                    <p style="color: #666; margin-bottom: 15px;">Complete a diagnostic assessment with a score below 60% to receive a personalized AI-generated study plan.</p>
                    <button onclick="window.location.href='/assessment'" class="btn-primary" style="margin-top: 10px;">
                        Take Your First Assessment
                    </button>
                </div>
            `;
        }

    } catch (error) {
        console.error("Dashboard Error:", error);
    }
}

document.addEventListener('DOMContentLoaded', loadDashboard);