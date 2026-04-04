package Utils;

import Model.FrontDesk;

public class FrontDeskUtil {

    public static void printHeader(String user, String role) {
        System.out.println("\n==================================================");
        System.out.println("      AVIATION HANGAR RESERVATION SYSTEM");
        System.out.printf("  Logged in as: %-15s Role: %-10s%n", user, role);
        System.out.println("==================================================");
    }

    public static void printDetails(String type, FrontDesk res) {
        System.out.println("\n" + type + " - RESERVATION DETAILS");
        System.out.println("--------------------------------------------------");
        System.out.printf("Tail Number    : %s%n", res.getAircraftTailNumber());
        System.out.printf("Customer       : %s%n", res.getCustomerName());
        System.out.printf("Hangar / Slot  : %s%n", res.getHangarSlot());
        System.out.printf("Schedule Start : %s%n", res.getStartDate());
        System.out.printf("Schedule End   : %s%n", res.getEndDate());
        System.out.println("==================================================");
    }

    public static void printReceipt(FrontDesk res) {
        System.out.println("\n********** OFFICIAL TRANSACTION RECORD **********");
        System.out.println("  Customer Name  : " + res.getCustomerName());
        System.out.println("  Tail Number    : " + res.getAircraftTailNumber());
        System.out.println("  Hangar Slot    : " + res.getHangarSlot());
        System.out.println("  Period of Stay : " + res.getStartDate() + " to " + res.getEndDate());
        System.out.println("  Status         : CHECKED OUT / COMPLETED");
        System.out.println("************************************************\n");
    }

    public static void pause(java.util.Scanner scanner) {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}