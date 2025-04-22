package controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import boundary.LoginPage;
import entity.list.ApplicantList;
import entity.list.ManagerList;
import entity.list.OfficerList;
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
import utils.UIController;

/**
 * Controller responsible for managing user accounts and authentication.
 * Handles registration, login, logout, password changes, password hashing,
 * user lookup across different user types (Applicant, Officer, Manager),
 * and stores the ID of the currently logged-in user.
 */
public class AccountController {

    /**
     * Stores the user ID of the currently logged-in user.
     * Null if no user is logged in.
     */
    private static String userID;

    /**
     * Gets the user ID of the currently authenticated user.
     *
     * @return The user ID string, or null if no user is logged in.
     */
    public static String getUserID() {
        return userID;
    }

    /**
     * Sets the user ID for the currently authenticated user.
     * Typically called upon successful login and set to null upon logout.
     *
     * @param ID The user ID to set.
     */
    public static void setUserID(String ID) {
        userID = ID;
    }

    /**
     * Registers a new user in the system.
     * Checks the user ID format and if the ID is already taken.
     * Hashes the password before storing.
     * Adds the user to the appropriate lists based on their UserType:
     * - All users are added to ApplicantList.
     * - Officers and Managers are also added to OfficerList.
     * - Managers are additionally added to ManagerList.
     *
     * @param userType      The type of user to register (APPLICANT, OFFICER, MANAGER).
     * @param userID        The user ID for the new account. Must match specific format.
     * @param password      The plain text password for the new account.
     * @param name          The name of the user.
     * @param age           The age of the user.
     * @param maritalStatus The marital status of the user.
     * @throws InvalidUserFormatException If the userID does not match the required format (e.g., "^[ST]\\d{7}[A-Z]$").
     * @throws AlreadyRegisteredException If a user with the given userID already exists.
     */
    public static void register(UserType userType, String userID, String password, String name, int age, MaritalStatus maritalStatus) throws InvalidUserFormatException, AlreadyRegisteredException {
        checkUserID(userID); // Validate format first
        try {
            findUser(userID); // Check if user already exists
            // If findUser doesn't throw, it means user exists
            throw new AlreadyRegisteredException();
        } catch (UserNotFoundException e) {
            // User does not exist, proceed with registration
            String hashedPassword = hashPassword(password); // Hash the password once

            // Always add as Applicant (base user type?)
            ApplicantList.getInstance().add(new Applicant(userID, name, hashedPassword, age, maritalStatus));

            // Add to OfficerList if Officer or Manager
            if (userType != UserType.APPLICANT) {
                OfficerList.getInstance().add(new Officer(userID, name, hashedPassword, age, maritalStatus));
            }
            // Add to ManagerList if Manager
            if (userType == UserType.MANAGER) {
                ManagerList.getInstance().add(new Manager(userID, name, hashedPassword, age, maritalStatus));
            }
            System.out.println("Registration completed successfully for User ID: " + userID); // Confirmation
        }
    }

    /**
     * Finds a user by their user ID across all user type lists.
     * Searches in the order: ManagerList, OfficerList, ApplicantList.
     * Returns the first user found with the matching ID.
     *
     * @param userID The user ID to search for.
     * @return The User object (Manager, Officer, or Applicant) if found.
     * @throws UserNotFoundException If no user with the given ID is found in any list.
     */
    public static User findUser(String userID) throws UserNotFoundException {
        // Search Managers first
        for (Manager m : ManagerList.getInstance().getAll()) {
            if (m.getUserID().equals(userID)) return m;
        }
        // Then search Officers
        for (Officer o : OfficerList.getInstance().getAll()) {
            if (o.getUserID().equals(userID)) return o;
        }
        // Finally search Applicants
        for (Applicant a : ApplicantList.getInstance().getAll()) {
            if (a.getUserID().equals(userID)) return a;
        }
        // If not found in any list
        throw new UserNotFoundException();
    }

    /**
     * Checks if the provided plain text password matches the stored hashed password for the given user ID.
     *
     * @param userID   The user ID whose password needs checking.
     * @param password The plain text password to check.
     * @return true if the password matches.
     * @throws UserNotFoundException      If the user ID does not exist.
     * @throws PasswordIncorrectException If the provided password does not match the stored hash.
     */
    public static boolean checkPassword(String userID, String password) throws UserNotFoundException, PasswordIncorrectException {
        User user = findUser(userID); // Find the user first (throws UserNotFoundException if not found)
        String providedHash = hashPassword(password); // Hash the provided password
        if (providedHash.equals(user.getHashedPassword())) {
            return true; // Passwords match
        } else {
            throw new PasswordIncorrectException(); // Passwords do not match
        }
    }

    /**
     * Attempts to log in a user with the provided credentials.
     * Finds the user by ID and checks if the provided password is correct.
     * If successful, sets the currently logged-in user ID and returns the User object.
     *
     * @param userID   The user ID attempting to log in.
     * @param password The plain text password provided for login.
     * @return The User object (Manager, Officer, or Applicant) upon successful login.
     * @throws UserNotFoundException      If the user ID does not exist.
     * @throws PasswordIncorrectException If the provided password does not match.
     */
    public static User login(String userID, String password) throws UserNotFoundException, PasswordIncorrectException {
        // findUser is implicitly called by checkPassword
        if (checkPassword(userID, password)) { // Checks both user existence and password correctness
            setUserID(userID); // Set the current user session
            return findUser(userID); // Return the logged-in user object
        } else {
            // This path should theoretically not be reached due to checkPassword throwing,
            // but included for logical completeness.
            throw new PasswordIncorrectException();
        }
    }

    /**
     * Logs out the currently authenticated user.
     * Clears the stored user ID and navigates back to the login page.
     */
    public static void logout() {
        System.out.println("Logging out user: " + getUserID()); // Optional logging
        setUserID(null); // Clear the current user session
        UIController.clearPage();
        LoginPage.welcome(); // Return to the login screen
    }

    /**
     * Hashes a plain text password using SHA3-256 algorithm and encodes the result in Base64.
     * This is a one-way process used for securely storing passwords.
     *
     * @param password The plain text password to hash.
     * @return A Base64 encoded string representing the hashed password.
     * @throws RuntimeException if the SHA3-256 algorithm is not available in the Java environment.
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            // This should generally not happen in standard Java environments
            // but indicates a critical configuration issue if it does.
            System.err.println("FATAL ERROR: SHA3-256 algorithm not found!");
            throw new RuntimeException("Error: SHA3-256 algorithm not found.", e);
        }
    }

    /**
     * Checks if the provided user ID conforms to the required format.
     * The expected format is defined by the regular expression "^[ST]\\d{7}[A-Z]$".
     * (Starts with S or T, followed by 7 digits, ending with an uppercase letter).
     *
     * @param userID The user ID string to validate.
     * @return true if the userID matches the format.
     * @throws InvalidUserFormatException If the userID does not match the required format.
     */
    private static boolean checkUserID(String userID) throws InvalidUserFormatException {
        // Regex: S or T start, 7 digits, 1 uppercase letter end.
        if (userID != null && userID.matches("^[ST]\\d{7}[A-Z]$")) {
            return true;
        }
        throw new InvalidUserFormatException();
    }

    /**
     * Changes the password for a given user ID after verifying the old password.
     * Finds the user, checks the old password, hashes the new password,
     * and updates the user object in all relevant user lists (Applicant, Officer, Manager).
     * Note: Assumes user objects in different lists (e.g., ApplicantList, OfficerList)
     * might need separate updates even if they represent the same underlying user.
     *
     * @param userID      The user ID whose password needs changing.
     * @param oldPassword The current plain text password for verification.
     * @param newPassword The new plain text password to set.
     * @throws UserNotFoundException      If the user ID does not exist.
     * @throws PasswordIncorrectException If the provided oldPassword does not match.
     */
    public static void changePassword(String userID, String oldPassword, String newPassword) throws UserNotFoundException, PasswordIncorrectException {
        // checkPassword verifies user existence and old password correctness
        if (checkPassword(userID, oldPassword)) {
            User user = findUser(userID); // Get the user object
            String newHashedPassword = hashPassword(newPassword); // Hash the new password
            user.setHashedPassoword(newHashedPassword); // Update the password hash in the user object

            // Update the user object in all relevant lists where they might exist.
            // This assumes updates need to happen in each list potentially holding the user.
            // Might need refinement based on how user lists and updates are truly managed.
            if (user instanceof Applicant) { // Update ApplicantList regardless
                 ApplicantList.getInstance().update(userID, (Applicant)user);
            }
             // Also update OfficerList if they are an Officer or Manager
            if (user instanceof Officer) { // This covers both Officer and Manager types
                 OfficerList.getInstance().update(userID, (Officer)user);
            }
             // Also update ManagerList if they are a Manager
            if (user instanceof Manager) {
                 ManagerList.getInstance().update(userID, (Manager)user);
            }

            System.out.println("Password changed successfully for User ID: " + userID); // Confirmation
        }
        // No else needed, checkPassword throws PasswordIncorrectException if old password fails
    }
}
