package com.mable.banking.app;

import com.mable.banking.domain.*;
import com.mable.banking.io.AccountCsvReader;
import com.mable.banking.io.ErrorReportWriter;
import com.mable.banking.io.BalanceLoadResult;
import com.mable.banking.io.TransactionCsvReader;
import com.mable.banking.io.TransactionLoadResult;
import com.mable.banking.io.TransactionReportWriter;
import com.mable.banking.service.AccountService;
import com.mable.banking.service.TransferProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Slf4j
public final class Main {

    private static final String DEFAULT_REPORT_PATH = "transaction_report.csv";
    private static final String DEFAULT_BALANCE_ERRORS_PATH = "balance_account_errors.csv";
    private static final String DEFAULT_TRANSACTION_ERRORS_PATH = "transaction_parse_errors.csv";

    public static void main(String[] args) {
        if (args.length < 2) {
            log.error("Usage: java com.mable.banking.app.Main <balances.csv> <transfers.csv> [transaction_report.csv] [balance_errors.csv] [transaction_errors.csv]");
            System.exit(1);
        }

        Path balancePath = Path.of(args[0]);
        Path transferPath = Path.of(args[1]);

        // output reports
        Path reportPath = args.length > 2 ? Path.of(args[2]) : Path.of(DEFAULT_REPORT_PATH);
        Path balanceErrorsPath = args.length > 3 ? Path.of(args[3]) : Path.of(DEFAULT_BALANCE_ERRORS_PATH);
        Path transactionErrorsPath = args.length > 4 ? Path.of(args[4]) : Path.of(DEFAULT_TRANSACTION_ERRORS_PATH);

        try {
            BalanceLoadResult balanceResult = new AccountCsvReader().load(balancePath);
            if (balanceResult.hasErrors()) {
                generateErrorReport(balanceErrorsPath, balanceResult.errors());
            }

            System.out.println(balanceResult);

            TransactionLoadResult transactionResult = new TransactionCsvReader().load(transferPath);
            if (balanceResult.hasErrors()) {
                generateErrorReport(transactionErrorsPath, transactionResult.errors());
            }

            var processResult = processTransfers(balanceResult.accounts(), transactionResult.transfers());
            writeTransactionReport(reportPath, processResult.getTransactionResults());

            log.info("Transaction report written to {}", reportPath);
            log.info("Loaded {} accounts, {} transfers", balanceResult.accounts().size(), transactionResult.transfers().size());
        } catch (IOException e) {
            log.error("IO error: {}", e.getMessage(), e);
            System.exit(2);
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage(), e);
            System.exit(3);
        }
    }

    private static ProcessResult processTransfers(Map<String, Account> accounts, List<Transfer> transfers) {
        var accountService = new AccountService();
        var transferProcessor = new TransferProcessor(accountService);
        return transferProcessor.process(accounts, transfers);
    }

    private static void writeTransactionReport(Path reportPath, List<TransactionResult> transactionResults) throws IOException {
        var transactionReportWriter = new TransactionReportWriter();
        transactionReportWriter.write(reportPath, transactionResults);
    }

    private static void generateErrorReport(Path errorFilesPath, List<LineError> errors) throws IOException {
        var errorReportWriter = new ErrorReportWriter();
        errorReportWriter.write(errorFilesPath, errors);
        log.info("Errors written to {}", errorFilesPath);
    }
}
