package exception;

/**
 * Custom checked exception thrown when an operation attempts to access or modify
 * a project using an identifier (Project ID) that does not correspond to any
 * existing project in the system's data store (e.g., {@link entity.list.ProjectList}).
 */
public class ProjectNotFoundException extends Exception {

    /**
     * Constructs a new {@code ProjectNotFoundException} with a default detail message.
     * The default message is "No project with this ID.".
     */
    public ProjectNotFoundException() {
        // Calls the superclass (Exception) constructor with a specific error message.
        super("No project with this ID.");
    }
}
