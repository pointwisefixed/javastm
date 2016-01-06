package com.walmart.stm.transaction;

import com.walmart.stm.transaction.Transaction;

/**
 * Created by grosal3 on 1/2/16.
 */
public interface TransactionalWithResult<T> {

    T run(Transaction transaction);
}
