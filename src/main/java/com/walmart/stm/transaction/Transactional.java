package com.walmart.stm.transaction;

@FunctionalInterface
public interface Transactional {

    void run(Transaction transaction);

}
