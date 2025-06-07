package com.expensetracker.controller;

import com.expensetracker.dto.JwtResponse;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.entity.User;
import com.expensetracker.security.JwtUtil;
import com.expensetracker.service.UserService;
import jakarta.validation.Valid;
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
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

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

    @PostMapping("/test-user")
    public ResponseEntity<?> testUser(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if user exists
            Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
            if (userOpt.isEmpty()) {
                response.put("userExists", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }
            
            User user = userOpt.get();
            response.put("userExists", true);
            response.put("username", user.getUsername());
            response.put("encodedPassword", user.getPassword());
            
            // Test password matching
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            response.put("passwordMatches", passwordMatches);
            response.put("rawPassword", loginRequest.getPassword());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("=== REGISTRATION DEBUG ===");
            System.out.println("Username: " + registerRequest.getUsername());
            System.out.println("Email: " + registerRequest.getEmail());
            System.out.println("Raw Password: " + registerRequest.getPassword());
            
            User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFullName()
            );

            User savedUser = userService.registerUser(user);
            
            System.out.println("User saved with encoded password: " + savedUser.getPassword());
            System.out.println("========================");

            return ResponseEntity.ok().body("User registered successfully!");
        } catch (RuntimeException e) {
            System.out.println("Registration error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("=== LOGIN ATTEMPT ===");
            System.out.println("Username: " + loginRequest.getUsername());
            
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

            System.out.println("Authentication successful for: " + userDetails.getUsername());
            System.out.println("==================");

            return ResponseEntity.ok(new JwtResponse(jwt, user.getUsername(), user.getEmail(), user.getFullName()));
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            System.out.println("Error type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            System.out.println("==================");
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
                    return ResponseEntity.ok(new JwtResponse(token, user.getUsername(), user.getEmail(), user.getFullName()));
                }
            }
            return ResponseEntity.badRequest().body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid token");
        }
    }

    @PostMapping("/debug-auth")
    public ResponseEntity<?> debugAuth(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== DEBUG AUTHENTICATION ===");
            
            // Step 1: Check if user exists
            Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
            if (userOpt.isEmpty()) {
                response.put("step1_userExists", false);
                return ResponseEntity.ok(response);
            }
            
            User user = userOpt.get();
            response.put("step1_userExists", true);
            response.put("step1_userDetails", Map.of(
                "username", user.getUsername(),
                "enabled", user.isEnabled(),
                "accountNonExpired", user.isAccountNonExpired(),
                "accountNonLocked", user.isAccountNonLocked(),
                "credentialsNonExpired", user.isCredentialsNonExpired()
            ));
            
            // Step 2: Check password manually
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            response.put("step2_passwordMatches", passwordMatches);
            
            // Step 3: Load user through UserDetailsService
            try {
                UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
                response.put("step3_userDetailsLoaded", true);
                response.put("step3_userDetailsClass", userDetails.getClass().getSimpleName());
            } catch (Exception e) {
                response.put("step3_userDetailsLoaded", false);
                response.put("step3_error", e.getMessage());
            }
            
            // Step 4: Try authentication
            try {
                Authentication authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                );
                
                Authentication result = authenticationManager.authenticate(authToken);
                response.put("step4_authenticationSuccessful", true);
                response.put("step4_principal", result.getPrincipal().getClass().getSimpleName());
                response.put("step4_authenticated", result.isAuthenticated());
                
            } catch (Exception e) {
                response.put("step4_authenticationSuccessful", false);
                response.put("step4_error", e.getMessage());
                response.put("step4_errorClass", e.getClass().getSimpleName());
                e.printStackTrace();
            }
            
            System.out.println("=== END DEBUG AUTHENTICATION ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("generalError", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }
} 