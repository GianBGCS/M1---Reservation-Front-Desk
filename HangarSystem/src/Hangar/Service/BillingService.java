package Service;

import DAO.*;
import Model.*;
import Utils.PaymentFramework;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class BillingService {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final HangarPricingDAO pricingDAO = new HangarPricingDAO();

    /**
     * Create an invoice for a reservation when the deposit is paid.
     * Only the invoice table is written here – payment record will be added later.
     */
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

        // Print receipt (no DB payment record yet)
        PaymentFramework processor = new CashPayment(reservation.getCustomerName(), depositAmount, 0);
        processor.processInvoice();

        return invId;
    }

    // Additional methods (getInvoiceForCheckOut, recordPayment, etc.) will be added in later commits.
}