package HangarSystem.UI;

import HangarSystem.Model.FrontDesk;
import HangarSystem.Service.FrontDeskService;
import HangarSystem.Service.FrontDeskService.WalkInResult;
import HangarSystem.Utils.FrontDeskUtil;
import java.util.Scanner;

public class FrontDeskUI {

    private final Scanner          scanner     = new Scanner(System.in);
    private final FrontDeskService service     = new FrontDeskService();
    private final String           currentUser = "gian";
    private final String           currentRole = "FRONT DESK";

    public void start() {
        boolean running = true;
        while (running) {
            FrontDeskUtil.printHeader(currentUser, currentRole);
            System.out.println(" [1] Check In");
            System.out.println(" [2] Check Out");
            System.out.println(" [3] Walk-In Reservation");
            System.out.println(" [0] Logout");
            System.out.println(FrontDeskUtil.DIVIDER);
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": handleProcess("CHECK IN");  break;
                case "2": handleProcess("CHECK OUT"); break;
                case "3": handleWalkIn();              break;
                case "0": running = false;             break;
                default:  System.out.println("\n[!] Invalid choice.");
            }
        }
    }


    private void handleProcess(String type) {
        FrontDeskUtil.printHeader(currentUser, currentRole);
        System.out.print("Enter Tail Number: ");
        String tailNum = scanner.nextLine().trim();

        FrontDesk res = service.validateTailNumber(tailNum);

        if (res != null) {
            FrontDeskUtil.printDetails(type, res);
            System.out.printf("Confirm %s for RES-%04d? (Y/N): ", type, res.getReservationId());

            if (FrontDeskUtil.isConfirmed(scanner.nextLine())) {
                if (type.equals("CHECK OUT")) {
                    FrontDeskUtil.printReceipt(res);
                    if (service.performCheckOut(res.getReservationId())) {
                        System.out.println("\n>>> TRANSACTION FINALIZED.");
                    }
                } else {
                    System.out.println("\n>>> CHECK-IN SUCCESSFUL.");
                }
            }
        } else {
            System.out.println("\n[!] ERROR: No record found for Tail Number [" + tailNum + "].");
        }
        FrontDeskUtil.pause(scanner);
    }

    private void handleWalkIn() {
        FrontDeskUtil.printHeader(currentUser, currentRole);
        System.out.println("WALK-IN RESERVATION");
        System.out.println();
        System.out.println(FrontDeskUtil.DIVIDER);

        System.out.print("Enter Aircraft Registration: ");
        String tailNumber = scanner.nextLine().trim().toUpperCase();

        if (FrontDeskUtil.validateString(tailNumber) == null) {
            System.out.println("\n[!] Aircraft registration cannot be empty.");
            FrontDeskUtil.pause(scanner);
            return;
        }

        FrontDesk existing = service.findWalkInAircraft(tailNumber);

        String customerName;
        String aircraftModel;
        double wingspan;
        double length;

        if (existing != null) {
            System.out.println();
            System.out.println("  Aircraft Found!");
            System.out.printf("  Owner     : %s%n", existing.getCustomerName());

            customerName  = existing.getCustomerName();
            aircraftModel = promptString("  Enter Aircraft Model    : ");
            if (aircraftModel == null) { printCancelled(); return; }

            wingspan = promptPositiveDouble("  Aircraft Wingspan (m)   : ");
            if (wingspan < 0) { printCancelled(); return; }

            length = promptPositiveDouble("  Aircraft Length   (m)   : ");
            if (length < 0) { printCancelled(); return; }

            FrontDesk found = new FrontDesk.Builder()
                    .tail(tailNumber)
                    .name(customerName)
                    .aircraftModel(aircraftModel)
                    .wingspan(String.valueOf(wingspan))
                    .length(String.valueOf(length))
                    .build();
            FrontDeskUtil.printWalkInAircraftFound(found);

        } else {
            System.out.println();
            System.out.println("  [ERROR] Aircraft not found.");
            System.out.println();
            System.out.print("  Would you like to register a new aircraft? (Y/N): ");

            if (!FrontDeskUtil.isConfirmed(scanner.nextLine())) {
                System.out.println("\n  Walk-In cancelled.");
                FrontDeskUtil.pause(scanner);
                return;
            }

            customerName = promptString("  Enter Owner Name        : ");
            if (customerName == null) { printCancelled(); return; }

            aircraftModel = promptString("  Enter Aircraft Model    : ");
            if (aircraftModel == null) { printCancelled(); return; }

            wingspan = promptPositiveDouble("  Aircraft Wingspan (m)   : ");
            if (wingspan < 0) { printCancelled(); return; }

            length = promptPositiveDouble("  Aircraft Length   (m)   : ");
            if (length < 0) { printCancelled(); return; }

            FrontDesk registered = new FrontDesk.Builder()
                    .tail(tailNumber)
                    .name(customerName)
                    .aircraftModel(aircraftModel)
                    .wingspan(String.valueOf(wingspan))
                    .length(String.valueOf(length))
                    .build();
            FrontDeskUtil.printWalkInRegistration(registered);
        }

        // Step 3: Enter estimated departure
        System.out.println();
        String estimatedDeparture = promptDateTime("Enter Estimated Departure (YYYY-MM-DD HH:MM): ");
        if (estimatedDeparture == null) { printCancelled(); return; }

        // Check-in time is now
        String checkInTime = java.time.LocalDateTime.now()
                .format(FrontDeskService.DATETIME_FORMAT);

        // Step 4: Send to service
        WalkInResult result = service.processWalkIn(
                tailNumber, customerName, aircraftModel,
                wingspan, length, checkInTime, estimatedDeparture);

        // Step 5: Print result
        System.out.println();
        System.out.println(FrontDeskUtil.DIVIDER);
        if (result.isSuccess()) {
            FrontDeskUtil.printWalkInSlotFound(result.getData(), result.getDays());
        } else {
            FrontDeskUtil.printNoSlotFound();
            System.out.println(FrontDeskUtil.DIVIDER);
        }

        FrontDeskUtil.pause(scanner);
    }

    // ════════════════════════════════════════════════════════════════════════
    // PROMPT METHODS
    // ════════════════════════════════════════════════════════════════════════

    private String promptString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (FrontDeskUtil.validateString(input) != null) return input;
            System.out.println("  [!] Input cannot be empty.");
        }
    }

    private double promptPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            Double val = FrontDeskUtil.validatePositiveDouble(input);
            if (val != null) return val;
            System.out.println("  [!] Invalid number. Enter a value like 12.5");
        }
    }

    private String promptDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (FrontDeskUtil.isValidDateTime(input)) return input;
            System.out.println("  [!] Invalid format. Use YYYY-MM-DD HH:MM (e.g. 2026-03-12 15:00)");
        }
    }

    private void printCancelled() {
        System.out.println("\n  Walk-In cancelled. Returning to menu...");
        FrontDeskUtil.pause(scanner);
    }
}