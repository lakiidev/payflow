package com.payflow.application.command;

import java.util.Currency;
import java.util.UUID;

public record CreateWalletCommand(UUID userId, Currency currency) {}
