package com.payflow.application.port;

import com.payflow.application.dto.TransactionView;

import java.util.List;
import java.util.UUID;

public interface PdfExportPort {
    byte[] generatePdf(UUID walletId, List<TransactionView> transactions);
}