package src.Hangar.UI;

import Model.FrontDesk;
import Model.Invoice;
import Service.FrontDeskService;
import Service.FrontDeskService.WalkInResult;
import Util.FrontDeskUtil;

import java.util.Scanner;
import java.util.regex.Pattern;

public class FrontDeskUI {

    private final Scanner scanner;
    private final FrontDeskService service;
    private final String currentUser;
    private final String currentRole;

    // Regex patterns for phone (11 digits) and email (@gmail.com)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$");

    public FrontDeskUI(Scanner scanner, String currentUser, String currentRole) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.currentRole = currentRole;
        this.service = new FrontDeskService();
    }

    public void start() {
        boolean running = true;
        while (running) {
            FrontDeskUtil.printHeader(currentUser, currentRole);
            System.out.println(" [1] Check In");
            System.out.println(" [2] Check Out");
            System.out.println(" [3] Walk-In Reservation");
            System.out.println(" [0] Back to Main Menu");
            System.out.println(FrontDeskUtil.DIVIDER);
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleProcess("CHECK IN");
                case "2" -> handleCheckOut();
                case "3" -> handleWalkIn();
                case "0" -> running = false;
                default  -> System.out.println("\n[!] Invalid choice.");
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
            System.out.println("\n[!] No record found for Tail Number [" + tailNum + "].");
        }
        FrontDeskUtil.pause(scanner);
    }

    private void handleCheckOut() {
        FrontDeskUtil.printHeader(currentUser, currentRole);
        System.out.print("Enter Tail Number: ");
        String tailNum = scanner.nextLine().trim();

        FrontDesk res = service.validateTailNumber(tailNum);
        if (res == null) {
            System.out.println("\n[!] No record found for Tail Number [" + tailNum + "].");
            FrontDeskUtil.pause(scanner);
            return;
        }

        FrontDeskUtil.printDetails("CHECK OUT", res);
        System.out.print("Confirm Check-Out for RES-" + res.getReservationId() + "? (Y/N): ");
        if (!FrontDeskUtil.isConfirmed(scanner.nextLine())) {
            System.out.println("Cancelled.");
            FrontDeskUtil.pause(scanner);
            return;
        }

        // Generate invoice
        Invoice invoice = service.getInvoiceForCheckOut(res.getReservationId());
        if (invoice == null) {
            System.out.println("\n[!] Failed to generate invoice.");
            FrontDeskUtil.pause(scanner);
            return;
        }

        System.out.println("\nINVOICE DETAILS:");
        System.out.println(invoice);
        if (invoice.getBalance() > 0) {
            System.out.printf("Outstanding balance: %.2f%n", invoice.getBalance());
            while (invoice.getBalance() > 0) {
                System.out.print("Record payment? (Enter amount, or 0 to skip): ");
                String amtStr = scanner.nextLine().trim();
                if (amtStr.equals("0")) break;
                try {
                    double amt = Double.parseDouble(amtStr);
                    if (amt <= 0) { System.out.println("[!] Amount must be positive."); continue; }
                    System.out.print("Payment method: ");
                    String method = scanner.nextLine().trim();
                    boolean paid = service.recordCheckOutPayment(invoice.getId(), amt, method);
                    if (paid) {
                        invoice = service.getInvoiceForCheckOut(res.getReservationId());
                        System.out.println("\nPayment recorded. Updated invoice:");
                        System.out.println(invoice);
                    } else {
                        System.out.println("[!] Payment recording failed.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[!] Invalid amount.");
                }
            }
        }

        service.finalizeCheckOut(res.getReservationId());
        System.out.println("\n>>> CHECK-OUT COMPLETED. Invoice INV-" + invoice.getId() + " closed.");
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
        String phone;
        String email;
        String aircraftModel;
        double wingspan;
        double length;

        if (existing != null) {
            System.out.println();
            System.out.println("  Aircraft Found!");
            System.out.printf("  Owner     : %s%n", existing.getCustomerName());

            customerName  = existing.getCustomerName();
            phone = promptPhone("  Enter customer phone (11 digits) : ");
            if (phone == null) { printCancelled(); return; }
            email = promptEmail("  Enter customer email (@gmail.com): ");
            if (email == null) { printCancelled(); return; }

            aircraftModel = promptString("  Enter Aircraft Model    : ");
            if (aircraftModel == null) { printCancelled(); return; }

            wingspan = promptPositiveDouble("  Aircraft Wingspan (m)   : ");
            if (wingspan < 0) { printCancelled(); return; }

            length = promptPositiveDouble("  Aircraft Length   (m)   : ");
            if (length < 0) { printCancelled(); return; }

            FrontDesk found = new FrontDesk.Builder()
                    .tail(tailNumber).name(customerName)
                    .aircraftModel(aircraftModel)
                    .wingspan(String.valueOf(wingspan))
                    .length(String.valueOf(length)).build();
            FrontDeskUtil.printWalkInAircraftFound(found);

        } else {
            System.out.println();
            System.out.println("  [!] Aircraft not found.");
            System.out.print("  Register as new? (Y/N): ");
            if (!FrontDeskUtil.isConfirmed(scanner.nextLine())) {
                System.out.println("\n  Walk-In cancelled.");
                FrontDeskUtil.pause(scanner);
                return;
            }

            customerName = promptString("  Enter Owner Name        : ");
            if (customerName == null) { printCancelled(); return; }

            phone = promptPhone("  Enter customer phone (11 digits) : ");
            if (phone == null) { printCancelled(); return; }
            email = promptEmail("  Enter customer email (@gmail.com): ");
            if (email == null) { printCancelled(); return; }

            aircraftModel = promptString("  Enter Aircraft Model    : ");
            if (aircraftModel == null) { printCancelled(); return; }

            wingspan = promptPositiveDouble("  Aircraft Wingspan (m)   : ");
            if (wingspan < 0) { printCancelled(); return; }

            length = promptPositiveDouble("  Aircraft Length   (m)   : ");
            if (length < 0) { printCancelled(); return; }

            FrontDesk registered = new FrontDesk.Builder()
                    .tail(tailNumber).name(customerName)
                    .aircraftModel(aircraftModel)
                    .wingspan(String.valueOf(wingspan))
                    .length(String.valueOf(length)).build();
            FrontDeskUtil.printWalkInRegistration(registered);
        }

        System.out.println();
        String estimatedDeparture = promptDateTime("Enter Estimated Departure (YYYY-MM-DD HH:MM): ");
        if (estimatedDeparture == null) { printCancelled(); return; }

        String checkInTime = java.time.LocalDateTime.now()
                .format(FrontDeskService.DATETIME_FORMAT);

        WalkInResult result = service.processWalkIn(
                tailNumber, customerName, phone, email, aircraftModel,
                wingspan, length, checkInTime, estimatedDeparture);

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

    // ── Input helpers ──────────────────────────────────────────────────────────
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
            Double val = FrontDeskUtil.validatePositiveDouble(scanner.nextLine().trim());
            if (val != null) return val;
            System.out.println("  [!] Enter a positive number (e.g. 12.5).");
        }
    }

    private String promptDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (FrontDeskUtil.isValidDateTime(input)) return input;
            System.out.println("  [!] Use YYYY-MM-DD HH:MM (e.g. 2026-06-15 14:00).");
        }
    }

    private String promptPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals("0")) return null;
            if (PHONE_PATTERN.matcher(input).matches()) return input;
            System.out.println("  [!] Phone must be exactly 11 digits. Enter 0 to cancel.");
        }
    }

    private String promptEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals("0")) return null;
            if (EMAIL_PATTERN.matcher(input).matches()) return input;
            System.out.println("  [!] Email must be a valid @gmail.com address. Enter 0 to cancel.");
        }
    }

    private void printCancelled() {
        System.out.println("\n  Walk-In cancelled. Returning to menu...");
        FrontDeskUtil.pause(scanner);
    }
}