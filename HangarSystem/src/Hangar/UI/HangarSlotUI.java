package UI;

import Model.HangarSlot;
import Model.Reservation;
import Service.HangarSlotService;
import Util.HangarSlotUtil;
import Util.HangarSlotUtil.MenuAction;
import Util.HangarSlotUtil.ServiceResult;
import DAO.ReservationDAO;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HangarSlotUI {

    private final Scanner           scanner;
    private final String            loggedInUser;
    private final String            userRole;
    private final HangarSlotService service;

    public HangarSlotUI(Scanner scanner, String loggedInUser, String userRole) {
        this.scanner      = scanner;
        this.loggedInUser = loggedInUser;
        this.userRole     = userRole;
        this.service      = new HangarSlotService();
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            MenuAction action = HangarSlotUtil.resolveMenuChoice(choice);

            switch (action) {
                case VIEW_ALL_HANGARS_AND_SLOTS:
                    runViewAllHangarsAndSlots();
                    break;
                case CHECK_SLOT_AVAILABILITY:
                    runCheckSlotAvailability();
                    break;
                case LOGOUT:
                    System.out.println("\n  Logging out...\n");
                    running = false;
                    break;
                case INVALID:
                    System.out.println("\n  [!] Invalid choice. Please enter 0-2.\n");
                    break;
            }
        }
    }

    private void runViewAllHangarsAndSlots() {
        printHeader();
        System.out.println("  VIEW ALL HANGARS AND SLOTS");
        System.out.println();

        System.out.println("  [1] View all hangars and slots");
        System.out.println("  [2] View slots by specific hangar");
        System.out.println();
        System.out.print("  Enter choice (or 0 to cancel): ");
        String choice = scanner.nextLine().trim();

        if (HangarSlotUtil.isCancelled(choice)) { printCancelled(); return; }

        if (choice.equals("1")) {
            ServiceResult<List<HangarSlot>> result = service.viewAllHangarsAndSlots();
            if (result.isSuccess()) {
                Map<String, String> occupiedDates = getOccupiedDatesForAllSlots();
                printSlotListWithDates(result.getData(), "ALL HANGARS AND SLOTS", occupiedDates);
            } else {
                System.out.println("\n  [!] " + result.getMessage());
            }
        } else if (choice.equals("2")) {
            String hangarName = promptString("  Enter hangar name: ");
            if (hangarName == null) { printCancelled(); return; }
            ServiceResult<List<HangarSlot>> result = service.viewSlotsByHangar(hangarName);
            if (result.isSuccess()) {
                Map<String, String> occupiedDates = getOccupiedDatesForAllSlots();
                printSlotListWithDates(result.getData(), "SLOTS IN HANGAR: " + hangarName.toUpperCase(), occupiedDates);
            } else {
                System.out.println("\n  [!] " + result.getMessage());
            }
        } else {
            System.out.println("\n  [!] Invalid choice.\n");
        }
        promptEnterToContinue();
    }

    private void runCheckSlotAvailability() {
        printHeader();
        System.out.println("  CHECK SLOT AVAILABILITY");
        System.out.println();

        System.out.println("  [1] Check all available slots");
        System.out.println("  [2] Check available slots by hangar");
        System.out.println("  [3] Check a specific slot by slot code");
        System.out.println();
        System.out.print("  Enter choice (or 0 to cancel): ");
        String choice = scanner.nextLine().trim();

        if (HangarSlotUtil.isCancelled(choice)) { printCancelled(); return; }

        if (choice.equals("1")) {
            ServiceResult<List<HangarSlot>> result = service.checkAllSlotAvailability();
            if (result.isSuccess()) {
                Map<String, String> upcomingReservations = getUpcomingReservationsBySlot();
                printAvailableSlotsWithReservations(result.getData(), "ALL AVAILABLE SLOTS", upcomingReservations);
            } else {
                printSlotListResult(result, "ALL AVAILABLE SLOTS");
            }
        } else if (choice.equals("2")) {
            String hangarName = promptString("  Enter hangar name: ");
            if (hangarName == null) { printCancelled(); return; }
            ServiceResult<List<HangarSlot>> result = service.checkSlotAvailabilityByHangar(hangarName);
            if (result.isSuccess()) {
                Map<String, String> upcoming = getUpcomingReservationsBySlot();
                printAvailableSlotsWithReservations(result.getData(), "AVAILABLE SLOTS IN: " + hangarName.toUpperCase(), upcoming);
            } else {
                printSlotListResult(result, "AVAILABLE SLOTS IN: " + hangarName.toUpperCase());
            }
        } else if (choice.equals("3")) {
            String slotCode = promptSlotCode("  Enter slot code: ");
            if (slotCode == null) { printCancelled(); return; }

            System.out.print("  Check availability for which date (yyyy-MM-dd, Enter for today): ");
            String dateInput = scanner.nextLine().trim();
            LocalDate checkDate;
            try {
                checkDate = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput, Reservation.DATE_FORMAT);
            } catch (Exception e) {
                System.out.println("  [!] Invalid date format. Using today.");
                checkDate = LocalDate.now();
            }

            ServiceResult<HangarSlot> result = service.checkSlotByCodeForDate(slotCode, checkDate);
            printSingleSlotResultForDate(result, checkDate);
        } else {
            System.out.println("\n  [!] Invalid choice.\n");
        }
        promptEnterToContinue();
    }

    private void printMenu() {
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.printf("  Logged in as: %-20s Role: %s%n", loggedInUser, userRole);
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println();
        System.out.println("HANGAR & SLOT CONFIGURATION");
        System.out.println();
        System.out.println("[1] View All Hangars and Slots");
        System.out.println("[2] Check Slot Availability");
        System.out.println();
        System.out.println("[0] Logout");
        System.out.println();
        System.out.println(HangarSlotUtil.DIVIDER);
    }

    private void printHeader() {
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.printf("  Logged in as: %-20s Role: %s%n", loggedInUser, userRole);
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println();
    }

    private void printSlotListResult(ServiceResult<List<HangarSlot>> result, String title) {
        System.out.println();
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println("  " + title);
        System.out.println(HangarSlotUtil.DIVIDER);
        if (result.isSuccess()) {
            for (HangarSlot slot : result.getData()) {
                System.out.println(slot);
            }
        } else {
            System.out.println("  [!] " + result.getMessage());
        }
        System.out.println(HangarSlotUtil.DIVIDER);
    }

    private void printSingleSlotResultForDate(ServiceResult<HangarSlot> result, LocalDate date) {
        System.out.println();
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println("  SLOT AVAILABILITY RESULT for " + date);
        System.out.println(HangarSlotUtil.DIVIDER);
        if (result.isSuccess()) {
            HangarSlot slot = result.getData();
            System.out.println("  [AVAILABLE] Slot is free on " + date);
            System.out.println(slot);
        } else {
            System.out.println("  [!] " + result.getMessage());
        }
        System.out.println(HangarSlotUtil.DIVIDER);
    }

    private void printCancelled() {
        System.out.println();
        System.out.println("  Cancelled. Returning to menu...");
        System.out.println();
    }

    // ── NEW helpers for occupied/upcoming reservation date display ─────────
    /**
     * Returns a map from slot code -> "start to end" for any ACTIVE reservation that
     * overlaps today (i.e., currently occupied).
     */
    private Map<String, String> getOccupiedDatesForAllSlots() {
        Map<String, String> map = new HashMap<>();
        List<Reservation> all = new ReservationDAO().findAll();
        LocalDate today = LocalDate.now();
        for (Reservation r : all) {
            if (Reservation.STATUS_ACTIVE.equals(r.getStatus())
                    && !r.getStartDate().isAfter(today)
                    && !r.getEndDate().isBefore(today)) {
                map.put(r.getHangarSlot(),
                        r.getStartDate().format(Reservation.DATE_FORMAT) + " to " +
                                r.getEndDate().format(Reservation.DATE_FORMAT));
            }
        }
        return map;
    }

    /**
     * Prints a list of hangar slots, adding the occupied date range if the slot is
     * currently occupied.
     */
    private void printSlotListWithDates(List<HangarSlot> slots, String title,
                                        Map<String, String> occupiedDates) {
        System.out.println();
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println("  " + title);
        System.out.println(HangarSlotUtil.DIVIDER);
        for (HangarSlot slot : slots) {
            System.out.print(slot);   // existing toString()
            String dates = occupiedDates.get(slot.getSlotCode());
            if (dates != null) {
                System.out.print("  [RESERVED: " + dates + "]");
            }
            System.out.println();
        }
        System.out.println(HangarSlotUtil.DIVIDER);
    }

    /**
     * Returns a map of slot code -> reservation date range for the FIRST ACTIVE
     * reservation that ends today or later (i.e., upcoming or ongoing).
     * Used to show future booking info even if the slot is currently available.
     */
    private Map<String, String> getUpcomingReservationsBySlot() {
        Map<String, String> map = new HashMap<>();
        List<Reservation> all = new ReservationDAO().findAll();
        LocalDate today = LocalDate.now();
        for (Reservation r : all) {
            if (Reservation.STATUS_ACTIVE.equals(r.getStatus())) {
                // If the reservation hasn't ended yet, it's "upcoming" or current
                if (!r.getEndDate().isBefore(today)) {
                    map.put(r.getHangarSlot(),
                            r.getStartDate().format(Reservation.DATE_FORMAT) + " to " +
                                    r.getEndDate().format(Reservation.DATE_FORMAT));
                }
            }
        }
        return map;
    }

    private void printAvailableSlotsWithReservations(List<HangarSlot> slots, String title,
                                                     Map<String, String> upcoming) {
        System.out.println();
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println("  " + title);
        System.out.println(HangarSlotUtil.DIVIDER);
        for (HangarSlot slot : slots) {
            System.out.print(slot);
            String dates = upcoming.get(slot.getSlotCode());
            if (dates != null) {
                System.out.print("  (Upcoming reservation: " + dates + ")");
            }
            System.out.println();
        }
        System.out.println(HangarSlotUtil.DIVIDER);
    }

    // ── Input helpers (unchanged) ───────────────────────────────────────────
    private String promptString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (HangarSlotUtil.isCancelled(input)) return null;
            if (HangarSlotUtil.validateString(input) != null) return input;
            System.out.println("  [!] Input cannot be empty. Enter 0 to cancel.");
        }
    }

    private String promptSlotCode(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (HangarSlotUtil.isCancelled(input)) return null;
            String code = HangarSlotUtil.validateSlotCode(input);
            if (code != null) return code;
            System.out.println("  [!] Slot code cannot be empty. Enter 0 to cancel.");
        }
    }

    private void promptEnterToContinue() {
        System.out.print("  Press Enter to return to menu...");
        scanner.nextLine();
        System.out.println();
    }
}