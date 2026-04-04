package UI;

import Model.FrontDesk;
import Service.FrontDeskService;
import Util.FrontDeskUtil; // Import the utility
import java.util.Scanner;

public class FrontDeskUI {
    private final Scanner scanner = new Scanner(System.in);
    private final FrontDeskService service = new FrontDeskService();
    private String currentUser = "gian";
    private String currentRole = "FRONT DESK";

    public void start() {
        boolean running = true;
        while (running) {
            FrontDeskUtil.printHeader(currentUser, currentRole);
            System.out.println(" [1] Check In\n [2] Check Out\n [0] Logout");
            System.out.println("==================================================");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> handleProcess("CHECK IN");
                case "2" -> handleProcess("CHECK OUT");
                case "0" -> running = false;
                default -> System.out.println("\n[!] Invalid choice.");
            }
        }
    }

    private void handleProcess(String type) {
        FrontDeskUtil.printHeader(currentUser, currentRole);
        System.out.print("Enter Tail Number: ");
        String tailNum = scanner.nextLine();

        FrontDesk res = service.validateTailNumber(tailNum);

        if (res != null) {
            FrontDeskUtil.printDetails(type, res);
            System.out.printf("Confirm %s for RES-%04d? (Y/N): ", type, res.getReservationId());

            if (scanner.nextLine().equalsIgnoreCase("Y")) {
                if (type.equals("CHECK OUT")) {
                    FrontDeskUtil.printReceipt(res); // Use Utility Receipt
                    if (service.performCheckOut(res.getReservationId())) {
                        System.out.println("\n>>> TRANSACTION FINALIZED.");
                    }
                } else {
                    System.out.println("\n>>> CHECK-IN SUCCESSFUL.");
                }
            }
        } else {
            System.out.println("\n[!] ERROR: No record found for Tail Number [" + tailNum + "].");
        }
        FrontDeskUtil.pause(scanner);
    }
}