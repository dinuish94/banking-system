package com.mable.banking.io;

import com.mable.banking.domain.Account;
import com.mable.banking.domain.BalanceLoadResult;
import com.mable.banking.exception.ValidationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccountCsvReader: parsing CSV, validation, and error collection into report.
 */
class AccountCsvReaderTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Successful load")
    class SuccessfulLoad {

        @Test
        @DisplayName("loads multiple accounts from valid CSV")
        void loadsValidCsv() throws IOException {
            Path file = tempDir.resolve("balances.csv");
            Files.writeString(file, """
                1111234522226789,5000.00
                1111234522221234,10000.00
                2222123433331212,550.00
                """);
            BalanceLoadResult result = new AccountCsvReader().load(file);
            assertFalse(result.hasErrors());
            Map<String, Account> accounts = result.accounts();
            assertEquals(3, accounts.size());
            assertEquals(new BigDecimal("5000.00"), accounts.get("1111234522226789").getBalance());
            assertEquals(new BigDecimal("10000.00"), accounts.get("1111234522221234").getBalance());
            assertEquals(new BigDecimal("550.00"), accounts.get("2222123433331212").getBalance());
        }

        @Test
        @DisplayName("skips blank lines")
        void skipsBlankLines() throws IOException {
            Path file = tempDir.resolve("balances.csv");
            Files.writeString(file, "1111234522226789,100.00\n\n2222123433331212,200.00\n");
            BalanceLoadResult result = new AccountCsvReader().load(file);
            assertFalse(result.hasErrors());
            assertEquals(2, result.accounts().size());
        }

        @Test
        @DisplayName("accepts zero balance")
        void acceptsZeroBalance() throws IOException {
            Path file = tempDir.resolve("balances.csv");
            Files.writeString(file, "1111234522226789,0.00\n");
            BalanceLoadResult result = new AccountCsvReader().load(file);
            assertFalse(result.hasErrors());
            assertEquals(BigDecimal.ZERO.setScale(2), result.accounts().get("1111234522226789").getBalance());
        }
    }

    @Nested
    @DisplayName("Parse and validation errors collected")
    class ErrorsCollected {

        @Test
        @DisplayName("wrong number of columns added to errors")
        void wrongColumnCount() throws IOException {
            Path file = tempDir.resolve("bad.csv");
            Files.writeString(file, "1111234522226789,5000.00,extra\n");
            BalanceLoadResult result = new AccountCsvReader().load(file);
            assertTrue(result.hasErrors());
            assertEquals(1, result.errors().size());
            assertTrue(result.errors().get(0).errorMessage().contains("2 columns"));
            assertTrue(result.accounts().isEmpty());
        }

        @Test
        @DisplayName("invalid balance added to errors")
        void invalidBalanceFormat() throws IOException {
            Path file = tempDir.resolve("bad.csv");
            Files.writeString(file, "1111234522226789,notanumber\n");
            BalanceLoadResult result = new AccountCsvReader().load(file);
            assertTrue(result.hasErrors());
            assertEquals(1, result.errors().size());
            assertTrue(result.errors().get(0).errorMessage().toLowerCase().contains("balance"));
        }

        @Test
        @DisplayName("invalid account id added to errors")
        void invalidAccountIdInFile() throws IOException {
            Path file = tempDir.resolve("bad.csv");
            Files.writeString(file, "12345,100.00\n");
            BalanceLoadResult result = new AccountCsvReader().load(file);
            assertTrue(result.hasErrors());
            assertEquals(1, result.errors().size());
        }

        @Test
        @DisplayName("duplicate account id added to errors")
        void duplicateAccountId() throws IOException {
            Path file = tempDir.resolve("bad.csv");
            Files.writeString(file, """
                1111234522226789,100.00
                1111234522226789,200.00
                """);
            BalanceLoadResult result = new AccountCsvReader().load(file);
            assertTrue(result.hasErrors());
            assertEquals(1, result.errors().size());
            assertTrue(result.errors().get(0).errorMessage().contains("Duplicate account ID"));
            assertEquals(1, result.accounts().size());
        }

        @Test
        @DisplayName("throws when path is null or not a file")
        void invalidPath() {
            assertThrows(ValidationException.class, () -> new AccountCsvReader().load((Path) null));
            assertThrows(ValidationException.class,
                () -> new AccountCsvReader().load(tempDir.resolve("nonexistent.csv")));
        }
    }

    @Test
    @DisplayName("empty file produces empty map and no errors")
    void emptyFileProducesEmptyMap() throws IOException {
        Path file = tempDir.resolve("empty.csv");
        Files.writeString(file, "");
        BalanceLoadResult result = new AccountCsvReader().load(file);
        assertTrue(result.accounts().isEmpty());
        assertFalse(result.hasErrors());
    }
}
