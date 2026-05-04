package Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class PaymentFramework {
    protected static final double VAT_RATE = 0.12;

    protected String customerName;
    protected String paymentMethod;
    protected double originalAmount;
    protected double discountPercent;
    protected double discountAmount;
    protected double amountAfterDiscount;
    protected double vatAmount;
    protected double totalAmount;
    protected boolean transactionSuccess;
    protected String transactionId;
    protected LocalDateTime transactionDate;

    public PaymentFramework(String customerName, double originalAmount,
                            double discountPercent, String paymentMethod) {
        this.customerName = customerName;
        this.originalAmount = originalAmount;
        this.discountPercent = discountPercent;
        this.paymentMethod = paymentMethod;
        this.transactionDate = LocalDateTime.now();
        this.transactionId = generateTransactionId();
    }

    // Template method
    public void processInvoice() {
        printSectionHeader("INITIATING TRANSACTION");
        System.out.println(" [Step 1] Validating payment...");
        boolean valid = validatePayment();
        if (!valid) {
            transactionSuccess = false;
            printSectionHeader("TRANSACTION ABORTED");
            System.out.println(" Reason: Payment validation failed.");
            printDivider();
            return;
        }
        System.out.println(" OK Payment validated successfully.");

        System.out.println(" [Step 2] Applying discount...");
        applyDiscount();
        System.out.printf(" OK Discount applied : %.2f%% ( - %.2f )%n", discountPercent, discountAmount);

        System.out.println(" [Step 3] Applying 12%% VAT (inclusive)...");
        applyVAT();
        System.out.printf(" OK VAT amount : %.2f%n", vatAmount);
        System.out.printf(" OK Total payable : %.2f%n", totalAmount);

        System.out.println(" [Step 4] Finalizing transaction...");
        finalizeTransaction();

        printInvoice();
    }

    protected abstract boolean validatePayment();
    protected abstract void finalizeTransaction();

    protected void applyDiscount() {
        discountAmount = originalAmount * (discountPercent / 100.0);
        amountAfterDiscount = originalAmount - discountAmount;
    }

    protected void applyVAT() {
        vatAmount = amountAfterDiscount * VAT_RATE;
        totalAmount = amountAfterDiscount + vatAmount;
    }

    protected void printInvoice() {
        String border = "=".repeat(50);
        String thin = "-".repeat(46);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        System.out.println(" " + border + "+");
        System.out.println(" | " + center("OFFICIAL RECEIPT", 50) + "|");
        System.out.println(" | Transaction ID : " + transactionId);
        System.out.println(" | Date/Time      : " + transactionDate.format(fmt));
        System.out.println(" | Customer       : " + customerName);
        System.out.println(" | Payment Method : " + paymentMethod);
        System.out.println(" | " + thin + "|");
        System.out.printf(" | Original Amount : %12.2f%n", originalAmount);
        System.out.printf(" | Discount (%%)    : %6.2f%n", discountPercent);
        System.out.printf(" | Discount Amount  : %12.2f%n", discountAmount);
        System.out.println(" | " + thin + "|");
        System.out.printf(" | After Discount   : %12.2f%n", amountAfterDiscount);
        System.out.printf(" | VAT (12%%)        : %12.2f%n", vatAmount);
        System.out.println(" | " + thin + "|");
        System.out.printf(" | TOTAL AMOUNT DUE : %12.2f%n", totalAmount);
        System.out.println(" | " + border + "+");
        if (transactionSuccess) {
            System.out.println(" | " + center("PAYMENT SUCCESSFUL", 50) + "|");
        } else {
            System.out.println(" | " + center("PAYMENT FAILED", 50) + "|");
        }
        System.out.println(" " + border + "+");
        System.out.println();
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis();
    }

    private static void printSectionHeader(String title) {
        System.out.println();
        System.out.println("+--- " + title + " ---+");
    }

    private static void printDivider() {
        System.out.println(" " + "-".repeat(49) + "+");
        System.out.println();
    }

    private static String center(String text, int width) {
        int padding = (width - text.length()) / 2;
        String pad = " ".repeat(Math.max(0, padding));
        String result = pad + text + pad;
        while (result.length() < width) result += " ";
        return result;
    }
}