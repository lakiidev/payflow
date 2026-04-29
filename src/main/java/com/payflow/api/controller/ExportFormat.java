package com.payflow.api.controller;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ExportFormat {
    CSV, PDF;

    @JsonCreator
    public static ExportFormat fromString(String value) {
        return valueOf(value.toUpperCase());
    }
}
