async function handleLogin() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorElement = document.getElementById('error');

    // Hide error message initially
    if (errorElement) {
        errorElement.style.display = 'none';
    }

    if (!username || !password) {
        if (errorElement) {
            errorElement.textContent = 'Please enter both username and password';
            errorElement.style.display = 'block';
        }
        return;
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            // Redirect to topic selection page
            window.location.href = '/topic-selection';
        } else {
            // Handle error response
            if (errorElement) {
                errorElement.textContent = 'Invalid username or password';
                errorElement.style.display = 'block';
            }
            console.error('Login failed:', response.status, response.statusText);
        }
    } catch (error) {
        console.error("Critical Login Error:", error);
        if (errorElement) {
            errorElement.textContent = 'Network error. Please try again.';
            errorElement.style.display = 'block';
        }
    }
}