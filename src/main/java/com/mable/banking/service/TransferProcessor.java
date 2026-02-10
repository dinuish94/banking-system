package com.mable.banking.service;

import com.mable.banking.domain.Account;
import com.mable.banking.domain.ProcessResult;
import com.mable.banking.domain.TransactionResult;
import com.mable.banking.domain.TransactionStatus;
import com.mable.banking.domain.Transfer;
import com.mable.banking.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class TransferProcessor {

    private final AccountService accountService;

    public ProcessResult process(Map<String, Account> accounts, List<Transfer> transfers) {
        validateData(accounts, transfers);
        log.info("Processing {} transfers across {} accounts", transfers.size(), accounts.size());

        Map<String, Account> copyOfAccounts = new LinkedHashMap<>();
        for (Account a : accounts.values()) {
            copyOfAccounts.put(a.getAccountId(), new Account(a.getAccountId(), a.getBalance()));
        }

        List<TransactionResult> results = new ArrayList<>();

        for (Transfer transfer : transfers) {
            performTransaction(transfer, copyOfAccounts, results);
        }
        long applied = results.stream().filter(r -> r.status() == TransactionStatus.APPLIED).count();
        log.info("Processed {} transfers: {} applied", results.size(), applied);
        return new ProcessResult(copyOfAccounts, results);
    }

    private void performTransaction(Transfer transfer, Map<String, Account> copyOfAccounts, List<TransactionResult> results) {
        TransactionStatus status = resolveStatus(copyOfAccounts, transfer);
        results.add(TransactionResult.of(transfer, status));

        if (status == TransactionStatus.APPLIED) {
            Account from = copyOfAccounts.get(transfer.fromAccountId());
            Account to = copyOfAccounts.get(transfer.toAccountId());
            accountService.debit(from, transfer.amount());
            accountService.credit(to, transfer.amount());
            log.info("Transfer processed: {} -> {} amount: {}", transfer.fromAccountId(), transfer.toAccountId(), transfer.amount());
        }
    }

    private static void validateData(Map<String, Account> accounts, List<Transfer> transfers) {
        if (accounts == null || accounts.isEmpty()) {
            throw new ValidationException("Accounts cannot be null or empty");
        }

        if (transfers == null) {
            throw new ValidationException("Transfers cannot be null");
        }
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
