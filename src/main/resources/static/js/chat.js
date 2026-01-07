// Chat state
let isWaitingForResponse = false;

function sendMessage() {
    const input = document.getElementById('chat-input');
    const message = input.value.trim();

    if (!message) {
        showStatus('Please enter a message', 'error');
        return;
    }

    if (isWaitingForResponse) {
        showStatus('Please wait for the AI to respond...', 'info');
        return;
    }

    // Add user message to chat
    addMessage(message, 'user');

    // Clear input
    input.value = '';
    showStatus('AI is thinking...', 'info');

    // Send to backend
    sendToAI(message);
}

async function sendToAI(message) {
    const token = localStorage.getItem('token');

    if (!token) {
        showStatus('Please login first', 'error');
        setTimeout(() => window.location.href = '/login', 2000);
        return;
    }

    isWaitingForResponse = true;

    try {
        const response = await fetch('/api/chat/message', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ message: message })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to get AI response' }));
            throw new Error(errorData.message || 'AI service unavailable');
        }

        const data = await response.json();
        addMessage(data.response, 'assistant');

    } catch (error) {
        console.error('Chat error:', error);
        addMessage('Sorry, I encountered an error. Please try again or check if the AI service is running.', 'assistant', true);
    } finally {
        isWaitingForResponse = false;
        showStatus('Type your question and press Enter or click Send');
    }
}

function addMessage(text, sender, isError = false) {
    const messagesContainer = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}-message ${isError ? 'error-message' : ''}`;

    const avatar = sender === 'user' ? '👤' : '🤖';

    messageDiv.innerHTML = `
        <div class="message-avatar">${avatar}</div>
        <div class="message-content">
            <div class="message-text">${formatMessage(text)}</div>
            <div class="message-time">${new Date().toLocaleTimeString()}</div>
        </div>
    `;

    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function formatMessage(text) {
    if (!text) return '';

    // First, escape HTML to prevent XSS
    text = escapeHtml(text);

    // Then convert markdown to HTML
    return text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>');
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

function showStatus(message, type = 'info') {
    const statusElement = document.getElementById('chat-status');
    statusElement.textContent = message;
    statusElement.className = `chat-status ${type}`;

    // Auto-hide success messages after 3 seconds
    if (type === 'info') {
        setTimeout(() => {
            statusElement.textContent = 'Type your question and press Enter or click Send';
            statusElement.className = 'chat-status';
        }, 3000);
    }
}

function logout() {
    // Clear all stored data
    localStorage.removeItem('token');
    localStorage.removeItem('selectedTopic');

    // Redirect to login page
    window.location.href = '/login';
}

// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    const input = document.getElementById('chat-input');
    const sendButton = document.getElementById('send-button');

    // Enter key to send
    input.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Send button click
    sendButton.addEventListener('click', sendMessage);

    // Focus input
    input.focus();

    // Check authentication
    const token = localStorage.getItem('token');
    if (!token) {
        showStatus('Please login first', 'error');
        setTimeout(() => window.location.href = '/login', 2000);
    }
});