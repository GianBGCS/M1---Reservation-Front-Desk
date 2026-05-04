package UI;

import Model.HangarSlot;
import Model.Reservation;
import Service.ReservationService;
import Util.ReservationUtil;
import Util.ReservationUtil.MenuAction;
import Util.ReservationUtil.ServiceResult;

import DAO.HangarPricingDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ReservationUI {

    private final Scanner scanner;
    private final String loggedInUser;
    private final String userRole;
    private final ReservationService service;

    // Regex patterns for phone (11 digits) and email (only @gmail.com)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$");

    public ReservationUI(Scanner scanner, String loggedInUser, String userRole) {
        this.scanner = scanner;
        this.loggedInUser = loggedInUser;
        this.userRole = userRole;
        this.service = new ReservationService();
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            MenuAction action = ReservationUtil.resolveMenuChoice(choice);

            switch (action) {
                case NEW_RESERVATION    -> runNewReservation();
                case MODIFY_RESERVATION -> runModifyReservation();
                case CANCEL_RESERVATION -> runCancelReservation();
                case VIEW_BY_CUSTOMER   -> runViewByCustomer();
                case VIEW_BY_AIRCRAFT   -> runViewByAircraft();
                case LOGOUT -> {
                    System.out.println("\n  Returning to main menu...\n");
                    running = false;
                }
                case INVALID -> System.out.println("\n  [!] Invalid choice. Please enter 0-5.\n");
            }
        }
    }

    // ── Menu Actions ──────────────────────────────────────────────────────────
    private void runNewReservation() {
        printHeader();
        System.out.println("  NEW RESERVATION\n");

        String customerName = promptString("  Enter customer name        : ");
        if (customerName == null) { printCancelled(); return; }

        String phone = promptPhone("  Enter customer phone (11 digits) : ");
        if (phone == null) { printCancelled(); return; }

        String email = promptEmail("  Enter customer email (@gmail.com): ");
        if (email == null) { printCancelled(); return; }

        String tailNumber = promptString("  Enter aircraft tail number : ");
        if (tailNumber == null) { printCancelled(); return; }
        tailNumber = tailNumber.toUpperCase();

        System.out.println("\n  Enter aircraft dimensions for hangar size validation:");
        Double wingspan = promptPositiveDouble("  Aircraft wingspan (meters) : ");
        if (wingspan == null) { printCancelled(); return; }

        Double length = promptPositiveDouble("  Aircraft length   (meters) : ");
        if (length == null) { printCancelled(); return; }

        printSlotTable();
        String hangarSlot = promptHangarSlot();
        if (hangarSlot == null) { printCancelled(); return; }

        LocalDate startDate = promptDate("  Enter start date (yyyy-MM-dd): ");
        if (startDate == null) { printCancelled(); return; }

        LocalDate endDate = promptEndDate(startDate);
        if (endDate == null) { printCancelled(); return; }

        // ── Deposit and invoice generation ──
        HangarSlot slot = ReservationUtil.findSlotByCode(hangarSlot);
        double dailyRate = new HangarPricingDAO().getDailyRate(slot.getCategory());
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double estimatedTotal = days * dailyRate;
        System.out.printf("\n  Estimated total for %d day(s): %.2f%n", days, estimatedTotal);
        System.out.print("  Confirm deposit payment of full amount? (Y/N): ");
        if (!ReservationUtil.isConfirmed(scanner.nextLine().trim())) {
            System.out.println("\n  Reservation cancelled – deposit not paid.");
            promptEnterToContinue();
            return;
        }
        System.out.print("  Payment method (CASH/CARD): ");
        String paymentMethod = scanner.nextLine().trim().toUpperCase();

        // Call service with deposit
        ServiceResult result = service.createReservation(
                customerName, phone, email, tailNumber, hangarSlot,
                wingspan, length, startDate, endDate,
                estimatedTotal, paymentMethod);

        printResult("Reservation created successfully!", result);
        promptEnterToContinue();
    }

    private void runCancelReservation() {
        printHeader();
        System.out.println("  CANCEL RESERVATION\n");

        Integer id = promptPositiveInt("  Enter Reservation ID: ");
        if (id == null) { printCancelled(); return; }

        Reservation res = service.findById(id);
        if (res == null) {
            System.out.println("\n  [!] Reservation ID " + id + " not found.\n");
            promptEnterToContinue();
            return;
        }

        System.out.println("\n  Reservation to cancel:");
        System.out.println(res);
        System.out.println();
        System.out.print("  Confirm cancellation? [Y/N]: ");
        if (!ReservationUtil.isConfirmed(scanner.nextLine().trim())) { printCancelled(); return; }

        ServiceResult result = service.cancelReservation(id);
        printResult("Reservation #" + id + " has been CANCELLED.", result);
        promptEnterToContinue();
    }

    private void runModifyReservation() {
        printHeader();
        System.out.println("  MODIFY RESERVATION\n");

        Integer id = promptPositiveInt("  Enter Reservation ID: ");
        if (id == null) { printCancelled(); return; }

        Reservation current = service.findById(id);
        if (current == null) {
            System.out.println("\n  [!] Reservation ID " + id + " not found.\n");
            promptEnterToContinue();
            return;
        }
        if (!current.getStatus().equals(Reservation.STATUS_ACTIVE)) {
            System.out.println("\n  [!] Only ACTIVE reservations can be modified. Status: "
                    + current.getStatus() + "\n");
            promptEnterToContinue();
            return;
        }

        System.out.println("\n  Current reservation:");
        System.out.println(current);
        System.out.println("\n  Press Enter to keep the current value for any field.\n");

        String newTail = promptStringOrKeep(
                "  Aircraft tail number [" + current.getAircraftTailNumber() + "]: ",
                current.getAircraftTailNumber());
        if (newTail == null) { printCancelled(); return; }
        newTail = newTail.toUpperCase();

        System.out.println("  Enter aircraft dimensions for slot validation:");
        Double ws = promptPositiveDouble("  Wingspan (meters): ");
        if (ws == null) { printCancelled(); return; }
        Double ln = promptPositiveDouble("  Length   (meters): ");
        if (ln == null) { printCancelled(); return; }

        printSlotTable();
        String newSlot = promptHangarSlotOrKeep(current.getHangarSlot());
        if (newSlot == null) { printCancelled(); return; }

        LocalDate newStart = promptDateOrKeep(
                "  Start date [" + current.getStartDate().format(Reservation.DATE_FORMAT) + "]: ",
                current.getStartDate());
        if (newStart == null) { printCancelled(); return; }

        LocalDate newEnd = promptEndDateOrKeep(
                "  End date   [" + current.getEndDate().format(Reservation.DATE_FORMAT) + "]: ",
                current.getEndDate(), newStart);
        if (newEnd == null) { printCancelled(); return; }

        System.out.println("\n" + ReservationUtil.DIVIDER);
        System.out.println("  CONFIRM CHANGES — Reservation #" + id);
        System.out.println(ReservationUtil.DIVIDER);
        System.out.printf("  Aircraft Tail No : %s → %s%n", current.getAircraftTailNumber(), newTail);
        System.out.printf("  Wingspan / Length: %.1f m / %.1f m%n", ws, ln);
        System.out.printf("  Hangar Slot      : %s → %s%n", current.getHangarSlot(), newSlot);
        System.out.printf("  Start Date       : %s → %s%n",
                current.getStartDate().format(Reservation.DATE_FORMAT),
                newStart.format(Reservation.DATE_FORMAT));
        System.out.printf("  End Date         : %s → %s%n",
                current.getEndDate().format(Reservation.DATE_FORMAT),
                newEnd.format(Reservation.DATE_FORMAT));
        System.out.println(ReservationUtil.DIVIDER);
        System.out.print("  Confirm? [Y/N]: ");
        if (!ReservationUtil.isConfirmed(scanner.nextLine().trim())) { printCancelled(); return; }

        ServiceResult result = service.modifyReservation(id, newTail, newSlot, ws, ln, newStart, newEnd);
        printResult("Reservation modified successfully!", result);
        promptEnterToContinue();
    }

    private void runViewByCustomer() {
        printHeader();
        System.out.println("  VIEW RESERVATIONS BY CUSTOMER\n");
        String name = promptString("  Enter customer name: ");
        if (name == null) { printCancelled(); return; }
        List<Reservation> list = service.getReservationsByCustomer(name);
        System.out.println("\n" + ReservationUtil.DIVIDER);
        if (list.isEmpty()) {
            System.out.println("  No reservations found for: " + name);
        } else {
            System.out.println("  RESERVATIONS FOR: " + name.toUpperCase());
            System.out.println(ReservationUtil.DIVIDER);
            for (Reservation r : list) System.out.println(r);
        }
        System.out.println(ReservationUtil.DIVIDER);
        promptEnterToContinue();
    }

    private void runViewByAircraft() {
        printHeader();
        System.out.println("  VIEW RESERVATIONS BY AIRCRAFT\n");
        String tail = promptString("  Enter aircraft tail number: ");
        if (tail == null) { printCancelled(); return; }
        tail = tail.toUpperCase();
        List<Reservation> list = service.getReservationsByAircraft(tail);
        System.out.println("\n" + ReservationUtil.DIVIDER);
        if (list.isEmpty()) {
            System.out.println("  No reservations found for aircraft: " + tail);
        } else {
            System.out.println("  RESERVATIONS FOR AIRCRAFT: " + tail);
            System.out.println(ReservationUtil.DIVIDER);
            for (Reservation r : list) System.out.println(r);
        }
        System.out.println(ReservationUtil.DIVIDER);
        promptEnterToContinue();
    }

    // ─── Display helpers ─────────────────────────────────────────────────────
    private void printMenu() {
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println(ReservationUtil.DIVIDER);
        System.out.printf("  Logged in as: %-20s Role: %s%n", loggedInUser, userRole);
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println("\nRESERVATION MANAGEMENT\n");
        System.out.println("[1] New Reservation");
        System.out.println("[2] Modify Reservation");
        System.out.println("[3] Cancel Reservation");
        System.out.println("[4] View Reservations by Customer");
        System.out.println("[5] View Reservations by Aircraft");
        System.out.println("\n[0] Back to Main Menu\n");
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
        System.out.println("\n" + ReservationUtil.DIVIDER);
        System.out.println("  HANGAR SLOTS — SIZE LIMITS");
        System.out.println(ReservationUtil.DIVIDER);
        for (HangarSlot slot : ReservationUtil.getAllSlots()) {
            System.out.printf("  Slot %-3s | Category: %-7s | Max Wingspan: %5.1f m | Max Length: %5.1f m%n",
                    slot.getSlotCode(), slot.getCategory(),
                    slot.getMaxWingspan(), slot.getMaxLength());
        }
        System.out.println(ReservationUtil.DIVIDER);
        System.out.println();
    }

    private void printConfirmation(String customerName, String tailNumber,
                                   double wingspan, double length, String hangarSlot,
                                   LocalDate startDate, LocalDate endDate) {
        System.out.println("\n" + ReservationUtil.DIVIDER);
        System.out.println("  CONFIRM RESERVATION DETAILS");
        System.out.println(ReservationUtil.DIVIDER);
        System.out.printf("  Customer Name    : %s%n",              customerName);
        System.out.printf("  Aircraft Tail No : %s%n",              tailNumber);
        System.out.printf("  Wingspan / Length: %.1f m / %.1f m%n", wingspan, length);
        System.out.printf("  Hangar Slot      : %s%n",              hangarSlot);
        System.out.printf("  Start Date       : %s%n",              startDate.format(Reservation.DATE_FORMAT));
        System.out.printf("  End Date         : %s%n",              endDate.format(Reservation.DATE_FORMAT));
        System.out.println(ReservationUtil.DIVIDER);
    }

    private void printResult(String successMessage, ServiceResult result) {
        System.out.println("\n" + ReservationUtil.DIVIDER);
        if (result.isSuccess()) {
            System.out.println("  [SUCCESS] " + successMessage);
            System.out.println(ReservationUtil.DIVIDER);
            System.out.println(result.getData());
        } else {
            System.out.println("  [!] ERROR: " + result.getMessage());
            if (result.hasAlternatives()) {
                System.out.println("\n  Suggested available slots for your aircraft:\n");
                for (String alt : result.getAlternatives()) System.out.println(alt);
            }
        }
        System.out.println(ReservationUtil.DIVIDER);
    }

    private void printCancelled() {
        System.out.println("\n  Cancelled. Returning to menu...\n");
    }

    // ─── Input helpers (including new phone/email) ───────────────────────────
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
            System.out.println("  [!] Invalid number. Enter a value like 12.5 (0 to cancel).");
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

    private String promptHangarSlotOrKeep(String current) {
        while (true) {
            System.out.print("  Hangar slot [" + current + "] (Enter to keep, 0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0"))  return null;
            if (input.isEmpty())    return current;
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

    private LocalDate promptDateOrKeep(String prompt, LocalDate current) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.equals("0")) return null;
        if (input.isEmpty())   return current;
        LocalDate date = ReservationUtil.validateDate(input);
        if (date != null) return date;
        System.out.println("  [!] Invalid format. Keeping current: "
                + current.format(Reservation.DATE_FORMAT));
        return current;
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

    private LocalDate promptEndDateOrKeep(String prompt, LocalDate current, LocalDate start) {
        while (true) {
            LocalDate end = promptDateOrKeep(prompt, current);
            if (end == null) return null;
            if (ReservationUtil.validateEndDate(start, end)) return end;
            System.out.println("  [!] End date must be on or after start date.");
        }
    }

    private String promptStringOrKeep(String prompt, String current) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.equals("0")) return null;
        if (input.isEmpty())   return current;
        return input;
    }

    private String promptPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (ReservationUtil.isCancelled(input)) return null;
            if (PHONE_PATTERN.matcher(input).matches()) return input;
            System.out.println("  [!] Phone must be exactly 11 digits (0-9). Enter 0 to cancel.");
        }
    }

    private String promptEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (ReservationUtil.isCancelled(input)) return null;
            if (EMAIL_PATTERN.matcher(input).matches()) return input;
            System.out.println("  [!] Email must be a valid @gmail.com address. Enter 0 to cancel.");
        }
    }

    private void promptEnterToContinue() {
        System.out.print("  Press Enter to return to menu...");
        scanner.nextLine();
        System.out.println();
    }
}