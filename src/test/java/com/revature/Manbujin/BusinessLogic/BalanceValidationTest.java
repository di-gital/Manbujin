package com.revature.Manbujin.BusinessLogic;

import com.revature.Manbujin.model.AccountInfo;
import com.revature.Manbujin.model.AccountType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BalanceValidationTest {

    @Test
    void positiveBalanceShouldReturnFalse() {

        AccountInfo account = new AccountInfo(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1234,
                AccountType.CHECKING,
                10000L,
                false
        );

        assertFalse(BalanceValidation.isBalanceNegative(account));
    }

    @Test
    void zeroBalanceShouldReturnFalse() {

        AccountInfo account = new AccountInfo(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1234,
                AccountType.CHECKING,
                0L,
                false
        );

        assertFalse(BalanceValidation.isBalanceNegative(account));
    }

    @Test
    void negativeBalanceShouldReturnTrue() {

        AccountInfo account = new AccountInfo(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1234,
                AccountType.CHECKING,
                -1L,
                false
        );

        assertTrue(BalanceValidation.isBalanceNegative(account));
    }

    @Test
    void nullAccountShouldThrowException() {

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> BalanceValidation.isBalanceNegative(null)
        );
        assertEquals("Account cannot be null", error.getMessage());
    }
}