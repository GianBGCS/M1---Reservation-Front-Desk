package Hangar;

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
    }
}