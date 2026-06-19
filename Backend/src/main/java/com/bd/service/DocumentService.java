package com.bd.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bd.exception.AccountNotFoundException;
import com.bd.model.Account;
import com.bd.model.TransactionLog;
import com.bd.repository.AccountRepository;
import com.bd.repository.RewardTransactionRepository;
import com.bd.repository.TransactionLogRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class DocumentService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final TransactionLogRepository transactions;
    private final AccountRepository accounts;
    private final RewardTransactionRepository rewards;
    private final CurrentUserService currentUser;

    public DocumentService(TransactionLogRepository transactions,
                           AccountRepository accounts,
                           RewardTransactionRepository rewards,
                           CurrentUserService currentUser) {
        this.transactions = transactions;
        this.accounts = accounts;
        this.rewards = rewards;
        this.currentUser = currentUser;
    }

    public byte[] receipt(Long transactionId) {
        TransactionLog transaction = transactions.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        assertTransactionOwner(transaction);
        return render(doc -> {
            addHeader(doc, "Transaction Receipt");
            PdfPTable table = keyValueTable();
            addRow(table, "Reference number", transaction.getReferenceNumber());
            addRow(table, "Sender account", transaction.getSenderAccount().getAccountNumber());
            addRow(table, "Receiver account", transaction.getReceiverAccount().getAccountNumber());
            addRow(table, "Amount", "INR " + transaction.getAmount());
            addRow(table, "Status", transaction.getStatus());
            addRow(table, "Date and time", transaction.getCreatedAt().format(DATE_TIME));
            addRow(table, "Reward points earned", String.valueOf(rewards.pointsForTransaction(transaction.getId())));
            doc.add(table);
        });
    }

    public byte[] statement(String accountId, LocalDate from, LocalDate to) {
        Account account = accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        if (!currentUser.username().equals(account.getUser().getUsername())) {
            throw new IllegalArgumentException("Cannot download another user's statement");
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<TransactionLog> rows = transactions.findAccountStatementTransactions(accountId, start, end);
        int rewardPoints = rewards.pointsForUserBetween(currentUser.username(), start, end);

        return render(doc -> {
            addHeader(doc, "Account Statement");
            PdfPTable summary = keyValueTable();
            addRow(summary, "Customer", account.getUser().getFullName());
            addRow(summary, "Account number", account.getAccountNumber());
            addRow(summary, "Date range", from + " to " + to);
            addRow(summary, "Opening balance", "Derived from available transaction history");
            addRow(summary, "Closing balance", "INR " + account.getBalance());
            addRow(summary, "Total rewards earned", String.valueOf(rewardPoints));
            doc.add(summary);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[] { 2, 2, 2, 2, 2 });
            table.setWidthPercentage(100);
            addHeaderCell(table, "Date");
            addHeaderCell(table, "Reference");
            addHeaderCell(table, "From");
            addHeaderCell(table, "To");
            addHeaderCell(table, "Amount");
            for (TransactionLog tx : rows) {
                table.addCell(cell(tx.getCreatedAt().format(DATE_TIME)));
                table.addCell(cell(tx.getReferenceNumber()));
                table.addCell(cell(tx.getSenderAccount().getAccountNumber()));
                table.addCell(cell(tx.getReceiverAccount().getAccountNumber()));
                table.addCell(cell("INR " + tx.getAmount()));
            }
            doc.add(table);
        });
    }

    private void assertTransactionOwner(TransactionLog transaction) {
        String username = currentUser.username();
        boolean ownsSender = username.equals(transaction.getSenderAccount().getUser().getUsername());
        boolean ownsReceiver = username.equals(transaction.getReceiverAccount().getUser().getUsername());
        if (!ownsSender && !ownsReceiver) {
            throw new IllegalArgumentException("Cannot download another user's receipt");
        }
    }

    private byte[] render(PdfRenderer renderer) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();
            renderer.render(doc);
            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate PDF", ex);
        }
    }

    private void addHeader(Document doc, String title) throws Exception {
        Paragraph logo = new Paragraph("MONEY-GER BANK", new Font(Font.HELVETICA, 18, Font.BOLD, new Color(15, 39, 66)));
        logo.setAlignment(Element.ALIGN_CENTER);
        doc.add(logo);
        Paragraph heading = new Paragraph(title, new Font(Font.HELVETICA, 14, Font.BOLD, new Color(15, 118, 110)));
        heading.setSpacingAfter(18);
        heading.setAlignment(Element.ALIGN_CENTER);
        doc.add(heading);
    }

    private PdfPTable keyValueTable() {
        PdfPTable table = new PdfPTable(new float[] { 1, 2 });
        table.setWidthPercentage(100);
        return table;
    }

    private void addRow(PdfPTable table, String key, String value) {
        table.addCell(cell(key, true));
        table.addCell(cell(value, false));
    }

    private void addHeaderCell(PdfPTable table, String value) {
        table.addCell(cell(value, true));
    }

    private PdfPCell cell(String value) {
        return cell(value, false);
    }

    private PdfPCell cell(String value, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, new Font(Font.HELVETICA, 10, header ? Font.BOLD : Font.NORMAL)));
        cell.setPadding(8);
        cell.setBorderColor(new Color(226, 232, 240));
        if (header) {
            cell.setBackgroundColor(new Color(240, 253, 250));
        }
        return cell;
    }

    @FunctionalInterface
    private interface PdfRenderer {
        void render(Document document) throws Exception;
    }
}
