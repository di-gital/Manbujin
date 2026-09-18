package com.revature.Manbujin.Repository;

import com.revature.Manbujin.Utility.ConnectionFactory;
import com.revature.Manbujin.Utility.PasswordEncryption;
import com.revature.Manbujin.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountRepoTest {

    private AccountRepo repo;

    @BeforeEach
    void setUp() {
        // Prevent table cleanup from touching the application's real database
        if (!"jdbc:sqlite:./data/test-bank.db".equals(System.getenv("DATABASE-PATH")))
            throw new IllegalStateException("Run tests through Maven with the test database.");
        repo = new AccountRepo();
        repo.initSchema();
        wipeTables();
    }

    private void wipeTables() {
        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                Statement statement = connection.createStatement()
        ) {
            statement.execute("DELETE FROM Transactions");
            statement.execute("DELETE FROM Accounts");
            statement.execute("DELETE FROM Owners");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void insertUserThenFindById() {
        User user = new User("TestUser", 21, "Pass123!");
        AccountRepo.insertUser(user);

        User found = repo.findUserById(user.getUserID());
        assertNotNull(found);
        assertEquals(user.getUserID(), found.getUserID());
        assertEquals("TestUser", found.getName());
        assertEquals(21, found.getAge());
        assertEquals("Pass123!", PasswordEncryption.decrypt(found.getPassword()));
        //must set environment variable for this test to pass
    }

    @Test
    void findUserByIdReturnsNullWhenMissing() {
        assertNull(repo.findUserById(UUID.randomUUID()));
    }

    @Test
    void findUserByNameAndPassword() {
        User user = new User("LoginUser", 30, "Sectret!");
        AccountRepo.insertUser(user);

        User found = repo.findUserByNameAndPassword("LoginUser", "Sectret!");
        assertNotNull(found);
        assertEquals(user.getUserID(), found.getUserID());
        assertNull(repo.findUserByNameAndPassword("LoginUser", "Wrong"));
    }

    @Test
    void insertAccountThenFindById() {
        User user = new User("Owner", 30, "pass123!");
        AccountRepo.insertUser(user);

        AccountInfo account = new AccountInfo(user.getUserID(), 1234);
        AccountRepo.insertAccount(account);

        AccountInfo found = repo.findAccountById(account.getAccountID());
        assertNotNull(found);
        assertEquals(account.getAccountID(), found.getAccountID());
        assertEquals(user.getUserID(), found.getUserID());
        assertEquals(AccountType.CHECKING, found.getAccountType());
        assertEquals(0L, found.getBalance());
        assertFalse(found.isFrozen());
    }

    @Test
    void findAccountsByUserIdReturnsThatUsersAccounts() {
        User user = new User("Owner2", 29, "Pass123!");
        AccountRepo.insertUser(user);

        AccountInfo checking = new AccountInfo(user.getUserID(), 1111);
        AccountInfo savings = new AccountInfo(user.getUserID(), 2222, AccountType.SAVINGS);
        AccountRepo.insertAccount(checking);
        AccountRepo.insertAccount(savings);

        List<AccountInfo> accounts = repo.findAccountsByUserId(user.getUserID());
        assertEquals(2, accounts.size());
    }

    @Test
    void findAccountByIdReturnsNullWhenMissing() {
        assertNull(repo.findAccountById(UUID.randomUUID()));
    }

    @Test
    void updateUserChangesDatabase() {
        User user = new User("Before", 24, "Old");
        AccountRepo.insertUser(user);

        user.setName("After");
        repo.updateUser(user);

        User found = repo.findUserById(user.getUserID());
        assertEquals("After", found.getName());
    }

    @Test
    void updateAccountChangesBlanaceAndFrozen() {
        User user = new User("Owner3", 40, "Pass123!");
        AccountRepo.insertUser(user);

        AccountInfo account = new AccountInfo(user.getUserID(), 9999);
        AccountRepo.insertAccount(account);

        account.setBalance(25000L);
        account.setFrozen(true);
        repo.updateAccount(account);

        AccountInfo found = repo.findAccountById(account.getAccountID());
        assertEquals(25000L, found.getBalance());
        assertTrue(found.isFrozen());

    }

    @Test
    void insertTransactionThenFindByAccountId() {
        User user = new User("Owner4", 22, "Pass123!");
        AccountRepo.insertUser(user);

        AccountInfo account = new AccountInfo(user.getUserID(), 4444);
        AccountRepo.insertAccount(account);

        Transaction deposit = new Transaction(account.getAccountID(), TransactionType.DEPOSIT, 500L);
        repo.insertTransaction(deposit);

        List<Transaction> found = repo.findTransactionsByAccountId(account.getAccountID());
        assertEquals(1, found.size());
        assertEquals(deposit.getTransactionId(), found.get(0).getTransactionId());
        assertEquals(TransactionType.DEPOSIT, found.get(0).getType());
        assertEquals(500L, found.get(0).getAmount());
        assertNull(found.get(0).getDestinationAccountId());
    }

    @Test
    void findTransactionsByAccountIdRespectsLimit() {
        User user = new User("Owner5", 22, "Pass123!");
        AccountRepo.insertUser(user);

        AccountInfo account = new AccountInfo(user.getUserID(), 5555);
        AccountRepo.insertAccount(account);

        repo.insertTransaction(new Transaction(account.getAccountID(), TransactionType.DEPOSIT, 100L));
        repo.insertTransaction(new Transaction(account.getAccountID(), TransactionType.DEPOSIT, 200L));
        repo.insertTransaction(new Transaction(account.getAccountID(), TransactionType.WITHDRAWAL, 50L));

        List<Transaction> found = repo.findTransactionsByAccountId(account.getAccountID(), 2);
        assertEquals(2, found.size());
    }

    @Test
    void transferMoneyMovesBalanceAndRecordsTransaction() {
        User user = new User("Owner6", 35, "Pass123!");
        AccountRepo.insertUser(user);

        AccountInfo source = new AccountInfo(UUID.randomUUID(), user.getUserID(), 1001, AccountType.CHECKING, 10000L, false);
        AccountInfo dest = new AccountInfo(UUID.randomUUID(), user.getUserID(), 1002, AccountType.SAVINGS, 0L, false);
        AccountRepo.insertAccount(source);
        AccountRepo.insertAccount(dest);

        assertTrue(repo.transferMoney(source, dest, 3000L));

        AccountInfo sourceFound = repo.findAccountById(source.getAccountID());
        AccountInfo destFound = repo.findAccountById(dest.getAccountID());
        assertEquals(7000L, sourceFound.getBalance());
        assertEquals(3000L, destFound.getBalance());

        List<Transaction> ts = repo.findTransactionsByAccountId(source.getAccountID());
        assertEquals(1, ts.size());
        assertEquals(TransactionType.TRANSFER, ts.get(0).getType());
        assertEquals(dest.getAccountID(), ts.get(0).getDestinationAccountId());
    }

    @Test
    void deleteAccountRemovesAccountAndTransactions() {
        User user = new User("Owner7", 28, "Pass123!");
        AccountRepo.insertUser(user);

        AccountInfo account = new AccountInfo(user.getUserID(), 7777);
        AccountRepo.insertAccount(account);
        repo.insertTransaction(new Transaction(account.getAccountID(), TransactionType.DEPOSIT, 100L));

        AccountRepo.deleteAccount(account);

        assertNull(repo.findAccountById(account.getAccountID()));
        assertEquals(0, repo.findTransactionsByAccountId(account.getAccountID()).size());
    }

    @Test
    void deleteAllAccountsRemovesThatUsersAccounts() {
        User user = new User("Owner8", 33, "Pass123!");
        AccountRepo.insertUser(user);

        AccountRepo.insertAccount(new AccountInfo(user.getUserID(), 1111));
        AccountRepo.insertAccount(new AccountInfo(user.getUserID(), 2222, AccountType.SAVINGS));

        AccountRepo.deleteAllAccounts(user.getUserID());
        assertEquals(0, repo.findAccountsByUserId(user.getUserID()).size());
    }

    @Test
    void deleteAllAccountsDoesNotRemoveOtherUsersAccounts() {
        User user1 = new User("Keep", 40, "Pass123!");
        User user2 = new User("Remove", 41, "Pass123!");
        AccountRepo.insertUser(user1);
        AccountRepo.insertUser(user2);

        AccountRepo.insertAccount(new AccountInfo(user1.getUserID(), 1000));
        AccountRepo.insertAccount(new AccountInfo(user2.getUserID(), 2000));

        AccountRepo.deleteAllAccounts(user2.getUserID());

        assertEquals(0, repo.findAccountsByUserId(user2.getUserID()).size());
        assertEquals(1, repo.findAccountsByUserId(user1.getUserID()).size());
    }
}
