import DAO.UserDAO;
import Model.User;
import Service.AuthService;
import UI.LoginMenu;
import UI.MainMenu;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        UserDAO     userDAO     = new UserDAO();
        AuthService authService = new AuthService(userDAO);
        LoginMenu   loginMenu   = new LoginMenu(authService, scanner);

        User loggedInUser = loginMenu.display();

        new MainMenu(loggedInUser, scanner).display();

        scanner.close();
    }
}