package Hangar.UI;

import Hangar.Model.User;
import Hangar.DAO.UserDAO;
import UI.CustomerUI;
import UI.ReservationUI;
import UI.HangarSlotUI;
import java.util.Scanner;
import java.util.List;

public class MainMenu {

    private final User    currentUser;
    private final Scanner scanner;
    private final String  username;
    private final String  role;

    public MainMenu(User currentUser, Scanner scanner) {
        this.currentUser = currentUser;
        this.scanner     = scanner;
        this.username    = currentUser.getUSERNAME();
        this.role        = currentUser.isADMIN() ? "ADMIN" : "FRONT DESK";
    }

    public void display() {
        while (true) {
            printMenu();
            System.out.print("Select option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
                continue;
            }

            if (currentUser.isADMIN()) {
                switch (choice) {
                    case 1 -> new CustomerUI(scanner, username, role).start();
                    case 2 -> System.out.println("[TODO] Aircraft Management");
                    case 3 -> new ReservationUI(scanner, username, role).run();
                    case 4 -> System.out.println("[TODO] Front Desk (Check-In / Check-Out)");
                    case 5 -> System.out.println("[TODO] Billing and Invoicing");
                    case 6 -> new HangarSlotUI(scanner, username, role).run();
                    case 7 -> manageUsers();
                    case 0 -> { System.out.println("Logging out..."); return; }
                    default -> System.out.println("[ERROR] Invalid option. Please try again.");
                }
            } else {
                switch (choice) {
                    case 1 -> new CustomerUI(scanner, username, role).start();
                    case 2 -> System.out.println("[TODO] Aircraft Management");
                    case 3 -> new ReservationUI(scanner, username, role).run();
                    case 4 -> System.out.println("[TODO] Front Desk (Check-In / Check-Out)");
                    case 5 -> System.out.println("[TODO] Billing and Invoicing");
                    case 0 -> { System.out.println("Logging out..."); return; }
                    default -> System.out.println("[ERROR] Invalid option. Please try again.");
                }
            }
        }
    }

    private void printMenu() {
        System.out.println("\n========================================");
        System.out.println("   Aviation Hangar System");
        System.out.println("   User : " + username);
        System.out.println("   Role : " + role);
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
        UserDAO userDAO = new UserDAO();

        System.out.println("\n---- User Management ----");
        System.out.println("  1. View All Users");
        System.out.println("  2. Add User");
        System.out.println("  3. Delete User");
        System.out.println("  0. Back");
        System.out.print("Select option: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid input.");
            return;
        }

        switch (choice) {
            case 1 -> {
                var users = userDAO.getAllUser();
                if (users.isEmpty()) {
                    System.out.println("No users found.");
                } else {
                    users.forEach(u -> System.out.printf(
                            "  Username: %-20s Role: %s%n",
                            u.getUSERNAME(), u.isADMIN() ? "ADMIN" : "FRONT DESK"));
                }
            }
            case 2 -> System.out.println("[TODO] Add user");
            case 3 -> System.out.println("[TODO] Delete user");
            case 0 -> { /* back */ }
            default -> System.out.println("[ERROR] Invalid option.");
        }
    }
}