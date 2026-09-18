package com.revature.Manbujin.BusinessLogic;

import com.revature.Manbujin.model.AccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalanceValidation {

    private static final Logger logger =
            LoggerFactory.getLogger(BalanceValidation.class);

    public static boolean isBalanceNegative(AccountInfo account) {

        if (account == null) {
            logger.error("Balance validation failed: account was null");
            throw new IllegalArgumentException("Account cannot be null");
        }

        if (account.getBalance() < 0L) {
            logger.error(
                    "Negative balance detected for account {}",
                    account.getAccountID()
            );

            return true;
        }

        return false;
    }
}