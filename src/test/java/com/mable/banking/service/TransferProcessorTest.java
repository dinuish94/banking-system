package com.mable.banking.service;

import com.mable.banking.domain.Account;
import com.mable.banking.domain.ProcessResult;
import com.mable.banking.domain.TransactionResult;
import com.mable.banking.domain.TransactionStatus;
import com.mable.banking.domain.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransferProcessor: applying transfers and status for business rules (no exceptions).
 */
class TransferProcessorTest {

    private Map<String, Account> accounts;
    private TransferProcessor processor;

    private static final String ACC_A = "1111234522226789";
    private static final String ACC_B = "1212343433335665";
    private static final String ACC_C = "3212343433335755";

    @BeforeEach
    void setUp() {
        accounts = Map.of(
            ACC_A, new Account(ACC_A, new BigDecimal("5000.00")),
            ACC_B, new Account(ACC_B, new BigDecimal("1200.00")),
            ACC_C, new Account(ACC_C, new BigDecimal("50000.00"))
        );
        processor = new TransferProcessor(new AccountService());
    }

    private static List<TransactionResult> nonApplied(ProcessResult result) {
        return result.getTransactionResults().stream()
            .filter(r -> r.getStatus() != TransactionStatus.APPLIED)
            .toList();
    }

    @Nested
    @DisplayName("Successful processing")
    class SuccessfulProcessing {

        @Test
        @DisplayName("applies single transfer and updates both accounts")
        void singleTransfer() {
            var result = processor.process(accounts, List.of(
                new Transfer(ACC_A, ACC_B, new BigDecimal("500.00"))));
            assertEquals(0, nonApplied(result).size());
            assertEquals(TransactionStatus.APPLIED, result.getTransactionResults().get(0).getStatus());
            assertEquals(new BigDecimal("4500.00"), result.getAccounts().get(ACC_A).getBalance());
            assertEquals(new BigDecimal("1700.00"), result.getAccounts().get(ACC_B).getBalance());
        }

        @Test
        @DisplayName("applies multiple transfers in order")
        void multipleTransfers() {
            var result = processor.process(accounts, List.of(
                new Transfer(ACC_A, ACC_B, new BigDecimal("500.00")),
                new Transfer(ACC_C, ACC_A, new BigDecimal("320.50"))));
            assertEquals(0, nonApplied(result).size());
            assertEquals(new BigDecimal("4820.50"), result.getAccounts().get(ACC_A).getBalance());
            assertEquals(new BigDecimal("1700.00"), result.getAccounts().get(ACC_B).getBalance());
            assertEquals(new BigDecimal("49679.50"), result.getAccounts().get(ACC_C).getBalance());
        }

        @Test
        @DisplayName("empty transfer list leaves all balances unchanged")
        void emptyTransfers() {
            var result = processor.process(accounts, List.of());
            assertEquals(0, result.getTransactionResults().size());
            assertEquals(new BigDecimal("5000.00"), result.getAccounts().get(ACC_A).getBalance());
        }

        @Test
        @DisplayName("original account map is not mutated")
        void doesNotMutateInput() {
            processor.process(accounts, List.of(
                new Transfer(ACC_A, ACC_B, new BigDecimal("500.00"))));
            assertEquals(new BigDecimal("5000.00"), accounts.get(ACC_A).getBalance());
        }
    }

    @Nested
    @DisplayName("Insufficient balance status")
    class InsufficientBalance {

        @Test
        @DisplayName("status INSUFFICIENT_BALANCE when source has insufficient balance")
        void insufficientBalanceStatus() {
            var result = processor.process(accounts, List.of(
                new Transfer(ACC_A, ACC_B, new BigDecimal("10000.00"))));
            assertEquals(1, nonApplied(result).size());
            assertEquals(TransactionStatus.INSUFFICIENT_BALANCE, result.getTransactionResults().get(0).getStatus());
        }

        @Test
        @DisplayName("status INSUFFICIENT_BALANCE when amount exactly exceeds balance")
        void amountExceedsBalance() {
            var result = processor.process(accounts, List.of(
                new Transfer(ACC_B, ACC_A, new BigDecimal("1200.01"))));
            assertEquals(1, nonApplied(result).size());
            assertEquals(TransactionStatus.INSUFFICIENT_BALANCE, result.getTransactionResults().get(0).getStatus());
        }
    }

    @Nested
    @DisplayName("Unknown account status")
    class UnknownAccount {

        @Test
        @DisplayName("status UNKNOWN_FROM_ACCOUNT when from account not in balances")
        void unknownFromAccount() {
            var result = processor.process(accounts, List.of(
                new Transfer("9999999999999999", ACC_B, new BigDecimal("100"))));
            assertEquals(1, nonApplied(result).size());
            assertEquals(TransactionStatus.UNKNOWN_FROM_ACCOUNT, result.getTransactionResults().get(0).getStatus());
        }

        @Test
        @DisplayName("status UNKNOWN_TO_ACCOUNT when to account not in balances")
        void unknownToAccount() {
            var result = processor.process(accounts, List.of(
                new Transfer(ACC_A, "9999999999999999", new BigDecimal("100"))));
            assertEquals(1, nonApplied(result).size());
            assertEquals(TransactionStatus.UNKNOWN_TO_ACCOUNT, result.getTransactionResults().get(0).getStatus());
        }

        @Test
        @DisplayName("processes remaining transfers after unknown account")
        void continuesAfterUnknownAccount() {
            var result = processor.process(accounts, List.of(
                new Transfer("9999999999999999", ACC_B, new BigDecimal("100")),
                new Transfer(ACC_A, ACC_B, new BigDecimal("200.00"))));
            assertEquals(2, result.getTransactionResults().size());
            assertEquals(TransactionStatus.UNKNOWN_FROM_ACCOUNT, result.getTransactionResults().get(0).getStatus());
            assertEquals(TransactionStatus.APPLIED, result.getTransactionResults().get(1).getStatus());
        }
    }

    @Nested
    @DisplayName("Same account status")
    class SameAccount {

        @Test
        @DisplayName("status SAME_ACCOUNT when from and to are equal")
        void sameAccountStatus() {
            var result = processor.process(accounts, List.of(
                new Transfer(ACC_A, ACC_A, new BigDecimal("100"))));
            assertEquals(1, nonApplied(result).size());
            assertEquals(TransactionStatus.SAME_ACCOUNT, result.getTransactionResults().get(0).getStatus());
        }
    }

    @Nested
    @DisplayName("Input validation")
    class InputValidation {

        @Test
        @DisplayName("throws when accounts null or empty")
        void invalidAccounts() {
            assertThrows(IllegalArgumentException.class,
                () -> processor.process(null, List.of()));
            assertThrows(IllegalArgumentException.class,
                () -> processor.process(Map.of(), List.of()));
        }

        @Test
        @DisplayName("throws when transfers null")
        void nullTransfers() {
            assertThrows(IllegalArgumentException.class,
                () -> processor.process(accounts, null));
        }
    }
}
