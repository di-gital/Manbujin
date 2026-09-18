package com.revature.Manbujin;
import com.revature.Manbujin.Repository.AccountRepo;
import com.revature.Manbujin.model.*;
import java.util.UUID;

/** Creates saved fixtures because successful transactions now require database rows. */
public final class TestAccounts {
    public static void save(AccountInfo account) {
        if (!"jdbc:sqlite:./data/test-bank.db".equals(System.getenv("DATABASE-PATH")))
            throw new IllegalStateException("Run tests through Maven with the test database.");
        AccountRepo repo = new AccountRepo();
        repo.initSchema();
        User owner = new User(account.getUserID(), UUID.randomUUID().toString(), 21, "fixture");
        if (repo.findUserById(owner.getUserID()) == null) AccountRepo.insertUser(owner);
        AccountRepo.insertAccount(account);
    }
}
