package com.mable.banking.service;

import com.mable.banking.domain.Account;
import com.mable.banking.exception.BankingException;
import com.mable.banking.exception.ValidationException;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
public class AccountService {

    public boolean hasSufficientBalance(Account account, BigDecimal amount) {
        return amount != null && amount.signum() > 0 && account.getBalance().compareTo(amount) >= 0;
    }

    public void debit(Account account, BigDecimal amount) {
        if (!hasSufficientBalance(account, amount)) {
            throw new ValidationException("Insufficient balance for debit");
        }
        account.setBalance(account.getBalance().subtract(amount));
    }

    public void credit(Account account, BigDecimal amount) {
        if (isAmountNegative(amount)) {
            throw new ValidationException("Credit amount must be positive");
        }
        account.setBalance(account.getBalance().add(amount));
    }

    private static boolean isAmountNegative(BigDecimal amount) {
        return amount == null || amount.signum() <= 0;
    }
}
