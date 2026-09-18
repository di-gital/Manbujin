package com.revature.Manbujin.BusinessLogic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Fixed event names keep account objects, passwords and PINs out of messages
public final class BankLog {
    private static final Logger logger = LoggerFactory.getLogger(BankLog.class);
    private BankLog() { }

    public enum Event {
        DEPOSIT_IN_MEMORY, WITHDRAWAL_IN_MEMORY, TRANSFER,
        TRANSACTION_HISTORY, LOGIN, DATABASE,
        DEPOSIT, WITHDRAWAL // These events now mean the database save completed.
    }

    // Call after the result is known; A cause always means failure
    public static void outcome(Event event, boolean successful, Throwable cause) {
        if (successful && cause == null) {
            logger.info("{} succeeded", event);
        } else {
            logger.error("{} failed or was rejected", event, cause);
        }
    }
}
