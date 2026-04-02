import UI.ReservationUI;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String loggedInUser = "gian";
        String userRole     = "FRONT DESK";

        new ReservationUI(scanner, loggedInUser, userRole).run();

        scanner.close();
    }
}