package com.payflow.domain.model.transaction;

import java.util.Currency;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(Currency from, Currency to) {
        super("Currency mismatch: cannot transfer from " + from + " to " + to);
    }
}
