package Service;

import DAO.UserDAO;
import Model.User;
import Util.PasswordUtils;

public class AuthService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean login(String username, String password) {
        User found = userDAO.getUserByUsername(username);
        if (found == null) return false;

        String hashedInput = PasswordUtils.hash(password);
        if (!hashedInput.equals(found.getPasswordHash())) return false;

        currentUser = found;
        return true;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser()    { return currentUser; }
    public boolean isAuthenticated(){ return currentUser != null; }
    public boolean isAdmin()        { return currentUser != null && currentUser.isAdmin(); }
}