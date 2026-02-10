package com.mable.banking.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AccountValidator {

    private static final int ACCOUNT_ID_LENGTH = 16;
    private static final String ACCOUNT_ID_PATTERN = "\\d{" + ACCOUNT_ID_LENGTH + "}";

    public static Account createAccount(String accountId, BigDecimal balance) {
        return new Account(validateAccountId(accountId), validateBalance(balance));
    }

    private static String validateAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        String trimmed = accountId.trim();
        if (!trimmed.matches(ACCOUNT_ID_PATTERN)) {
            throw new IllegalArgumentException(
                "Account ID must be exactly " + ACCOUNT_ID_LENGTH + " digits: " + accountId);
        }
        return trimmed;
    }

    private static BigDecimal validateBalance(BigDecimal balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        if (balance.signum() < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        return balance.setScale(2, RoundingMode.UNNECESSARY);
    }
}
