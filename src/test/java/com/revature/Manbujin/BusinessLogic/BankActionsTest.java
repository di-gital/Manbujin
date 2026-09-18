package com.revature.Manbujin.BusinessLogic;

import com.revature.Manbujin.model.AccountInfo;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BankActionsTest {


    @Test
    void checkDeposit() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setFrozen(false);

        BankActions bankActions = new BankActions();

        assertEquals(true, bankActions.checkDeposit(account, 50L));
    }


    @Test
    void checkDepositFrozen() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setFrozen(true);

        BankActions bankActions = new BankActions();

        assertFalse(bankActions.checkDeposit(account, 50L));
    }


    @Test
    void checkDepositNegative() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setFrozen(false);

        BankActions bankActions = new BankActions();

        assertEquals(false, bankActions.checkDeposit(account, -50L));
    }


    @Test
    void checkWithdraw() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setBalance(100L);
        account.setFrozen(false);

        BankActions bankActions = new BankActions();

        assertEquals(true, bankActions.checkWithdraw(account, 50L));
    }


    @Test
    void checkWithdrawNegative() {
        AccountInfo account = new AccountInfo(UUID.randomUUID(), 1234);
        account.setBalance(100L);
        account.setFrozen(false);

        BankActions bankActions = new BankActions();

        assertEquals(false, bankActions.checkWithdraw(account, 150L));
    }

    @Test
    void checkTransfer() {
        AccountInfo sender = new AccountInfo(UUID.randomUUID(), 1234);
        sender.setBalance(100L);
        sender.setFrozen(false);

        AccountInfo receiver = new AccountInfo(UUID.randomUUID(), 4321);
        receiver.setBalance(50L);
        receiver.setFrozen(false);

        BankActions bankActions = new BankActions();

        assertEquals(true, bankActions.checkTransfer(sender, receiver, 25L));
    }


    @Test
    void checkTransferNegative() {
        AccountInfo sender = new AccountInfo(UUID.randomUUID(), 1234);
        sender.setBalance(100L);
        sender.setFrozen(false);

        AccountInfo receiver = new AccountInfo(UUID.randomUUID(), 4321);
        receiver.setBalance(50L);
        receiver.setFrozen(false);

        BankActions bankActions = new BankActions();

        assertEquals(false, bankActions.checkTransfer(sender, receiver, 150L));
    }
}