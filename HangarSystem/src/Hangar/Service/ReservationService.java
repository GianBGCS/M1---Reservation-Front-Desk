package Service;

import DAO.ReservationDAO;
import Model.Reservation;
import Util.ReservationUtil;
import Util.ReservationUtil.ServiceResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    private static final int NO_EXCLUDE_ID = 0;

    private final ReservationDAO dao;

    public ReservationService() {
        this.dao = new ReservationDAO();
    }

    public ServiceResult createReservation(
            String    customerName,
            String    aircraftTailNumber,
            String    hangarSlot,
            double    wingspan,
            double    length,
            LocalDate startDate,
            LocalDate endDate,
            double    depositAmount) {

        if (!ReservationUtil.isValidSlot(hangarSlot)) {
            return ServiceResult.failure("Hangar slot '" + hangarSlot + "' does not exist.");
        }

        if (!ReservationUtil.doesAircraftFit(hangarSlot, wingspan, length)) {
            return ServiceResult.failure(
                    ReservationUtil.buildSizeMismatchMessage(hangarSlot, wingspan, length),
                    findSuitableSlots(wingspan, length, startDate, endDate)
            );
        }

        if (dao.hasOverlap(hangarSlot, startDate, endDate, NO_EXCLUDE_ID)) {
            return ServiceResult.failure(
                    "Slot " + hangarSlot + " is already booked for the selected dates.",
                    findSuitableSlots(wingspan, length, startDate, endDate)
            );
        }

        Reservation reservation = new Reservation.Builder()
                .customerName(customerName)
                .aircraftTailNumber(aircraftTailNumber)
                .hangarSlot(hangarSlot)
                .startDate(startDate)
                .endDate(endDate)
                .depositAmount(depositAmount)
                .build();

        Reservation saved = dao.insert(reservation);
        if (saved == null) {
            return ServiceResult.failure("Database error: reservation could not be saved.");
        }

        return ServiceResult.success(saved);
    }



    public boolean isSlotAvailable(String hangarSlot, LocalDate start, LocalDate end) {
        return !dao.hasOverlap(hangarSlot, start, end, NO_EXCLUDE_ID);
    }

    public List<String> findSuitableSlots(double wingspan, double length,
                                          LocalDate start, LocalDate end) {
        List<String> suitable = new ArrayList<>();
        for (String[] slot : ReservationUtil.HANGAR_SLOTS) {
            boolean fits      = ReservationUtil.doesAircraftFit(slot[0], wingspan, length);
            boolean available = !dao.hasOverlap(slot[0], start, end, NO_EXCLUDE_ID);
            if (fits && available) {
                suitable.add(String.format(
                        "  Slot %-3s | Category: %-7s | Max Wingspan: %s m | Max Length: %s m",
                        slot[0], slot[3], slot[1], slot[2]
                ));
            }
        }
        return suitable;
    }

    public ServiceResult cancelReservation(int reservationId) {
        Reservation existing = dao.findById(reservationId);
        if (existing == null)
            return ServiceResult.failure("Reservation ID " + reservationId + " not found.");
        if (!existing.getStatus().equals(Reservation.STATUS_ACTIVE))
            return ServiceResult.failure("Reservation is already " + existing.getStatus() + ".");
        if (!LocalDate.now().isBefore(existing.getStartDate()))
            return ServiceResult.failure(
                    "Cannot cancel: the aircraft's start date has already passed.");

        if (!dao.updateStatus(reservationId, Reservation.STATUS_CANCELLED))
            return ServiceResult.failure("Database error: status could not be updated.");

        existing.setStatus(Reservation.STATUS_CANCELLED);
        return ServiceResult.success(existing);
    }

    public ServiceResult modifyReservation(int reservationId,
                                           String    newTailNumber,
                                           String    newHangarSlot,
                                           double    wingspan,
                                           double    length,
                                           LocalDate newStartDate,
                                           LocalDate newEndDate) {
        Reservation existing = dao.findById(reservationId);
        if (existing == null)
            return ServiceResult.failure("Reservation ID " + reservationId + " not found.");
        if (!existing.getStatus().equals(Reservation.STATUS_ACTIVE))
            return ServiceResult.failure("Only ACTIVE reservations can be modified.");

        if (!ReservationUtil.isValidSlot(newHangarSlot))
            return ServiceResult.failure("Hangar slot '" + newHangarSlot + "' does not exist.");

        if (!ReservationUtil.doesAircraftFit(newHangarSlot, wingspan, length))
            return ServiceResult.failure(
                    ReservationUtil.buildSizeMismatchMessage(newHangarSlot, wingspan, length),
                    findSuitableSlots(wingspan, length, newStartDate, newEndDate));

        if (dao.hasOverlap(newHangarSlot, newStartDate, newEndDate, reservationId))
            return ServiceResult.failure(
                    "Slot " + newHangarSlot + " is already booked for those dates.",
                    findSuitableSlots(wingspan, length, newStartDate, newEndDate));

        existing.setAircraftTailNumber(newTailNumber);
        existing.setHangarSlot(newHangarSlot);
        existing.setStartDate(newStartDate);
        existing.setEndDate(newEndDate);

        if (!dao.update(existing))
            return ServiceResult.failure("Database error: reservation could not be updated.");

        return ServiceResult.success(existing);
    }


    public RefundResult applyRefund(int reservationId) {
        // Condition 1 — reservation exists
        Reservation reservation = dao.findById(reservationId);
        if (reservation == null) {
            return RefundResult.failure("Reservation ID " + reservationId + " not found.");
        }

        if (ReservationUtil.isAlreadyCancelled(reservation.getStatus())) {
            return RefundResult.failure(
                    "Reservation #" + reservationId + " is already cancelled."
            );
        }

        LocalDate today             = LocalDate.now();
        double refundPercentage     = ReservationUtil.calculateRefundPercentage(today, reservation.getStartDate());
        double refundAmount         = ReservationUtil.calculateRefundAmount(reservation.getDepositAmount(), refundPercentage);

        boolean updated = dao.updateStatus(reservationId, Reservation.STATUS_CANCELLED);
        if (!updated) {
            return RefundResult.failure("Database error: could not cancel reservation.");
        }

        return RefundResult.success(reservation, today, refundPercentage, refundAmount);
    }

    public List<Reservation> getAllReservations()                 { return dao.findAll(); }
    public List<Reservation> getReservationsByCustomer(String n) { return dao.findByCustomer(n); }
    public List<Reservation> getReservationsByAircraft(String t) { return dao.findByAircraft(t); }
    public Reservation       getReservationById(int id)          { return dao.findById(id); }
    public boolean           updateStatus(int id, String status) { return dao.updateStatus(id, status); }
    public Reservation findById(int id) { return dao.findById(id); }


    public static class RefundResult {

        private final boolean     success;
        private final String      message;
        private final Reservation reservation;
        private final LocalDate   cancelDate;
        private final double      refundPercentage;
        private final double      refundAmount;

        private RefundResult(boolean success, String message, Reservation reservation,
                             LocalDate cancelDate, double refundPercentage, double refundAmount) {
            this.success          = success;
            this.message          = message;
            this.reservation      = reservation;
            this.cancelDate       = cancelDate;
            this.refundPercentage = refundPercentage;
            this.refundAmount     = refundAmount;
        }

        public static RefundResult success(Reservation reservation, LocalDate cancelDate,
                                           double refundPercentage, double refundAmount) {
            return new RefundResult(true, "Success", reservation,
                    cancelDate, refundPercentage, refundAmount);
        }

        public static RefundResult failure(String message) {
            return new RefundResult(false, message, null, null, 0, 0);
        }

        public boolean     isSuccess()          { return success; }
        public String      getMessage()         { return message; }
        public Reservation getReservation()     { return reservation; }
        public LocalDate   getCancelDate()      { return cancelDate; }
        public double      getRefundPercentage(){ return refundPercentage; }
        public double      getRefundAmount()    { return refundAmount; }
    }
}