package Util;

import Model.Reservation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReservationUtil {

    public static final String DIVIDER = "================================================================";
    public static final String CANCEL  = "0";

    public static final String[][] HANGAR_SLOTS = {
            { "A1", "20.0", "15.0", "SMALL"  },
            { "A2", "20.0", "15.0", "SMALL"  },
            { "A3", "20.0", "15.0", "SMALL"  },
            { "B1", "36.0", "30.0", "MEDIUM" },
            { "B2", "36.0", "30.0", "MEDIUM" },
            { "B3", "36.0", "30.0", "MEDIUM" },
            { "C1", "65.0", "55.0", "LARGE"  },
            { "C2", "65.0", "55.0", "LARGE"  }
    };

    private ReservationUtil() {}

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

    public static boolean isValidSlot(String slotCode) {
        return findSlot(slotCode) != null;
    }

    public static boolean doesAircraftFit(String slotCode, double wingspan, double length) {
        String[] slot = findSlot(slotCode);
        if (slot == null) return false;
        double maxWingspan = Double.parseDouble(slot[1]);
        double maxLength   = Double.parseDouble(slot[2]);
        return wingspan <= maxWingspan && length <= maxLength;
    }

    public static boolean isCancelled(String input) {
        return input.equals(CANCEL);
    }

    public static boolean isConfirmed(String input) {
        return input.equalsIgnoreCase("Y");
    }

    public static MenuAction resolveMenuChoice(String choice) {
        switch (choice) {
            case "1": return MenuAction.NEW_RESERVATION;
            case "2": return MenuAction.MODIFY_RESERVATION;
            case "3": return MenuAction.CANCEL_RESERVATION;
            case "4": return MenuAction.VIEW_BY_CUSTOMER;
            case "5": return MenuAction.VIEW_BY_AIRCRAFT;
            case "6": return MenuAction.APPLY_CANCELLATION_REFUND;
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
        APPLY_CANCELLATION_REFUND,
        LOGOUT,
        INVALID
    }

    public static double calculateRefundPercentage(LocalDate cancelDate, LocalDate startDate) {
        long daysBeforeStart = cancelDate.until(startDate, java.time.temporal.ChronoUnit.DAYS);
        if (daysBeforeStart > 30)  return 100.0;
        if (daysBeforeStart >= 15) return 50.0;
        if (daysBeforeStart >= 7)  return 25.0;
        return 0.0;
    }


    public static double calculateRefundAmount(double depositAmount, double refundPercentage) {
        return depositAmount * (refundPercentage / 100.0);
    }

    public static boolean isAlreadyCancelled(String status) {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    public static String[] findSlot(String slotCode) {
        for (String[] slot : HANGAR_SLOTS) {
            if (slot[0].equalsIgnoreCase(slotCode)) return slot;
        }
        return null;
    }

    public static String buildSizeMismatchMessage(String slotCode, double wingspan, double length) {
        String[] slot = findSlot(slotCode);
        if (slot == null) return "Hangar slot '" + slotCode + "' does not exist.";
        double maxWingspan = Double.parseDouble(slot[1]);
        double maxLength   = Double.parseDouble(slot[2]);
        return String.format(
                "Aircraft does not fit in slot %s.\n" +
                        "  Slot limit    — Wingspan: %.1f m | Length: %.1f m\n" +
                        "  Your aircraft — Wingspan: %.1f m | Length: %.1f m",
                slotCode, maxWingspan, maxLength, wingspan, length
        );
    }

    public static class ServiceResult {

        private static final String MSG_SUCCESS = "Success";

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
            return new ServiceResult(true, MSG_SUCCESS, data, null);
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