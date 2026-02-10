package com.mable.banking.io;

import com.mable.banking.domain.TransactionResult;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TransactionReportWriter {

    private static final String HEADER = "From Account,To Account,Amount,Status";

    public void write(java.nio.file.Path path, List<TransactionResult> results) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Report path cannot be null");
        }

        if (results == null) {
            throw new IllegalArgumentException("Results cannot be null");
        }

        List<String> lines = new java.util.ArrayList<>();
        lines.add(HEADER);

        for (TransactionResult r : results) {
            lines.add(String.format("%s,%s,%s,%s", r.getFromAccountId(), r.getToAccountId(), r.getAmountDisplay(), r.getStatus()));
        }

        Files.write(path, lines);
    }
}
