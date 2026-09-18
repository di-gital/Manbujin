package com.revature.Manbujin.Utility;

import java.math.BigDecimal;
import java.util.Locale;

public class AccountNumber {
    public final String number;

    private static final String US = "US";
    private static final String UScode = "USAC"; // "USAC"
    private static final String manSortCode = "201551";

    private BigDecimal computeCheckDigit(BigDecimal partial) {
        final BigDecimal ninetySeven = new BigDecimal(97);
        partial = partial.multiply(new BigDecimal(100));
        BigDecimal remainder = partial.remainder(ninetySeven);
        return ninetySeven.subtract(remainder).add(BigDecimal.ONE);
    }

    private String randomNumber() {
        int acct = (int) (100_000_000 * Math.random());
        StringBuilder sb = new StringBuilder();
        sb.append(adjust(UScode.charAt(0)));
        sb.append(adjust(UScode.charAt(1)));
        sb.append(adjust(UScode.charAt(2)));
        sb.append(adjust(UScode.charAt(3)));
        sb.append(manSortCode);
        sb.append(acct);
        sb.append(adjust(US.charAt(0)));
        sb.append(adjust(US.charAt(1)));

        BigDecimal num = new BigDecimal(sb.toString());
        BigDecimal checkDigit = computeCheckDigit(num);

        sb = new StringBuilder();
        sb.append(US);
        sb.append(checkDigit);
        sb.append(UScode);
        sb.append(manSortCode);
        sb.append(acct);

        return sb.toString();
    }

    AccountNumber() {
        this.number = randomNumber();
    }

    static int adjust(char c) {
        if(c >= 'A' && c <= 'Z') {
            return c - 'A' + 10;
        } else {
            return c;
        }
    }

    public static boolean isValidNumber(String num) {
        num = num.toUpperCase().replace(" ", "");

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(adjust(num.charAt(4)));
            sb.append(adjust(num.charAt(5)));
            sb.append(adjust(num.charAt(6)));
            sb.append(adjust(num.charAt(7)));

            sb.append(num.substring(8, 14));
            sb.append(num.substring(14));

            sb.append(adjust(num.charAt(0)));
            sb.append(adjust(num.charAt(1)));
            sb.append(num.substring(2, 4));

            return new BigDecimal(sb.toString()).remainder(new BigDecimal(97)).equals(BigDecimal.ONE);
        } catch(Exception e) {
            return false;
        }
    }

    public String toString() {
        return this.number;
    }
}
