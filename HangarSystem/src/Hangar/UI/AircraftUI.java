package UI;

import DAO.AircraftDAO;
import Model.Aircraft;
import Service.AircraftService;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class AircraftUI {
    private static final Random random = new Random();
    private final Scanner scanner;
    private final String loggedInUser;
    private final String userRole;
    private final AircraftDAO repo;

    public AircraftUI(Scanner scanner, String loggedInUser, String userRole) {
        this.scanner = scanner;
        this.loggedInUser = loggedInUser;
        this.userRole = userRole;
        this.repo = new AircraftDAO();
    }

    public void run() {
        int choice = -1;

        while (choice != 0) {
            printMenu();
            choice = AircraftService.getValidInt(scanner, "Enter choice: ");

            switch (choice) {
                case 1 -> addNewAircraft();
                case 2 -> searchAircraft();
                case 3 -> updateAircraft();
                case 4 -> deleteAircraft();
                case 5 -> viewAllRecords();
                case 0 -> {
                    System.out.println("\nLogging out...");
                    repo.close();
                }
                default -> System.out.println("\n[!] Invalid choice.");
            }
        }
    }

    private void viewAllRecords() {
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

    private void addNewAircraft() {
        System.out.println("\n--- NEW REGISTRATION ---");
        String name = AircraftService.getValidUniqueName(scanner, repo, "Enter Name: ");
        String phone = AircraftService.getValidUniquePhone(scanner, repo, "Enter Phone: ");
        String email = AircraftService.getValidUniqueEmail(scanner, repo, "Enter Email: ");

        repo.saveAircraft(new Aircraft.Builder()
                .setId(10000 + random.nextInt(90000))
                .setName(name).setPhone(phone).setEmail(email).build());
    }

    private void searchAircraft() {
        boolean stay = true;
        while (stay) {
            int id = AircraftService.getValidInt(scanner, "Enter ID to search: ");
            repo.searchAircraftById(id);
            System.out.print("\nGo back to menu? [Y/N]: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y")) stay = false;
        }
    }

    private void updateAircraft() {
        int id = AircraftService.getValidInt(scanner, "Enter OLD ID to replace: ");
        if (repo.deleteOldRecord(id)) {
            System.out.println("[SYSTEM] ID found. Enter new details:");
            addNewAircraft();
        } else System.out.println("[!] ID not found.");
    }

    private void deleteAircraft() {
        int id = AircraftService.getValidInt(scanner, "Enter ID to delete: ");
        if (repo.deleteOldRecord(id)) System.out.println("[SUCCESS] Deleted.");
        else System.out.println("[!] ID not found.");
    }

    private void printMenu() {
        System.out.println("\n===============================================================");
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println("===============================================================");
        System.out.printf("   Logged in as: %-20s Role: %s %n", loggedInUser, userRole);
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