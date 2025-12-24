package com.expensetracker.controller;

import com.expensetracker.dto.CategoryRequest;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.exception.UnauthorizedException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryController(CategoryRepository categoryRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<?> getUserCategories(Authentication authentication) {
        try {
            logger.debug("Fetching categories for user: {}", authentication.getName());
            
            User user = getCurrentUser(authentication);
            List<Category> categories = categoryRepository.findByUserOrIsDefaultTrueOrderByName(user);
            logger.info("Found {} categories for user: {}", categories.size(), user.getUsername());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error fetching categories for user {}: {}", authentication.getName(), e.getMessage());
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
            logger.info("Creating category '{}' of type {} for user: {}", 
                       request.getName(), request.getType(), authentication.getName());
            
            User user = getCurrentUser(authentication);
            
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
            logger.info("Category saved successfully with ID: {}", savedCategory.getId());
            return ResponseEntity.ok(savedCategory);
        } catch (Exception e) {
            logger.error("Error creating category for user {}: {}", authentication.getName(), e.getMessage());
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
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
            
            // Only allow users to update their own categories, not default ones
            if (category.getIsDefault() || !category.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("You do not have permission to modify this category");
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
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
            
            // Only allow users to delete their own categories, not default ones
            if (category.getIsDefault() || !category.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("You do not have permission to delete this category");
            }
            
            categoryRepository.delete(category);
            return ResponseEntity.ok("Category deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 