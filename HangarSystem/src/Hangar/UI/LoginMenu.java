package Hangar.UI;

import Hangar.Model.User;
import Hangar.Service.AuthService;
import java.util.Scanner;

public class LoginMenu {

    Scanner scan = new Scanner(System.in);

    private final AuthService authService;

    public LoginMenu(AuthService authService) {
        this.authService = authService;
    }

    public User display() {
        while (true) {
            System.out.println("========================================");
            System.out.println("   Aviation Hangar System — Login");
            System.out.println("========================================");



            System.out.println("Username: ");
            String username = scan.nextLine();

            System.out.println("Password: ");
            String password = scan.nextLine();

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
