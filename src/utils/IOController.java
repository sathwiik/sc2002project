package utils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.InputMismatchException; // Import for specific exception
import java.util.Scanner;

/**
 * Utility class providing static methods for handling console input operations.
 * Encapsulates reading integers, lines of text, passwords (securely if possible),
 * and dates from standard input (System.in). Includes basic validation and
 * re-prompting mechanisms for invalid input.
 */
public class IOController {

    /**
     * Shared Scanner instance used for reading from System.in across all methods in this class.
     * Initialized once statically.
     */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Reads the next integer value from the console input.
     * If the user enters non-integer input, it catches the exception, prints an error message
     * prompting for a valid integer, consumes the invalid input line, and recursively calls itself
     * until a valid integer is entered. Also consumes the trailing newline character after
     * successfully reading an integer.
     *
     * @return The valid integer entered by the user.
     */
    public static int nextInt() {
        try {
            int ret = scanner.nextInt();
            scanner.nextLine(); // Consume the leftover newline character after reading the int
            return ret;
        } catch (InputMismatchException e) { // Catch specific exception
            System.out.print("Invalid input. Please enter a valid integer: ");
            scanner.nextLine(); // Consume the entire invalid input line
            return nextInt(); // Recursive call to retry input
        } catch (Exception e) { // Catch other potential scanner issues
             System.out.print("An unexpected error occurred reading integer. Please try again: ");
             scanner.nextLine(); // Consume line
             return nextInt(); // Recursive call
        }
    }

    /**
     * Reads the next complete line of text (up to the newline character) from the console input.
     *
     * @return The line of text entered by the user.
     */
    public static String nextLine() {
        return scanner.nextLine();
    }

    /**
     * Reads a password securely from the console, if available.
     * Attempts to use {@link System#console()} to read the password without echoing characters.
     * If the console is not available (e.g., running in some IDEs), it falls back to
     * reading a normal line of text using {@link #nextLine()}.
     *
     * @return The password entered by the user as a String. Be aware that the fallback method is not secure.
     */
    public static String readPassword() {
        String password;
        if (System.console() == null) {
            // Fallback if console is not available (less secure)
             System.out.println("Warning: Console not available. Password input will be visible."); // Added warning
            password = nextLine();
        } else {
            // Use console for secure password input (masks characters)
            char[] passwordChars = System.console().readPassword();
            password = new String(passwordChars);
            // Optionally clear the char array for security: java.util.Arrays.fill(passwordChars, ' ');
        }
        return password;
    }

    /**
     * Prompts the user to enter a date (day, month, year separately as integers)
     * and constructs a {@link LocalDate} object.
     * If the entered combination results in an invalid date (e.g., Feb 30th),
     * it catches the {@link DateTimeException}, prints an error message, and recursively
     * calls itself until a valid date is entered.
     *
     * @return The valid {@link LocalDate} entered by the user.
     */
    public static LocalDate nextDate() {
        Integer d, m, y;
        // Use the validated nextInt() method for reading components
        System.out.print("Enter Day (DD): ");
        d = nextInt();
        System.out.print("Enter Month (MM): ");
        m = nextInt();
        System.out.print("Enter Year (YYYY): ");
        y = nextInt();

        try {
            // Attempt to create the LocalDate object
            return LocalDate.of(y, m, d);
        } catch (DateTimeException e) {
            // Handle invalid date combinations (e.g., day out of range for month)
            System.out.println("Invalid Date Entered (" + y + "-" + m + "-" + d + "). Please enter a valid Date.");
            System.err.println("Date Error Details: " + e.getMessage()); // Optionally print more details
            return nextDate(); // Recursive call to retry input
        }
    }

    // Optional: Method to close the scanner if needed at application shutdown
    /**
     * Closes the underlying static Scanner used by this controller.
     * Should be called only when the application is shutting down and no further
     * console input is required.
     */
    public static void closeScanner() {
         scanner.close();
    }
}
