package UI;

import Model.HangarSlot;
import Service.HangarSlotService;
import Util.HangarSlotUtil;
import Util.HangarSlotUtil.MenuAction;
import Util.HangarSlotUtil.ServiceResult;

import java.util.List;
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

            // All case routing handled by Util
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

        // Ask if user wants to filter by hangar
        System.out.println("  [1] View all hangars and slots");
        System.out.println("  [2] View slots by specific hangar");
        System.out.println();
        System.out.print("  Enter choice (or 0 to cancel): ");
        String choice = scanner.nextLine().trim();

        if (HangarSlotUtil.isCancelled(choice)) { printCancelled(); return; }

        if (choice.equals("1")) {
            // View all
            ServiceResult<List<HangarSlot>> result = service.viewAllHangarsAndSlots();
            printSlotListResult(result, "ALL HANGARS AND SLOTS");

        } else if (choice.equals("2")) {
            // View by specific hangar
            String hangarName = promptString("  Enter hangar name: ");
            if (hangarName == null) { printCancelled(); return; }

            ServiceResult<List<HangarSlot>> result = service.viewSlotsByHangar(hangarName);
            printSlotListResult(result, "SLOTS IN HANGAR: " + hangarName.toUpperCase());

        } else {
            System.out.println("\n  [!] Invalid choice.\n");
        }

        promptEnterToContinue();
    }

    private void runCheckSlotAvailability() {
        printHeader();
        System.out.println("  CHECK SLOT AVAILABILITY");
        System.out.println();

        // Ask how user wants to check
        System.out.println("  [1] Check all available slots");
        System.out.println("  [2] Check available slots by hangar");
        System.out.println("  [3] Check a specific slot by slot code");
        System.out.println();
        System.out.print("  Enter choice (or 0 to cancel): ");
        String choice = scanner.nextLine().trim();

        if (HangarSlotUtil.isCancelled(choice)) { printCancelled(); return; }

        if (choice.equals("1")) {
            ServiceResult<List<HangarSlot>> result = service.checkAllSlotAvailability();
            printSlotListResult(result, "ALL AVAILABLE SLOTS");

        } else if (choice.equals("2")) {
            String hangarName = promptString("  Enter hangar name: ");
            if (hangarName == null) { printCancelled(); return; }

            ServiceResult<List<HangarSlot>> result = service.checkSlotAvailabilityByHangar(hangarName);
            printSlotListResult(result, "AVAILABLE SLOTS IN: " + hangarName.toUpperCase());

        } else if (choice.equals("3")) {
            String slotCode = promptSlotCode("  Enter slot code  : ");
            if (slotCode == null) { printCancelled(); return; }

            ServiceResult<HangarSlot> result = service.checkSlotByCode(slotCode);
            printSingleSlotResult(result);

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

    private void printSingleSlotResult(ServiceResult<HangarSlot> result) {
        System.out.println();
        System.out.println(HangarSlotUtil.DIVIDER);
        System.out.println("  SLOT AVAILABILITY RESULT");
        System.out.println(HangarSlotUtil.DIVIDER);
        if (result.isSuccess()) {
            HangarSlot slot = result.getData();
            System.out.println("  [AVAILABLE] Slot is free for reservation.");
            System.out.println();
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