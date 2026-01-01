async function handleLogin() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        // If the server returns 401 (Unauthorized), handle it here
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            // Redirect to the clean endpoint, not the file!
            window.location.href = '/assessment';
        }

        const data = await response.json();
        localStorage.setItem('token', data.token);
        window.location.href = '/assessment.html';

    } catch (error) {
        console.error("Critical Login Error:", error);
    }
}