package com.revature.Manbujin.BusinessLogic;

import com.revature.Manbujin.Repository.AccountRepo;
import com.revature.Manbujin.model.AccountInfo;
import com.revature.Manbujin.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class AccountValidationTest {
    @org.junit.jupiter.api.BeforeEach void schema() { new AccountRepo().initSchema(); }
    public AccountValidation accountValidation;

    /*
    Nate, create an account, insert it with insertAccount.java, then look that same id back up with
    findAccountById(account.getAccountID()) (this method is in AccountRepo.java) and assert if they are equal
    that proves it saved. For the duplicate check, take that existing UUID and pass it into isUUIDValid, it should
    return false if findAccountById already finds something
     */

    @Test
    public void UUIDPositive(){
        User user = new User(UUID.randomUUID(), "TestUser", 30, "password");
        AccountRepo.insertUser(user);

        AccountInfo accountInfo = new AccountInfo(user.getUserID(), 1234);
        AccountRepo.insertAccount(accountInfo);

        AccountRepo accountRepo = new AccountRepo();
        AccountInfo createdAccount = accountRepo.findAccountById(accountInfo.getAccountID());

        Assertions.assertNotNull(createdAccount, "Created Account found in Repo");
        Assertions.assertEquals(accountInfo.getAccountID(), createdAccount.getAccountID());
    }

    @Test
    public void UUIDNegative() {
        User user = new User(UUID.randomUUID(), "TestUser2", 30, "password");
        AccountRepo.insertUser(user);

        AccountInfo accountInfo = new AccountInfo(user.getUserID(), 4321);
        AccountRepo.insertAccount(accountInfo);

        AccountRepo accountRepo = new AccountRepo();
        AccountInfo existingAccount = accountRepo.findAccountById(user.getUserID());

        Assertions.assertNull(existingAccount, "UserID should return null");

    }

    @Test
    public void usernamePositive() {
        String validUsername ="UsernameValid";
        boolean usernamePositive = AccountValidation.isUsernameValid(validUsername);

        Assertions.assertTrue(usernamePositive, "Expected Username to be valid");
    }

    @Test
    public void usernameNegative() {
        String invalidUsername = "usernameinvalid";
        boolean usernameNegative = AccountValidation.isUsernameValid(invalidUsername);

        Assertions.assertFalse(usernameNegative, "Expected Username to be invalid");
    }

    /*
    Boundary Value Analysis     -> test the "edges" of your requirements
            - example: if testing password length is 5-15 characters you can start with 4 different passwords:
                - positive data
                    - 5 character password
                    - 15 character password
                - negative data
                    - 4 character password
                    - 16 character password
        Equivalence Partitioning    -> let 1 value represent all possible values of a "class"
            - example: testing if a password is correctly checked for lower, upper, and numeric characters
                - positive data
                    - P0sitive
                - negative data
                    - p0sitive
                    - Positive
                    - P0SITIVE
     */

    @Test
    public void passwordPositive() {
        String validPassword = "ValidPassword$2026";
        boolean passwordPositive = AccountValidation.isPasswordValid(validPassword);

        Assertions.assertTrue(passwordPositive, "Expected Password to be valid");
    }

    @Test
    public void passwordNegative() {
        String invalidPassword = "validpassword123";
        boolean passwordNegative = AccountValidation.isPasswordValid(invalidPassword);

        Assertions.assertFalse(passwordNegative, "Expected Password to be invalid");
    }
}
