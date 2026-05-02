package Service;

import DAO.*;
import Model.*;
import Util.PaymentFramework;

import java.time.LocalDate;
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

        // Record the deposit payment
        Payment p = new Payment.Builder()
                .invoiceId(invId)
                .amount(depositAmount)
                .paymentDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .method(paymentMethod)
                .reference("DEPOSIT")
                .build();
        paymentDAO.insert(p);

        // Print receipt
        PaymentFramework processor = new CashPayment(reservation.getCustomerName(), depositAmount, 0);
        processor.processInvoice();

        return invId;
    }

    // recordPayment and other methods will be added in later commits.
}