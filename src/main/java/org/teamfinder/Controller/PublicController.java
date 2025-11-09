package org.teamfinder.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.teamfinder.Entity.User;
import org.teamfinder.Repository.UserRepository;
import org.teamfinder.Service.UserService;
import org.teamfinder.Security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class PublicController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 🔹 1. User Signup
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {
        Optional<User> existing = userRepository.findByUserName(newUser.getUserName());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }

        // Encrypt password before saving
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // Default role
        if (newUser.getRoles() == null || newUser.getRoles().isEmpty()) {
            newUser.setRoles(Collections.singletonList("USER"));
        }

        userRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully!");
    }

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody User user) {
            try {
                // Authenticate credentials
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword())
                );
                // Load user from DB (full User object)
                User fullUser = userService.findByUserName(user.getUserName());
                if (fullUser == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
                }

                // Generate token using the User object
                String token = jwtUtil.generateToken(fullUser);

                // Return token + basic user info
                return ResponseEntity.ok(Map.of(
                        "message", "Login successful",
                        "username", fullUser.getUserName(),
                        "email", fullUser.getEmail(),
                        "token", token
                ));

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid username or password"));
            }
        }

}
