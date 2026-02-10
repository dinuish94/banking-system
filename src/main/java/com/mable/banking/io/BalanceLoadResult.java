package com.mable.banking.io;

import com.mable.banking.domain.Account;
import com.mable.banking.domain.LineError;

import java.util.List;
import java.util.Map;

public record BalanceLoadResult(Map<String, Account> accounts, List<LineError> errors) {

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
