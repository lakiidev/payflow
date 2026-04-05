package com.payflow.application.command;

public record RegisterCommand(
        String email,
        String password,
        String fullName
) {}
