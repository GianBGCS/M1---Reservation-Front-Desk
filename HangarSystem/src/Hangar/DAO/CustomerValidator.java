import java.util.Scanner;
import java.util.regex.Pattern;

public class CustomerValidator {
    // Regex: First letter Capital, rest lowercase (Case Sensitive)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Z][a-z]+( [A-Z][a-z]+)*$");
    private static final Pattern GMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");

    public static int getValidInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("[!] ERROR: Numbers only.");
            scanner.next();
            System.out.print(prompt);
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }

    public static String getValidUniqueName(Scanner scanner, CustomerRepository repo, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!NAME_PATTERN.matcher(input).matches()) {
                System.out.println("[!] ERROR: Use Proper Case (e.g., 'Nataniel').");
            } else if (repo.isNameDuplicate(input)) {
                System.out.println("[!] ERROR: This name is already taken (Case-Insensitive match).");
            } else return input;
        }
    }

    public static String getValidUniquePhone(Scanner scanner, CustomerRepository repo, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!PHONE_PATTERN.matcher(input).matches()) {
                System.out.println("[!] ERROR: Must be exactly 11 digits.");
            } else if (repo.isPhoneDuplicate(input)) {
                System.out.println("[!] ERROR: Phone number already exists.");
            } else return input;
        }
    }

    public static String getValidUniqueEmail(Scanner scanner, CustomerRepository repo, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!GMAIL_PATTERN.matcher(input).matches()) {
                System.out.println("[!] ERROR: Must end with @gmail.com.");
            } else if (repo.isEmailDuplicate(input)) {
                System.out.println("[!] ERROR: Email already exists (Case-Insensitive match).");
            } else return input;
        }
    }
}