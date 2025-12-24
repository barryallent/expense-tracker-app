package com.expensetracker.service;

import com.expensetracker.dto.TransactionRequest;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.exception.UnauthorizedException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, 
                             CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        // Validate category exists and belongs to user or is default
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        
        if (!category.getIsDefault() && !category.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this category");
        }

        Transaction transaction = new Transaction(
            request.getAmount(),
            request.getDescription(),
            request.getTransactionDate(),
            request.getType(),
            category,
            user
        );
        transaction.setNotes(request.getNotes());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return new TransactionResponse(savedTransaction);
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest request, User user) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        // Validate category if changed
        if (!transaction.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            
            if (!category.getIsDefault() && !category.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("You do not have access to this category");
            }
            transaction.setCategory(category);
        }

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setType(request.getType());
        transaction.setNotes(request.getNotes());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return new TransactionResponse(savedTransaction);
    }

    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        transactionRepository.delete(transaction);
    }

    public TransactionResponse getTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        return new TransactionResponse(transaction);
    }

    public List<TransactionResponse> getUserTransactions(User user) {
        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);
        return transactions.stream()
            .map(TransactionResponse::new)
            .collect(Collectors.toList());
    }

    public List<TransactionResponse> getUserTransactionsPaginated(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactionPage = transactionRepository.findByUser(user, pageable);
        return transactionPage.getContent().stream()
            .map(TransactionResponse::new)
            .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByUserAndTransactionDateBetween(user, startDate, endDate);
        return transactions.stream()
            .map(TransactionResponse::new)
            .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsByType(User user, Transaction.TransactionType type) {
        List<Transaction> transactions = transactionRepository.findByUserAndType(user, type);
        return transactions.stream()
            .map(TransactionResponse::new)
            .collect(Collectors.toList());
    }

    public List<TransactionResponse> getMonthlyTransactions(User user, int year, int month) {
        List<Transaction> transactions = transactionRepository.findByUserAndYearAndMonth(user, year, month);
        return transactions.stream()
            .map(TransactionResponse::new)
            .collect(Collectors.toList());
    }

    public BigDecimal getMonthlySum(User user, Transaction.TransactionType type, int year, int month) {
        return transactionRepository.sumByUserAndTypeAndYearAndMonth(user, type, year, month);
    }

    public BigDecimal getCurrentMonthIncome(User user) {
        LocalDate now = LocalDate.now();
        return getMonthlySum(user, Transaction.TransactionType.INCOME, now.getYear(), now.getMonthValue());
    }

    public BigDecimal getCurrentMonthExpense(User user) {
        LocalDate now = LocalDate.now();
        return getMonthlySum(user, Transaction.TransactionType.EXPENSE, now.getYear(), now.getMonthValue());
    }

    public BigDecimal getCurrentMonthBalance(User user) {
        BigDecimal income = getCurrentMonthIncome(user);
        BigDecimal expense = getCurrentMonthExpense(user);
        return income.subtract(expense);
    }
} 