package com.payflow.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Currency;

public record CreateWalletRequest(@NotNull Currency currency) {}
