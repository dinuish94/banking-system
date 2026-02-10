package com.mable.banking.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Account domain model: construction, validation, and equality.
 */
class AccountTest {

    private static final String VALID_ID = "1111234522226789";

    @Nested
    @DisplayName("Construction and validation")
    class Construction {

        @Test
        @DisplayName("accepts valid 16-digit id and non-negative balance")
        void validAccount() {
            Account a = new Account(VALID_ID, new BigDecimal("5000.00"));
            assertEquals(VALID_ID, a.getAccountId());
            assertEquals(new BigDecimal("5000.00"), a.getBalance());
        }

        @Test
        @DisplayName("trims whitespace from account id")
        void trimsAccountId() {
            Account a = AccountValidator.createAccount("  " + VALID_ID + "  ", new BigDecimal("0"));
            assertEquals(VALID_ID, a.getAccountId());
        }

        @Test
        @DisplayName("rejects null account id")
        void rejectsNullAccountId() {
            assertThrows(IllegalArgumentException.class,
                () -> AccountValidator.createAccount(null, new BigDecimal("100")));
        }

        @Test
        @DisplayName("rejects blank account id")
        void rejectsBlankAccountId() {
            assertThrows(IllegalArgumentException.class,
                () -> AccountValidator.createAccount("   ", new BigDecimal("100")));
        }

        @Test
        @DisplayName("rejects account id not exactly 16 digits")
        void rejectsInvalidLengthId() {
            assertThrows(IllegalArgumentException.class,
                () -> AccountValidator.createAccount("123456789012345", new BigDecimal("100")));
            assertThrows(IllegalArgumentException.class,
                () -> AccountValidator.createAccount("12345678901234567", new BigDecimal("100")));
            assertThrows(IllegalArgumentException.class,
                () -> AccountValidator.createAccount("12345678901234ab", new BigDecimal("100")));
        }

        @Test
        @DisplayName("rejects null balance")
        void rejectsNullBalance() {
            assertThrows(IllegalArgumentException.class,
                () -> AccountValidator.createAccount(VALID_ID, null));
        }

        @Test
        @DisplayName("rejects negative balance")
        void rejectsNegativeBalance() {
            assertThrows(IllegalArgumentException.class,
                () -> AccountValidator.createAccount(VALID_ID, new BigDecimal("-1")));
        }

        @Test
        @DisplayName("normalises balance to 2 decimal places where no rounding needed")
        void twoDecimalPlaces() {
            Account a = AccountValidator.createAccount(VALID_ID, new BigDecimal("5000.00"));
            assertEquals(2, a.getBalance().scale());
        }
    }

    @Nested
    @DisplayName("Equals and hashCode")
    class Equality {

        @Test
        @DisplayName("equality based on account id only")
        void equalityByAccountId() {
            Account a1 = new Account(VALID_ID, new BigDecimal("100"));
            Account a2 = new Account(VALID_ID, new BigDecimal("200"));
            assertEquals(a1, a2);
            assertEquals(a1.hashCode(), a2.hashCode());
        }

        @Test
        @DisplayName("not equal to different account id")
        void notEqualDifferentId() {
            Account a1 = new Account(VALID_ID, new BigDecimal("100"));
            Account a2 = new Account("1111234522221234", new BigDecimal("100"));
            assertNotEquals(a1, a2);
        }
    }
}
