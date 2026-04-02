package UI;

import Model.Reservation;
import Service.ReservationService;
import Service.ReservationService.RefundResult;
import Util.ReservationUtil;
import Util.ReservationUtil.MenuAction;
import Util.ReservationUtil.ServiceResult;

import java.time.LocalDate;
import java.util.Scanner;

public class ReservationUI {

    private final Scanner            scanner;
    private final String             loggedInUser;
    private final String             userRole;
    private final ReservationService service;

    public ReservationUI(Scanner scanner, String loggedInUser, String userRole) {
        this.scanner      = scanner;
        this.loggedInUser = loggedInUser;
        this.userRole     = userRole;
        this.service      = new ReservationService();
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            MenuAction action = ReservationUtil.resolveMenuChoice(choice);

            switch (action) {
                case NEW_RESERVATION:
                    runNewReservation();
                    break;
                case MODIFY_RESERVATION:
                    System.out.println("\n  [Modify Reservation — not yet implemented]\n");
                    break;
                case CANCEL_RESERVATION:
                    System.out.println("\n  [Cancel Reservation — not yet implemented]\n");
                    break;
                case VIEW_BY_CUSTOMER:
                    System.out.println("\n  [View by Customer — not yet implemented]\n");
                    break;
                case VIEW_BY_AIRCRAFT:
                    System.out.println("\n  [View by Aircraft — not yet implemented]\n");
                    break;
                case APPLY_CANCELLATION_REFUND:
                    runApplyCancellationRefund();
                    break;
                case LOGOUT:
                    System.out.println("\n  Logging out...\n");
                    running = false;
                    break;
                case INVALID:
                    System.out.println("\n  [!] Invalid choice. Please enter 0-6.\n");
                    break;
            }
        }
    }


    private void runNewReservation() {
        printHeader();
        System.out.println("  NEW RESERVATION");
        System.out.println();

        String customerName = promptString("  Enter customer name        : ");
        if (customerName == null) { printCancelled(); return; }

        String tailNumber = promptString("  Enter aircraft tail number : ");
        if (tailNumber == null) { printCancelled(); return; }
        tailNumber = tailNumber.toUpperCase();

        System.out.println();
        System.out.println("  Enter aircraft dimensions for hangar size validation:");
        Double wingspan = promptPositiveDouble("  Aircraft wingspan (meters) : ");
        if (wingspan == null) { printCancelled(); return; }

        Double length = promptPositiveDouble("  Aircraft length  (meters) : ");
        if (length == null) { printCancelled(); return; }

        printSlotTable();
        String hangarSlot = promptHangarSlot();
        if (hangarSlot == null) { printCancelled(); return; }

        LocalDate startDate = promptDate("  Enter start date (yyyy-MM-dd): ");
        if (startDate == null) { printCancelled(); return; }

        LocalDate endDate = promptEndDate(startDate);
        if (endDate == null) { printCancelled(); return; }

        Double deposit = promptPositiveDouble("  Enter deposit amount (PHP)  : ");
        if (deposit == null) { printCancelled(); return; }

        printConfirmation(customerName, tailNumber, wingspan, length, hangarSlot, startDate, endDate, deposit);
        System.out.print("  Confirm? [Y/N]: ");
        if (!ReservationUtil.isConfirmed(scanner.nextLine().trim())) {
            printCancelled();
            return;
        }

        ServiceResult result = service.createReservation(
                customerName, tailNumber, hangarSlot,
                wingspan, length, startDate, endDate, deposit);

        printServiceResult(result);
        promptEnterToContinue();
    }

    private void runApplyCancellationRefund() {
        printHeader();
        System.out.println("  APPLY CANCELLATION REFUND");
        System.out.println();

        Integer reservationId = promptPositiveInt("  Enter Reservation ID (or 0 to cancel): ");
        if (reservationId == null) { printCancelled(); return; }

        Reservation reservation = service.getReservationById(reservationId);
        if (reservation == null) {
            System.out.println();
            System.out.println(ReservationUtil.DIVIDER);
            System.out.println("  [!] Reservation ID " + reservationId + " not found.");
            System.out.println(ReservationUtil.DIVIDER);
            promptEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println("  RESERVATION TO CANCEL");
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println(reservation);
        System.out.println(ReservationUtil.DIVIDER);
        System.out.print("  Confirm cancellation and apply refund? [Y/N]: ");
        if (!ReservationUtil.isConfirmed(scanner.nextLine().trim())) {
            printCancelled();
            return;
        }

        RefundResult result = service.applyRefund(reservationId);

        printRefundResult(result);
        promptEnterToContinue();
    }


    private void printMenu() {
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println(ReservationUtil.DIVIDER);
        System.out.printf("  Logged in as: %-20s Role: %s%n", loggedInUser, userRole);
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println();
        System.out.println("RESERVATION MANAGEMENT");
        System.out.println();
        System.out.println("[1] New Reservation");
        System.out.println("[2] Modify Reservation");
        System.out.println("[3] Cancel Reservation");
        System.out.println("[4] View Reservations by Customer");
        System.out.println("[5] View Reservations by Aircraft");
        System.out.println("[6] Apply Cancellation Refund");
        System.out.println();
        System.out.println("[0] Logout");
        System.out.println();
        System.out.println(ReservationUtil.DIVIDER);
    }

    private void printHeader() {
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println(ReservationUtil.DIVIDER);
        System.out.printf("  Logged in as: %-20s Role: %s%n", loggedInUser, userRole);
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println();
    }

    private void printSlotTable() {
        System.out.println();
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println("  HANGAR SLOTS — SIZE LIMITS");
        System.out.println(ReservationUtil.DIVIDER);
        for (String[] slot : ReservationUtil.HANGAR_SLOTS) {
            System.out.printf(
                    "  Slot %-3s | Category: %-7s | Max Wingspan: %5s m | Max Length: %5s m%n",
                    slot[0], slot[3], slot[1], slot[2]
            );
        }
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println();
    }

    private void printConfirmation(String customerName, String tailNumber,
                                   double wingspan, double length, String hangarSlot,
                                   LocalDate startDate, LocalDate endDate, double deposit) {
        System.out.println();
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println("  CONFIRM RESERVATION DETAILS");
        System.out.println(ReservationUtil.DIVIDER);
        System.out.printf("  Customer Name    : %s%n",              customerName);
        System.out.printf("  Aircraft Tail No : %s%n",              tailNumber);
        System.out.printf("  Wingspan / Length: %.1f m / %.1f m%n", wingspan, length);
        System.out.printf("  Hangar Slot      : %s%n",              hangarSlot);
        System.out.printf("  Start Date       : %s%n",              startDate.format(Reservation.DATE_FORMAT));
        System.out.printf("  End Date         : %s%n",              endDate.format(Reservation.DATE_FORMAT));
        System.out.printf("  Deposit Amount   : PHP %.2f%n",        deposit);
        System.out.println(ReservationUtil.DIVIDER);
    }

    private void printServiceResult(ServiceResult result) {
        System.out.println();
        System.out.println(ReservationUtil.DIVIDER);
        if (result.isSuccess()) {
            System.out.println("  [SUCCESS] Reservation created successfully!");
            System.out.println(ReservationUtil.DIVIDER);
            System.out.println(result.getData());
        } else {
            System.out.println("  [!] ERROR: " + result.getMessage());
            if (result.hasAlternatives()) {
                System.out.println();
                System.out.println("  Suggested available slots for your aircraft:");
                System.out.println();
                for (String alt : result.getAlternatives()) {
                    System.out.println(alt);
                }
            }
        }
        System.out.println(ReservationUtil.DIVIDER);
    }

    private void printRefundResult(RefundResult result) {
        System.out.println();
        System.out.println(ReservationUtil.DIVIDER);
        if (result.isSuccess()) {
            System.out.println("  [SUCCESS] Reservation cancelled and refund applied!");
            System.out.println(ReservationUtil.DIVIDER);
            System.out.printf("  Reservation ID   : %d%n",       result.getReservation().getReservationId());
            System.out.printf("  Customer         : %s%n",       result.getReservation().getCustomerName());
            System.out.printf("  Cancellation Date: %s%n",       result.getCancelDate().format(Reservation.DATE_FORMAT));
            System.out.printf("  Start Date       : %s%n",       result.getReservation().getStartDate().format(Reservation.DATE_FORMAT));
            System.out.printf("  Deposit Paid     : PHP %.2f%n", result.getReservation().getDepositAmount());
            System.out.printf("  Refund Rate      : %.0f%%%n",   result.getRefundPercentage());
            System.out.printf("  Refund Amount    : PHP %.2f%n", result.getRefundAmount());
            System.out.println(ReservationUtil.DIVIDER);
            System.out.println("  Refund Policy:");
            System.out.println("    > 30 days before start  → 100% refund");
            System.out.println("    15-30 days before start →  50% refund");
            System.out.println("    7-14 days before start  →  25% refund");
            System.out.println("    < 7 days before start   →   0% refund");
        } else {
            System.out.println("  [!] ERROR: " + result.getMessage());
        }
        System.out.println(ReservationUtil.DIVIDER);
    }

    private void printCancelled() {
        System.out.println();
        System.out.println("  Cancelled. Returning to menu...");
        System.out.println();
    }


    private String promptString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (ReservationUtil.isCancelled(input)) return null;
            if (ReservationUtil.validateString(input) != null) return input;
            System.out.println("  [!] Input cannot be empty. Enter 0 to cancel.");
        }
    }

    private Double promptPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (ReservationUtil.isCancelled(input)) return null;
            Double val = ReservationUtil.validatePositiveDouble(input);
            if (val != null) return val;
            System.out.println("  [!] Invalid number. Enter a value like 12.5 or 5000 (0 to cancel).");
        }
    }

    private Integer promptPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (ReservationUtil.isCancelled(input)) return null;
            try {
                int val = Integer.parseInt(input);
                if (val > 0) return val;
                System.out.println("  [!] ID must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid ID. Enter a number (0 to cancel).");
            }
        }
    }

    private String promptHangarSlot() {
        while (true) {
            System.out.print("  Enter hangar slot (or 0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (ReservationUtil.isCancelled(input)) return null;
            if (ReservationUtil.isValidSlot(input)) return input;
            System.out.println("  [!] Invalid slot. Choose from the table above.");
        }
    }

    private LocalDate promptDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (ReservationUtil.isCancelled(input)) return null;
            LocalDate date = ReservationUtil.validateDate(input);
            if (date != null) return date;
            System.out.println("  [!] Invalid format. Use yyyy-MM-dd (e.g. 2025-06-15).");
        }
    }

    private LocalDate promptEndDate(LocalDate startDate) {
        while (true) {
            LocalDate end = promptDate("  Enter end date   (yyyy-MM-dd): ");
            if (end == null) return null;
            if (ReservationUtil.validateEndDate(startDate, end)) return end;
            System.out.println("  [!] End date must be on or after start date ("
                    + startDate.format(Reservation.DATE_FORMAT) + ").");
        }
    }

    private void promptEnterToContinue() {
        System.out.print("  Press Enter to return to menu...");
        scanner.nextLine();
        System.out.println();
    }
}