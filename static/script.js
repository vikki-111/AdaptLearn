const chatMessages = document.getElementById('chatMessages');
const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendBtn');

async function sendMessage() {
    const message = messageInput.value.trim();
    if (!message) return;

    // Add user message to UI
    addMessage('user', message);
    messageInput.value = '';

    // Disable input while processing
    sendBtn.disabled = true;
    messageInput.disabled = true;
    sendBtn.innerHTML = '<div class="loading"></div>';

    try {
        const response = await fetch('/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: message })
        });
        const data = await response.json();
        
        if (data.error) {
            addMessage('assistant', '❌ ' + data.error);
        } else {
            addMessage('assistant', data.reply);
        }
    } catch (error) {
        addMessage('assistant', '❌ Error sending message. Please try again.');
    } finally {
        sendBtn.disabled = false;
        messageInput.disabled = false;
        sendBtn.innerText = 'Send';
        messageInput.focus();
    }
}

function addMessage(role, content) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${role}`;
    messageDiv.innerHTML = renderMarkdown(content);
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function renderMarkdown(text) {
    // Simple Markdown rendering for basic formatting
    return text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')  // **bold**
        .replace(/\*(.*?)\*/g, '<em>$1</em>')  // *italic*
        .replace(/`(.*?)`/g, '<code>$1</code>')  // `code`
        .replace(/\n/g, '<br>');  // Line breaks
}

async function resetConversation() {
    try {
        await fetch('/reset', { method: 'POST' });
        chatMessages.innerHTML = '<div class="message assistant">Hello! I\'m your AI assistant. How can I help you today?</div>';
    } catch (error) {
        alert('Error resetting conversation');
    }
}

// Allow Enter key to send message
messageInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter' && !sendBtn.disabled) {
        sendMessage();
    }
});

// Focus input on load
window.addEventListener('load', () => messageInput.focus());