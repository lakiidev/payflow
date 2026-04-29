package com.payflow.application.port;

import com.payflow.application.dto.TransactionView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

public interface PdfExportPort {
    void writePdf(UUID walletId, List<TransactionView> transactions, OutputStream out) throws IOException;
}