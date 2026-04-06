package Service;

import DAO.HangarSlotDAO;
import DAO.ReservationDAO;
import Model.HangarSlot;
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
            LocalDate endDate) {

        if (!ReservationUtil.isValidSlot(hangarSlot))
            return ServiceResult.failure("Hangar slot '" + hangarSlot + "' does not exist.");

        if (!ReservationUtil.doesAircraftFit(hangarSlot, wingspan, length))
            return ServiceResult.failure(
                    ReservationUtil.buildSizeMismatchMessage(hangarSlot, wingspan, length),
                    findSuitableSlots(wingspan, length, startDate, endDate));

        if (dao.hasOverlap(hangarSlot, startDate, endDate, NO_EXCLUDE_ID))
            return ServiceResult.failure(
                    "Slot " + hangarSlot + " is already booked for the selected dates.",
                    findSuitableSlots(wingspan, length, startDate, endDate));

        Reservation reservation = new Reservation.Builder()
                .customerName(customerName)
                .aircraftTailNumber(aircraftTailNumber)
                .hangarSlot(hangarSlot)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        Reservation saved = dao.insert(reservation);
        if (saved == null)
            return ServiceResult.failure("Database error: reservation could not be saved.");

        return ServiceResult.success(saved);
    }

    public ServiceResult cancelReservation(int reservationId) {
        Reservation existing = dao.findById(reservationId);
        if (existing == null)
            return ServiceResult.failure("Reservation ID " + reservationId + " not found.");
        if (!existing.getStatus().equals(Reservation.STATUS_ACTIVE))
            return ServiceResult.failure("Reservation is already " + existing.getStatus() + ".");
        if (!LocalDate.now().isBefore(existing.getStartDate()))
            return ServiceResult.failure("Cannot cancel: the aircraft's start date has already passed.");

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

    // Updated to use database slots
    public List<String> findSuitableSlots(double wingspan, double length,
                                          LocalDate start, LocalDate end) {
        List<String> suitable = new ArrayList<>();
        for (HangarSlot slot : ReservationUtil.getAllSlots()) {
            boolean fits = wingspan <= slot.getMaxWingspan() && length <= slot.getMaxLength();
            boolean available = !dao.hasOverlap(slot.getSlotCode(), start, end, NO_EXCLUDE_ID);
            if (fits && available) {
                suitable.add(String.format(
                        "  Slot %-3s | Category: %-7s | Max Wingspan: %.1f m | Max Length: %.1f m",
                        slot.getSlotCode(), slot.getCategory(),
                        slot.getMaxWingspan(), slot.getMaxLength()
                ));
            }
        }
        return suitable;
    }

    public boolean isSlotAvailable(String hangarSlot, LocalDate start, LocalDate end) {
        return !dao.hasOverlap(hangarSlot, start, end, NO_EXCLUDE_ID);
    }

    public List<Reservation> getAllReservations()                 { return dao.findAll(); }
    public List<Reservation> getReservationsByCustomer(String n) { return dao.findByCustomer(n); }
    public List<Reservation> getReservationsByAircraft(String t) { return dao.findByAircraft(t); }
    public Reservation       getReservationById(int id)          { return dao.findById(id); }
    public Reservation       findById(int id)                    { return dao.findById(id); }
    public boolean           updateStatus(int id, String status) { return dao.updateStatus(id, status); }
}