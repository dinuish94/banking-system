package com.mable.banking.app;

import com.mable.banking.domain.TransactionStatus;
import com.mable.banking.io.AccountCsvReader;
import com.mable.banking.io.TransactionCsvReader;
import com.mable.banking.service.AccountService;
import com.mable.banking.service.TransferProcessor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: loads example files, runs processor, asserts final balances.
 */
class BankingIntegrationTest {

    @Test
    @DisplayName("example files produce correct final balances")
    void exampleFilesProduceCorrectBalances() throws Exception {
        Path balancePath = Path.of("mable_account_balances.csv");
        Path transferPath = Path.of("mable_transactions.csv");
        if (!balancePath.toFile().exists() || !transferPath.toFile().exists()) {
            return;
        }

        var balanceResult = new AccountCsvReader().load(balancePath);
        var transactionResult = new TransactionCsvReader().load(transferPath);
        var result = new TransferProcessor(new AccountService()).process(balanceResult.accounts(), transactionResult.transfers());

        long nonApplied = result.getTransactionResults().stream()
            .filter(r -> r.getStatus() != TransactionStatus.APPLIED)
            .count();
        assertEquals(0, nonApplied);

        assertEquals(new BigDecimal("4820.50"), result.getAccounts().get("1111234522226789").getBalance());
        assertEquals(new BigDecimal("9974.40"), result.getAccounts().get("1111234522221234").getBalance());
        assertEquals(new BigDecimal("1550.00"), result.getAccounts().get("2222123433331212").getBalance());
        assertEquals(new BigDecimal("1725.60"), result.getAccounts().get("1212343433335665").getBalance());
        assertEquals(new BigDecimal("48679.50"), result.getAccounts().get("3212343433335755").getBalance());
    }
}
