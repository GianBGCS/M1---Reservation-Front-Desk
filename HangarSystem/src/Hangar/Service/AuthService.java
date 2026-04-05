package Hangar.Service;

import Hangar.DAO.UserDAO;
import Hangar.Model.User;
import Hangar.Utils.PasswordUtils;

public class AuthService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean login(String username, String password) {
        User found = userDAO.getUserByUsername(username);

        if (found == null) {
            return false; // username not found
        }

        String hashedInput = PasswordUtils.hash(password);

        if (!hashedInput.equals(found.getPASSWORDHASH())) {
            return false; // wrong password
        }

        currentUser = found; // store in session
        return true;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isADMIN();
    }
}
