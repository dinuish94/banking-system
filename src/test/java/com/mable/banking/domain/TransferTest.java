package com.mable.banking.domain;

import com.mable.banking.service.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.mable.banking.exception.ValidationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferTest {

    private static final String FROM = "1111234522226789";
    private static final String TO = "1212343433335665";

    @Nested
    @DisplayName("Valid construction")
    class ValidConstruction {

        @Test
        @DisplayName("accepts valid from, to, and positive amount")
        void validTransfer() {
            Transfer t = new Transfer(
                Validator.validateAccountId(FROM, "from"),
                Validator.validateAccountId(TO, "to"),
                Validator.validateTransferAmount(new BigDecimal("500.00")));
            assertEquals(FROM, t.fromAccountId());
            assertEquals(TO, t.toAccountId());
            assertEquals(new BigDecimal("500.00"), t.amount());
        }

        @Test
        @DisplayName("trims whitespace from account ids")
        void trimsIds() {
            Transfer t = new Transfer(
                Validator.validateAccountId("  " + FROM + "  ", "from"),
                Validator.validateAccountId("  " + TO + "  ", "to"),
                Validator.validateTransferAmount(new BigDecimal("1.00")));
            assertEquals(FROM, t.fromAccountId());
            assertEquals(TO, t.toAccountId());
        }
    }

    @Nested
    @DisplayName("Invalid from account")
    class InvalidFrom {

        @Test
        @DisplayName("rejects null from account")
        void rejectsNullFrom() {
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId(null, "from"));
        }

        @Test
        @DisplayName("rejects blank from account")
        void rejectsBlankFrom() {
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId("  ", "from"));
        }

        @Test
        @DisplayName("rejects from account not 16 digits")
        void rejectsInvalidFromLength() {
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId("123", "from"));
        }
    }

    @Nested
    @DisplayName("Invalid to account")
    class InvalidTo {

        @Test
        @DisplayName("rejects null to account")
        void rejectsNullTo() {
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId(null, "to"));
        }

        @Test
        @DisplayName("rejects to account not 16 digits")
        void rejectsInvalidToLength() {
            assertThrows(ValidationException.class,
                () -> Validator.validateAccountId("abc", "to"));
        }
    }

    @Nested
    @DisplayName("Invalid amount")
    class InvalidAmount {

        @Test
        @DisplayName("rejects null amount")
        void rejectsNullAmount() {
            assertThrows(ValidationException.class,
                () -> Validator.validateTransferAmount(null));
        }

        @Test
        @DisplayName("rejects zero amount")
        void rejectsZeroAmount() {
            assertThrows(ValidationException.class,
                () -> Validator.validateTransferAmount(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("rejects negative amount")
        void rejectsNegativeAmount() {
            assertThrows(ValidationException.class,
                () -> Validator.validateTransferAmount(new BigDecimal("-1")));
        }
    }

    @Test
    @DisplayName("accepts same from and to account (processor assigns SAME_ACCOUNT status)")
    void acceptsSameFromAndTo() {
        Transfer t = new Transfer(
            Validator.validateAccountId(FROM, "from"),
            Validator.validateAccountId(FROM, "to"),
            Validator.validateTransferAmount(new BigDecimal("100")));
        assertEquals(FROM, t.fromAccountId());
        assertEquals(FROM, t.toAccountId());
    }

    @Nested
    @DisplayName("Equals and hashCode")
    class Equality {

        @Test
        @DisplayName("equality based on from, to, and amount")
        void equality() {
            Transfer t1 = new Transfer(FROM, TO, new BigDecimal("100"));
            Transfer t2 = new Transfer(FROM, TO, new BigDecimal("100"));
            assertEquals(t1, t2);
            assertEquals(t1.hashCode(), t2.hashCode());
        }

        @Test
        @DisplayName("not equal when amount differs")
        void notEqualAmount() {
            Transfer t1 = new Transfer(FROM, TO, new BigDecimal("100"));
            Transfer t2 = new Transfer(FROM, TO, new BigDecimal("200"));
            assertNotEquals(t1, t2);
        }
    }
}
