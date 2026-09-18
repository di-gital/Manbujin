package com.revature.Manbujin.BusinessLogic;

import com.revature.Manbujin.Repository.AccountRepo;

import java.util.UUID;

public class AccountValidation {

    /*
        Public Methods for Checking Regex?
            hasUpper
            hasLower
            hasSpecialCharacter
            hasNumber
     */

    /*
        Checking validity of UUID
        Params : No Duplicate UUID
     */

    public static boolean isUUIDValid(String accountID) {
        UUID uuid = UUID.fromString(accountID);
        AccountRepo accountRepo = new AccountRepo();
        return accountRepo.findAccountById(uuid) == null;
    }

    /*
        Checking validity of Username
        Params : 1 UpperCase, 1 Lowercase,  8 - 16 characters
     */

    public static boolean isUsernameValid (String username) {
        return username.matches("^(?=.*[A-Z])(?=.*[a-z]).{8,16}$");
    }

    public static void usernameInvalid() {
        System.out.println("Username is invalid... Please Enter a valid Username");
        System.out.println("Username must include 1 Uppercase, 1 Lowercase, and must be between 8 and 16 characters");
    }

    /*
        Checking validity of Password
        Params : 1 Uppercase, 1 Lowercase, 1 number, 1 special character, 8 - 16 Characters
        Special Characters include : !,@,#,$,%,^,&,*,?,/,\
     */

    public static boolean isPasswordValid (String password) {
        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*?/\\\\])[A-Za-z\\d!@#$%^&*?/\\\\]{16,24}$");
    }

    public static void passwordInvalid() {
        System.out.println("Password is invalid... Please Enter a valid Password");
        System.out.println("Password must include 1 Uppercase, 1 Lowercase, 1 number, 1 special character, and be 16-24 characters");
    }

}
