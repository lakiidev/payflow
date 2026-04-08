package com.payflow.application.command;

import java.util.UUID;

public record LogoutCommand(UUID userId, String tokenJti) {}