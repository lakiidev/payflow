package com.payflow.application.port;

import com.payflow.application.dto.TransactionView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface CsvExportPort {
    void writeCsv(List<TransactionView> transactions, OutputStream out) throws IOException;
}
