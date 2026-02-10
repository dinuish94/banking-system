package com.mable.banking.domain;

public record TransactionResult(String fromAccountId, String toAccountId, String amountDisplay, TransactionStatus status) {

    public static TransactionResult of(Transfer transfer, TransactionStatus status) {
        return new TransactionResult(
                transfer.fromAccountId(),
                transfer.toAccountId(),
                transfer.amount().toPlainString(),
                status);
    }
}
