function processMarkdown(text) {
    if (!text) return '';

    // Split into lines for processing
    const lines = text.split('\n');
    const processedLines = [];
    let inCodeBlock = false;
    let codeLanguage = '';
    let listStack = [];

    for (let i = 0; i < lines.length; i++) {
        let line = lines[i];

        // Handle code blocks (```)
        if (line.trim().startsWith('```')) {
            if (inCodeBlock) {
                // End of code block
                processedLines.push('</code></pre>');
                inCodeBlock = false;
                codeLanguage = '';
            } else {
                // Start of code block
                codeLanguage = line.trim().substring(3).toLowerCase();
                processedLines.push(`<pre><code class="language-${codeLanguage}">`);
                inCodeBlock = true;
            }
            continue;
        }

        if (inCodeBlock) {
            // Inside code block, escape HTML and add
            processedLines.push(escapeHtml(line));
            continue;
        }

        // Handle headers (# ## ###)
        if (line.match(/^#{1,6}\s/)) {
            const level = line.match(/^(#{1,6})/)[1].length;
            const content = line.replace(/^#{1,6}\s/, '');
            processedLines.push(`<h${level}>${processInlineMarkdown(content)}</h${level}>`);
            continue;
        }

        // Handle lists (- or * or numbers)
        const listMatch = line.match(/^(\s*)([-*]|\d+\.)\s+(.*)$/);
        if (listMatch) {
            const indent = listMatch[1].length;
            const bullet = listMatch[2];
            const content = listMatch[3];

            // Determine list type and level
            const isOrdered = /^\d+\./.test(bullet);

            // Close previous lists if indent level decreased
            while (listStack.length > 0 && listStack[listStack.length - 1].indent >= indent) {
                const prevList = listStack.pop();
                processedLines.push(prevList.isOrdered ? '</ol>' : '</ul>');
            }

            // Open new list if needed
            if (listStack.length === 0 || listStack[listStack.length - 1].indent < indent) {
                const listTag = isOrdered ? '<ol>' : '<ul>';
                processedLines.push(listTag);
                listStack.push({ indent, isOrdered });
            }

            processedLines.push(`<li>${processInlineMarkdown(content)}</li>`);
            continue;
        }

        // Close any remaining lists
        while (listStack.length > 0) {
            const prevList = listStack.pop();
            processedLines.push(prevList.isOrdered ? '</ol>' : '</ul>');
        }

        // Handle horizontal rules (--- or ***)
        if (line.match(/^[-*_]{3,}$/)) {
            processedLines.push('<hr>');
            continue;
        }

        // Handle blockquotes (>)
        if (line.startsWith('> ')) {
            processedLines.push(`<blockquote>${processInlineMarkdown(line.substring(2))}</blockquote>`);
            continue;
        }

        // Handle paragraphs (non-empty lines)
        if (line.trim()) {
            processedLines.push(`<p>${processInlineMarkdown(line)}</p>`);
        } else {
            // Empty lines become line breaks
            processedLines.push('<br>');
        }
    }

    // Close any remaining lists
    while (listStack.length > 0) {
        const prevList = listStack.pop();
        processedLines.push(prevList.isOrdered ? '</ol>' : '</ul>');
    }

    return processedLines.join('\n');
}

function processInlineMarkdown(text) {
    // Handle inline code (`code`)
    text = text.replace(/`([^`]+)`/g, '<code>$1</code>');

    // Handle bold (**text** or __text__)
    text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    text = text.replace(/__(.*?)__/g, '<strong>$1</strong>');

    // Handle italic (*text* or _text_)
    text = text.replace(/\*(.*?)\*/g, '<em>$1</em>');
    text = text.replace(/_(.*?)_/g, '<em>$1</em>');

    // Handle links [text](url)
    text = text.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');

    return text;
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}

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
            const rawContent = latestPlan.aiRecommendations || latestPlan.content || 'No plan content available';
            const planContent = processMarkdown(rawContent);
            const createdDate = latestPlan.createdAt ? new Date(latestPlan.createdAt).toLocaleDateString() : 'Recently';

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
                    <button onclick="window.location.href='/topic-selection'" class="btn-primary" style="margin-top: 10px;">
                        Take Your First Assessment
                    </button>
                </div>
            `;
        }

    } catch (error) {
        console.error("Dashboard Error:", error);
    }
}

function logout() {
    // Clear all stored data
    localStorage.removeItem('token');
    localStorage.removeItem('selectedTopic');

    // Redirect to login page
    window.location.href = '/login';
}

document.addEventListener('DOMContentLoaded', loadDashboard);