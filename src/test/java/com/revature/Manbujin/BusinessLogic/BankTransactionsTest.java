package com.revature.Manbujin.BusinessLogic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import com.revature.Manbujin.Repository.AccountRepo;
import com.revature.Manbujin.model.AccountType;
import com.revature.Manbujin.model.User;
import org.junit.jupiter.api.Test;

import com.revature.Manbujin.model.AccountInfo;

class BankTransactionsTest {
    @org.junit.jupiter.api.BeforeEach void schema() { new AccountRepo().initSchema(); }

    @Test
    void deposit() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setBalance(100L);
        account.setFrozen(false);

        com.revature.Manbujin.TestAccounts.save(account);
        BankTransactions bankTransactions = new BankTransactions();
        long result = bankTransactions.deposit(account, 50L);

        assertEquals(50L, result);
        assertEquals(150L, account.getBalance());
    }

    @Test
    void depositNegative() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setBalance(100L);
        account.setFrozen(false);

        BankTransactions bankTransactions = new BankTransactions();
        long result = bankTransactions.deposit(account, -50L);

        assertEquals(0L, result);
        assertEquals(100L, account.getBalance());
    }

    @Test
    void withdraw() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setBalance(100L);
        account.setFrozen(false);

        com.revature.Manbujin.TestAccounts.save(account);
        BankTransactions bankTransactions = new BankTransactions();
        long result = bankTransactions.withdraw(account, 50L);

        assertEquals(50L, result);
        assertEquals(50L, account.getBalance());
    }

    @Test
    void withdrawNegative() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setBalance(100L);
        account.setFrozen(false);

        BankTransactions bankTransactions = new BankTransactions();
        long result = bankTransactions.withdraw(account, 150L);

        assertEquals(0L, result);
        assertEquals(100L, account.getBalance());
    }

    @Test
    void transfer() {
        AccountRepo accountRepo = new AccountRepo();
        User user = new User("Owner67", 45, "Pass1234!");
        accountRepo.insertUser(user);

        AccountInfo sender = new AccountInfo(UUID.randomUUID(), user.getUserID(), 1001, AccountType.CHECKING, 10000L, false);
        sender.setBalance(100L);
        sender.setFrozen(false);

        AccountInfo receiver = new AccountInfo(UUID.randomUUID(), user.getUserID(), 1002, AccountType.SAVINGS, 0L, false);

        receiver.setBalance(50L);
        receiver.setFrozen(false);
        AccountRepo.insertAccount(sender);
        AccountRepo.insertAccount(receiver);


        BankTransactions bankTransactions = new BankTransactions();
        bankTransactions.setAccountRepo(accountRepo);
        long result = bankTransactions.transfer(sender, receiver, 25L);

        assertEquals(25L, result);
        assertEquals(75L, sender.getBalance());
        assertEquals(75L, receiver.getBalance());
    }

    @Test
    void transferNegative() {
        AccountInfo sender = new AccountInfo(UUID.randomUUID(), 1234);
        sender.setBalance(100L);
        sender.setFrozen(false);

        AccountInfo receiver = new AccountInfo(UUID.randomUUID(), 4321);
        receiver.setBalance(50L);
        receiver.setFrozen(false);

        BankTransactions bankTransactions = new BankTransactions();
        long result = bankTransactions.transfer(sender, receiver, 150L);

        assertEquals(0L, result);
        assertEquals(100L, sender.getBalance());
        assertEquals(50L, receiver.getBalance());
    }
}