import java.util.Scanner;
import java.util.Random;
import java.util.List;

public class HangarSystem {
    private static final CustomerRepository repo = new CustomerRepository();
    private static final Random random = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            printMenu("gian", "FRONT DESK");
            choice = CustomerValidator.getValidInt(scanner, "Enter choice: ");

            switch (choice) {
                case 1 -> addNewCustomer(scanner);
                case 2 -> searchCustomer(scanner);
                case 3 -> updateCustomer(scanner);
                case 4 -> deleteCustomer(scanner);
                case 5 -> viewAllRecords(scanner);
                case 0 -> { System.out.println("\nLogging out..."); repo.close(); }
                default -> System.out.println("\n[!] Invalid choice.");
            }
        }
    }

    private static void viewAllRecords(Scanner scanner) {
        List<Customer> list = repo.getAllCustomers();

        System.out.println("\n\n\n==================== ALL CUSTOMER RECORDS ====================");
        System.out.printf("%-10s | %-20s | %-15s | %-25s%n", "ID", "NAME", "PHONE", "EMAIL");
        System.out.println("--------------------------------------------------------------");

        if (list.isEmpty()) {
            System.out.println("            No records found in database.           ");
        } else {
            for (Customer c : list) {
                System.out.printf("%-10d | %-20s | %-15s | %-25s%n",
                        c.getId(), c.getName(), c.getPhone(), c.getEmail());
            }
        }
        System.out.println("==============================================================");

        boolean stay = true;
        while (stay) {
            System.out.print("\nGo back to Customer Management Menu? [Y/N]: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y")) stay = false;
        }
    }

    private static void addNewCustomer(Scanner scanner) {
        System.out.println("\n--- NEW REGISTRATION ---");
        String name = CustomerValidator.getValidUniqueName(scanner, repo, "Enter Name: ");
        String phone = CustomerValidator.getValidUniquePhone(scanner, repo, "Enter Phone: ");
        String email = CustomerValidator.getValidUniqueEmail(scanner, repo, "Enter Email: ");

        repo.saveCustomer(new Customer.Builder()
                .setId(10000 + random.nextInt(90000))
                .setName(name).setPhone(phone).setEmail(email).build());
    }

    private static void searchCustomer(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            int id = CustomerValidator.getValidInt(scanner, "Enter ID to search: ");
            repo.searchCustomerById(id);
            System.out.print("\nGo back to menu? [Y/N]: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y")) stay = false;
        }
    }

    private static void updateCustomer(Scanner scanner) {
        int id = CustomerValidator.getValidInt(scanner, "Enter OLD ID to replace: ");
        if (repo.deleteOldRecord(id)) {
            System.out.println("[SYSTEM] ID found. Enter new details:");
            addNewCustomer(scanner);
        } else System.out.println("[!] ID not found.");
    }

    private static void deleteCustomer(Scanner scanner) {
        int id = CustomerValidator.getValidInt(scanner, "Enter ID to delete: ");
        if (repo.deleteOldRecord(id)) System.out.println("[SUCCESS] Deleted.");
        else System.out.println("[!] ID not found.");
    }

    public static void printMenu(String user, String role) {
        System.out.println("\n===============================================================");
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println("===============================================================");
        System.out.printf("   Logged in as: %-20s Role: %s %n", user, role);
        System.out.println("===============================================================");
        System.out.println("[1] Add New Customer");
        System.out.println("[2] Search Customer (by ID)");
        System.out.println("[3] Update Customer (Replace & Re-ID)");
        System.out.println("[4] Delete Customer");
        System.out.println("[5] View All Records");
        System.out.println("\n[0] Logout");
        System.out.println("===============================================================");
    }
}