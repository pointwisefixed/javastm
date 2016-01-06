package com.walmart.stm.transaction;

/**
 * Created by grosal3 on 1/2/16.
 */
public class TransactionWithResultBlock<T> {

    private final TransactionalWithResult<T> target;

    public TransactionWithResultBlock(TransactionalWithResult<T> t) {
        this.target = t;
    }

    public T run(Transaction tx) {
        if (target != null) {
            return target.run(tx);
        }
        return null;
    }
}
