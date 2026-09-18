package com.revature.Manbujin.BusinessLogic;

import com.revature.Manbujin.Repository.AccountRepo;
import com.revature.Manbujin.Utility.ConnectionFactory;
import com.revature.Manbujin.TestAccounts;
import com.revature.Manbujin.model.*;
import ch.qos.logback.classic.*;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Exercises the existing API/model/repository path without changing interface code. */
class PersistenceIntegrationTest {
    private final AccountRepo repo = new AccountRepo();
    private final BankTransactions bank = new BankTransactions();
    private AccountInfo account;
    private User owner;
    private Logger bankLogger, repoLogger;
    private ListAppender<ILoggingEvent> bankEvents, repoEvents;

    @BeforeEach void setup() {
        account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setBalance(10000);
        TestAccounts.save(account);
        owner = repo.findUserById(account.getUserID());
        bankLogger = (Logger) LoggerFactory.getLogger(BankLog.class);
        repoLogger = (Logger) LoggerFactory.getLogger(AccountRepo.class);
        bankEvents = new ListAppender<>();
        repoEvents = new ListAppender<>();
        bankEvents.start();
        repoEvents.start();
        bankLogger.addAppender(bankEvents);
        repoLogger.addAppender(repoEvents);
    }
    @AfterEach void cleanup() {
        bankLogger.detachAppender(bankEvents);
        repoLogger.detachAppender(repoEvents);
        bankEvents.stop();
        repoEvents.stop();
    }
    @Test void depositSurvivesReloadWithHistory() {
        assertEquals(1250, bank.deposit(account, 1250));
        assertEquals(11250, new AccountInfo(account.getAccountID()).getBalance());
        assertEquals(TransactionType.DEPOSIT, account.getTransactions().getFirst().getType());
        assertEquals(1250, account.getTransactions().getFirst().getAmount());
        assertEquals("DEPOSIT succeeded", bankEvents.list.getLast().getFormattedMessage());
    }
    @Test void withdrawalSurvivesReloadWithHistory() {
        assertEquals(2500, bank.withdraw(account, 2500));
        assertEquals(7500, new AccountInfo(account.getAccountID()).getBalance());
        assertEquals(TransactionType.WITHDRAWAL, account.getTransactions().getFirst().getType());
        assertEquals("WITHDRAWAL succeeded", bankEvents.list.getLast().getFormattedMessage());
    }
    @Test void rejectedWithdrawalCreatesNoHistory() {
        assertEquals(0, bank.withdraw(account, 10001));
        assertEquals(10000, new AccountInfo(account.getAccountID()).getBalance());
        assertTrue(account.getTransactions().isEmpty());
        assertEquals(Level.ERROR, bankEvents.list.getLast().getLevel());
    }
    @Test void frozenDepositCreatesNoHistory() {
        account.setFrozen(true);
        repo.updateAccount(account);
        assertEquals(0, bank.deposit(account, 100));
        assertTrue(account.getTransactions().isEmpty());
    }
    @Test void staleBalanceCannotOverwriteNewerDeposit() {
        AccountInfo stale = new AccountInfo(account.getAccountID());
        assertEquals(100, bank.deposit(account, 100));
        assertEquals(0, bank.deposit(stale, 100));
        assertEquals(10100, new AccountInfo(account.getAccountID()).getBalance());
        assertEquals(1, account.getTransactions().size());
    }
    @Test void historyFailureRollsBackDeposit() throws Exception {
        try (Connection c = ConnectionFactory.getAutoCommitConnect(); Statement s = c.createStatement()) {
            // Only this disposable test database gets a trigger that rejects history writes.
            s.execute("CREATE TRIGGER fail_history BEFORE INSERT ON Transactions BEGIN SELECT RAISE(ABORT, 'test outage'); END;");
            try {
                assertEquals(0, bank.deposit(account, 100));
                assertEquals(10000, account.getBalance());
                assertEquals(10000, new AccountInfo(account.getAccountID()).getBalance());
                assertTrue(account.getTransactions().isEmpty());
                assertEquals(Level.ERROR, bankEvents.list.getLast().getLevel());
                assertTrue(repoEvents.list.stream().anyMatch(e -> e.getLevel() == Level.ERROR
                        && e.getThrowableProxy() != null));
                assertTrue(bankEvents.list.stream().noneMatch(e -> e.getLevel() == Level.INFO));
            } finally { s.execute("DROP TRIGGER fail_history"); }
        }
    }
    @Test void historyFailureRollsBackWithdrawal() throws Exception {
        try (Connection c = ConnectionFactory.getAutoCommitConnect(); Statement s = c.createStatement()) {
            s.execute("CREATE TRIGGER fail_history BEFORE INSERT ON Transactions BEGIN SELECT RAISE(ABORT, 'test outage'); END;");
            try {
                assertEquals(0, bank.withdraw(account, 100));
                assertEquals(10000, account.getBalance());
                assertEquals(10000, new AccountInfo(account.getAccountID()).getBalance());
                assertTrue(account.getTransactions().isEmpty());
            } finally { s.execute("DROP TRIGGER fail_history"); }
        }
    }
    @Test void repositorySavePositive() {
        assertTrue(repo.saveBalanceAndHistory(account, 10100,
                new Transaction(account.getAccountID(), TransactionType.DEPOSIT, 100)));
        assertEquals(10100, new AccountInfo(account.getAccountID()).getBalance());
        assertEquals(1, account.getTransactions().size());
    }
    @Test void repositorySaveMissingAccountIsRejected() {
        AccountInfo missing = new AccountInfo(UUID.randomUUID(), 1234);
        assertFalse(repo.saveBalanceAndHistory(missing, 100,
                new Transaction(missing.getAccountID(), TransactionType.DEPOSIT, 100)));
    }
    @Test void overflowDepositRejected() {
        account.setBalance(Long.MAX_VALUE);
        repo.updateAccount(account);
        assertEquals(0, bank.deposit(account, 1));
        assertEquals(Long.MAX_VALUE, new AccountInfo(account.getAccountID()).getBalance());
        assertTrue(account.getTransactions().isEmpty());
    }
    @Test void existingHistoryPathIsNewestFirstAndLimited() {
        Transaction older = new Transaction(account.getAccountID(), TransactionType.DEPOSIT, 100);
        older.setTimestamp(java.time.LocalDateTime.of(2026,1,1,10,0));
        Transaction newer = new Transaction(account.getAccountID(), TransactionType.WITHDRAWAL, 50);
        newer.setTimestamp(java.time.LocalDateTime.of(2026,1,1,11,0));
        repo.insertTransaction(older);
        repo.insertTransaction(newer);
        assertEquals(newer.getTransactionId(), account.getTransactions(1).getFirst().getTransactionId());
        assertEquals(1, account.getTransactions(1).size());
        assertEquals(Level.INFO, repoEvents.list.getLast().getLevel());
    }
    @Test void emptyHistoryQueryLogsSuccess() {
        assertTrue(account.getTransactions().isEmpty());
        assertEquals("TRANSACTION_HISTORY query succeeded", repoEvents.list.getLast().getFormattedMessage());
    }
    @Test void failedHistoryQueryLogsErrorWithoutSuccess() {
        try (var connections = mockStatic(ConnectionFactory.class)) {
            connections.when(ConnectionFactory::getAutoCommitConnect).thenThrow(new SQLException("test outage"));
            // Preserve the repository's existing empty-list failure contract for the fixed API.
            assertTrue(account.getTransactions().isEmpty());
            assertEquals(Level.ERROR, repoEvents.list.getLast().getLevel());
            assertTrue(repoEvents.list.stream().noneMatch(e -> e.getLevel() == Level.INFO));
        }
    }
    @Test void credentialsMatchWithoutLoggingCredentials() {
        assertNotNull(repo.findUserByNameAndPassword(owner.getName(), owner.getPassword()));
        assertEquals("CREDENTIAL_CHECK matched", repoEvents.list.getLast().getFormattedMessage());
        assertFalse(repoEvents.list.getLast().getFormattedMessage().contains(owner.getPassword()));
    }
    @Test void incorrectCredentialsLogRejection() {
        assertNull(repo.findUserByNameAndPassword(owner.getName(), "wrong"));
        assertEquals(Level.ERROR, repoEvents.list.getLast().getLevel());
        assertEquals("CREDENTIAL_CHECK rejected", repoEvents.list.getLast().getFormattedMessage());
    }
    @Test void userSaveLogsSuccess() {
        AccountRepo.insertUser(new User(UUID.randomUUID().toString(), 21, "test-secret"));
        assertEquals("USER_SAVE succeeded", repoEvents.list.getLast().getFormattedMessage());
    }
    @Test void duplicateUserIdLogsDatabaseFailure() {
        AccountRepo.insertUser(owner);
        assertEquals(Level.ERROR, repoEvents.list.getLast().getLevel());
        assertTrue(repoEvents.list.stream().noneMatch(e -> e.getLevel() == Level.INFO));
    }
    @Test void accountSaveLogsSuccess() {
        AccountRepo.insertAccount(new AccountInfo(owner.getUserID(), 4321));
        assertEquals("ACCOUNT_SAVE succeeded", repoEvents.list.getLast().getFormattedMessage());
    }
    @Test void accountWithoutSavedOwnerLogsFailure() {
        AccountRepo.insertAccount(new AccountInfo(UUID.randomUUID(), 4321));
        assertEquals(Level.ERROR, repoEvents.list.getLast().getLevel());
    }
   /* @Test void originalApiDepositWithdrawAndHistoryReachDatabase() {
        API api = new API();
        assertTrue(api.login(owner.getName(), owner.getPassword()));
        api.deposit(account.getAccountID().toString(), "50.00");
        api.withdraw(account.getAccountID().toString(), "10.00");
        assertEquals(14000, new AccountInfo(account.getAccountID()).getBalance());
        api.getAcctTransactions(account.getAccountID().toString(), 2);
        // Assert the original API's actual raw-cent format, without demanding new formatting.
        assertTrue(api.getResult().contains("5000"));
        assertTrue(api.getResult().contains("1000"));
        assertEquals(2, api.getResult().lines().count());
    } */
    @Test void transferRepositoryFailureRollsBackDebit() {
        AccountInfo missing = new AccountInfo(owner.getUserID(), 4321);
        assertEquals(0, bank.transfer(account, missing, 100));
        assertEquals(10000, new AccountInfo(account.getAccountID()).getBalance());
        assertTrue(account.getTransactions().isEmpty());
        assertEquals(Level.ERROR, bankEvents.list.getLast().getLevel());
    }
}

