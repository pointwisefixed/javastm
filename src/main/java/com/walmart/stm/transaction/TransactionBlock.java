package com.walmart.stm.transaction;

public class TransactionBlock {

    private Transactional target;

    public TransactionBlock(Transactional t) {
        this.target = t;
    }



    public void run(Transaction transaction) {
        if (target != null) {
            target.run(transaction);
        }
    }
}
