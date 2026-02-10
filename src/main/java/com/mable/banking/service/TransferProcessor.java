package com.mable.banking.service;

import com.mable.banking.domain.Account;
import com.mable.banking.domain.ProcessResult;
import com.mable.banking.domain.TransactionResult;
import com.mable.banking.domain.TransactionStatus;
import com.mable.banking.domain.Transfer;
import com.mable.banking.exception.ValidationException;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class TransferProcessor {

    private final AccountService accountService;

    public ProcessResult process(Map<String, Account> accounts, List<Transfer> transfers) {
        if (accounts == null || accounts.isEmpty()) {
            throw new ValidationException("Accounts cannot be null or empty");
        }

        if (transfers == null) {
            throw new ValidationException("Transfers cannot be null");
        }

        Map<String, Account> copy = new LinkedHashMap<>();
        for (Account a : accounts.values()) {
            copy.put(a.getAccountId(), new Account(a.getAccountId(), a.getBalance()));
        }

        List<TransactionResult> results = new ArrayList<>();

        for (Transfer t : transfers) {
            TransactionStatus status = resolveStatus(copy, t);
            results.add(TransactionResult.of(t, status));

            if (status == TransactionStatus.APPLIED) {
                Account from = copy.get(t.fromAccountId());
                Account to = copy.get(t.toAccountId());
                accountService.debit(from, t.amount());
                accountService.credit(to, t.amount());
            }
        }
        return new ProcessResult(copy, results);
    }

    private TransactionStatus resolveStatus(Map<String, Account> copy, Transfer t) {
        if (t.fromAccountId().equals(t.toAccountId())) {
            return TransactionStatus.SAME_ACCOUNT;
        }
        if (!copy.containsKey(t.fromAccountId())) {
            return TransactionStatus.UNKNOWN_FROM_ACCOUNT;
        }
        if (!copy.containsKey(t.toAccountId())) {
            return TransactionStatus.UNKNOWN_TO_ACCOUNT;
        }
        Account from = copy.get(t.fromAccountId());
        if (!accountService.hasSufficientBalance(from, t.amount())) {
            return TransactionStatus.INSUFFICIENT_BALANCE;
        }
        return TransactionStatus.APPLIED;
    }
}
