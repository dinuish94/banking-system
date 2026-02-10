package com.mable.banking.domain;

public record LineError(int lineNumber, String line, String errorMessage) {

}
