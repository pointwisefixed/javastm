package com.walmart.stm.service;

import com.walmart.stm.dto.Account;
import com.walmart.stm.transaction.STM;
import com.walmart.stm.transaction.Ref;
import com.walmart.stm.transaction.TransactionBlock;

import java.math.BigDecimal;

/**
 * Created by grosal3 on 1/1/16.
 */
public class TransferService {

    public void transfer(Ref<Account> from, Ref<Account> to, BigDecimal amount) {
        STM.transaction(new TransactionBlock((tx) -> {
            final Account oldAccount1 = from.getValue(tx);
            final Account oldAccount2 = to.getValue(tx);
            final Account newAccount1 =
                new Account(oldAccount1.getAccountName(), oldAccount1.getBalance().add(amount.negate()));
            final Account newAccount2 = new Account(oldAccount2.getAccountName(), oldAccount2.getBalance().add(amount));
            from.setValue(newAccount1, tx);
            to.setValue(newAccount2, tx);
            System.out.println(from);
            System.out.println(to);
        }));

    }
}
