package Hangar;

import Hangar.DAO.UserDAO;
import Hangar.Model.User;
import Hangar.UI.LoginMenu;
import Hangar.Service.AuthService;
import Hangar.Utils.PasswordUtils;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        // Optional: insert a default admin if no users exist
        if (userDAO.getAllUser().isEmpty()) {
            String defaultPassword = "admin123"; // change this!
            String hashed = PasswordUtils.hash(defaultPassword);
            User admin = new User.UserBuilder()
                    .username("admin")
                    .passwordHash(hashed)
                    .admin(true)
                    .build();
            userDAO.addUser(admin);
            System.out.println("Default admin user created.");
        }

        AuthService authService = new AuthService(userDAO);
        LoginMenu loginMenu = new LoginMenu(authService);
        loginMenu.display();
    }
}