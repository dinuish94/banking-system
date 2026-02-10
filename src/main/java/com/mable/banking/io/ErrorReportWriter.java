package com.mable.banking.io;

import com.mable.banking.domain.LineError;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public class ErrorReportWriter {

    private static final String HEADER = "Line Number,Line,Error";

    public void write(java.nio.file.Path path, List<LineError> errors) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Report path cannot be null");
        }

        if (isEmpty(errors)) {
            return;
        }

        List<String> lines = new java.util.ArrayList<>();
        lines.add(HEADER);

        for (LineError e : errors) {
            String escapedLine = escapeCsvField(e.line());
            String escapedMsg = escapeCsvField(e.errorMessage());
            lines.add(e.lineNumber() + "," + escapedLine + "," + escapedMsg);
        }

        Files.write(path, lines);
    }

    private static String escapeCsvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
