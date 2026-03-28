package Hangar.UI;

import java.util.Scanner;
import java.util.Random;
import java.util.List;

public class AircraftUI {
    private static final AircraftDAO repo = new AircraftDAO();
    private static final Random random = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            printMenu("gian", "FRONT DESK");
            choice = AircraftService.getValidInt(scanner, "Enter choice: ");

            switch (choice) {
                case 1 -> addNewAircraft(scanner);
                case 2 -> searchAircraft(scanner);
                case 3 -> updateAircraft(scanner);
                case 4 -> deleteAircraft(scanner);
                case 5 -> viewAllRecords(scanner);
                case 0 -> { System.out.println("\nLogging out..."); repo.close(); }
                default -> System.out.println("\n[!] Invalid choice.");
            }
        }
    }

    private static void viewAllRecords(Scanner scanner) {
        List<Aircraft> list = repo.getAllAircrafts();
        System.out.println("\n\n\n==================== ALL AIRCRAFT RECORDS ====================");
        System.out.printf("%-10s | %-20s | %-15s | %-25s%n", "ID", "NAME", "PHONE", "EMAIL");
        System.out.println("--------------------------------------------------------------");

        if (list.isEmpty()) {
            System.out.println("            No records found in database.           ");
        } else {
            for (Aircraft a : list) {
                System.out.printf("%-10d | %-20s | %-15s | %-25s%n",
                        a.getId(), a.getName(), a.getPhone(), a.getEmail());
            }
        }
        System.out.println("==============================================================");

        boolean stay = true;
        while (stay) {
            System.out.print("\nGo back to Management Menu? [Y/N]: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y")) stay = false;
        }
    }

    private static void addNewAircraft(Scanner scanner) {
        System.out.println("\n--- NEW REGISTRATION ---");
        String name = AircraftService.getValidUniqueName(scanner, repo, "Enter Name: ");
        String phone = AircraftService.getValidUniquePhone(scanner, repo, "Enter Phone: ");
        String email = AircraftService.getValidUniqueEmail(scanner, repo, "Enter Email: ");

        repo.saveAircraft(new Aircraft.Builder()
                .setId(10000 + random.nextInt(90000))
                .setName(name).setPhone(phone).setEmail(email).build());
    }

    private static void searchAircraft(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            int id = AircraftService.getValidInt(scanner, "Enter ID to search: ");
            repo.searchAircraftById(id);
            System.out.print("\nGo back to menu? [Y/N]: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y")) stay = false;
        }
    }

    private static void updateAircraft(Scanner scanner) {
        int id = AircraftService.getValidInt(scanner, "Enter OLD ID to replace: ");
        if (repo.deleteOldRecord(id)) {
            System.out.println("[SYSTEM] ID found. Enter new details:");
            addNewAircraft(scanner);
        } else System.out.println("[!] ID not found.");
    }

    private static void deleteAircraft(Scanner scanner) {
        int id = AircraftService.getValidInt(scanner, "Enter ID to delete: ");
        if (repo.deleteOldRecord(id)) System.out.println("[SUCCESS] Deleted.");
        else System.out.println("[!] ID not found.");
    }

    public static void printMenu(String user, String role) {
        System.out.println("\n===============================================================");
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println("===============================================================");
        System.out.printf("   Logged in as: %-20s Role: %s %n", user, role);
        System.out.println("===============================================================");
        System.out.println("[1] Add New Aircraft");
        System.out.println("[2] Search Aircraft (by ID)");
        System.out.println("[3] Update Aircraft (Replace & Re-ID)");
        System.out.println("[4] Delete Aircraft");
        System.out.println("[5] View All Records");
        System.out.println("\n[0] Logout");
        System.out.println("===============================================================");
    }
}