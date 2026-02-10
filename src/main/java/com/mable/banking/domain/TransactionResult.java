package com.mable.banking.domain;

import lombok.Value;

import java.util.Objects;

/**
 * Result of processing one transfer: from, to, amount (for report) and status.
 */
@Value
public class TransactionResult {

    String fromAccountId;
    String toAccountId;
    String amountDisplay;
    TransactionStatus status;

    public TransactionResult(String fromAccountId, String toAccountId, String amountDisplay, TransactionStatus status) {
        this.fromAccountId = Objects.requireNonNull(fromAccountId, "fromAccountId");
        this.toAccountId = Objects.requireNonNull(toAccountId, "toAccountId");
        this.amountDisplay = Objects.requireNonNull(amountDisplay, "amountDisplay");
        this.status = Objects.requireNonNull(status, "status");
    }

    public static TransactionResult of(Transfer transfer, TransactionStatus status) {
        return new TransactionResult(
            transfer.getFromAccountId(),
            transfer.getToAccountId(),
            transfer.getAmount().toPlainString(),
            status);
    }
}
