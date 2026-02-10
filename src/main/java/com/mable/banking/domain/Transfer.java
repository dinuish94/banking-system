package com.mable.banking.domain;

import java.math.BigDecimal;

public record Transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
}
