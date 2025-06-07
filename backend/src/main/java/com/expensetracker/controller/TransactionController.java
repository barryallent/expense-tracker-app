package com.expensetracker.controller;

import com.expensetracker.dto.TransactionRequest;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.service.TransactionService;
import com.expensetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, 
                               UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequest request, 
                                             Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            TransactionResponse response = transactionService.createTransaction(request, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, 
                                             @Valid @RequestBody TransactionRequest request,
                                             Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            TransactionResponse response = transactionService.updateTransaction(id, request, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id, 
                                             Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            transactionService.deleteTransaction(id, user);
            return ResponseEntity.ok().body("Transaction deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable Long id, 
                                          Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            TransactionResponse response = transactionService.getTransaction(id, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserTransactions(Authentication authentication,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size) {
        try {
            User user = getCurrentUser(authentication);
            List<TransactionResponse> transactions;
            
            if (page != null && size != null) {
                transactions = transactionService.getUserTransactionsPaginated(user, page, size);
            } else {
                transactions = transactionService.getUserTransactions(user);
            }
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<?> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            List<TransactionResponse> transactions = transactionService.getTransactionsByDateRange(user, startDate, endDate);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getTransactionsByType(@PathVariable Transaction.TransactionType type,
                                                 Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            List<TransactionResponse> transactions = transactionService.getTransactionsByType(user, type);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<?> getMonthlyTransactions(@PathVariable int year,
                                                   @PathVariable int month,
                                                   Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            List<TransactionResponse> transactions = transactionService.getMonthlyTransactions(user, year, month);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getCurrentMonthSummary(Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            
            BigDecimal income = transactionService.getCurrentMonthIncome(user);
            BigDecimal expense = transactionService.getCurrentMonthExpense(user);
            BigDecimal balance = transactionService.getCurrentMonthBalance(user);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("income", income);
            summary.put("expense", expense);
            summary.put("balance", balance);
            summary.put("year", LocalDate.now().getYear());
            summary.put("month", LocalDate.now().getMonthValue());
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/summary/{year}/{month}")
    public ResponseEntity<?> getMonthlySummary(@PathVariable int year,
                                             @PathVariable int month,
                                             Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            
            BigDecimal income = transactionService.getMonthlySum(user, Transaction.TransactionType.INCOME, year, month);
            BigDecimal expense = transactionService.getMonthlySum(user, Transaction.TransactionType.EXPENSE, year, month);
            BigDecimal balance = income.subtract(expense);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("income", income);
            summary.put("expense", expense);
            summary.put("balance", balance);
            summary.put("year", year);
            summary.put("month", month);
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 