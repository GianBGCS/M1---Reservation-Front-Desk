package Service;

import DAO.HangarSlotDAO;
import DAO.ReservationDAO;
import Model.FrontDesk;
import Model.HangarSlot;
import Model.Reservation;
import Util.ReservationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FrontDeskService {

    public static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Removed hardcoded HANGAR_SLOTS array

    private final ReservationDAO resDAO = new ReservationDAO();

    public FrontDesk validateTailNumber(String tailNum) {
        if (tailNum == null || tailNum.trim().isEmpty()) return null;

        Reservation res = resDAO.findByTailNumber(tailNum.trim());
        if (res == null) return null;

        return new FrontDesk.Builder()
                .id(res.getReservationId())
                .tail(res.getAircraftTailNumber())
                .name(res.getCustomerName())
                .slot(res.getHangarSlot())
                .start(res.getStartDate().toString())
                .end(res.getEndDate().toString())
                .status(res.getStatus())
                .build();
    }

    public boolean performCheckOut(int id) {
        return id > 0 && resDAO.delete(id);
    }

    public FrontDesk findWalkInAircraft(String tailNum) {
        return validateTailNumber(tailNum);
    }

    public WalkInResult processWalkIn(String tailNumber, String customerName,
                                      String aircraftModel, double wingspan, double length,
                                      String checkInTime, String estimatedDeparture) {

        LocalDateTime checkInDT   = LocalDateTime.parse(checkInTime,        DATETIME_FORMAT);
        LocalDateTime departureDT = LocalDateTime.parse(estimatedDeparture, DATETIME_FORMAT);

        LocalDate startDate = checkInDT.toLocalDate();
        LocalDate endDate   = departureDT.toLocalDate();

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) days = 1;

        String availableSlot = findAvailableSlot(wingspan, length, startDate, endDate);
        if (availableSlot == null) {
            return WalkInResult.failure(
                    "No available slots found that can accommodate this aircraft for the requested period."
            );
        }

        Reservation reservation = new Reservation.Builder()
                .customerName(customerName)
                .aircraftTailNumber(tailNumber)
                .hangarSlot(availableSlot)
                .startDate(startDate)
                .endDate(endDate)
                .status(Reservation.STATUS_ACTIVE)
                .build();

        if (!resDAO.save(reservation)) {
            return WalkInResult.failure("Database error: Walk-In reservation could not be saved.");
        }

        FrontDesk result = new FrontDesk.Builder()
                .tail(tailNumber)
                .name(customerName)
                .aircraftModel(aircraftModel)
                .wingspan(String.valueOf(wingspan))
                .length(String.valueOf(length))
                .slot(availableSlot)
                .checkInTime(checkInTime)
                .estimatedDep(estimatedDeparture)
                .start(startDate.toString())
                .end(endDate.toString())
                .status(Reservation.STATUS_ACTIVE)
                .build();

        return WalkInResult.success(result, (int) days);
    }

    // Updated to use database slots
    private String findAvailableSlot(double wingspan, double length,
                                     LocalDate start, LocalDate end) {
        for (HangarSlot slot : ReservationUtil.getAllSlots()) {
            boolean fits = wingspan <= slot.getMaxWingspan() && length <= slot.getMaxLength();
            boolean available = !resDAO.hasOverlap(slot.getSlotCode(), start, end);
            if (fits && available) return slot.getSlotCode();
        }
        return null;
    }

    // ── Result wrapper (unchanged) ─────────────────────────────────────────
    public static class WalkInResult {
        private final boolean  success;
        private final String   message;
        private final FrontDesk data;
        private final int      days;

        private WalkInResult(boolean success, String message, FrontDesk data, int days) {
            this.success = success;
            this.message = message;
            this.data    = data;
            this.days    = days;
        }

        public static WalkInResult success(FrontDesk data, int days) {
            return new WalkInResult(true, "Success", data, days);
        }
        public static WalkInResult failure(String message) {
            return new WalkInResult(false, message, null, 0);
        }

        public boolean  isSuccess() { return success; }
        public String   getMessage(){ return message; }
        public FrontDesk getData()  { return data; }
        public int      getDays()   { return days; }
    }
}