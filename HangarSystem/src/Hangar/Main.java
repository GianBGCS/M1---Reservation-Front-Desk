package Hangar;

import UI.HangarSlotUI;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String loggedInUser = "admin";
        String userRole     = "ADMIN";

        new HangarSlotUI(scanner, loggedInUser, userRole).run();

        scanner.close();
    }
}