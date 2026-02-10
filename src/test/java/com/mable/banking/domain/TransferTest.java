package com.mable.banking.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Transfer value object: validation of from, to, and amount.
 */
class TransferTest {

    private static final String FROM = "1111234522226789";
    private static final String TO = "1212343433335665";

    @Nested
    @DisplayName("Valid construction")
    class ValidConstruction {

        @Test
        @DisplayName("accepts valid from, to, and positive amount")
        void validTransfer() {
            Transfer t = new Transfer(FROM, TO, new BigDecimal("500.00"));
            assertEquals(FROM, t.getFromAccountId());
            assertEquals(TO, t.getToAccountId());
            assertEquals(new BigDecimal("500.00"), t.getAmount());
        }

        @Test
        @DisplayName("trims whitespace from account ids")
        void trimsIds() {
            Transfer t = new Transfer("  " + FROM + "  ", "  " + TO + "  ", new BigDecimal("1.00"));
            assertEquals(FROM, t.getFromAccountId());
            assertEquals(TO, t.getToAccountId());
        }
    }

    @Nested
    @DisplayName("Invalid from account")
    class InvalidFrom {

        @Test
        @DisplayName("rejects null from account")
        void rejectsNullFrom() {
            assertThrows(IllegalArgumentException.class,
                () -> new Transfer(null, TO, new BigDecimal("100")));
        }

        @Test
        @DisplayName("rejects blank from account")
        void rejectsBlankFrom() {
            assertThrows(IllegalArgumentException.class,
                () -> new Transfer("  ", TO, new BigDecimal("100")));
        }

        @Test
        @DisplayName("rejects from account not 16 digits")
        void rejectsInvalidFromLength() {
            assertThrows(IllegalArgumentException.class,
                () -> new Transfer("123", TO, new BigDecimal("100")));
        }
    }

    @Nested
    @DisplayName("Invalid to account")
    class InvalidTo {

        @Test
        @DisplayName("rejects null to account")
        void rejectsNullTo() {
            assertThrows(IllegalArgumentException.class,
                () -> new Transfer(FROM, null, new BigDecimal("100")));
        }

        @Test
        @DisplayName("rejects to account not 16 digits")
        void rejectsInvalidToLength() {
            assertThrows(IllegalArgumentException.class,
                () -> new Transfer(FROM, "abc", new BigDecimal("100")));
        }
    }

    @Nested
    @DisplayName("Invalid amount")
    class InvalidAmount {

        @Test
        @DisplayName("rejects null amount")
        void rejectsNullAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> new Transfer(FROM, TO, null));
        }

        @Test
        @DisplayName("rejects zero amount")
        void rejectsZeroAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> new Transfer(FROM, TO, BigDecimal.ZERO));
        }

        @Test
        @DisplayName("rejects negative amount")
        void rejectsNegativeAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> new Transfer(FROM, TO, new BigDecimal("-1")));
        }
    }

    @Test
    @DisplayName("accepts same from and to account (processor assigns SAME_ACCOUNT status)")
    void acceptsSameFromAndTo() {
        Transfer t = new Transfer(FROM, FROM, new BigDecimal("100"));
        assertEquals(FROM, t.getFromAccountId());
        assertEquals(FROM, t.getToAccountId());
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
