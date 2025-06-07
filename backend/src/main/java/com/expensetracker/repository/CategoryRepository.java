package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUserOrIsDefaultTrue(User user);
    
    List<Category> findByUserAndType(User user, Category.CategoryType type);
    
    List<Category> findByUser(User user);
    
    List<Category> findByIsDefaultTrue();

    @Query("SELECT c FROM Category c WHERE (c.user = :user OR c.isDefault = true) ORDER BY c.name")
    List<Category> findByUserOrIsDefaultTrueOrderByName(@Param("user") User user);

    @Query("SELECT c FROM Category c WHERE ((c.user = :user AND c.type = :type) OR (c.isDefault = true AND c.type = :type)) ORDER BY c.name")
    List<Category> findByUserAndTypeOrIsDefaultTrueAndTypeOrderByName(@Param("user") User user, 
                                                                     @Param("type") Category.CategoryType type);
} 