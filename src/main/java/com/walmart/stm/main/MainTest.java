package com.walmart.stm.main;

import com.walmart.stm.dto.Account;
import com.walmart.stm.service.TransferService;
import com.walmart.stm.transaction.GlobalContext;
import com.walmart.stm.transaction.Ref;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by grosal3 on 1/1/16.
 */
public class MainTest {



    public static void main(String[] args) {

        int numberOfTransactions = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfTransactions);
        Ref<Account> account1 = new Ref<>(new Account("1", new BigDecimal("100000")));
        Ref<Account> account2 = new Ref<>(new Account("2", new BigDecimal("40000")));
        BigDecimal amountToTransfer = new BigDecimal("50");

        TransferService service = new TransferService();
        IntStream.range(0, numberOfTransactions)
            .forEach(x -> executorService.execute(() -> service.transfer(account1, account2, amountToTransfer)));
        try {
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }

        System.out.printf("Account 1 value is %s \n", GlobalContext.getInstance().get(account1).getBalance());
        System.out.printf("Account 2 value is %s \n", GlobalContext.getInstance().get(account2).getBalance());

    }
}
