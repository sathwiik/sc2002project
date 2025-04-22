package boundary;

import controller.AccountController;
import controller.ApplicantController;
import controller.FilterController;
import controller.ManagerProjectController;
import controller.ManagerRequestController;
import controller.OfficerProjectController;
import controller.OfficerRequestController;
import entity.user.Applicant;
import entity.user.Manager;
import entity.user.MaritalStatus;
import entity.user.Officer;
import entity.user.User;
import entity.user.UserType;
import exception.AlreadyRegisteredException;
import exception.InvalidUserFormatException;
import exception.PasswordIncorrectException;
import exception.UserNotFoundException;
import utils.IOController;
import utils.UIController;

/**
 * Represents the boundary layer for handling user login, registration, and initial interactions.
 * This class provides static methods to display the welcome screen, handle login attempts,
 * facilitate user registration, and allow users to change their passwords.
 */
public class LoginPage {

    /**
     * Displays the initial welcome screen and the main menu options (Login, Register, Change Password, Exit).
     * Prompts the user for their choice and directs them to the corresponding functionality.
     * Handles invalid input and loops until a valid choice is made or the user exits.
     */
    public static void welcome() {
        UIController.clearPage();
        System.out.println(UIController.LINE_SEPARATOR);
        // ASCII Art Welcome Message
        System.out.println(
                "\u001B[34m ____ _  _ __ __   ____    \u001B[0m____ __     \u001B[33m__ ____ ____ ____ ____ \n" + //
                "\u001B[34m(  _ / )( (  (  ) (    \\   \u001B[0m(_  _/  \\   \u001B[33m /  (  _ (    (  __(  _ \\\n" + //
                "\u001B[34m ) _ ) \\/ ()(/ (_/\\) D (     \u001B[0m)((  O )   \u001B[33m(  O )   /) D () _) )   /\n" + //
                "\u001B[34m(____\\____(__\\____(____/    \u001B[0m(__)\\__/    \u001B[33m\\__(__\\_(____(____(__\\_)\n" + //
                "\u001B[31m" +
                " _  _    __   __ _   __    ___  ____  _  _  ____  __ _  ____ \n" + //
                "( \\/ ) / _\\ (  ( \\ / _\\  / __)(  __)( \\/ )(  __)(  ( \\(_  _)\n" + //
                "/ \\/ \\/    \\/    //    \\( (_ \\ ) _) / \\/ \\ ) _) /    /  )(  \n" + //
                "\\_)(_/\\_/\\_/\\_)__)\\_/\\_/ \\___/(____)\\_)(_/(____)\\_)__) (__) \n" + //
                "\u001B[32m" +
                " ____  _  _  ____  ____  ____  _  _                      \n" + //
                "/ ___)( \\/ )/ ___)(_  _)(  __)( \\/ )                     \n" + //
                "\\___ \\ )  / \\___ \\  )(   ) _) / \\/ \\                     \n" + //
                "(____/(__/  (____/ (__) (____)\\_)(_/                     \n" + //
                "\u001B[0m");
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("Please enter your choice to continue.");
        System.out.println("\t1. Login");
        System.out.println("\t2. Register");
        System.out.println("\t3. Change Password");
        System.out.println("\t4. Exit");
        System.out.print("Your choice (1-4): ");
        int choice = -1;
        // Loop until a valid choice (1-4) is entered
        while (choice < 1 || choice > 4) { // Corrected loop condition
            choice = IOController.nextInt();
            switch (choice) {
                case 1 -> login();
                case 2 -> register();
                case 3 -> changePassword();
                case 4 -> UIController.exit();
                default -> { // Handle invalid numeric input
                    System.out.println("Invalid choice. Please try again.");
                    System.out.print("Your choice (1-4): "); // Re-prompt
                    choice = -1; // Reset choice to continue loop
                }
            }
        }
    }

    /**
     * Handles the user login process.
     * Prompts for user ID and password.
     * Calls the {@link AccountController#login(String, String)} method to authenticate the user.
     * If successful, initializes relevant controllers based on the user type (Manager, Officer, Applicant)
     * and navigates to the appropriate user page.
     * Catches and displays messages for {@link UserNotFoundException} and {@link PasswordIncorrectException}.
     * Allows the user to retry login or return to the welcome screen.
     */
    public static void login() {
        UIController.clearPage();
        System.out.print("Enter ID: ");
        String userID = IOController.nextLine();
        System.out.print("Enter password: ");
        String password = IOController.readPassword();
        try {
            FilterController.init(); // Initialize filters upon login attempt
            User user = AccountController.login(userID, password);
            ApplicantController.setApplicantID(userID); // Always set applicant context initially? Review logic if needed.

            if (user instanceof Manager) {
                // Set context for all relevant controllers for Manager
                OfficerProjectController.setOfficerID(userID);
                OfficerRequestController.setOfficerID(userID);
                ManagerProjectController.setManagerID(userID);
                ManagerRequestController.setManagerID(userID);
                ManagerPage.allOptions(); // Navigate to Manager page
            } else if (user instanceof Officer) {
                 // Set context for Officer controllers
                OfficerProjectController.setOfficerID(userID);
                OfficerRequestController.setOfficerID(userID);
                OfficerPage.allOptions(); // Navigate to Officer page
            } else if (user instanceof Applicant) {
                ApplicantPage.allOptions(); // Navigate to Applicant page
            }
            // No return needed here as navigation methods handle the flow
        } catch (UserNotFoundException | PasswordIncorrectException e) {
            System.out.println(e.getMessage());
            // Offer retry or go back
            System.out.println("Press ENTER to try again, or any other key to go back.");
            String choice = IOController.nextLine();
            if (choice.isEmpty()) {
                login(); // Retry login
            } else {
                welcome(); // Go back to welcome screen
            }
        }
    }

    /**
     * Handles the user registration process.
     * Prompts the user to select a user type (Applicant, Officer, Manager).
     * Prompts for necessary user details: ID, password, name, age, and marital status.
     * Calls {@link AccountController#register(UserType, String, String, String, int, MaritalStatus)} to create the user account.
     * Catches and displays messages for {@link InvalidUserFormatException} or {@link AlreadyRegisteredException}.
     * Allows the user to retry registration or return to the welcome screen upon failure.
     * Returns to the welcome screen upon successful registration.
     */
    public static void register() {
        UIController.clearPage();
        System.out.println("Enter User Type: ");
        System.out.println("\t1. Applicant");
        System.out.println("\t2. Officer");
        System.out.println("\t3. Manager");
        System.out.print("Your choice (1-3): ");
        UserType userType = null;
        // Loop until a valid user type is selected
        while (userType == null) {
            int type = IOController.nextInt();
            switch (type) {
                case 1 -> userType = UserType.APPLICANT;
                case 2 -> userType = UserType.OFFICER;
                case 3 -> userType = UserType.MANAGER;
                default -> {
                    System.out.println("Invalid choice. Please try again.");
                    System.out.print("Your choice (1-3): "); // Re-prompt
                }
            }
        }
        // Collect user details
        System.out.print("Enter ID: ");
        String userID = IOController.nextLine();
        System.out.print("Enter password: ");
        String password = IOController.readPassword();
        System.out.print("Enter name: ");
        String name = IOController.nextLine();
        System.out.print("Enter age: ");
        int age = IOController.nextInt();
        System.out.println("Enter marital status:");
        System.out.println("\t1. Single");
        System.out.println("\t2. Married");
        System.out.print("Your choice (1-2): ");
        MaritalStatus maritalStatus = null;
        // Loop until a valid marital status is selected
        while (maritalStatus == null) {
            int marital = IOController.nextInt();
            switch (marital) {
                case 1 -> maritalStatus = MaritalStatus.SINGLE;
                case 2 -> maritalStatus = MaritalStatus.MARRIED;
                default -> {
                    System.out.println("Invalid choice. Please try again.");
                    System.out.print("Your choice (1-2): "); // Re-prompt
                }
            }
        }

        try {
            // Attempt registration
            AccountController.register(userType, userID, password, name, age, maritalStatus);
            System.out.println("Registration successful!"); // Added success message
            System.out.println("Press ENTER to go back to the main menu.");
            IOController.nextLine(); // Consume the Enter key press
            welcome(); // Go back to welcome screen
        } catch (InvalidUserFormatException | AlreadyRegisteredException e) {
            System.out.println(e.getMessage());
            // Offer retry or go back on failure
            System.out.println("Press ENTER to try again, or any other key to go back.");
            String choice = IOController.nextLine();
            if (choice.isEmpty()) {
                register(); // Retry registration
            } else {
                welcome(); // Go back to welcome screen
            }
        }
    }

    /**
     * Handles the password change process for a user.
     * Prompts for the user ID and the current password.
     * Verifies the current password using {@link AccountController#checkPassword(String, String)}.
     * If correct, prompts for the new password.
     * Calls {@link AccountController#changePassword(String, String, String)} to update the password.
     * Catches and displays messages for {@link UserNotFoundException} or {@link PasswordIncorrectException}.
     * Allows the user to retry or return to the welcome screen upon failure or completion.
     */
    public static void changePassword() {
        UIController.clearPage();
        System.out.print("Enter ID: ");
        String userID = IOController.nextLine();
        System.out.print("Enter current password: "); // Clarified prompt
        String password = IOController.readPassword();
        try {
            // Check if the provided current password is correct
            boolean correctPassword = AccountController.checkPassword(userID, password);
            if (correctPassword) {
                System.out.print("Enter new password: ");
                String newPassword = IOController.readPassword();
                // Perform the password change
                AccountController.changePassword(userID, password, newPassword); // Assumes checkPassword already verified user exists
                System.out.println("Successfully changed password!");
                System.out.println("Press ENTER to go back to the main menu."); // Changed prompt
                IOController.nextLine(); // Consume the Enter key press
                welcome(); // Go back to welcome screen
            } else {
                // This case should ideally not be reached if checkPassword throws PasswordIncorrectException
                // Included for robustness, but AccountController logic might make it redundant.
                System.out.println("Password incorrect.");
                // Fall through to the retry/back logic below
            }

        } catch (UserNotFoundException | PasswordIncorrectException e) {
            System.out.println(e.getMessage());
            // Fall through to the retry/back logic below
        }

        // Offer retry or go back (reached after try/catch or if password check fails without exception)
        System.out.println("Press ENTER to try again, or any other key to go back.");
        String choice = IOController.nextLine();
        if (choice.isEmpty()) {
            changePassword(); // Retry password change
        } else {
            welcome(); // Go back to welcome screen
        }
    }
}
