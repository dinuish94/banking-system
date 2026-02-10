# Banking Service

A simple Java application that loads account balances for a company from a CSV file and processes a day's transfers from a second CSV file. Each transfer gets a **transaction status** (APPLIED, INSUFFICIENT_BALANCE, UNKNOWN_FROM_ACCOUNT, UNKNOWN_TO_ACCOUNT, SAME_ACCOUNT). Business rules do not throw; the application completes and reports all results.

## Build and run

**Requirements:** Java 17+, Maven 3.6+

```bash
# Build and run tests
mvn clean test

# Run the application (from project root)
mvn exec:java -q -Dexec.mainClass="com.mable.banking.Main" -Dexec.args="mable_account_balances.csv mable_transactions.csv"
```

Or after building:

```bash
mvn package -q
java -cp target/banking-service-1.0.0.jar com.mable.banking.Main mable_account_balances.csv mable_transactions.csv
```

**Usage:** `java com.mable.banking.Main <balances.csv> <transfers.csv>`

The program prints final account balances and any transactions that were not applied, grouped by status.

## How it works

1. **Load balances** — The balance CSV has two columns per line: `accountId,balance`. Account IDs must be exactly 16 digits. Balances must be non-negative with up to 2 decimal places. Duplicate account IDs or invalid data cause an error.

2. **Parse transfers** — The transfer CSV has three columns: `fromAccountId,toAccountId,amount`. Blank lines are skipped. Invalid line format causes an error.

3. **Process transfers** — Transfers are processed in file order. Each transfer gets a status: APPLIED (debit/credit done), INSUFFICIENT_BALANCE, UNKNOWN_FROM_ACCOUNT, UNKNOWN_TO_ACCOUNT, or SAME_ACCOUNT. Only APPLIED transfers change balances. No exceptions are thrown for these business rules; the run always completes.

4. **Output** — Final balances for all accounts, then any non-applied transactions grouped by status.

## Design and trade-offs

- **Domain models:** `Account` (id, balance) and `Transfer` (from, to, amount) encapsulate validation. Account IDs are validated once at construction; monetary values use `BigDecimal` with 2 decimal places.

- **Transaction results:** Each transfer produces a `TransactionResult` (transfer + status). Statuses: APPLIED, INSUFFICIENT_BALANCE, UNKNOWN_FROM_ACCOUNT, UNKNOWN_TO_ACCOUNT, SAME_ACCOUNT. Business rules never throw; they are expressed as statuses so the application can finish and report every transaction.

- **Unknown accounts:** A transfer whose from or to account is not in the balance file gets status UNKNOWN_FROM_ACCOUNT or UNKNOWN_TO_ACCOUNT and is not applied. The rest of the batch is still processed.

- **Immutability of input:** The processor does not mutate the loaded account map; it works on a copy so callers can reuse the same balance set.

- **CSV format:** No header row is expected. Commas inside fields are not supported (simple split on comma).

## Assumptions

- Amounts are in the same currency (no conversion).
- Account IDs are numeric strings exactly 16 digits long.
