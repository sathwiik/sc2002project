package entity.user;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Manager user in the housing application system.
 * Implements the {@link User} interface.
 * Stores personal details (ID, name, hashed password, age, marital status)
 * and maintains a list of project IDs for the projects they are responsible for managing.
 */
public class Manager implements User {

    /** The unique identifier for the manager (User ID). */
    private String managerID;
    /** The name of the manager. */
    private String name;
    /** The securely hashed password for the manager's account. */
    private String hashedPassword;
    /** The age of the manager. */
    private int age;
    /** The marital status of the manager. */
    private MaritalStatus maritalStatus;
    /** A list of project IDs that this manager is responsible for. */
    private List<String> project;

    /**
     * Default constructor.
     * Initializes a new Manager instance with default values (e.g., empty strings, zero age).
     * The list of managed projects is initialized as an empty ArrayList.
     * Note: Explicit call to super() is redundant here.
     */
    public Manager() {
        // super(); // Redundant call to Object constructor
        this.managerID = ""; // Default value
        this.name = ""; // Default value
        this.hashedPassword = ""; // Default value
        this.age = 0; // Default value
        // Consider setting a default maritalStatus if desired, e.g., MaritalStatus.SINGLE
        this.project = new ArrayList<>(); // Initialize project list
    }

    /**
     * Parameterized constructor to create a Manager with specified initial personal details.
     * The list of managed projects is initialized as an empty ArrayList; projects are typically assigned later.
     *
     * @param userID         The unique user ID for the manager.
     * @param name           The name of the manager.
     * @param hashedPassword The hashed password for the account.
     * @param age            The age of the manager.
     * @param maritalStatus  The marital status of the manager.
     */
    public Manager(String userID, String name, String hashedPassword, int age, MaritalStatus maritalStatus) {
        this.managerID = userID;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.project = new ArrayList<>(); // Initialize project list
    }

    /**
     * Gets the manager's user ID.
     * Implements {@link User#getUserID()}.
     * @return The user ID string.
     */
    @Override
    public String getUserID() {
        return managerID;
    }

    /**
     * Sets the manager's user ID.
     * @param userID The user ID string.
     */
    public void setUserID(String userID) {
        this.managerID = userID;
    }

    /**
     * Gets the manager's name.
     * Implements {@link User#getName()}.
     * @return The name string.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the manager's name.
     * @param name The name string.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the manager's hashed password.
     * Implements {@link User#getHashedPassword()}.
     * @return The Base64 encoded hashed password string.
     */
    @Override
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Sets the manager's hashed password.
     * Note: Typo in original method name ("Passoword"). Kept here to match provided code.
     * Consider renaming to setHashedPassword for consistency.
     * Implements {@link User#setHashedPassoword(String)}.
     * @param hashedPassword The Base64 encoded hashed password string.
     */
    @Override
    public void setHashedPassoword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    /**
     * Gets the manager's age.
     * @return The age as an integer.
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the manager's age.
     * @param age The age as an integer.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Gets the manager's marital status.
     * @return The {@link MaritalStatus} enum value.
     */
    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Sets the manager's marital status.
     * @param maritalStatus The {@link MaritalStatus} enum value.
     */
    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    /**
     * Gets the list of project IDs managed by this manager.
     * Returns an empty list if the manager is not assigned to any projects.
     * Note: Returns the internal list reference. Consider returning a copy if immutability is desired.
     *
     * @return The list of project ID strings.
     */
    public List<String> getProject() {
        // Return a copy or ensure list is initialized
        return (project != null) ? project : new ArrayList<>();
    }

    /**
     * Sets the list of project IDs managed by this manager.
     * Replaces the existing list with the provided one.
     * @param project The list of project ID strings. A copy is stored internally.
     */
    public void setProject(List<String> project) {
         // Store a copy to prevent external modification of the internal list via the passed reference
        this.project = (project != null) ? new ArrayList<>(project) : new ArrayList<>();
    }
}
