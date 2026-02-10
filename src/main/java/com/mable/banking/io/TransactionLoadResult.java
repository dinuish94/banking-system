package com.mable.banking.io;

import com.mable.banking.domain.LineError;
import com.mable.banking.domain.Transfer;

import java.util.List;

public record TransactionLoadResult(List<Transfer> transfers, List<LineError> errors) {
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
