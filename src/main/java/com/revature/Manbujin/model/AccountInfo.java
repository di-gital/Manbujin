package com.revature.Manbujin.model;

import java.util.UUID;
import java.util.Collections;
import java.util.List;

import com.revature.Manbujin.Repository.AccountRepo;


public class AccountInfo {
    private UUID accountID;
    private UUID userID;
    private int pin;
    private AccountType accountType;
    private long balance;
    private boolean frozen;
    //private String accountName; //-Mo


    /** 
     * Constructor to initialize an account
     * It will take an userID and the pin of the account
     */

     public AccountInfo(UUID userID, int pin) {
         this.accountID = UUID.randomUUID();
         this.userID = userID;
         this.pin = pin;
         this.accountType = AccountType.CHECKING;
         this.balance = 0L;
         this.frozen = false;
     }

     /**
      * Constructor to initialize an account with an account ID
      */
     public AccountInfo(UUID accountID) {
        AccountInfo found = new AccountRepo().findAccountById(accountID);
        if (found != null) {
            this.accountID = found.accountID;
            this.userID = found.userID;
            this.pin = found.pin;
            this.accountType = found.accountType;
            this.balance = found.balance;
            this.frozen = found.frozen;
        }
     }

     /**
      * Constructor to initialize an account with a custom accountType
      */
    public AccountInfo(UUID userID, int pin, AccountType accountType) {
        this.accountID = UUID.randomUUID();
        this.userID = userID;
        this.pin = pin;
        this.accountType = accountType;
        this.balance = 0L;
        this.frozen = false;
    }


    /** 
     * Constructor to retrieve an account from the database
     */
    public AccountInfo(UUID accountID, UUID userID, int pin, AccountType accountType, long balance, boolean frozen) {
        this.accountID = accountID;
        this.userID = userID;
        this.pin = pin;
        this.accountType = accountType;
        this.balance = balance;
        this.frozen = frozen;
    }


    public UUID getAccountID() {
        return accountID;
    }

    public void setAccountID(UUID accountID) {
        this.accountID = accountID;
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    @Override
    public String toString() {
        return "AccountInfo [accountID=" + accountID + ", userID=" + userID + ", pin=" + pin + ", accountType=" + accountType + ", balance=" + balance + ", frozen=" + frozen + "]";
    }


    /**
     * Returns the most recent transactions for this account, up to numberOfTransactions
     */
    public List<Transaction> getTransactions(int numberOfTransactions) {
        if (this.accountID == null || numberOfTransactions <= 0) {
            return Collections.emptyList();
        }
        return new AccountRepo().findTransactionsByAccountId(this.accountID, numberOfTransactions);
    }

    /**
     * Return all transactions associated with this account.
     * @author Nicholas DiGirolamo
     */
    public List<Transaction> getTransactions() {
        if(this.accountID == null) return Collections.emptyList();
        return new AccountRepo().findTransactionsByAccountId(this.accountID);
    }
}
