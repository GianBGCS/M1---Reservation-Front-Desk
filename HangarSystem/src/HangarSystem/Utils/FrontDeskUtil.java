package HangarSystem.Utils;

import HangarSystem.Model.FrontDesk;

public class FrontDeskUtil {

    public static final String DIVIDER    = "==================================================";
    public static final double DAILY_RATE = 3000.00;

    private FrontDeskUtil() {}

    public static String validateString(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        return input.trim();
    }

    public static Double validatePositiveDouble(String input) {
        try {
            double val = Double.parseDouble(input.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isValidDateTime(String input) {
        if (input == null) return false;
        return input.trim().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
    }


    public static boolean isConfirmed(String input) {
        return input != null && input.trim().equalsIgnoreCase("Y");
    }


    public static void printHeader(String user, String role) {
        System.out.println("\n" + DIVIDER);
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.printf("  Logged in as: %-15s Role: %-10s%n", user, role);
        System.out.println(DIVIDER);
    }

    public static void printDetails(String type, FrontDesk res) {
        System.out.println("\n" + type + " - RESERVATION DETAILS");
        System.out.println("--------------------------------------------------");
        System.out.printf("  Tail Number    : %s%n", res.getAircraftTailNumber());
        System.out.printf("  Customer       : %s%n", res.getCustomerName());
        System.out.printf("  Hangar / Slot  : %s%n", res.getHangarSlot());
        System.out.printf("  Schedule Start : %s%n", res.getStartDate());
        System.out.printf("  Schedule End   : %s%n", res.getEndDate());
        System.out.println(DIVIDER);
    }

    public static void printReceipt(FrontDesk res) {
        System.out.println("\n********** OFFICIAL TRANSACTION RECORD **********");
        System.out.println("  Customer Name  : " + res.getCustomerName());
        System.out.println("  Tail Number    : " + res.getAircraftTailNumber());
        System.out.println("  Hangar Slot    : " + res.getHangarSlot());
        System.out.println("  Period of Stay : " + res.getStartDate() + " to " + res.getEndDate());
        System.out.println("  Status         : CHECKED OUT / COMPLETED");
        System.out.println("*************************************************\n");
    }

    public static void printWalkInAircraftFound(FrontDesk res) {
        System.out.println();
        System.out.println("  Aircraft Found!");
        System.out.printf("  Model     : %s%n", res.getAircraftModel());
        System.out.printf("  Wingspan  : %s m%n", res.getWingspan());
        System.out.printf("  Length    : %s m%n", res.getLength());
        System.out.printf("  Owner     : %s%n",   res.getCustomerName());
    }

    public static void printWalkInSlotFound(FrontDesk res, int days) {
        double estCost = DAILY_RATE * days;
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("WALK-IN — AVAILABLE SLOT FOUND");
        System.out.println(DIVIDER);
        System.out.printf("  Aircraft Registration num.  : %s%n",       res.getAircraftTailNumber());
        System.out.printf("  Customer                    : %s%n",       res.getCustomerName());
        System.out.printf("  Hangar / Slot               : %s%n",       res.getHangarSlot());
        System.out.printf("  Check-In Time               : %s%n",       res.getCheckInTime());
        System.out.printf("  Est. Departure              : %s%n",       res.getEstimatedDeparture());
        System.out.printf("  Days                        : %d days%n",  days);
        System.out.printf("  Daily Rate                  : PHP %.2f%n", DAILY_RATE);
        System.out.printf("  Est. Cost                   : PHP %.2f%n", estCost);
        System.out.println(DIVIDER);
    }

    public static void printWalkInRegistration(FrontDesk res) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("WALK-IN — REGISTER NEW AIRCRAFT");
        System.out.println(DIVIDER);
        System.out.printf("  Registration   : %s%n",   res.getAircraftTailNumber());
        System.out.printf("  Model          : %s%n",   res.getAircraftModel());
        System.out.printf("  Wingspan       : %s m%n", res.getWingspan());
        System.out.printf("  Length         : %s m%n", res.getLength());
        System.out.printf("  Owner          : %s%n",   res.getCustomerName());
        System.out.println();
        System.out.println("  [SUCCESS] New aircraft registered.");
    }

    public static void printNoSlotFound() {
        System.out.println();
        System.out.println("  [ERROR] No available slots found that can accommodate");
        System.out.println("          this aircraft for the requested period.");
        System.out.println("          Walk-In reservation was NOT created.");
    }

    public static void pause(java.util.Scanner scanner) {
        System.out.println("\nPress ENTER to return to Front Desk Menu...");
        scanner.nextLine();
    }
}