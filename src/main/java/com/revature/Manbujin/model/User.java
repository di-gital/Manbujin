package com.revature.Manbujin.model;

import com.revature.Manbujin.Repository.AccountRepo;
import com.revature.Manbujin.Utility.PasswordEncryption;
import java.util.UUID;
import java.util.List;
import java.util.Collections;

/**
 * Bank customer who can own one or more accounts.
 * Maps to the Owners table!
 */
public class User {
    private UUID userID;
    private String name;
    private int age;
    private String password;
    public boolean exists;

    /**
     * Creates a new user and generates a unique ID.
     */
    public User(String username, int age, String password) {
        this.userID = UUID.randomUUID();
        this.name = username;
        this.age = age;
        //this.password = password;
        this.password = PasswordEncryption.encrypt(password); //Mo
        this.exists = false;
    }

    /**
     * Rebuilds a user from an existing database row.
     */
    public User(UUID userID, String username, int age, String password) {
        this.userID = userID;
        this.name = username;
        this.age = age;
        this.password = password;
        this.exists = true;
    }

    /**
     * Looks up an existing user by username and password.
     * Sets exists to true and copies the row if the credentials match.
     */
    public User(String username, String password) {
        User found = new AccountRepo().findUserByNameAndPassword(username, password);
        if (found != null) {
            this.userID = found.userID;
            this.name = found.name;
            this.age = found.age;
            this.password = found.password;
            this.exists = true;
        } else {
            this.name = username;
            //this.password = password;
            this.password = PasswordEncryption.encrypt(password); //Mo
            this.exists = false;
        }
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassWord(String passWord) {
        this.password = passWord;

    }

    /**
     * Method to find the accounts associated with the user
     * Return null if the user has no accounts
     * Returns a list of the accounts that the user have
     */
    public List<AccountInfo> getAccounts() {
        if (this.userID == null) {
            return Collections.emptyList();
        } else {
            return new AccountRepo().findAccountsByUserId(this.userID);
        }
    }

}
