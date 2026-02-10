package com.mable.banking.domain;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
public class Transfer {

    String fromAccountId;
    String toAccountId;
    BigDecimal amount;

    public Transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        this.fromAccountId = validateAccountId(fromAccountId, "from");
        this.toAccountId = validateAccountId(toAccountId, "to");
        this.amount = validateAmount(amount);
    }

    private static String validateAccountId(String accountId, String field) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Transfer " + field + " account ID cannot be null or blank");
        }
        String trimmed = accountId.trim();
        if (!trimmed.matches("\\d{16}")) {
            throw new IllegalArgumentException(
                "Transfer " + field + " account ID must be exactly 16 digits: " + accountId);
        }
        return trimmed;
    }

    private static BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Transfer amount cannot be null");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
