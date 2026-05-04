package Service;

import DAO.*;
import Model.*;
import Util.PaymentFramework;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BillingService {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final HangarPricingDAO pricingDAO = new HangarPricingDAO();

    public int createInvoiceForReservation(Reservation reservation, double depositAmount, String paymentMethod) {
        HangarSlot slot = new HangarSlotDAO().findBySlotCode(reservation.getHangarSlot());
        double dailyRate = pricingDAO.getDailyRate(slot.getCategory());
        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate()) + 1;
        double total = days * dailyRate;

        Invoice inv = new Invoice.Builder()
                .reservationId(reservation.getReservationId())
                .customerName(reservation.getCustomerName())
                .aircraftTail(reservation.getAircraftTailNumber())
                .hangarSlot(reservation.getHangarSlot())
                .startDate(reservation.getStartDate().format(Reservation.DATE_FORMAT))
                .endDate(reservation.getEndDate().format(Reservation.DATE_FORMAT))
                .days((int) days)
                .dailyRate(dailyRate)
                .totalAmount(total)
                .depositPaid(depositAmount)
                .additionalPaid(0)
                .balance(total - depositAmount)
                .status(total - depositAmount <= 0 ? "PAID" : "PARTIAL")
                .build();

        int invId = invoiceDAO.insert(inv);
        if (invId == -1) return -1;

        Payment p = new Payment.Builder()
                .invoiceId(invId)
                .amount(depositAmount)
                .paymentDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .method(paymentMethod)
                .reference("DEPOSIT")
                .build();
        paymentDAO.insert(p);

        // Print receipt using the payment framework
        PaymentFramework processor = new CashPayment(reservation.getCustomerName(), depositAmount, 0);
        processor.processInvoice();

        return invId;
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
            PaymentFramework processor = new CashPayment(inv.getCustomerName(), amount, 0);
            processor.processInvoice();
        }
        return updated;
    }

    public Invoice getInvoiceForCheckOut(int reservationId) {
        Invoice inv = invoiceDAO.findByReservationId(reservationId);
        if (inv != null) return inv;

        ReservationDAO resDAO = new ReservationDAO();
        Reservation res = resDAO.findById(reservationId);
        if (res == null) return null;

        HangarSlot slot = new HangarSlotDAO().findBySlotCode(res.getHangarSlot());
        double dailyRate = pricingDAO.getDailyRate(slot.getCategory());
        long days = ChronoUnit.DAYS.between(res.getStartDate(), res.getEndDate()) + 1;
        double total = days * dailyRate;

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
}