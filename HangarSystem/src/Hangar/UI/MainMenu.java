package UI;

import DAO.UserDAO;
import Model.User;
import Service.AuthService;
import Util.PasswordUtils;
import java.util.Scanner;

public class MainMenu {

    private final User    currentUser;
    private final Scanner scanner;
    private final String  username;
    private final String  role;

    public MainMenu(User currentUser, Scanner scanner) {
        this.currentUser = currentUser;
        this.scanner     = scanner;
        this.username    = currentUser.getUsername();
        this.role        = currentUser.isAdmin() ? "ADMIN" : "FRONT DESK";
    }

    public void display() {
        while (true) {
            printMenu();
            System.out.print("Select option: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
                continue;
            }

            if (currentUser.isAdmin()) {
                switch (choice) {
                    case 1 -> new UI.CustomerUI(scanner, username, role).start();
                    case 2 -> new UI.ReservationUI(scanner, username, role).run();
                    case 3 -> new UI.FrontDeskUI(scanner, username, role).start();
                    case 4 -> new UI.HangarSlotUI(scanner, username, role).run();
                    case 5 -> manageUsers();
                    case 6 -> new UI.BillingUI(scanner, username, role).start();
                    case 0 -> { System.out.println("Logging out..."); return; }
                    default -> System.out.println("[ERROR] Invalid option.");
                }
            } else {
                switch (choice) {
                    case 1 -> new UI.CustomerUI(scanner, username, role).start();
                    case 2 -> new UI.ReservationUI(scanner, username, role).run();
                    case 3 -> new UI.FrontDeskUI(scanner, username, role).start();
                    case 4 -> new UI.BillingUI(scanner, username, role).start();
                    case 0 -> { System.out.println("Logging out..."); return; }
                    default -> System.out.println("[ERROR] Invalid option.");
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
        System.out.println("  2. Reservation Management");
        System.out.println("  3. Front Desk (Check-In / Check-Out)");
        if (currentUser.isAdmin()) {
            System.out.println("  ---- Admin Only ----------------");
            System.out.println("  4. Hangar and Slot Configuration");
            System.out.println("  5. User Management");
            System.out.println("  6. Billing and Invoices");
        } else {
            System.out.println("  4. Billing and Invoices");
        }
        System.out.println("  0. Logout");
        System.out.println("========================================");
    }

    private void manageUsers() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("   User Management");
            System.out.println("========================================");
            System.out.println("  1. View All Users");
            System.out.println("  2. Add User");
            System.out.println("  3. Delete User");
            System.out.println("  0. Back");
            System.out.println("========================================");
            System.out.print("Select option: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> viewAllUsers();
                case "2" -> addUser();
                case "3" -> deleteUser();
                case "0" -> { return; }
                default  -> System.out.println("[ERROR] Invalid option.");
            }
        }
    }

    private void viewAllUsers() {
        UserDAO userDAO = new UserDAO();
        var users = userDAO.getAllUser();
        System.out.println("\n--- All Users ---");
        if (users.isEmpty()) {
            System.out.println("  No users found.");
        } else {
            System.out.printf("  %-20s %s%n", "Username", "Role");
            System.out.println("  " + "-".repeat(35));
            for (User u : users) {
                System.out.printf("  %-20s %s%n",
                        u.getUsername(),
                        u.isAdmin() ? "ADMIN" : "FRONT DESK");
            }
        }
        pause();
    }

    private void addUser() {
        UserDAO userDAO = new UserDAO();
        System.out.println("\n--- Add New User ---");

        String username;
        while (true) {
            System.out.print("  Enter username (0 to cancel): ");
            username = scanner.nextLine().trim();
            if (username.equals("0")) { System.out.println("  Cancelled."); return; }
            if (username.isEmpty())   { System.out.println("  [!] Username cannot be empty."); continue; }
            if (userDAO.existsByUsername(username)) {
                System.out.println("  [!] Username '" + username + "' already exists.");
                continue;
            }
            break;
        }

        String password;
        while (true) {
            System.out.print("  Enter password        : ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) { System.out.println("  [!] Password cannot be empty."); continue; }
            System.out.print("  Confirm password      : ");
            String confirm = scanner.nextLine().trim();
            if (!password.equals(confirm)) { System.out.println("  [!] Passwords do not match."); continue; }
            break;
        }

        System.out.print("  Grant Admin access? (Y/N): ");
        boolean isAdmin = scanner.nextLine().trim().equalsIgnoreCase("Y");

        User newUser = new User.UserBuilder()
                .username(username)
                .passwordHash(PasswordUtils.hash(password))
                .admin(isAdmin)
                .build();

        UserDAO dao = new UserDAO();
        if (dao.addUser(newUser) != null) {
            System.out.println("\n>>> User '" + username + "' added as "
                    + (isAdmin ? "ADMIN" : "FRONT DESK") + ".");
        } else {
            System.out.println("\n[!] Failed to add user.");
        }
        pause();
    }

    private void deleteUser() {
        UserDAO userDAO = new UserDAO();
        System.out.println("\n--- Delete User ---");
        System.out.print("  Enter username to delete (0 to cancel): ");
        String username = scanner.nextLine().trim();

        if (username.equals("0")) { System.out.println("  Cancelled."); return; }

        if (username.equals(currentUser.getUsername())) {
            System.out.println("  [!] You cannot delete your own account.");
            pause();
            return;
        }

        if (!userDAO.existsByUsername(username)) {
            System.out.println("  [!] User '" + username + "' not found.");
            pause();
            return;
        }

        System.out.print("  Confirm delete '" + username + "'? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  Cancelled.");
            return;
        }

        if (userDAO.deleteByUsername(username)) {
            System.out.println(">>> User '" + username + "' deleted successfully.");
        } else {
            System.out.println("[!] Failed to delete user.");
        }
        pause();
    }

    private void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}