package com.mable.banking.domain;

import java.util.List;
import java.util.Map;

public record BalanceLoadResult(Map<String, Account> accounts, List<LineError> errors) {

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
