package com.mable.banking.domain;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class ProcessResult {

    Map<String, Account> accounts;
    List<TransactionResult> transactionResults;

}
