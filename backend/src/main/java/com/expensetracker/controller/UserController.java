package com.expensetracker.controller;

import com.expensetracker.entity.User;
import com.expensetracker.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/currency")
    public ResponseEntity<?> updateCurrency(@RequestBody Map<String, String> request, 
                                           Authentication authentication) {
        try {
            String currency = request.get("currency");
            if (currency == null || currency.isEmpty()) {
                return ResponseEntity.badRequest().body("Currency is required");
            }

            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            user.setCurrency(currency);
            userService.updateUser(user);

            return ResponseEntity.ok().body("Currency updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

