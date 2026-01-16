function selectTopic(topic) {
    // Store the selected topic in localStorage
    localStorage.setItem('selectedTopic', topic);

    // Redirect to assessment page
    window.location.href = '/assessment';
}

function showCustomTopicInput() {
    document.getElementById('customTopicModal').style.display = 'flex';
    document.getElementById('customTopicInput').focus();
}

function hideCustomTopicInput() {
    document.getElementById('customTopicModal').style.display = 'none';
    document.getElementById('customTopicInput').value = '';
}

function selectCustomTopic() {
    const customTopic = document.getElementById('customTopicInput').value.trim();

    if (!customTopic) {
        showError('Please enter a topic');
        return;
    }

    if (customTopic.length < 2) {
        showError('Topic must be at least 2 characters long');
        return;
    }

    if (customTopic.length > 50) {
        showError('Topic must be less than 50 characters');
        return;
    }

    hideCustomTopicInput();
    selectTopic(customTopic);
}

function showError(message) {
    const errorElement = document.getElementById('errorMessage');
    errorElement.textContent = message;
    errorElement.style.display = 'block';

    // Hide error after 3 seconds
    setTimeout(() => {
        errorElement.style.display = 'none';
    }, 3000);
}

function logout() {
    // Clear all stored data
    localStorage.removeItem('token');
    localStorage.removeItem('selectedTopic');

    // Redirect to login page
    window.location.href = '/login';
}

// Handle Enter key in custom topic input
document.addEventListener('DOMContentLoaded', function() {
    const customTopicInput = document.getElementById('customTopicInput');
    if (customTopicInput) {
        customTopicInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                selectCustomTopic();
            }
        });
    }

    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
        // Redirect to login if not authenticated
        window.location.href = '/login';
    }
});

// Close modal when clicking outside
document.addEventListener('click', function(e) {
    const modal = document.getElementById('customTopicModal');
    if (e.target === modal) {
        hideCustomTopicInput();
    }
});





