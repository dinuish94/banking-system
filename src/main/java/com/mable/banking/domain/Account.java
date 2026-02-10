package com.mable.banking.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Account {

    private String accountId;
    private BigDecimal balance;
}
