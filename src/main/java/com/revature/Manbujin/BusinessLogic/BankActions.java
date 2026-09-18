package com.revature.Manbujin.BusinessLogic;
import com.revature.Manbujin.model.AccountInfo;

public class BankActions {

    public boolean checkDeposit(AccountInfo accountInfo, long amount){
        return accountInfo.getAccountID() != null
                && !accountInfo.isFrozen()
                && amount > 0;

    }
    public boolean checkWithdraw(AccountInfo accountInfo, long amount){
        return accountInfo.getAccountID() != null
                && !accountInfo.isFrozen()
                && amount > 0
                && amount <= accountInfo.getBalance();

    }

   public boolean checkTransfer(AccountInfo sendingAccount, AccountInfo receivingAccount, long amount ){
        return sendingAccount != null
                && receivingAccount != null
                && !sendingAccount.isFrozen()
                && !receivingAccount.isFrozen()
                && !sendingAccount.getAccountID().equals(receivingAccount.getAccountID())
                && amount > 0
                && sendingAccount.getBalance() >= amount;
    }

}
