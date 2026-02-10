package com.mable.banking.service;

import com.mable.banking.domain.Account;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
public class AccountService {

    public boolean hasSufficientBalance(Account account, BigDecimal amount) {
        return amount != null && amount.signum() > 0 && account.getBalance().compareTo(amount) >= 0;
    }

    public void debit(Account account, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance for debit");
        }
        account.setBalance(account.getBalance().subtract(amount));
    }

    public void credit(Account account, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        account.setBalance(account.getBalance().add(amount));
    }
}
