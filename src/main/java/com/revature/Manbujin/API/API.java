package com.revature.Manbujin.API;

import com.revature.Manbujin.BusinessLogic.*;
import com.revature.Manbujin.Repository.AccountRepo;
import com.revature.Manbujin.Utility.Money;
import com.revature.Manbujin.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class API {

    private User user;
    private String result;
    private final BankTransactions bankTransactions;
    private final AccountRepo accountRepo;

    public API() {
        this.user = null;
        this.result = null;
        this.bankTransactions = new BankTransactions();
        this.accountRepo = new AccountRepo();
        accountRepo.initSchema();
    }

    /**
     * Send the credentials from the user to business layer
     * to add them to the database, so they pass the requirements.
     *
     * @author Nicholas DiGirolamo
     * @param username
     * @param password
     * @return boolean, the success of the registration
     */
    public boolean register(String username, String password) {
        if(this.accountRepo.findUserByName(username) != null) {
            this.result = "Username taken. Choose a different username.";
            return false;
        }

        boolean unameStat = AccountValidation.isUsernameValid(username);
        boolean pwStat = AccountValidation.isPasswordValid(password);

        if(unameStat && pwStat) {
            User tmpUser = new User(username, 0, password);
            AccountRepo.insertUser(tmpUser);
            this.result = "User " + username + " successfully registered.";
        } else {
            this.result =
             """
             Username must include 1 Uppercase, 1 Lowercase, and must be between 8 and 16 characters.
             Password must include 1 Uppercase, 1 Lowercase, 1 number, 1 special character, and be 16-24 characters.
             """;
        }

        return unameStat && pwStat;
    }

    /**
     * Send the credentials from the user to check if such a
     * user exists in the database. If so, update the session
     * to store the user corresponding to those credentials.
     *
     * @author Nicholas DiGirolamo
     * @param username
     * @param password
     * @return boolean, the success of the login attempt
     */
    public boolean login(String username, String password) {
        boolean uEmpty = username.isEmpty();
        boolean pEmpty = password.isEmpty();

        if(pEmpty) this.result = "Please enter a password";
        if(uEmpty) this.result = "Please enter a username";
        if(uEmpty || pEmpty) return false;

        User stagedUser = this.accountRepo.findUserByNameAndPassword(username, password);

        if(stagedUser == null) {
            this.result = "Login failed. Try again.";
            return false;
        }

        this.user = stagedUser;
        this.result = "Login successful.";
        return true;
    }

    /**
     * Add money to an account that the signed-in user owns
     *
     * @author Nicholas DiGirolamo
     * @param acct
     * @param amount
     */
    public void deposit(String acct, String amount) {
        AccountInfo ai = null;

        try {
            UUID.fromString(acct);
        } catch (IllegalArgumentException e) {
            this.result = "Not a valid bank account number.";
            return;
        }

        for (AccountInfo a : this.user.getAccounts())
            if (a.getAccountID().toString().equals(acct))
                ai = a;

        if(ai == null) {
            this.result = "You do not own the destination account.";
            return;
        }

        long specBalance = Money.toCents(amount);

        if(specBalance == 0 || this.bankTransactions.deposit(ai, specBalance) == 0) {
            this.result = "No money was deposited. Check your prompt again.";
        } else {
            this.result = "$" + specBalance / 100 + "." + specBalance % 100 + " successfully deposited into "
                    + ai.getAccountID().toString() + ".";
        }
    }

    /**
     * Remove a specified amount from an account
     *
     * @author Nicholas DiGirolamo
     * @param acct
     * @param amount
     */
    public void withdraw(String acct, String amount, String pin) {
        AccountInfo ai = null;

        try {
            UUID.fromString(acct);
        } catch (IllegalArgumentException e) {
            this.result = "Not a valid bank account number.";
            return;
        }

        int corrPin = validatePin(pin);
        if(corrPin == 0) {
            this.result = "Invalid PIN.";
            return;
        }

        for (AccountInfo a : this.user.getAccounts())
            if (a.getAccountID().toString().equals(acct))
                ai = a;

        if(ai == null) {
            this.result = "You do not own the source account.";
            return;
        }

        long specBalance = Money.toCents(amount);

        if(ai.getPin() != corrPin) {
            this.result = "Incorrect PIN";
            return;
        }

        if(specBalance == 0 || this.bankTransactions.withdraw(ai, specBalance) == 0) {
            this.result = "No money was withdrawn. Check your prompt again.";
        } else {
            this.result = "$" + specBalance / 100 + "." + specBalance % 100 + " successfully withdrawn from "
                    + ai.getAccountID().toString() + ".";
        }
    }

    public void transfer(String src, String dest, String amount) {
        long money = Money.toCents(amount);

        if(money <= 0L) {
            this.result = "Zero, negative, or improperly formatted amount.";
            return;
        }

        try {
            UUID.fromString(src);
            UUID.fromString(dest);
        } catch(IllegalArgumentException iae) {
            this.result = "One or both accounts are invalid bank account numbers.";
            return;
        }

        AccountInfo srcAcct = null, destAcct = null;
        for (AccountInfo a : this.user.getAccounts()) {
            if (a.getAccountID().toString().equals(src)) {
                srcAcct = a;
            } else if (a.getAccountID().toString().equals(dest)) {
                destAcct = a;
            }
        }

        if(srcAcct == null) {
            this.result = "Source account does not exist.";
            return;
        }

        if(!srcAcct.getUserID().toString().equals(this.user.getUserID().toString())) {
            this.result = "You are not the owner of the source account.";
            return;
        }

        if(destAcct == null) {
            this.result = "Destination account does not exist.";
            return;
        }

        if(srcAcct.equals(destAcct)) {
            this.result = "Cannot transfer money to the same account.";
            return;
        }

        if(this.bankTransactions.transfer(srcAcct, destAcct, money) == 0) {
            this.result = "No money was transferred.";
        } else {
            this.result = "Transfer successful.";
        }
    }

    /**
     * Retrieve `rows` number of the most recent transactions from a
     * single bank account.
     *
     * @author Nicholas DiGirolamo
     * @param acct, a String representation of a bank account UUID--not a user.
     * @param rows, the number of rows to return.
     *              rows >= 0: that number of rows are returned
     *              rows < 0: return all rows.
     */
    public void getAcctTransactions(String acct, int rows) {
        if(rows == 0) { this.result = ""; return; }

        List<AccountInfo> accounts = this.user.getAccounts();
        StringBuilder sb = new StringBuilder();

        for(AccountInfo ac : accounts) {
            if(ac.getAccountID().toString().equals(acct)) {
                List<Transaction> listTransactions;

                if(rows > 0) listTransactions = ac.getTransactions(rows);
                else listTransactions = ac.getTransactions(); /* Always negative */

                try {
                    for (Transaction t : listTransactions) {
                        sb.append(t.getSourceAccountId());
                        sb.append(" ");
                        sb.append(t.getDestinationAccountId());
                        sb.append(" ");
                        sb.append(t.getAmount());
                        sb.append(" ");
                        sb.append(t.getTimestamp());
                        sb.append("\n");
                    }
                } catch (NullPointerException npe) {
                    sb.append("Warning: null transaction.\n");
                }
            }
        }
        this.result = sb.toString();
    }

    /**
     * Get all transactions associated with a user, querying every
     * account.
     *
     * @author Nicholas DiGirolamo
     */
    public void getTransactions() {
        List<AccountInfo> accounts = this.user.getAccounts();
        StringBuilder sb = new StringBuilder();

        if(accounts.isEmpty()) {
            this.result = "You have no accounts. Open one to see information.";
            return;
        }

        for(AccountInfo ai : accounts) {
            getAcctTransactions(ai.getAccountID().toString(), -1);
            sb.append(this.result);
        }

        this.result = sb.toString();
    }

    public void getTransactions(int rows) {
        this.getTransactions();
        String[] subrange = Arrays.copyOf(this.result.split("\n"), rows);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < rows; i++) {
            sb.append(subrange[i]);
            sb.append("\n");
        }
        this.result = sb.toString();
    }

    /**
     * Retrieve the list of all accounts associated with the user
     * the API session currently holds.
     *
     * @author Nicholas DiGirolamo
     */
    public void getAccts() {
        StringBuilder sb = new StringBuilder();
        List<AccountInfo> accts = this.user.getAccounts();

        sb.append("Your accounts: ");
        sb.append(accts.size());
        sb.append("\n");
        sb.append("Account number/Type/Balance\n");
        for(AccountInfo ac : accts) {
            sb.append(ac.getAccountID());
            sb.append(" ");
            sb.append(ac.getAccountType());
            sb.append(" $");
            long bal = ac.getBalance();
            sb.append(bal / 100);
            sb.append(".");
            sb.append(bal % 100);
            sb.append("\n");
        }
        this.result = sb.toString();
    }

    public void creatAcct(String pin, String type) {
        AccountType at = validateAcctType(type);
        if(at == null) return;

        int correctPin = validatePin(pin);
        if(correctPin == -1) return;

        AccountInfo ai = new AccountInfo(this.user.getUserID(), correctPin, at);
        ai.setBalance(0);
        ai.setUserID(this.user.getUserID());
        ai.setAccountType(at);
        ai.setPin(correctPin);
        ai.setAccountID(ai.getAccountID());
        AccountRepo.insertAccount(ai);
        this.result = "Account successfully created.\nAccount number: " + ai.getAccountID();
    }

    public void deleteAcct(String uuid, String pin) {
        int corrPin = validatePin(pin);

        if(corrPin == -1) {
            this.result = "Invalid PIN";
            return;
        }

        try {
            UUID.fromString(uuid);
        } catch(IllegalArgumentException iae) {
            this.result = "Target account number invalid.";
            return;
        }

        for(AccountInfo ai : this.user.getAccounts()) {
            if(ai.getAccountID().toString().equals(uuid)) {
                AccountRepo.deleteAccount(ai);
                this.result = "Successfully deleted account " + uuid;
                return;
            }
        }
        this.result = "Did not delete account " + uuid + ". Try again.";
    }

    public User getUser() {
        return this.user;
    }

    public String getResult() {
        return this.result;
    }

    private int validatePin(String pin) {
        int correctPin;

        try {
            correctPin = Integer.parseInt(pin);
        } catch(NumberFormatException nfe) {
            this.result = "Invalid PIN.";
            return -1;
        }

        if(correctPin < 0 || correctPin >= 10_000) {
            this.result = "Invalid PIN.";
            return -1;
        }
        return correctPin;
    }

    AccountType validateAcctType(String type) {
        if(type.equalsIgnoreCase("checking")) {
            return AccountType.CHECKING;
        } else if(type.equalsIgnoreCase("savings")) {
            return AccountType.SAVINGS;
        } else {
            this.result = "Invalid account type.";
            return null;
        }
    }
}
