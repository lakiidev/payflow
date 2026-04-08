package com.payflow.application.command;

import java.util.UUID;

public record FreezeWalletCommand(UUID walletId, UUID userId) {}
