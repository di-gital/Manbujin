package com.revature.Manbujin.Repository;

import com.revature.Manbujin.Utility.ConnectionFactory;
import com.revature.Manbujin.Utility.PasswordEncryption;
import com.revature.Manbujin.model.*;
import java.time.LocalDateTime;
import java.util.*;

import java.sql.*;

public class AccountRepo {
    // Record database diagnostics in the log file instead of printing stack traces
    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(AccountRepo.class);


    /**
     * Creates the owners table if it does not exists
     */
    private void createOwnersTable() {
        String query = """
            CREATE TABLE IF NOT EXISTS Owners (
                userID TEXT PRIMARY KEY,
                name TEXT NOT NULL UNIQUE,
                age INTEGER NOT NULL,
                passWord TEXT NOT NULL
            ); """;

        try(
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                Statement simpleStatement = connection.createStatement();
        ) {
            simpleStatement.execute(query);

        } catch (SQLException exception) {
            logger.error("DATABASE operation failed", exception);
        }
    }

    /**
     * Create the accounts table if it does not exists
     */
    private void createAccountsTable() {

        String query = """
                CREATE TABLE IF NOT EXISTS Accounts (
                    accountID TEXT PRIMARY KEY,
                    userID TEXT NOT NULL,
                    pin INTEGER NOT NULL,
                    accountType TEXT NOT NULL,
                    balance INTEGER NOT NULL DEFAULT 0,
                    frozen INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (userID) REFERENCES Owners(userID)
                );
                """;
        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                Statement simpleStatement = connection.createStatement();
        ) {
            simpleStatement.execute(query);

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }

    }


    /**
     * Create the transactions table if it does not exist
     */
    private void createTransactionsTable() {
        String query = """
                CREATE TABLE IF NOT EXISTS Transactions (
                transactionID TEXT PRIMARY KEY,
                sourceAccountId TEXT NOT NULL,
                destinationAccountId TEXT,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                timestamp TEXT NOT NULL,
                FOREIGN KEY (sourceAccountId) REFERENCES Accounts(accountID),
                FOREIGN KEY (destinationAccountId) REFERENCES Accounts(accountID)
            );
                """;

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                Statement simpleStatement = connection.createStatement();
        ) {
            simpleStatement.execute(query);

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
    }

    /**
     * Method to initialize the schema and acreate the tables for the database
     */
    public void initSchema() {
        createOwnersTable();
        createAccountsTable();
        createTransactionsTable();
    }


    /**
     * Method designed to insert an User into the table
     * It will take an user object
     */
    public static void insertUser(User user) {
        String query = "INSERT INTO Owners (userID, name, age, passWord) VALUES (?, ?, ?, ?)";

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query);
        ) {
            ps.setString(1, user.getUserID().toString());
            ps.setString(2, user.getName());
            ps.setInt(3, user.getAge());
            ps.setString(4, user.getPassword());
            // Describe the database result; the API owns the overall user action.
            if (ps.executeUpdate() == 1) {
                logger.info("USER_SAVE succeeded");
                return;
            }
            logger.error("USER_SAVE failed: no row saved");

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
    }

    /**
     * Method designed to insert an account into the table in the database
     * It will take an Accountinfo object
     */
    public static void insertAccount(AccountInfo account) {
        String query = """
            INSERT INTO Accounts (accountID, userID, pin, accountType, balance, frozen)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query);
        ) {
            ps.setString(1, account.getAccountID().toString());
            ps.setString(2, account.getUserID().toString());
            ps.setInt(3, account.getPin());
            ps.setString(4, account.getAccountType().name());
            ps.setLong(5, account.getBalance());
            ps.setInt(6, account.isFrozen()? 1 : 0);
            // Describe the database result; the API owns the overall user action.
            if (ps.executeUpdate() == 1) logger.info("ACCOUNT_SAVE succeeded");
            else logger.error("ACCOUNT_SAVE failed: no row saved");

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }

    }

    /**
     * Method to insert a transaction with an auto commit
     * It will take a transaction object
     */
    public void insertTransaction(Transaction transaction) {
        try(Connection connection = ConnectionFactory.getAutoCommitConnect()) {
            insertTransaction(connection, transaction);
        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
    }

    /**
     * Method designed to insert a transaction into the table
     * It will take a transaction object
     * It will take a connection object
     */
    private void insertTransaction(Connection connection, Transaction transaction) throws SQLException {
        String query = """
            INSERT INTO Transactions (transactionID, sourceAccountId, destinationAccountId, type, amount, timestamp)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, transaction.getTransactionId().toString());
            ps.setString(2, transaction.getSourceAccountId().toString());

            if (transaction.getDestinationAccountId() == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, transaction.getDestinationAccountId().toString());
            }

            ps.setString(4, transaction.getType().name());
            ps.setLong(5, transaction.getAmount());
            ps.setString(6, transaction.getTimestamp().toString());

            if (ps.executeUpdate() != 1) {
                throw new SQLException("Insert transaction failed");
            }
        }
    }

    /**
     * Method to read from the ResultSet and create the user object
     * It will return an User object
     */
    private User mapUser(ResultSet rs) throws SQLException {
        return new User (
                UUID.fromString(rs.getString("userID")),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("passWord")
        );
    }

    /**
     * Method to read from the ResultSet and create the AccountInfo object
     * It will return an AccountInfo object
     */
    private AccountInfo mapAccount(ResultSet rs) throws SQLException {
        String ownerId = rs.getString("userID");
        return new AccountInfo(
                UUID.fromString(rs.getString("accountID")),
                ownerId == null ? null : UUID.fromString(ownerId),
                rs.getInt("pin"),
                AccountType.valueOf(rs.getString("accountType")),
                rs.getLong("balance"),
                rs.getInt("frozen") != 0
        );
    }

    /**
     * Method to read from the ResultSet and create the transaction object
     * It will return a transaction object
     */
    private Transaction mapTransaction(ResultSet rs) throws SQLException{
        return new Transaction(
                UUID.fromString(rs.getString("transactionID")),
                UUID.fromString(rs.getString("sourceAccountId")),
                rs.getString("destinationAccountId") == null ? null : UUID.fromString(rs.getString("destinationAccountId")),
                TransactionType.valueOf(rs.getString("type")),
                rs.getLong("amount"),
                LocalDateTime.parse(rs.getString("timestamp"))
        );
    }


    /**
     * Method to find an user by ID
     * will return Null if that record does not exist
     * Return an user object if it finds it
     */
    public User findUserById(UUID userID) {
        String query = "SELECT * FROM Owners WHERE userID = ?";

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query);
        ) {
            ps.setString(1, userID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }

        return null;
    }

    /**
     * Finds a user by username and password
     * Returns null if no matching row exists
     */
    public User findUserByNameAndPassword(String name, String passWord) {
        //String query = "SELECT * FROM Owners WHERE name = ? AND passWord = ?";
        String query = "SELECT * FROM Owners WHERE name = ?"; //Mo

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query);
        ) {
            ps.setString(1, name);
            //ps.setString(2, passWord);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User found = mapUser(rs);
                // Do not log the supplied username or password.

                String decryptedPassword = PasswordEncryption.decrypt(found.getPassword()); //Mo

                if (decryptedPassword.equals(passWord)) { //Mo
                    logger.info("CREDENTIAL_CHECK matched");
                    return found;
                    //logger.info("CREDENTIAL_CHECK matched");
                    //return found;
                }
            }
            logger.error("CREDENTIAL_CHECK rejected");

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
        return null;
    }

    /** 
     * Finds a user by name
     * Returns null if it does not find an user
     * Returns an User object if it does find it
     */
    public User findUserByName(String name) {
        String query = "SELECT * FROM Owners WHERE name = ?";

        try (
            Connection connection = ConnectionFactory.getAutoCommitConnect();
            PreparedStatement ps = connection.prepareStatement(query);
        ) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
        return null;
    }
    
    /** 
     * Method to find an account by ID
     * Return null if the record does not exists
     * Return the AccountInfo object if it finds it
     */
    public AccountInfo findAccountById(UUID accountID) {
        String query = "SELECT * FROM Accounts WHERE accountID = ?";

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query);
        ) {

            ps.setString(1, accountID.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return mapAccount(rs);
            }

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }

        return null;
    }

    /**
     * Method to find every account owned by a user
     * Return an empty list if the user has no accounts
     * Return a list of AccountInfo objects if it finds them
     */
    public List<AccountInfo> findAccountsByUserId(UUID userID) {
        String query = "SELECT * FROM Accounts WHERE userID = ?";
        List<AccountInfo> accounts = new ArrayList<>();

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query);
        ) {
            ps.setString(1, userID.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }

        return accounts;
    }


    /**
     * Method to find the transactions by account ID
     * Return a list of transaction objects
     */
    public List<Transaction> findTransactionsByAccountId(UUID accountID) {
        return findTransactionsByAccountId(accountID, -1);
    }

    /**
     * Find the most recent transactions by account ID
     * If limit is greater than 0, only that many rows are returned
     */
    public List<Transaction> findTransactionsByAccountId(UUID accountID, int limit) {
        String query = """
            SELECT * FROM Transactions
            WHERE sourceAccountId = ? OR destinationAccountId = ?
            ORDER BY timestamp DESC
        """;
        if (limit > 0) {
            query += " LIMIT ?";
        }
        List<Transaction> transactions = new ArrayList<>();

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query);
        ) {
            ps.setString(1, accountID.toString());
            ps.setString(2, accountID.toString());
            if (limit > 0) {
                ps.setInt(3, limit);
            }
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                transactions.add(mapTransaction(rs));
            }
            // Nick's AccountInfo/API methods already reach this database query.
            logger.info("TRANSACTION_HISTORY query succeeded");

        } catch (SQLException e) {
            logger.error("TRANSACTION_HISTORY database query failed", e);
        }
        return transactions;
    }


    /**
     * Method to update an user in the database
     * It will take an User object
     */
    public void updateUser(User user) {
        String query = "UPDATE Owners SET name = ?, age = ?, passWord = ? WHERE userID = ?";

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {

            ps.setString(1, user.getName());
            ps.setInt(2, user.getAge());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getUserID().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
    }

    /**
     * Method to update an account in the database
     * It will recieve an AccountInfo object
     */
    public void updateAccount(AccountInfo account) {
        String query = """
            UPDATE Accounts
            SET userID = ?, pin = ?, accountType = ?, balance = ?, frozen = ?
            WHERE accountID = ? 
            """;
        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, account.getUserID().toString());
            ps.setInt(2, account.getPin());
            ps.setString(3, account.getAccountType().name());
            ps.setLong(4, account.getBalance());
            ps.setInt(5, account.isFrozen() ? 1 : 0);
            ps.setString(6, account.getAccountID().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
    }


    /**
     * Saves a deposit/withdrawal balance and its history record on one connection.
     * Returns false on rejection or failure, matching the existing transaction contract.
     * Memory is updated by BankTransactions only after this method commits.
     */
    public boolean saveBalanceAndHistory(AccountInfo account, long newBalance, Transaction transaction) {
        String sql = "UPDATE Accounts SET balance = ? WHERE accountID = ? AND balance = ? AND frozen = 0";
        try (Connection connection = ConnectionFactory.getManualCommitConnection()) {
            try {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, newBalance);
                    ps.setString(2, account.getAccountID().toString());
                    // Reject stale accounts rather than overwrite someone else's newer balance.
                    ps.setLong(3, account.getBalance());
                    if (ps.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                insertTransaction(connection, transaction);
                connection.commit();
                return true;
            } catch (SQLException e) {
                try { connection.rollback(); }
                catch (SQLException rollbackFailure) { e.addSuppressed(rollbackFailure); }
                logger.error("DATABASE balance/history save failed", e);
                return false;
            }
        } catch (SQLException e) {
            logger.error("DATABASE balance/history save failed", e);
            return false;
        }
    }

    /**
     * Method to take money from one account and insert it into another account
     * It involves a mannual commit
     * It will return a boolean between whether the transaction was succesfully made or not
     */
    public boolean transferMoney(AccountInfo source, AccountInfo dest, long amount) {
        String addSQL = "UPDATE Accounts SET balance = balance + ? WHERE accountID = ? AND frozen = 0";
        String withdrawSQL = "UPDATE Accounts SET balance = balance - ? WHERE accountID = ? AND frozen = 0 AND balance >= ?";

        try (Connection connection = ConnectionFactory.getManualCommitConnection()) {
            try {
                try(PreparedStatement ps = connection.prepareStatement(withdrawSQL)) {
                    ps.setLong(1, amount);
                    ps.setString(2, source.getAccountID().toString());
                    ps.setLong(3, amount);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Withdrawing money from account failed");
                    }
                }

                try(PreparedStatement ps2 = connection.prepareStatement(addSQL)) {
                    ps2.setLong(1, amount);
                    ps2.setString(2, dest.getAccountID().toString());

                    if(ps2.executeUpdate() != 1) {
                        throw new SQLException("Adding money to account failed");
                    }
                }

                insertTransaction(connection, new Transaction(
                        source.getAccountID(),
                        dest.getAccountID(),
                        TransactionType.TRANSFER,
                        amount
                ));

                connection.commit();
                source.setBalance(source.getBalance() - amount);
                dest.setBalance(dest.getBalance() + amount);
                return true;
            } catch (SQLException e) {
                connection.rollback();
                logger.error("DATABASE operation failed", e);
                return false;
            }
        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
            return false;
        }
    }


    /**
     * Method to delete an account in the database
     * It will automatically delete the transactions that only involves this account
     */
    public static void deleteAccount(AccountInfo account) {
        String deleteOwnTransactions = """
            DELETE FROM Transactions
            WHERE sourceAccountId = ?
            AND destinationAccountId IS NULL
            """;
        String deleteAccount = "DELETE FROM Accounts WHERE accountID = ?";

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                Statement pragma = connection.createStatement();
                PreparedStatement tx = connection.prepareStatement(deleteOwnTransactions);
                PreparedStatement acct = connection.prepareStatement(deleteAccount);
        ) {
            pragma.execute("PRAGMA foreign_keys = OFF");
            connection.setAutoCommit(false);
            String accountId = account.getAccountID().toString();
            tx.setString(1, accountId);
            tx.executeUpdate();
            acct.setString(1, accountId);
            acct.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
    }

    /**
     * Deletes every account owned by the user
     * Own deposits, withdrawals, and transfers between this user's accounts are removed
     * Transfers with another user's account are kept for that other account.
     */
    public static void deleteAllAccounts(UUID userId) {
        String deleteOwnTransactions = """
            DELETE FROM Transactions
            WHERE sourceAccountId IN (SELECT accountID FROM Accounts WHERE userID = ?)
            AND (
            destinationAccountId IS NULL
            OR destinationAccountId IN (SELECT accountID FROM Accounts WHERE userID = ?)
            )
            """;
        String deleteAccounts = "DELETE FROM Accounts WHERE userID = ?";

        try (
                Connection connection = ConnectionFactory.getAutoCommitConnect();
                Statement pragma = connection.createStatement();
                PreparedStatement tx = connection.prepareStatement(deleteOwnTransactions);
                PreparedStatement accts = connection.prepareStatement(deleteAccounts);
        ) {
            pragma.execute("PRAGMA foreign_keys = OFF");
            connection.setAutoCommit(false);
            tx.setString(1, userId.toString());
            tx.setString(2, userId.toString());
            tx.executeUpdate();
            accts.setString(1, userId.toString());
            accts.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("DATABASE operation failed", e);
        }
    }



}
