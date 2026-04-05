package Util;

import Model.HangarSlot;
import java.util.List;

public class HangarSlotUtil {

    public static final String DIVIDER = "================================================================";
    public static final String CANCEL  = "0";

    private HangarSlotUtil() {}

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

    public static String validateSlotCode(String input) {
        if (input == null || input.isBlank()) return null;
        return input.toUpperCase();
    }

    public static String validateCategory(String input) {
        if (input == null) return null;
        String upper = input.toUpperCase();
        if (upper.equals(HangarSlot.CATEGORY_SMALL)  ||
                upper.equals(HangarSlot.CATEGORY_MEDIUM) ||
                upper.equals(HangarSlot.CATEGORY_LARGE)) {
            return upper;
        }
        return null;
    }

    public static boolean isCancelled(String input) {
        return CANCEL.equals(input);
    }

    public static boolean isConfirmed(String input) {
        return input.equalsIgnoreCase("Y");
    }

  
    public static boolean isOccupied(HangarSlot slot) {
        return HangarSlot.STATUS_OCCUPIED.equals(slot.getStatus());
    }

   
    public static boolean isDuplicateSlotCode(String slotCode, List<HangarSlot> existingSlots) {
        for (HangarSlot slot : existingSlots) {
            if (slot.getSlotCode().equalsIgnoreCase(slotCode)) return true;
        }
        return false;
    }

    
    public static MenuAction resolveMenuChoice(String choice) {
        switch (choice) {
            case "1": return MenuAction.VIEW_ALL_HANGARS_AND_SLOTS;
            case "2": return MenuAction.CHECK_SLOT_AVAILABILITY;
            case "0": return MenuAction.LOGOUT;
            default:  return MenuAction.INVALID;
        }
    }

    public enum MenuAction {
        VIEW_ALL_HANGARS_AND_SLOTS,
        CHECK_SLOT_AVAILABILITY,
        LOGOUT,
        INVALID
    }

    public static class ServiceResult<T> {

        private static final String MSG_SUCCESS = "Success";

        private final boolean success;
        private final String  message;
        private final T       data;

        private ServiceResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data    = data;
        }

        public static <T> ServiceResult<T> success(T data) {
            return new ServiceResult<>(true, MSG_SUCCESS, data);
        }

        public static <T> ServiceResult<T> failure(String message) {
            return new ServiceResult<>(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public String  getMessage(){ return message; }
        public T       getData()   { return data; }
    }
}