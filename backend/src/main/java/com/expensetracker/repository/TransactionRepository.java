package com.expensetracker.repository;

import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
    
    Page<Transaction> findByUser(User user, Pageable pageable);
    
    List<Transaction> findByUserAndTransactionDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findByUserAndType(User user, Transaction.TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND " +
           "YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndYearAndMonth(@Param("user") User user, 
                                               @Param("year") int year, 
                                               @Param("month") int month);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type " +
           "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    BigDecimal sumByUserAndTypeAndYearAndMonth(@Param("user") User user, 
                                              @Param("type") Transaction.TransactionType type,
                                              @Param("year") int year, 
                                              @Param("month") int month);
    
    @Query("SELECT t.category.name as categoryName, t.category.color as categoryColor, " +
           "COALESCE(SUM(t.amount), 0) as totalAmount FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type AND " +
           "YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month " +
           "GROUP BY t.category.id, t.category.name, t.category.color")
    List<Object[]> getCategoryWiseSum(@Param("user") User user, 
                                     @Param("type") Transaction.TransactionType type,
                                     @Param("year") int year, 
                                     @Param("month") int month);
} 