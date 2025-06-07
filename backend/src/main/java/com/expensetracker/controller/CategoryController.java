package com.expensetracker.controller;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryController(CategoryRepository categoryRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<?> getUserCategories(Authentication authentication) {
        try {
            System.out.println("=== GET Categories Debug ===");
            System.out.println("Authentication: " + authentication);
            if (authentication != null) {
                System.out.println("Username: " + authentication.getName());
                System.out.println("Authorities: " + authentication.getAuthorities());
            }
            
            User user = getCurrentUser(authentication);
            List<Category> categories = categoryRepository.findByUserOrIsDefaultTrueOrderByName(user);
            System.out.println("Found " + categories.size() + " categories");
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            System.out.println("Error in getUserCategories: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getCategoriesByType(@PathVariable Category.CategoryType type,
                                               Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            List<Category> categories = categoryRepository.findByUserAndTypeOrIsDefaultTrueAndTypeOrderByName(user, type);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest request,
                                          Authentication authentication) {
        try {
            System.out.println("=== POST Categories Debug ===");
            System.out.println("Authentication: " + authentication);
            if (authentication != null) {
                System.out.println("Username: " + authentication.getName());
                System.out.println("Authorities: " + authentication.getAuthorities());
            }
            System.out.println("Request: " + request.getName() + ", " + request.getType());
            
            User user = getCurrentUser(authentication);
            System.out.println("User found: " + user.getUsername());
            
            Category category = new Category();
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            category.setColor(request.getColor());
            category.setType(request.getType());
            category.setUser(user);
            category.setIsDefault(false);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            
            Category savedCategory = categoryRepository.save(category);
            System.out.println("Category saved successfully: " + savedCategory.getId());
            return ResponseEntity.ok(savedCategory);
        } catch (Exception e) {
            System.out.println("Error in createCategory: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id,
                                          @Valid @RequestBody CategoryRequest request,
                                          Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            
            Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
            
            // Only allow users to update their own categories, not default ones
            if (category.getIsDefault() || !category.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("Cannot modify this category");
            }
            
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            category.setColor(request.getColor());
            category.setType(request.getType());
            category.setUpdatedAt(LocalDateTime.now());
            
            Category savedCategory = categoryRepository.save(category);
            return ResponseEntity.ok(savedCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id,
                                          Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            
            Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
            
            // Only allow users to delete their own categories, not default ones
            if (category.getIsDefault() || !category.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("Cannot delete this category");
            }
            
            categoryRepository.delete(category);
            return ResponseEntity.ok("Category deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DTO for category requests
    public static class CategoryRequest {
        private String name;
        private String description;
        private String color;
        private Category.CategoryType type;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public Category.CategoryType getType() { return type; }
        public void setType(Category.CategoryType type) { this.type = type; }
    }
} 