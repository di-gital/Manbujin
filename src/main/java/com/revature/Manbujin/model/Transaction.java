package com.revature.Manbujin.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction for a deposit, withdrawal, or transfer.
 * Maps to the Transactions table
 */
public class Transaction {
    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private TransactionType type;
    private long amount;
    private LocalDateTime timestamp;

    /**
     * Creates a new transfer between two accounts.
     * Generates a unique ID and sets the timestamp to now.
     *
     */
    public Transaction(UUID sourceAccountId, UUID destinationAccountId, TransactionType type, long amount) {
        this.transactionId = UUID.randomUUID(); 
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a new deposit or withdrawal on a single account.
     * Generates a unique ID, sets destination and timestamps now.
     */
    public Transaction(UUID sourceAccountId, TransactionType type, long amount) {
        this.transactionId = UUID.randomUUID();
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = null;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Rebuilds a transaction from an existing database row.
     */
    public Transaction(UUID transactionId, UUID sourceAccountId, UUID destinationAccountId, TransactionType type, long amount, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(UUID destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }     
 
}



