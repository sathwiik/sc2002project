package exception;

/**
 * Custom checked exception thrown when an operation attempts to find, authenticate, or operate on
 * a user using an identifier (User ID) that does not correspond to any existing user
 * in the system's data stores (e.g., {@link entity.list.ApplicantList}, {@link entity.list.OfficerList}, {@link entity.list.ManagerList}).
 */
public class UserNotFoundException extends Exception {

    /**
     * Constructs a new {@code UserNotFoundException} with a default detail message.
     * The default message is "No user with this ID.".
     */
    public UserNotFoundException() {
        // Calls the superclass (Exception) constructor with a specific error message.
        super("No user with this ID.");
    }
}
