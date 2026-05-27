package lab3.util;

import lab1.model.Transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CSVExporter() {}

    // Export full transaction list to CSV file
    public static void exportTransactions(List<Transaction> transactions, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header row
            writer.println("Reference ID,Type,From Account,To Account,Amount (RWF),Fee (RWF),Total (RWF),Status,Description,Date");
            // Data rows
            for (Transaction t : transactions) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,\"%s\",\"%s\",\"%s\"%n",
                    t.getReferenceId(),
                    t.getTransactionType(),
                    t.getFromAccountNumber() != null ? t.getFromAccountNumber() : "-",
                    t.getToAccountNumber()   != null ? t.getToAccountNumber()   : "-",
                    t.getAmount(),
                    t.getFee()    != null ? t.getFee()    : 0.0,
                    t.getTotalAmount(),
                    t.getStatus(),
                    t.getDescription() != null ? t.getDescription().replace("\"", "'") : "",
                    t.getCreatedAt()   != null ? t.getCreatedAt().format(FMT) : ""
                );
            }
        }
    }

    // Export daily summary — counts and totals grouped by transaction type
    public static void exportDailySummary(List<Transaction> transactions, String filePath, String date) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("IgirePay - Daily Transaction Summary");
            writer.println("Date: " + date);
            writer.println();
            writer.println("Type,Count,Total Amount (RWF),Total Fees (RWF)");

            String[] types = {"DEPOSIT", "WITHDRAW", "TRANSFER"};
            double grandAmt = 0, grandFee = 0;
            int grandCount = 0;

            for (String type : types) {
                List<Transaction> filtered = transactions.stream()
                    .filter(t -> type.equals(t.getTransactionType())).toList();
                long count   = filtered.size();
                double amt   = filtered.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum();
                double fee   = filtered.stream().mapToDouble(t -> t.getFee() != null ? t.getFee().doubleValue() : 0).sum();
                writer.printf("%s,%d,%.2f,%.2f%n", type, count, amt, fee);
                grandAmt   += amt;
                grandFee   += fee;
                grandCount += count;
            }
            writer.printf("TOTAL,%d,%.2f,%.2f%n", grandCount, grandAmt, grandFee);
        }
    }
}
