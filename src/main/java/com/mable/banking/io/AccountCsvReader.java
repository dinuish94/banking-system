package com.mable.banking.io;

import com.mable.banking.domain.Account;
import com.mable.banking.domain.AccountValidator;
import com.mable.banking.domain.LineError;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AccountCsvReader {

    public BalanceLoadResult load(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Balance file path must be an existing file: " + path);
        }

        Map<String, Account> accounts = new LinkedHashMap<>();
        List<LineError> errors = new ArrayList<>();

        try (Stream<String> lines = Files.lines(path)) {
            List<String> list = lines.filter(line -> !line.isBlank()).toList();
            for (int i = 0; i < list.size(); i++) {
                String line = list.get(i);
                int lineNumber = i + 1;
                parseAccountBalance(line, lineNumber, errors, accounts);
            }
        }
        return new BalanceLoadResult(accounts, errors);
    }

    private static void parseAccountBalance(String line, int lineNumber, List<LineError> errors, Map<String, Account> accounts) {
        ParseLineResult parsed = parseLine(line, lineNumber);
        if (parsed.error != null) {
            errors.add(parsed.error);
            return;
        }

        Account account = parsed.account;
        if (accounts.containsKey(account.getAccountId())) {
            errors.add(new LineError(lineNumber, line, "Duplicate account ID " + account.getAccountId()));
            return;
        }
        accounts.put(account.getAccountId(), account);
    }

    private static ParseLineResult parseLine(String line, int lineNumber) {
        String[] parts = line.split(",", -1);
        if (parts.length != 2) {
            return ParseLineResult.error(lineNumber, line,
                "Line must have exactly 2 columns (accountId,balance)");
        }
        String accountId = parts[0].trim();
        String balanceStr = parts[1].trim();
        BigDecimal balance;
        try {
            balance = new BigDecimal(balanceStr);
        } catch (NumberFormatException e) {
            return ParseLineResult.error(lineNumber, line, "Invalid balance: " + balanceStr);
        }
        try {
            Account account = AccountValidator.createAccount(accountId, balance);
            return ParseLineResult.ok(account);
        } catch (IllegalArgumentException e) {
            return ParseLineResult.error(lineNumber, line, e.getMessage());
        }
    }

    private record ParseLineResult(Account account, LineError error) {
        static ParseLineResult ok(Account account) {
            return new ParseLineResult(account, null);
        }

        static ParseLineResult error(int lineNumber, String line, String message) {
            return new ParseLineResult(null, new LineError(lineNumber, line, message));
        }
    }
}
