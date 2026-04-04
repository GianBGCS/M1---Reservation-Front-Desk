package Service;

import DAO.CustomerDAO;
import java.util.regex.Pattern;

public class CustomerService {
    private final CustomerDAO dao = new CustomerDAO();
    private static final Pattern NAME_P = Pattern.compile("^[A-Z][a-z]+( [A-Z][a-z]+)*$");
    private static final Pattern PHONE_P = Pattern.compile("^\\d{11}$");
    private static final Pattern EMAIL_P = Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$");

    public boolean validateName(String n) { return NAME_P.matcher(n).matches() && !dao.isNameDuplicate(n); }
    public boolean validatePhone(String p) { return PHONE_P.matcher(p).matches() && !dao.isPhoneDuplicate(p); }
    public boolean validateEmail(String e) { return EMAIL_P.matcher(e).matches() && !dao.isEmailDuplicate(e); }
    public CustomerDAO getDAO() { return dao; }
}