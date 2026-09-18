package com.revature.Manbujin.Utility;

/**
 * Converts dollar text the user types into cents (long), and back.
 * Does not use floating-point.
 */
public class Money {

    /**
     * "12.50" -> 1250, "12" -> 1200, "12.5" -> 1250
     */
    public static long toCents(String amount) {
        String amountTrimmed = amount.trim();
        boolean negative = amountTrimmed.startsWith("-");
        if (negative) {
            amountTrimmed = amountTrimmed.substring(1);
        }

        String[] parts = amountTrimmed.split("\\.", -1);
        if (parts.length > 2) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }

        long dollars = 0;

        if (parts[0].isEmpty()) {
            dollars = Long.parseLong("0");
        } else {
            dollars = Long.parseLong(parts[0]);
        }

        long cents = 0;
        if (parts.length == 2 && !parts[1].isEmpty()) {
            String fraction = parts[1];
            if (fraction.length() == 1) {
                fraction = fraction + "0";
            }

            if (fraction.length() != 2) {
                throw new IllegalArgumentException("Amount cannot have more than 2 decimal places: " + amount);
            }
            cents = Long.parseLong(fraction);
        }

        long total = dollars * 100 + cents;

        if (negative) {
            return -total;
        } else {
            return total;
        }
    }

    /**
     * 1250 -> "12.50"
     */
    public static String fromCents(long cents) {
        long absolute = Math.abs(cents);
        long dollars = absolute / 100;
        long leftover = absolute % 100;
        String formatted = dollars + "." + String.format("%02d", leftover);
        if (cents < 0) {
            return "-" + formatted;
        } else {
            return formatted;
        }
    }
}
