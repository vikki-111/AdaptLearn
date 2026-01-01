document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    // Inside your registerForm submit listener
    const usernameValue = document.getElementById('username').value;
    const emailValue = document.getElementById('email').value;
    const passwordValue = document.getElementById('password').value;

    const userData = {
        username: usernameValue,
        email: emailValue,
        password: passwordValue,
        name: usernameValue,      // ADD THIS LINE: It maps to your Java @Column(name="name")
        fullName: usernameValue,  // ADD THIS LINE: It maps to your Java @Column(name="full_name")
        role: "STUDENT"
    };

    const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData)
    });
});