package Hangar.UI;

import Hangar.Model.User;
import java.util.Scanner;

public class MainMenu {

    private final User currentUser;
    private final Scanner scan = new Scanner(System.in);

    public MainMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void display() {
        while (true) {
            printMenu();
            System.out.print("Select option: ");

            int choice;
            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
                continue;
            }

            if (currentUser.isADMIN()) {
                switch (choice) {
                    case 1 -> System.out.println("[TODO] Customer Management");
                    case 2 -> System.out.println("[TODO] Aircraft Management");
                    case 3 -> System.out.println("[TODO] Reservation Management");
                    case 4 -> System.out.println("[TODO] Front Desk");
                    case 5 -> System.out.println("[TODO] Billing and Invoicing");
                    case 6 -> System.out.println("[TODO] Hangar and Slot Configuration");
                    case 7 -> manageUsers();
                    case 0 -> { return; }
                    default -> System.out.println("[ERROR] Invalid option. Please try again.");
                }
            } else {
                switch (choice) {
                    case 1 -> System.out.println("[TODO] Customer Management");
                    case 2 -> System.out.println("[TODO] Aircraft Management");
                    case 3 -> System.out.println("[TODO] Reservation Management");
                    case 4 -> System.out.println("[TODO] Front Desk");
                    case 5 -> System.out.println("[TODO] Billing and Invoicing");
                    case 0 -> { return; }
                    default -> System.out.println("[ERROR] Invalid option. Please try again.");
                }
            }
        }
    }

    private void printMenu() {
        System.out.println("\n========================================");
        System.out.println("   Aviation Hangar System");
        System.out.println("   User : " + currentUser.getUSERNAME());
        System.out.println("   Role : " + (currentUser.isADMIN() ? "ADMIN" : "FRONT DESK"));
        System.out.println("========================================");
        System.out.println("  1. Customer Management");
        System.out.println("  2. Aircraft Management");
        System.out.println("  3. Reservation Management");
        System.out.println("  4. Front Desk (Check-In / Check-Out)");
        System.out.println("  5. Billing and Invoicing");
        if (currentUser.isADMIN()) {
            System.out.println("  ---- Admin Only ----------------");
            System.out.println("  6. Hangar and Slot Configuration");
            System.out.println("  7. User Management");
        }
        System.out.println("  0. Logout");
        System.out.println("========================================");
    }

    private void manageUsers() {
        System.out.println("\n---- User Management ----");
        System.out.println("  1. View All Users");
        System.out.println("  2. Add User");
        System.out.println("  3. Delete User");
        System.out.println("  0. Back");
        System.out.print("Select option: ");

        int choice;
        try {
            choice = Integer.parseInt(scan.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid input.");
            return;
        }

        switch (choice) {
            case 1 -> System.out.println("[TODO] View all users");
            case 2 -> System.out.println("[TODO] Add user");
            case 3 -> System.out.println("[TODO] Delete user");
            case 0 -> { /* back */ }
            default -> System.out.println("[ERROR] Invalid option.");
        }
    }
}