package Hangar;

<<<<<<< HEAD
import UI.ReservationUI;
import UI.HangarSlotUI;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String loggedInUser = "gian";
        String userRole     = "FRONT DESK";

        new ReservationUI(scanner, loggedInUser, userRole).run();
        new HangarSlotUI(scanner, loggedInUser, userRole).run();

        scanner.close();
=======
import Hangar.DAO.UserDAO;
import Hangar.Model.User;
import Hangar.UI.LoginMenu;
import Hangar.Service.AuthService;
import Hangar.Utils.PasswordUtils;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        AuthService authService = new AuthService(userDAO);
        LoginMenu loginMenu = new LoginMenu(authService);
        loginMenu.display();
>>>>>>> ui/mainmenu
    }
}