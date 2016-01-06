package com.walmart.stm.dto;

import java.math.BigDecimal;

/**
 * Created by grosal3 on 1/1/16.
 */
public class Account {
    private BigDecimal balance;
    private String accountName;

    public Account(String name, BigDecimal balance) {
        this.accountName = name;
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public String toString() {
        return String.format("Account: %s, Balance: %s", accountName, balance);
    }
}
