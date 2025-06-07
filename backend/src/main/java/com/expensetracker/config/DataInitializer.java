package com.expensetracker.config;

import com.expensetracker.entity.Category;
import com.expensetracker.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if default categories already exist
        if (categoryRepository.findByIsDefaultTrue().isEmpty()) {
            createDefaultCategories();
        }
    }

    private void createDefaultCategories() {
        // Default Expense Categories
        String[][] expenseCategories = {
            {"Food & Dining", "Restaurant, groceries, food delivery", "#FF6B6B"},
            {"Transportation", "Gas, public transport, taxi, uber", "#4ECDC4"},
            {"Shopping", "Clothing, electronics, personal items", "#45B7D1"},
            {"Entertainment", "Movies, games, concerts, subscriptions", "#FD79A8"},
            {"Bills & Utilities", "Electricity, water, internet, phone", "#FDCB6E"},
            {"Healthcare", "Medical expenses, pharmacy, insurance", "#6C5CE7"},
            {"Education", "Books, courses, school fees", "#A29BFE"},
            {"Travel", "Hotels, flights, vacation expenses", "#00B894"},
            {"Personal Care", "Haircut, cosmetics, spa", "#E17055"},
            {"Home & Garden", "Furniture, repairs, gardening", "#81ECEC"}
        };

        // Default Income Categories
        String[][] incomeCategories = {
            {"Salary", "Monthly salary, wages", "#00B894"},
            {"Freelance", "Freelance work, consulting", "#FDCB6E"},
            {"Business", "Business income, profits", "#6C5CE7"},
            {"Investment", "Dividends, interest, capital gains", "#A29BFE"},
            {"Bonus", "Work bonus, incentives", "#FD79A8"},
            {"Gift", "Money gifts, cash gifts", "#FF6B6B"},
            {"Other Income", "Miscellaneous income", "#74B9FF"}
        };

        // Create expense categories
        for (String[] cat : expenseCategories) {
            Category category = new Category();
            category.setName(cat[0]);
            category.setDescription(cat[1]);
            category.setType(Category.CategoryType.EXPENSE);
            category.setColor(cat[2]);
            category.setIsDefault(true);
            categoryRepository.save(category);
        }

        // Create income categories
        for (String[] cat : incomeCategories) {
            Category category = new Category();
            category.setName(cat[0]);
            category.setDescription(cat[1]);
            category.setType(Category.CategoryType.INCOME);
            category.setColor(cat[2]);
            category.setIsDefault(true);
            categoryRepository.save(category);
        }

        System.out.println("Default categories created successfully!");
    }
} 