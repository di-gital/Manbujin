package com.revature.Manbujin.BusinessLogic;

import com.revature.Manbujin.Repository.AccountRepo;
import com.revature.Manbujin.model.AccountInfo;
import java.util.UUID;
import static org.mockito.Mockito.*;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;

class BankLogTest {
    private Logger logger;
    private ListAppender<ILoggingEvent> events;
    @BeforeEach void attach() {
        logger = (Logger) LoggerFactory.getLogger(BankLog.class);
        events = new ListAppender<>();
        events.start();
        logger.addAppender(events);
    }
    @AfterEach void detach() { logger.detachAppender(events); events.stop(); }

    @Test void successUsesInfo() {
        BankLog.outcome(BankLog.Event.DEPOSIT, true, null);
        assertEquals(1, events.list.size());
        assertEquals(Level.INFO, events.list.getFirst().getLevel());
        assertEquals("DEPOSIT succeeded", events.list.getFirst().getFormattedMessage());
    }
    @Test void rejectionUsesError() {
        BankLog.outcome(BankLog.Event.LOGIN, false, null);
        assertEquals(Level.ERROR, events.list.getFirst().getLevel());
        assertEquals("LOGIN failed or was rejected", events.list.getFirst().getFormattedMessage());
    }
    @Test void databaseFailureKeepsTechnicalCause() {
        BankLog.outcome(BankLog.Event.DATABASE, false, new IllegalStateException("test outage"));
        assertEquals(Level.ERROR, events.list.getFirst().getLevel());
        assertEquals("test outage", events.list.getFirst().getThrowableProxy().getMessage());
    }
    @Test void causePreventsFalseSuccess() {
        BankLog.outcome(BankLog.Event.TRANSACTION_HISTORY, true, new IllegalStateException("test outage"));
        assertEquals(Level.ERROR, events.list.getFirst().getLevel());
        assertFalse(events.list.getFirst().getFormattedMessage().contains("succeeded"));
    }

    @Test void depositLogsAfterBalanceChanges() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        com.revature.Manbujin.TestAccounts.save(account);
        assertEquals(50L, new BankTransactions().deposit(account, 50L));
        assertEquals(50L, account.getBalance());
        assertEquals("DEPOSIT succeeded", events.list.getFirst().getFormattedMessage());
    }
    @Test void depositRejectionKeepsBalance() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        assertEquals(0L, new BankTransactions().deposit(account, -50L));
        assertEquals(0L, account.getBalance());
        assertEquals(Level.ERROR, events.list.getFirst().getLevel());
    }
    @Test void withdrawalLogsSuccess() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setBalance(100L);
        com.revature.Manbujin.TestAccounts.save(account);
        assertEquals(50L, new BankTransactions().withdraw(account, 50L));
        assertEquals(50L, account.getBalance());
        assertEquals("WITHDRAWAL succeeded", events.list.getFirst().getFormattedMessage());
    }
    @Test void overdraftLogsFailure() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        assertEquals(0L, new BankTransactions().withdraw(account, 50L));
        assertEquals(Level.ERROR, events.list.getFirst().getLevel());
    }
    @Test void nullAccountIsRejectedAndLogged() {
        assertEquals(0L, new BankTransactions().deposit(null, 50L));
        assertEquals(Level.ERROR, events.list.getFirst().getLevel());
    }
    @Test void transferLogsRepositorySuccess() {
        AccountInfo from = new AccountInfo(UUID.randomUUID(), 1234);
        AccountInfo to = new AccountInfo(UUID.randomUUID(), 4321);
        from.setBalance(100L);
        try (var repos = mockConstruction(AccountRepo.class,
                (repo, context) -> when(repo.transferMoney(from, to, 50L)).thenReturn(true))) {
            assertEquals(50L, new BankTransactions().transfer(from, to, 50L));
            verify(repos.constructed().getFirst()).transferMoney(from, to, 50L);
            assertEquals("TRANSFER succeeded", events.list.getFirst().getFormattedMessage());
        }
    }
    @Test void transferDoesNotClaimSuccessWhenRepositoryFails() {
        AccountInfo from = new AccountInfo(UUID.randomUUID(), 1234);
        AccountInfo to = new AccountInfo(UUID.randomUUID(), 4321);
        from.setBalance(100L);
        try (var repos = mockConstruction(AccountRepo.class)) {
            assertEquals(0L, new BankTransactions().transfer(from, to, 50L));
            verify(repos.constructed().getFirst()).transferMoney(from, to, 50L);
            assertEquals(1, events.list.size());
            assertEquals(Level.ERROR, events.list.getFirst().getLevel());
        }
    }




    @Test void historySuccessEventUsesInfo() {
        BankLog.outcome(BankLog.Event.TRANSACTION_HISTORY, true, null);
        assertEquals("TRANSACTION_HISTORY succeeded", events.list.getFirst().getFormattedMessage());
        assertEquals(Level.INFO, events.list.getFirst().getLevel());
    }
    @Test void historyFailureEventUsesError() {
        BankLog.outcome(BankLog.Event.TRANSACTION_HISTORY, false, new IllegalStateException("test outage"));
        assertEquals("TRANSACTION_HISTORY failed or was rejected", events.list.getFirst().getFormattedMessage());
        assertEquals(Level.ERROR, events.list.getFirst().getLevel());
    }
}
