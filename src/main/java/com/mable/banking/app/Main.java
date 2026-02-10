package com.mable.banking.app;

import com.mable.banking.domain.*;
import com.mable.banking.exception.BankingException;
import com.mable.banking.exception.ValidationException;
import com.mable.banking.io.AccountCsvReader;
import com.mable.banking.io.ErrorReportWriter;
import com.mable.banking.domain.BalanceLoadResult;
import com.mable.banking.io.TransactionCsvReader;
import com.mable.banking.io.TransactionLoadResult;
import com.mable.banking.io.TransactionReportWriter;
import com.mable.banking.service.AccountService;
import com.mable.banking.service.TransferProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Slf4j
public final class Main {

    private static final String INPUT_DIR = "src/main/resources/input";
    private static final String OUTPUT_DIR = "output";
    private static final Path DEFAULT_BALANCE_PATH = Path.of(INPUT_DIR, "mable_account_balances.csv");
    private static final Path DEFAULT_TRANSFER_PATH = Path.of(INPUT_DIR, "mable_transactions.csv");
    private static final Path DEFAULT_REPORT_PATH = Path.of(OUTPUT_DIR, "transaction_report.csv");
    private static final Path DEFAULT_BALANCE_ERRORS_PATH = Path.of(OUTPUT_DIR, "balance_account_errors.csv");
    private static final Path DEFAULT_TRANSACTION_ERRORS_PATH = Path.of(OUTPUT_DIR, "transaction_parse_errors.csv");

    public static void main(String[] args) {
        Path balancePath = args.length > 0 ? Path.of(args[0]) : DEFAULT_BALANCE_PATH;
        Path transferPath = args.length > 1 ? Path.of(args[1]) : DEFAULT_TRANSFER_PATH;
        Path reportPath = args.length > 2 ? Path.of(args[2]) : DEFAULT_REPORT_PATH;
        Path balanceErrorsPath = args.length > 3 ? Path.of(args[3]) : DEFAULT_BALANCE_ERRORS_PATH;
        Path transactionErrorsPath = args.length > 4 ? Path.of(args[4]) : DEFAULT_TRANSACTION_ERRORS_PATH;

        try {
            BalanceLoadResult balanceResult = new AccountCsvReader().load(balancePath);
            if (balanceResult.hasErrors()) {
                generateErrorReport(balanceErrorsPath, balanceResult.errors());
            }

            TransactionLoadResult transactionResult = new TransactionCsvReader().load(transferPath);
            if (transactionResult.hasErrors()) {
                generateErrorReport(transactionErrorsPath, transactionResult.errors());
            }

            var processResult = processTransfers(balanceResult.accounts(), transactionResult.transfers());
            writeTransactionReport(reportPath, processResult.transactionResults());

            log.info("Transaction report written to {}", reportPath);
            log.info("Loaded {} accounts, {} transfers", balanceResult.accounts().size(), transactionResult.transfers().size());
        } catch (IOException e) {
            log.error("IO error: {}", e.getMessage(), e);
            System.exit(2);
        } catch (ValidationException | BankingException e) {
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
        ensureParentDir(reportPath);
        var transactionReportWriter = new TransactionReportWriter();
        transactionReportWriter.write(reportPath, transactionResults);
    }

    private static void ensureParentDir(Path path) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
    }

    private static void generateErrorReport(Path errorFilesPath, List<LineError> errors) throws IOException {
        ensureParentDir(errorFilesPath);
        var errorReportWriter = new ErrorReportWriter();
        errorReportWriter.write(errorFilesPath, errors);
        log.info("Errors written to {}", errorFilesPath);
    }
}
