package UI;

import Model.Customer;
import Service.CustomerService;
import Util.CustomerUtils;
import java.util.Random;
import java.util.Scanner;

public class CustomerUI {
    private final Scanner scanner = new Scanner(System.in);
    private final CustomerService service = new CustomerService();
    private final Random random = new Random();

    public void start() {
        while (true) {
            CustomerUtils.printHeader("CUSTOMER MANAGEMENT");
            System.out.println("[1] Add New Customer");
            System.out.println("[2] Search Customer");
            System.out.println("[3] Update Customer");
            System.out.println("[4] Delete Customer");
            System.out.println("\n[0] Logout");
            System.out.println("===============================================================");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> handleRegistration();
                case "2" -> handleSearch();
                case "3" -> handleUpdate();
                case "4" -> handleDelete();
                case "0" -> { return; }
                default -> System.out.println("[!] Invalid Selection.");
            }
        }
    }

    private void handleRegistration() {
        System.out.println("\n--- NEW REGISTRATION ---");
        String name = prompt("Enter Name: ", service::validateName);
        String phone = prompt("Enter Phone: ", service::validatePhone);
        String email = prompt("Enter Email: ", service::validateEmail);

        int newId = 10000 + random.nextInt(90000);
        Customer c = new Customer.Builder().setId(newId).setName(name).setPhone(phone).setEmail(email).build();

        if (service.getDAO().saveCustomer(c)) {
            System.out.println("\n>>> REGISTERED SUCCESSFULLY.");
            System.out.println(">>> CUSTOMER ID: " + newId);
        }
        CustomerUtils.pause(scanner);
    }

    private void handleSearch() {
        System.out.print("Enter Customer ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Customer c = service.getDAO().findById(id);
            if (c != null) {
                CustomerUtils.printDbStyleRow(c);
            } else System.out.println("\n[!] ID not found.");
        } catch (Exception e) { System.out.println("[!] Numeric ID required."); }
        CustomerUtils.pause(scanner);
    }

    private void handleUpdate() {
        System.out.print("Enter current ID to replace: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            if (service.getDAO().delete(id)) {
                System.out.println("[SYSTEM] ID found. Please provide new details.");
                handleRegistration(); // This will generate a new ID
            } else System.out.println("[!] ID not found.");
        } catch (Exception e) { System.out.println("[!] Numeric ID required."); }
    }

    private void handleDelete() {
        System.out.print("Enter ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            if (service.getDAO().delete(id)) {
                System.out.println("\n>>> RECORD DELETED SUCCESSFULLY.");
            } else {
                System.out.println("\n[!] ID not found.");
            }
        } catch (Exception e) { System.out.println("[!] Numeric ID required."); }
        CustomerUtils.pause(scanner);
    }

    private String prompt(String msg, java.util.function.Predicate<String> validator) {
        while (true) {
            System.out.print(msg);
            String input = scanner.nextLine().trim();
            if (validator.test(input)) return input;
            System.out.println("[!] Invalid input or duplicate entry.");
        }
    }
}