async function loadDashboard() {
    const token = localStorage.getItem('token');

    if (!token) {
        window.location.href = '/login.html';
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
        if (data.latestPlans.length > 0) {
            const latestPlan = data.latestPlans[0];
            // Using innerText for security, or innerHTML if you trust the AI's Markdown
            planContainer.innerHTML = `
                <div class="plan-card" style="background: #f9f9f9; padding: 20px; border-left: 5px solid #007bff;">
                    <h3>Your AI Learning Path (Generated ${new Date(latestPlan.createdAt).toLocaleDateString()})</h3>
                    <div class="content" style="white-space: pre-wrap;">${latestPlan.content}</div>
                </div>
            `;
        } else {
            planContainer.innerHTML = "<p>Complete a test with a score below 60% to generate a plan.</p>";
        }

    } catch (error) {
        console.error("Dashboard Error:", error);
    }
}

document.addEventListener('DOMContentLoaded', loadDashboard);