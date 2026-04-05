<<<<<<< HEAD
import UI.ReservationUI;
=======
package Hangar;

import UI.HangarSlotUI;
>>>>>>> origin/feature/Hangar-and-Slot-Configuration
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

<<<<<<< HEAD
        String loggedInUser = "gian";
        String userRole     = "FRONT DESK";

        new ReservationUI(scanner, loggedInUser, userRole).run();
=======
        String loggedInUser = "admin";
        String userRole     = "ADMIN";

        new HangarSlotUI(scanner, loggedInUser, userRole).run();
>>>>>>> origin/feature/Hangar-and-Slot-Configuration

        scanner.close();
    }
}