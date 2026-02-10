package com.mable.banking.domain;

import java.util.List;
import java.util.Map;

public record ProcessResult(Map<String, Account> accounts, List<TransactionResult> transactionResults) {

}
