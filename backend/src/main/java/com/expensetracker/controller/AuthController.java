package com.expensetracker.controller;

import com.expensetracker.dto.JwtResponse;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.entity.User;
import com.expensetracker.security.JwtUtil;
import com.expensetracker.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, 
                         UserService userService, 
                         JwtUtil jwtUtil,
                         PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Registration attempt for username: {}", registerRequest.getUsername());
            
            User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFullName()
            );

            User savedUser = userService.registerUser(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());

            return ResponseEntity.ok().body("User registered successfully!");
        } catch (RuntimeException e) {
            logger.error("Registration error for username {}: {}", registerRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for username: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);

            // Get user for response
            User user = userService.findByUsername(loginRequest.getUsername()).get();

            logger.info("Authentication successful for user: {}", userDetails.getUsername());

            return ResponseEntity.ok(new JwtResponse(jwt, user.getUsername(), user.getEmail(), user.getFullName(), user.getCurrency()));
        } catch (Exception e) {
            logger.error("Login error for username {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Invalid username or password!");
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
                String username = jwtUtil.extractUsername(token);
                UserDetails userDetails = userService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(token, userDetails)) {
                    User user = userService.findByUsername(username).get();
                    logger.debug("Token validated successfully for user: {}", username);
                    return ResponseEntity.ok(new JwtResponse(token, user.getUsername(), user.getEmail(), user.getFullName(), user.getCurrency()));
                }
            }
            logger.warn("Invalid token validation attempt");
            return ResponseEntity.badRequest().body("Invalid token");
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid token");
        }
    }
} 