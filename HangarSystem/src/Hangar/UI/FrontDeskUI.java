package Hangar.UI;

package UI;

import Model.Reservation;
import Service.FrontDeskService;
import java.util.Scanner;

public class FrontDeskUI {
    private Scanner scanner = new Scanner(System.in);
    private FrontDeskService service = new FrontDeskService();
    private String currentUser = "gian";
    private String currentRole = "FRONT DESK";

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1": handleProcess("CHECK IN"); break;
                case "2": handleProcess("CHECK OUT"); break;
                case "0": running = false; break;
                default: System.out.println("\n[!] Invalid choice."); pause();
            }
        }
    }

    private void handleProcess(String type) {
        printHeader();
        System.out.println(type + "\n==================================================");
        System.out.print("Enter Tail Number: ");
        String tailNum = scanner.nextLine();

        Reservation res = service.validateTailNumber(tailNum);

        if (res != null) {
            showDetails(type, res);
            System.out.printf("Confirm %s for RES-%04d? (Y/N): ", type, res.getReservationId());

            if (scanner.nextLine().equalsIgnoreCase("Y")) {
                if (type.equals("CHECK OUT")) {
                    if (service.performCheckOut(res.getReservationId())) {
                        System.out.println("\n>>> SUCCESS: Data removed from database.");
                        System.out.print("Print Receipt? (Y/N): ");
                        if (scanner.nextLine().equalsIgnoreCase("Y")) printReceipt(res);
                    }
                } else {
                    System.out.println("\n>>> CHECK-IN SUCCESSFUL.");
                }
            }
        } else {
            System.out.println("\n[!] ERROR: No active reservation found for [" + tailNum + "].");
        }
        pause();
    }

    private void showDetails(String title, Reservation res) {
        System.out.println("\n" + title + " - RESERVATION DETAILS");
        System.out.println("--------------------------------------------------");
        System.out.printf("Reservation ID : RES-%04d%n", res.getReservationId());
        System.out.printf("Tail Number    : %s%n", res.getAircraftTailNumber());
        System.out.printf("Customer       : %s%n", res.getCustomerName());
        System.out.printf("Hangar / Slot  : %s%n", res.getHangarSlot());
        System.out.printf("Schedule Start : %s%n", res.getStartDate());
        System.out.printf("Schedule End   : %s%n", res.getEndDate());
        System.out.printf("Current Status : %s%n", res.getStatus());
        System.out.println("==================================================");
    }

    private void printHeader() {
        System.out.println("\n==================================================");
        System.out.println("      AVIATION HANGAR RESERVATION SYSTEM");
        System.out.printf("  Logged in as: %-15s Role: %-10s%n", currentUser, currentRole);
        System.out.println("==================================================");
    }

    private void printMainMenu() {
        printHeader();
        System.out.println(" [1] Check In\n [2] Check Out\n [0] Logout");
        System.out.println("==================================================");
    }

    private void printReceipt(Reservation res) {
        System.out.println("\n********** OFFICIAL RECEIPT **********");
        System.out.println("Tail Number : " + res.getAircraftTailNumber());
        System.out.println("Customer    : " + res.getCustomerName());
        System.out.println("Hangar Slot : " + res.getHangarSlot());
        System.out.println("Duration    : " + res.getStartDate() + " to " + res.getEndDate());
        System.out.println("**************************************");
    }

    private void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}