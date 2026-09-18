package com.revature.Manbujin.Utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountNumberTest {
    @Test
    public void goodAccountTest() {
        String validAcct = "GB82WEST12345698765432";
        String validAcctSpaces = "GB82 WEST 1234 5698 7654 32";
        Assertions.assertTrue(AccountNumber.isValidNumber(validAcct));
        Assertions.assertEquals(AccountNumber.isValidNumber(validAcct),
                AccountNumber.isValidNumber(validAcctSpaces));
    }

    @Test
    public void badAccountTest() {
        String nan = "NaN";
        String invalidAcct = "GB82WEST12345698765433";

        Assertions.assertFalse(AccountNumber.isValidNumber(nan));
        Assertions.assertFalse(AccountNumber.isValidNumber(invalidAcct));
    }

    @Test
    public void newAccountNumTest() {
        AccountNumber an = new AccountNumber();
        Assertions.assertTrue(AccountNumber.isValidNumber(an.toString()));
    }

    @Test
    public void adjustmentTest() {
        Assertions.assertEquals(10, AccountNumber.adjust('A'));
        Assertions.assertEquals(32, AccountNumber.adjust('W'));
    }
}
