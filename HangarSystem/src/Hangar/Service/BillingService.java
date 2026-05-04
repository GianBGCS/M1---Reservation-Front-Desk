package Service;

import DAO.*;
import Model.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BillingService {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final HangarPricingDAO pricingDAO = new HangarPricingDAO();

    /**
     * Creates an invoice for a reservation with VAT‑inclusive total,
     * applying a membership discount before VAT.
     */
    public int createInvoiceForReservation(Reservation reservation, double depositAmount,
                                           String paymentMethod, double discountPercent) {
        HangarSlot slot = new HangarSlotDAO().findBySlotCode(reservation.getHangarSlot());
        double dailyRate = pricingDAO.getDailyRate(slot.getCategory());
        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate()) + 1;

        // Apply discount before VAT
        double base = days * dailyRate;
        double discountAmount = base * discountPercent / 100.0;
        double afterDiscount = base - discountAmount;
        double vat = afterDiscount * 0.12;
        double total = afterDiscount + vat;

        Invoice inv = new Invoice.Builder()
                .reservationId(reservation.getReservationId())
                .customerName(reservation.getCustomerName())
                .aircraftTail(reservation.getAircraftTailNumber())
                .hangarSlot(reservation.getHangarSlot())
                .startDate(reservation.getStartDate().format(Reservation.DATE_FORMAT))
                .endDate(reservation.getEndDate().format(Reservation.DATE_FORMAT))
                .days((int) days)
                .dailyRate(dailyRate)
                .totalAmount(total)                         // VAT‑inclusive after discount
                .depositPaid(depositAmount)
                .additionalPaid(0)
                .balance(total - depositAmount)
                .status(total - depositAmount <= 0 ? "PAID" : "PARTIAL")
                .build();

        int invId = invoiceDAO.insert(inv);
        if (invId == -1) return -1;

        // Record the deposit payment
        Payment p = new Payment.Builder()
                .invoiceId(invId)
                .amount(depositAmount)
                .paymentDate(LocalDate.now().toString())
                .method(paymentMethod)
                .reference("DEPOSIT")
                .build();
        paymentDAO.insert(p);

        // Print a proper receipt including discount
        printDepositReceipt(inv, depositAmount, paymentMethod, discountPercent, discountAmount, base, afterDiscount, vat);

        return invId;
    }

    private void printDepositReceipt(Invoice inv, double deposit, String method,
                                     double discountPercent, double discountAmount,
                                     double base, double afterDiscount, double vat) {
        String border = "=".repeat(52);
        String thin = "-".repeat(48);

        System.out.println(" " + border);
        System.out.println(" | " + center("DEPOSIT RECEIPT", 52) + " |");
        System.out.println(" | " + thin + " |");
        System.out.printf(" | Reservation ID   : %-28d |\n", inv.getReservationId());
        System.out.printf(" | Customer         : %-28s |\n", inv.getCustomerName());
        System.out.printf(" | Aircraft         : %-28s |\n", inv.getAircraftTail());
        System.out.printf(" | Hangar Slot      : %-28s |\n", inv.getHangarSlot());
        System.out.printf(" | Period           : %s to %s |\n", inv.getStartDate(), inv.getEndDate());
        System.out.printf(" | Days             : %-27d |\n", inv.getDays());
        System.out.println(" | " + thin + " |");
        System.out.printf(" | Daily Rate       : %-27.2f |\n", inv.getDailyRate());
        System.out.printf(" | Base Amount      : %-27.2f |\n", base);
        if (discountPercent > 0) {
            System.out.printf(" | Discount (%5.1f%%) : %-27.2f |\n", discountPercent, discountAmount);
            System.out.printf(" | After Discount   : %-27.2f |\n", afterDiscount);
        }
        System.out.printf(" | VAT (12%%)        : %-27.2f |\n", vat);
        System.out.println(" | " + thin + " |");
        System.out.printf(" | TOTAL (incl VAT) : %-27.2f |\n", inv.getTotalAmount());
        System.out.printf(" | Deposit Paid     : %-27.2f |\n", deposit);
        System.out.printf(" | Payment Method   : %-27s |\n", method);
        System.out.printf(" | Balance Due      : %-27.2f |\n", inv.getBalance());
        System.out.println(" | " + border);
        System.out.println(" | " + center("TRANSACTION SUCCESSFUL", 52) + " |");
        System.out.println(" " + border);
        System.out.println();
    }

    public boolean recordPayment(int invoiceId, double amount, String method) {
        Invoice inv = invoiceDAO.findById(invoiceId);
        if (inv == null) return false;

        double newAdditional = inv.getAdditionalPaid() + amount;
        double newBalance = inv.getTotalAmount() - inv.getDepositPaid() - newAdditional;
        String newStatus = newBalance <= 0 ? "PAID" : "PARTIAL";

        Payment p = new Payment.Builder()
                .invoiceId(invoiceId)
                .amount(amount)
                .paymentDate(LocalDate.now().toString())
                .method(method)
                .reference("ADDITIONAL")
                .build();
        if (!paymentDAO.insert(p)) return false;

        inv = new Invoice.Builder()
                .id(inv.getId())
                .reservationId(inv.getReservationId())
                .customerName(inv.getCustomerName())
                .aircraftTail(inv.getAircraftTail())
                .hangarSlot(inv.getHangarSlot())
                .startDate(inv.getStartDate())
                .endDate(inv.getEndDate())
                .days(inv.getDays())
                .dailyRate(inv.getDailyRate())
                .totalAmount(inv.getTotalAmount())
                .depositPaid(inv.getDepositPaid())
                .additionalPaid(newAdditional)
                .balance(newBalance)
                .status(newStatus)
                .build();

        boolean updated = invoiceDAO.update(inv);
        if (updated) {
            System.out.println("\n>>> Payment of " + amount + " received. New balance: " + newBalance);
        }
        return updated;
    }

    /**
     * Returns the invoice for a reservation, using the discount stored in the reservation.
     * If no invoice exists, it is created on the fly applying that discount.
     */
    public Invoice getInvoiceForCheckOut(int reservationId) {
        Invoice inv = invoiceDAO.findByReservationId(reservationId);
        if (inv != null) return inv;

        ReservationDAO resDAO = new ReservationDAO();
        Reservation res = resDAO.findById(reservationId);
        if (res == null) return null;

        HangarSlot slot = new HangarSlotDAO().findBySlotCode(res.getHangarSlot());
        double dailyRate = pricingDAO.getDailyRate(slot.getCategory());
        long days = ChronoUnit.DAYS.between(res.getStartDate(), res.getEndDate()) + 1;

        double base = days * dailyRate;
        double discountAmount = base * res.getDiscountPercent() / 100.0;
        double afterDiscount = base - discountAmount;
        double total = afterDiscount * 1.12;   // VAT included

        inv = new Invoice.Builder()
                .reservationId(reservationId)
                .customerName(res.getCustomerName())
                .aircraftTail(res.getAircraftTailNumber())
                .hangarSlot(res.getHangarSlot())
                .startDate(res.getStartDate().format(Reservation.DATE_FORMAT))
                .endDate(res.getEndDate().format(Reservation.DATE_FORMAT))
                .days((int) days)
                .dailyRate(dailyRate)
                .totalAmount(total)
                .depositPaid(0)
                .additionalPaid(0)
                .balance(total)
                .status("PENDING")
                .build();

        int newId = invoiceDAO.insert(inv);
        if (newId == -1) return null;
        return invoiceDAO.findById(newId);
    }

    public Invoice findInvoiceById(int id) { return invoiceDAO.findById(id); }
    public List<Invoice> getAllInvoices() { return invoiceDAO.findAll(); }
    public List<Payment> getPaymentsForInvoice(int invoiceId) { return paymentDAO.findByInvoice(invoiceId); }

    // ─── Helper (center text) ─────────────────────────────────────────────
    private static String center(String text, int width) {
        int padding = (width - text.length()) / 2;
        String pad = " ".repeat(Math.max(0, padding));
        String result = pad + text + pad;
        while (result.length() < width) result += " ";
        return result;
    }
}