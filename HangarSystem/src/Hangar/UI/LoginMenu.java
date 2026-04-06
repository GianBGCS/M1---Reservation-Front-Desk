package Hangar.UI;

import Hangar.Model.User;
import Hangar.Service.AuthService;
import java.util.Scanner;

public class LoginMenu {

    private final AuthService authService;
    private final Scanner     scanner;

    public LoginMenu(AuthService authService, Scanner scanner) {
        this.authService = authService;
        this.scanner     = scanner;
    }

    public User display() {
        while (true) {
            System.out.println("========================================");
            System.out.println("   Aviation Hangar System — Login");
            System.out.println("========================================");

            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (authService.login(username, password)) {
                System.out.println("[OK] Welcome, "
                        + authService.getCurrentUser().getUSERNAME() + "!");
                return authService.getCurrentUser();
            }

            System.out.println("[ERROR] Invalid username or password.");
            System.out.println();
        }
    }
}
