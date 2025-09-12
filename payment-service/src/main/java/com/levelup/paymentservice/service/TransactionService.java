package com.levelup.paymentservice.service;

import com.levelup.paymentservice.model.Transaction;
import com.levelup.paymentservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId);
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionByStripePaymentIntentId(String stripePaymentIntentId) {
        return transactionRepository.findByStripePaymentIntentId(stripePaymentIntentId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUserId(UUID userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUserIdAndStatus(UUID userId, Transaction.TransactionStatus status) {
        return transactionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByReference(String referenceType, UUID referenceId) {
        return transactionRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    @Transactional
    public Transaction updateTransactionStatus(UUID transactionId, Transaction.TransactionStatus status) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }

        Transaction transaction = transactionOpt.get();
        Transaction.TransactionStatus oldStatus = transaction.getStatus();
        transaction.setStatus(status);

        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transaction {} status updated from {} to {}", transactionId, oldStatus, status);

        return savedTransaction;
    }

    @Transactional
    public Transaction updateTransactionStripeId(UUID transactionId, String stripePaymentIntentId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }

        Transaction transaction = transactionOpt.get();
        transaction.setStripePaymentIntentId(stripePaymentIntentId);

        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transaction {} updated with Stripe PaymentIntent: {}", transactionId, stripePaymentIntentId);

        return savedTransaction;
    }

    @Transactional(readOnly = true)
    public long getTransactionCountByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByTypeAndStatus(Transaction.TransactionType type,
            Transaction.TransactionStatus status) {
        return transactionRepository.findByTypeAndStatus(type, status);
    }
}
