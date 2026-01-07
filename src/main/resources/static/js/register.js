async function handleRegister() {
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const errorElement = document.getElementById('error');

    // Hide error message initially
    if (errorElement) {
        errorElement.style.display = 'none';
    }

    // Basic client-side validation
    if (!username || !email || !password) {
        if (errorElement) {
            errorElement.textContent = 'Please fill in all required fields';
            errorElement.style.display = 'block';
        }
        return;
    }

    if (username.length < 3) {
        if (errorElement) {
            errorElement.textContent = 'Username must be at least 3 characters long';
            errorElement.style.display = 'block';
        }
        return;
    }

    if (password.length < 6) {
        if (errorElement) {
            errorElement.textContent = 'Password must be at least 6 characters long';
            errorElement.style.display = 'block';
        }
        return;
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        if (errorElement) {
            errorElement.textContent = 'Please enter a valid email address';
            errorElement.style.display = 'block';
        }
        return;
    }

    const userData = {
        username: username,
        email: email,
        password: password,
        name: username,      // Maps to your Java @Column(name="name")
        fullName: username,  // Maps to your Java @Column(name="full_name")
        role: "STUDENT"
    };

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            const data = await response.json();
            alert('Registration successful! Welcome to AdaptLearn!');
            // Redirect to login page after successful registration
            window.location.href = '/login';
        } else {
            // Handle different error types
            const errorData = await response.json().catch(() => ({ message: 'Registration failed' }));

            if (errorElement) {
                if (response.status === 409) {
                    // Conflict - duplicate user
                    errorElement.textContent = errorData.message || 'Username or email already exists';
                } else if (response.status === 400) {
                    // Bad request - validation error
                    errorElement.textContent = errorData.message || 'Invalid registration data';
                } else {
                    // Other server errors
                    errorElement.textContent = errorData.message || 'Registration failed. Please try again.';
                }
                errorElement.style.display = 'block';
            }
            console.error('Registration failed:', response.status, response.statusText, errorData);
        }
    } catch (error) {
        console.error("Critical Registration Error:", error);
        if (errorElement) {
            errorElement.textContent = 'Network error. Please check your connection and try again.';
            errorElement.style.display = 'block';
        }
    }
}

// Add event listener when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleRegister();
        });
    }
});