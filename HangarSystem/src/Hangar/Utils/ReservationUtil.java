package Util;

import DAO.HangarSlotDAO;
import Model.HangarSlot;
import Model.Reservation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReservationUtil {

    public static final String DIVIDER = "================================================================";
    public static final String CANCEL  = "0";

    private static final HangarSlotDAO slotDAO = new HangarSlotDAO();

    private ReservationUtil() {}

    // ─── DB-backed slot methods (replaces hardcoded array) ───────────────────
    public static List<HangarSlot> getAllSlots() {
        return slotDAO.findAll();
    }

    public static HangarSlot findSlotByCode(String slotCode) {
        return slotDAO.findBySlotCode(slotCode);
    }

    public static boolean isValidSlot(String slotCode) {
        return findSlotByCode(slotCode) != null;
    }

    public static boolean doesAircraftFit(String slotCode, double wingspan, double length) {
        HangarSlot slot = findSlotByCode(slotCode);
        if (slot == null) return false;
        return wingspan <= slot.getMaxWingspan() && length <= slot.getMaxLength();
    }

    public static String buildSizeMismatchMessage(String slotCode, double wingspan, double length) {
        HangarSlot slot = findSlotByCode(slotCode);
        if (slot == null) return "Hangar slot '" + slotCode + "' does not exist.";
        return String.format(
                "Aircraft does not fit in slot %s.\n" +
                        "  Slot limit    — Wingspan: %.1f m | Length: %.1f m\n" +
                        "  Your aircraft — Wingspan: %.1f m | Length: %.1f m",
                slotCode, slot.getMaxWingspan(), slot.getMaxLength(), wingspan, length
        );
    }

    // ─── Input validation helpers (unchanged) ─────────────────────────────────
    public static String validateString(String input) {
        if (input == null || input.isBlank()) return null;
        return input;
    }

    public static Double validatePositiveDouble(String input) {
        try {
            double val = Double.parseDouble(input);
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static LocalDate validateDate(String input) {
        try {
            return LocalDate.parse(input, Reservation.DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean validateEndDate(LocalDate startDate, LocalDate endDate) {
        return endDate != null && !endDate.isBefore(startDate);
    }

    public static boolean isCancelled(String input) {
        return CANCEL.equals(input);
    }

    public static boolean isConfirmed(String input) {
        return input.equalsIgnoreCase("Y");
    }

    // ─── Menu action resolution (unchanged) ───────────────────────────────────
    public static MenuAction resolveMenuChoice(String choice) {
        switch (choice) {
            case "1": return MenuAction.NEW_RESERVATION;
            case "2": return MenuAction.MODIFY_RESERVATION;
            case "3": return MenuAction.CANCEL_RESERVATION;
            case "4": return MenuAction.VIEW_BY_CUSTOMER;
            case "5": return MenuAction.VIEW_BY_AIRCRAFT;
            case "0": return MenuAction.LOGOUT;
            default:  return MenuAction.INVALID;
        }
    }

    public enum MenuAction {
        NEW_RESERVATION,
        MODIFY_RESERVATION,
        CANCEL_RESERVATION,
        VIEW_BY_CUSTOMER,
        VIEW_BY_AIRCRAFT,
        LOGOUT,
        INVALID
    }

    // ─── ServiceResult inner class (unchanged) ────────────────────────────────
    public static class ServiceResult {
        private final boolean      success;
        private final String       message;
        private final Reservation  data;
        private final List<String> alternatives;

        private ServiceResult(boolean success, String message,
                              Reservation data, List<String> alternatives) {
            this.success      = success;
            this.message      = message;
            this.data         = data;
            this.alternatives = alternatives;
        }

        public static ServiceResult success(Reservation data) {
            return new ServiceResult(true, "Success", data, null);
        }

        public static ServiceResult failure(String message) {
            return new ServiceResult(false, message, null, null);
        }

        public static ServiceResult failure(String message, List<String> alternatives) {
            return new ServiceResult(false, message, null, alternatives);
        }

        public boolean      isSuccess()       { return success; }
        public String       getMessage()      { return message; }
        public Reservation  getData()         { return data; }
        public List<String> getAlternatives() { return alternatives; }
        public boolean      hasAlternatives() { return alternatives != null && !alternatives.isEmpty(); }
    }
}