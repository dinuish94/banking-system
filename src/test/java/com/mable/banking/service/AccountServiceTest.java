package com.mable.banking.service;

import com.mable.banking.domain.Account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccountService: hasSufficientBalance, debit and credit business rules.
 */
class AccountServiceTest {

    private static final String VALID_ID = "1111234522226789";
    private final AccountService accountService = new AccountService();

    @Nested
    @DisplayName("hasSufficientBalance")
    class SufficientBalance {

        @Test
        @DisplayName("returns true when balance equals amount")
        void exactBalance() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            assertTrue(accountService.hasSufficientBalance(a, new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("returns true when balance exceeds amount")
        void moreThanAmount() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            assertTrue(accountService.hasSufficientBalance(a, new BigDecimal("99.99")));
        }

        @Test
        @DisplayName("returns false when balance less than amount")
        void insufficientBalance() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            assertFalse(accountService.hasSufficientBalance(a, new BigDecimal("100.01")));
        }

        @Test
        @DisplayName("returns false for null or non-positive amount")
        void invalidAmount() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            assertFalse(accountService.hasSufficientBalance(a, null));
            assertFalse(accountService.hasSufficientBalance(a, BigDecimal.ZERO));
            assertFalse(accountService.hasSufficientBalance(a, new BigDecimal("-1")));
        }
    }

    @Nested
    @DisplayName("Debit and credit")
    class DebitCredit {

        @Test
        @DisplayName("debit reduces balance by amount")
        void debitReducesBalance() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            accountService.debit(a, new BigDecimal("30.00"));
            assertEquals(new BigDecimal("70.00"), a.getBalance());
        }

        @Test
        @DisplayName("credit increases balance by amount")
        void creditIncreasesBalance() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            accountService.credit(a, new BigDecimal("25.50"));
            assertEquals(new BigDecimal("125.50"), a.getBalance());
        }

        @Test
        @DisplayName("debit throws when insufficient balance")
        void debitThrowsWhenInsufficient() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            assertThrows(IllegalStateException.class,
                () -> accountService.debit(a, new BigDecimal("100.01")));
            assertEquals(new BigDecimal("100.00"), a.getBalance());
        }

        @Test
        @DisplayName("debit throws for null or non-positive amount")
        void debitValidatesAmount() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            assertThrows(IllegalArgumentException.class, () -> accountService.debit(a, null));
            assertThrows(IllegalArgumentException.class, () -> accountService.debit(a, BigDecimal.ZERO));
            assertThrows(IllegalArgumentException.class, () -> accountService.debit(a, new BigDecimal("-1")));
        }

        @Test
        @DisplayName("credit throws for null or non-positive amount")
        void creditValidatesAmount() {
            Account a = new Account(VALID_ID, new BigDecimal("100.00"));
            assertThrows(IllegalArgumentException.class, () -> accountService.credit(a, null));
            assertThrows(IllegalArgumentException.class, () -> accountService.credit(a, BigDecimal.ZERO));
            assertThrows(IllegalArgumentException.class, () -> accountService.credit(a, new BigDecimal("-1")));
        }
    }
}
