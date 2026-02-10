package com.mable.banking.service;

import com.mable.banking.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Validator {

    private static final int ACCOUNT_ID_LENGTH = 16;
    private static final String ACCOUNT_ID_PATTERN = "\\d{" + ACCOUNT_ID_LENGTH + "}";

    private Validator() {
    }

    public static String validateAccountId(String accountId) {
        return validateAccountId(accountId, null);
    }

    public static String validateAccountId(String accountId, String fieldName) {
        String prefix = fieldName != null ? "Transfer " + fieldName + " account ID" : "Account ID";

        if (accountId == null || accountId.isBlank()) {
            throw new ValidationException(prefix + " cannot be null or blank");
        }

        String trimmed = accountId.trim();
        if (!trimmed.matches(ACCOUNT_ID_PATTERN)) {
            throw new ValidationException(prefix + " must be exactly " + ACCOUNT_ID_LENGTH + " digits: " + accountId);
        }
        return trimmed;
    }

    public static BigDecimal validateBalance(BigDecimal balance) {
        if (balance == null) {
            throw new ValidationException("Balance cannot be null");
        }
        if (balance.signum() < 0) {
            throw new ValidationException("Balance cannot be negative");
        }
        return balance.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal validateTransferAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException("Transfer amount cannot be null");
        }
        if (amount.signum() <= 0) {
            throw new ValidationException("Transfer amount must be positive");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
