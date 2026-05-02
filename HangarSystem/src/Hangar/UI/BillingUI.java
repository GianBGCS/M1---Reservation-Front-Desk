package UI;

import Model.Invoice;
import Model.Payment;
import Service.BillingService;
import java.util.List;
import java.util.Scanner;

public class BillingUI {
    private final Scanner scanner;
    private final BillingService billingService;
    private final String currentUser;
    private final String currentRole;

    public BillingUI(Scanner scanner, String currentUser, String currentRole) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.currentRole = currentRole;
        this.billingService = new BillingService();
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n========================================");
            System.out.println("   BILLING & INVOICES");
            System.out.println("========================================");
            System.out.println("  1. View All Invoices");
            System.out.println("  2. View Invoice by ID");
            System.out.println("  3. Record Payment");
            System.out.println("  0. Back");
            System.out.println("========================================");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllInvoices();
                case "2" -> viewInvoiceById();
                case "3" -> recordPayment();
                case "0" -> running = false;
                default -> System.out.println("[!] Invalid choice.");
            }
        }
    }

    private void viewAllInvoices() {
        List<Invoice> invoices = billingService.getAllInvoices();
        System.out.println("\n--- All Invoices ---");
        if (invoices.isEmpty()) {
            System.out.println("No invoices found.");
        } else {
            System.out.printf("%-8s %-15s %-10s %4s %8s %8s %8s %s%n",
                    "Inv#", "Customer", "Slot", "Days", "Total", "Paid", "Balance", "Status");
            for (Invoice inv : invoices) {
                System.out.printf("INV-%04d %-15s %-10s %4d %8.2f %8.2f %8.2f %s%n",
                        inv.getId(), inv.getCustomerName(), inv.getHangarSlot(),
                        inv.getDays(), inv.getTotalAmount(),
                        inv.getDepositPaid() + inv.getAdditionalPaid(),
                        inv.getBalance(), inv.getStatus());
            }
        }
        pause();
    }

    private void viewInvoiceById() {
        System.out.print("Enter Invoice ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Invoice inv = billingService.findInvoiceById(id);
            if (inv != null) {
                System.out.println("\n" + inv);
                List<Payment> payments = billingService.getPaymentsForInvoice(id);
                System.out.println("\nPayments:");
                for (Payment p : payments) {
                    System.out.printf("  %s | %8.2f | %s%n", p.getPaymentDate(), p.getAmount(), p.getMethod());
                }
            } else {
                System.out.println("[!] Invoice not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid ID.");
        }
        pause();
    }

    private void recordPayment() {
        System.out.print("Enter Invoice ID: ");
        try {
            int invId = Integer.parseInt(scanner.nextLine().trim());
            Invoice inv = billingService.findInvoiceById(invId);
            if (inv == null) {
                System.out.println("[!] Invoice not found.");
                pause();
                return;
            }
            System.out.printf("Current balance: %.2f%n", inv.getBalance());
            if (inv.getBalance() <= 0) {
                System.out.println("This invoice is already fully paid.");
                pause();
                return;
            }
            System.out.print("Enter payment amount: ");
            double amt = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Payment method (CASH/CARD): ");
            String method = scanner.nextLine().trim();
            if (billingService.recordPayment(invId, amt, method)) {
                System.out.println("\n>>> Payment recorded successfully.");
            } else {
                System.out.println("[!] Payment recording failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid input.");
        }
        pause();
    }

    private void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}