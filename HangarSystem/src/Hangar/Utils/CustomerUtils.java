package Util;

import Model.Customer;
import java.util.Scanner;

public class CustomerUtils {

    public static void printHeader(String title, String loggedInUser, String userRole) {
        System.out.println("\n===============================================================");
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println("===============================================================");
        System.out.printf("   Logged in as: %-15s Role: %s %n", loggedInUser, userRole);
        System.out.println("===============================================================");
        System.out.println(title + "\n");
    }

    public static void printDbStyleRow(Customer c) {
        System.out.println("\nMATCH FOUND:");
        System.out.printf("id: %d | name: %s | phone: %s | email: %s %n",
                c.getId(), c.getName(), c.getPhone(), c.getEmail());
    }

    public static void pause(Scanner scanner) {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}