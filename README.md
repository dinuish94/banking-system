# Banking Service

A simple Java application that loads account balances for a company from a CSV file and processes a day's transfers from a second CSV file. Each transfer gets a **transaction status** (APPLIED, INSUFFICIENT_BALANCE, UNKNOWN_FROM_ACCOUNT, UNKNOWN_TO_ACCOUNT, SAME_ACCOUNT). Business rules do not throw; the application completes and reports all results.

## Build and run

**Requirements:** Java 17+, Maven 3.6+

```bash
# Build and run tests
mvn clean test

# Run the application (from project root; uses input from src/main/resources/input, writes to output/)
mvn exec:java
```

With custom paths:

```bash
mvn exec:java -Dexec.args="path/to/balances.csv path/to/transfers.csv [report.csv] [balance_errors.csv] [transaction_errors.csv]"
```

Or after building:

```bash
mvn package -q
mvn exec:java -q
```

**Paths:**
- **Input (defaults):** `src/main/resources/input/mable_account_balances.csv`, `src/main/resources/input/mable_transactions.csv`
- **Output (defaults):** `output/transaction_report.csv`, `output/balance_account_errors.csv`, `output/transaction_parse_errors.csv`
- **Usage:** `com.mable.banking.app.Main [balances.csv] [transfers.csv] [transaction_report.csv] [balance_errors.csv] [transaction_errors.csv]` — all arguments optional.

The program prints final account balances and any transactions that were not applied, grouped by status.

## How it works

1. **Load balances** — The balance CSV has two columns per line: `accountId,balance`. Account IDs must be exactly 16 digits. Balances must be non-negative with up to 2 decimal places. Duplicate account IDs or invalid data cause an error.

2. **Parse transfers** — The transfer CSV has three columns: `fromAccountId,toAccountId,amount`. Blank lines are skipped. Invalid line format causes an error.

3. **Process transfers** — Transfers are processed in file order. Each transfer gets a status: `APPLIED` (debit/credit done), `INSUFFICIENT_BALANCE`, `UNKNOWN_FROM_ACCOUNT`, `UNKNOWN_TO_ACCOUNT`, or `SAME_ACCOUNT`. Only `APPLIED` transfers change balances. No exceptions are thrown for these business rules; the run always completes.

4. **Output** — Final balances for all accounts, then any non-applied transactions grouped by status.

## Design and trade-offs

- **Transaction results:** Each transfer produces a `TransactionResult` (transfer + status). Statuses: `APPLIED`, `INSUFFICIENT_BALANCE`, `UNKNOWN_FROM_ACCOUNT`, `UNKNOWN_TO_ACCOUNT`, `SAME_ACCOUNT`. Business rules never throw; they are expressed as statuses so the application can finish and report every transaction.

- **Unknown accounts:** A transfer whose from or to account is not in the balance file gets status `UNKNOWN_FROM_ACCOUNT `or `UNKNOWN_TO_ACCOUNT` and is not applied. The rest of the batch is still processed.

- **Invalid data:** Invalid lines are not loaded but are reported instead of failing the run. Lines in the **balance file** that have an invalid account ID, invalid balance, or duplicate account ID are written to `balance_account_errors.csv`. Lines in the **transactions file** that have invalid format or invalid amounts are written to `transaction_parse_errors.csv`. Transfers that reference unknown accounts (valid lines but from/to not in the balance set) receive status `UNKNOWN_FROM_ACCOUNT` or `UNKNOWN_TO_ACCOUNT` in the main transaction report.

## Assumptions

- Amounts are in the same currency (no conversion).
- Account IDs are numeric strings exactly 16 digits long.
