package com.mable.banking.domain;

/**
 * Outcome of processing a single transfer. Business rules are expressed as statuses
 * so the application completes and reports all results without throwing.
 */
public enum TransactionStatus {
    APPLIED,
    INSUFFICIENT_BALANCE,
    UNKNOWN_FROM_ACCOUNT,
    UNKNOWN_TO_ACCOUNT,
    SAME_ACCOUNT
}
