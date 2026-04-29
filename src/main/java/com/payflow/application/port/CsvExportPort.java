package com.payflow.application.port;

import com.payflow.application.dto.TransactionView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

public interface CsvExportPort {
    void writeCsv(Stream<TransactionView> transactions, OutputStream out) throws IOException;
}
