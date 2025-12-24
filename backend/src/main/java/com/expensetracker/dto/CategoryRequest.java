package com.expensetracker.dto;

import com.expensetracker.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @Size(max = 7, message = "Color must be a valid hex color code")
    private String color;
    
    @NotNull(message = "Category type is required")
    private Category.CategoryType type;

    // Constructors
    public CategoryRequest() {}

    public CategoryRequest(String name, String description, String color, Category.CategoryType type) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.type = type;
    }

    // Getters and Setters
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }

    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }

    public String getColor() { 
        return color; 
    }
    
    public void setColor(String color) { 
        this.color = color; 
    }

    public Category.CategoryType getType() { 
        return type; 
    }
    
    public void setType(Category.CategoryType type) { 
        this.type = type; 
    }
}

