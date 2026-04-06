package src.Hangar;

import Hangar.DAO.UserDAO;
import Hangar.Model.User;
import Hangar.Service.AuthService;
import Hangar.UI.LoginMenu;
import Hangar.UI.MainMenu;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        UserDAO      userDAO     = new UserDAO();
        AuthService  authService = new AuthService(userDAO);
        LoginMenu    loginMenu   = new LoginMenu(authService, scanner);

        User loggedInUser = loginMenu.display();

        new MainMenu(loggedInUser, scanner).display();

        scanner.close();
    }
}
