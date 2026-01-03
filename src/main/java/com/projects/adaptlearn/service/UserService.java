package com.projects.adaptlearn.service;

import com.projects.adaptlearn.model.User;
import com.projects.adaptlearn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User authenticate(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("Email not found!"));
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Incorrect Password!");
        }
        return user;
    }
    
    public User authenticateByUsername(String username, String password){
        User user = userRepository.findByUsername(username)
                .orElseThrow(()->new RuntimeException("Username not found!"));
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Incorrect Password!");
        }
        return user;
    }

    public User authenticateByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

}