package utils;

import boundary.ApplicantPage;
import boundary.ManagerPage;
import boundary.OfficerPage;

/**
 * Utility class providing static methods for controlling common console-based
 * User Interface (UI) behaviors within the application.
 * Includes functionality for clearing the console screen, defining standard UI elements
 * like separators, looping back to main menus for different user roles, and exiting the application.
 */
public class UIController {
    /**
     * Private constructor to prevent instantiation of this utility class.
     * Throwing an error ensures it's not accidentally called via reflection.
     */
    private UIController() {
        // Prevent instantiation
        throw new IllegalStateException("Utility class should not be instantiated");
    }
    /**
     * A constant string used as a visual separator in console output,
     * typically for separating different sections or headers.
     */
    public static final String LINE_SEPARATOR = "===================================================================";

    /**
     * Attempts to clear the console screen.
     * Uses different methods based on the detected operating system:
     * - Windows: Executes the 'cls' command via ProcessBuilder.
     * - Unix/Linux/macOS: Prints ANSI escape codes to clear the screen and move the cursor.
     * Catches and prints potential exceptions if screen clearing fails.
     */
    public static void clearPage() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                // For Windows Command Prompt/PowerShell
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Unix-like terminals supporting ANSI escape codes
                // \033[H moves cursor to top-left, \033[2J clears screen
                System.out.print("\033[H\033[2J");
                System.out.flush(); // Ensures the codes are sent to the terminal
            }
        } catch (Exception e) {
             // Handles potential IOExceptions, InterruptedExceptions, SecurityExceptions
            System.err.println("Error attempting to clear screen: " + e.getMessage());
            // Optionally print a few blank lines as a fallback
             System.out.println("\n\n\n");
        }
    }

    /**
     * Pauses execution, prompts the user to press Enter, and then navigates
     * back to the main Applicant menu by calling {@link ApplicantPage#allOptions()}.
     * Typically called after an applicant action is completed.
     */
    public static void loopApplicant() {
        System.out.println("\nPress ENTER to return to the applicant main menu.");
        IOController.nextLine(); // Wait for user to press Enter
        ApplicantPage.allOptions(); // Navigate back to the applicant menu
    }

    /**
     * Pauses execution, prompts the user to press Enter, and then navigates
     * back to the main Officer menu by calling {@link OfficerPage#allOptions()}.
     * Typically called after an officer action is completed.
     */
    public static void loopOfficer() {
        System.out.println("\nPress ENTER to return to the officer main menu.");
        IOController.nextLine(); // Wait for user to press Enter
        OfficerPage.allOptions(); // Navigate back to the officer menu
    }

    /**
     * Pauses execution, prompts the user to press Enter, and then navigates
     * back to the main Manager menu by calling {@link ManagerPage#allOptions()}.
     * Typically called after a manager action is completed.
     */
    public static void loopManager() {
        System.out.println("\nPress ENTER to return to the manager main menu.");
        IOController.nextLine(); // Wait for user to press Enter
        ManagerPage.allOptions(); // Navigate back to the manager menu
    }

    /**
     * Clears the console screen and terminates the Java Virtual Machine,
     * effectively exiting the application. Uses an exit code of 0, indicating
     * normal termination.
     */
    public static void exit() {
        System.out.println("Exiting application...");
        clearPage(); // Attempt to clear screen before exiting
        System.exit(0); // Terminate the application normally
    }
}
