package com.projects.adaptlearn.service;

import com.projects.adaptlearn.exception.DuplicateUserException;
import com.projects.adaptlearn.exception.InvalidCredentialsException;
import com.projects.adaptlearn.exception.UserNotFoundException;
import com.projects.adaptlearn.exception.ValidationException;
import com.projects.adaptlearn.model.User;
import com.projects.adaptlearn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        validateUserForRegistration(user);

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("email")) {
                throw new DuplicateUserException("Email address is already registered");
            } else if (e.getMessage().contains("username")) {
                throw new DuplicateUserException("Username is already taken");
            }
            throw new RuntimeException("Registration failed due to data constraint violation");
        }
    }

    public User findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new ValidationException("Username cannot be empty");
        }
        return userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    public User authenticate(String email, String password) {
        validateCredentials(email, password);

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("No account found with this email address"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        return user;
    }

    public User authenticateByUsername(String username, String password) {
        validateCredentials(username, password);

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UserNotFoundException("No account found with this username"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        return user;
    }

    public User authenticateByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ValidationException("Email cannot be empty");
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private void validateUserForRegistration(User user) {
        if (user == null) {
            throw new ValidationException("User information is required");
        }

        if (!StringUtils.hasText(user.getUsername())) {
            throw new ValidationException("Username is required");
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new ValidationException("Email is required");
        }

        if (!StringUtils.hasText(user.getPassword())) {
            throw new ValidationException("Password is required");
        }

        if (!StringUtils.hasText(user.getName())) {
            throw new ValidationException("Name is required");
        }

        if (user.getUsername().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long");
        }

        if (user.getUsername().length() > 50) {
            throw new ValidationException("Username cannot exceed 50 characters");
        }

        if (!isValidEmail(user.getEmail())) {
            throw new ValidationException("Invalid email format");
        }

        if (user.getPassword().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }

        if (userRepository.existsByEmail(user.getEmail().trim().toLowerCase())) {
            throw new DuplicateUserException("Email address is already registered");
        }

        if (userRepository.existsByUsername(user.getUsername().trim())) {
            throw new DuplicateUserException("Username is already taken");
        }
    }

    private void validateCredentials(String identifier, String password) {
        if (!StringUtils.hasText(identifier)) {
            throw new ValidationException("Username/Email is required");
        }

        if (!StringUtils.hasText(password)) {
            throw new ValidationException("Password is required");
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                           "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username.trim());
    }
}