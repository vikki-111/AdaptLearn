package com.projects.adaptlearn.controller;
import com.projects.adaptlearn.dto.LoginRequest;
import com.projects.adaptlearn.dto.LoginResponse;
import com.projects.adaptlearn.dto.UserResponse;
import com.projects.adaptlearn.model.User;
import com.projects.adaptlearn.security.JwtUtil;
import com.projects.adaptlearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            UserResponse userResponse = new UserResponse(
                    registeredUser.getId(),
                    registeredUser.getUsername(),
                    registeredUser.getEmail()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        try {
            User user = userService.authenticateByUsername(loginRequest.getUsername(), loginRequest.getPassword());
            String token = jwtService.generateToken(user.getEmail(), user.getId());

            LoginResponse response = new LoginResponse(
                    token,
                    user.getId(),
                    user.getUsername()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}