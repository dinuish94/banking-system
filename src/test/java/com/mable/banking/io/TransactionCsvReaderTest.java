package com.mable.banking.io;

import com.mable.banking.domain.Transfer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransactionCsvReader: parsing CSV and error collection into report.
 */
class TransactionCsvReaderTest {

    private static final String FROM = "1111234522226789";
    private static final String TO = "1212343433335665";

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Successful load")
    class SuccessfulLoad {

        @Test
        @DisplayName("loads multiple transfers from valid CSV")
        void loadsValidCsv() throws IOException {
            Path file = tempDir.resolve("transfers.csv");
            Files.writeString(file, FROM + "," + TO + ",500.00\n3212343433335755,2222123433331212,1000.00\n");
            TransactionLoadResult result = new TransactionCsvReader().load(file);
            assertFalse(result.hasErrors());
            List<Transfer> transfers = result.transfers();
            assertEquals(2, transfers.size());
            assertEquals(FROM, transfers.get(0).getFromAccountId());
            assertEquals(TO, transfers.get(0).getToAccountId());
            assertEquals(new BigDecimal("500.00"), transfers.get(0).getAmount());
        }

        @Test
        @DisplayName("skips blank lines")
        void skipsBlankLines() throws IOException {
            Path file = tempDir.resolve("transfers.csv");
            Files.writeString(file, FROM + "," + TO + ",100.00\n\n3212343433335755,2222123433331212,200.00\n");
            TransactionLoadResult result = new TransactionCsvReader().load(file);
            assertFalse(result.hasErrors());
            assertEquals(2, result.transfers().size());
        }
    }

    @Nested
    @DisplayName("Parse errors collected")
    class ErrorsCollected {

        @Test
        @DisplayName("wrong number of columns added to errors")
        void wrongColumnCount() throws IOException {
            Path file = tempDir.resolve("bad.csv");
            Files.writeString(file, FROM + "," + TO + ",500.00,extra\n");
            TransactionLoadResult result = new TransactionCsvReader().load(file);
            assertTrue(result.hasErrors());
            assertEquals(1, result.errors().size());
            assertTrue(result.errors().get(0).errorMessage().contains("3 columns"));
        }

        @Test
        @DisplayName("invalid amount added to errors")
        void invalidAmount() throws IOException {
            Path file = tempDir.resolve("bad.csv");
            Files.writeString(file, FROM + "," + TO + ",invalid\n");
            TransactionLoadResult result = new TransactionCsvReader().load(file);
            assertTrue(result.hasErrors());
            assertEquals(1, result.errors().size());
        }

        @Test
        @DisplayName("invalid from account id added to errors")
        void invalidFromAccountId() throws IOException {
            Path file = tempDir.resolve("bad.csv");
            Files.writeString(file, "123," + TO + ",100.00\n");
            TransactionLoadResult result = new TransactionCsvReader().load(file);
            assertTrue(result.hasErrors());
            assertEquals(1, result.errors().size());
        }
    }

    @Test
    @DisplayName("throws when path is null or not a file")
    void invalidPath() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionCsvReader().load((Path) null));
        assertThrows(IllegalArgumentException.class,
            () -> new TransactionCsvReader().load(tempDir.resolve("nonexistent.csv")));
    }

    @Test
    @DisplayName("empty file produces empty list and no errors")
    void emptyFile() throws IOException {
        Path file = tempDir.resolve("empty.csv");
        Files.writeString(file, "");
        TransactionLoadResult result = new TransactionCsvReader().load(file);
        assertTrue(result.transfers().isEmpty());
        assertFalse(result.hasErrors());
    }
}
