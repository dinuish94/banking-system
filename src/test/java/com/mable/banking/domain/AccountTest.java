package com.mable.banking.domain;

import com.mable.banking.service.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import com.mable.banking.exception.ValidationException;

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
            Account a = new Account(Validator.validateAccountId("  " + VALID_ID + "  "), Validator.validateBalance(new BigDecimal("0")));
            assertEquals(VALID_ID, a.getAccountId());
        }

        @Test
        @DisplayName("rejects null account id")
        void rejectsNullAccountId() {
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId(null));
        }

        @Test
        @DisplayName("rejects blank account id")
        void rejectsBlankAccountId() {
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId("   "));
        }

        @Test
        @DisplayName("rejects account id not exactly 16 digits")
        void rejectsInvalidLengthId() {
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId("123456789012345"));
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId("12345678901234567"));
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId("12345678901234ab"));
        }

        @Test
        @DisplayName("rejects null balance")
        void rejectsNullBalance() {
            assertThrows(ValidationException.class,
                () -> Validator.validateBalance(null));
        }

        @Test
        @DisplayName("rejects negative balance")
        void rejectsNegativeBalance() {
            assertThrows(ValidationException.class,
                () -> Validator.validateBalance(new BigDecimal("-1")));
        }

        @Test
        @DisplayName("normalises balance to 2 decimal places where no rounding needed")
        void twoDecimalPlaces() {
            BigDecimal balance = Validator.validateBalance(new BigDecimal("5000.00"));
            assertEquals(2, balance.scale());
        }
    }
}
