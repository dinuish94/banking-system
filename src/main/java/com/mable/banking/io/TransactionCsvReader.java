package com.mable.banking.io;

import com.mable.banking.domain.LineError;
import com.mable.banking.domain.Transfer;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TransactionCsvReader {

    public TransactionLoadResult load(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Transfer file path must be an existing file: " + path);
        }

        List<String> lines = Files.readAllLines(path);
        List<Transfer> transfers = new ArrayList<>();
        List<LineError> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            int lineNumber = i + 1;
            ParseLineResult parsed = parseLine(line, lineNumber);
            if (parsed.error != null) {
                errors.add(parsed.error);
                continue;
            }
            transfers.add(parsed.transfer);
        }

        return new TransactionLoadResult(transfers, errors);
    }

    private static ParseLineResult parseLine(String line, int lineNumber) {
        String[] parts = line.split(",", -1);
        if (parts.length != 3) {
            return ParseLineResult.error(lineNumber, line, "Line must have exactly 3 columns (from,to,amount)");
        }

        String from = parts[0].trim();
        String to = parts[1].trim();
        String amountStr = parts[2].trim();
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            return ParseLineResult.error(lineNumber, line, "Invalid amount: " + amountStr);
        }

        try {
            Transfer transfer = new Transfer(from, to, amount);
            return ParseLineResult.ok(transfer);
        } catch (IllegalArgumentException e) {
            return ParseLineResult.error(lineNumber, line, e.getMessage());
        }
    }

    private record ParseLineResult(Transfer transfer, LineError error) {

        static ParseLineResult ok(Transfer transfer) {
            return new ParseLineResult(transfer, null);
        }

        static ParseLineResult error(int lineNumber, String line, String message) {
            return new ParseLineResult(null, new LineError(lineNumber, line, message));
        }
    }
}
