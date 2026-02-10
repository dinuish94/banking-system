package com.mable.banking.domain;

public enum TransactionStatus {
    APPLIED,
    INSUFFICIENT_BALANCE,
    UNKNOWN_FROM_ACCOUNT,
    UNKNOWN_TO_ACCOUNT,
    SAME_ACCOUNT
}
