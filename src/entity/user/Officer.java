package entity.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an Officer user in the housing application system.
 * An Officer is also an {@link Applicant}, inheriting all applicant properties and behaviors,
 * but additionally has attributes related to their official duties. This includes a list
 * of projects they are assigned to work on and their registration status for various projects.
 *
 * @see Applicant
 * @see User
 */
public class Officer extends Applicant {

    /**
     * A list of project IDs that this officer is officially registered for or assigned to work on.
     */
    private List<String> officerProject;
    /**
     * A map storing the officer's {@link RegistrationStatus} for different projects they might have applied to register for.
     * Keys are project IDs (String), values are {@link RegistrationStatus} (e.g., PENDING, APPROVED, REJECTED).
     */
    private Map<String, RegistrationStatus> registrationStatus;

    /**
     * Default constructor.
     * Initializes a new Officer instance by calling the default {@link Applicant} constructor
     * and initializing the officer-specific fields ({@code officerProject} list and
     * {@code registrationStatus} map) as empty collections.
     */
    public Officer() {
        super(); // Calls the default constructor of Applicant
        this.officerProject = new ArrayList<>();
        this.registrationStatus = new HashMap<>();
    }

    /**
     * Parameterized constructor to create an Officer with specified initial personal details.
     * Calls the corresponding {@link Applicant} constructor to initialize inherited fields.
     * Initializes the officer-specific fields ({@code officerProject} list and
     * {@code registrationStatus} map) as empty collections. Project assignments and statuses
     * are typically managed separately.
     *
     * @param userID         The unique user ID for the officer.
     * @param name           The name of the officer.
     * @param hashedPassword The hashed password for the account.
     * @param age            The age of the officer.
     * @param maritalStatus  The marital status of the officer.
     */
    public Officer(String userID, String name, String hashedPassword, int age, MaritalStatus maritalStatus) {
        super(userID, name, hashedPassword, age, maritalStatus); // Calls the Applicant constructor
        this.officerProject = new ArrayList<>();
        this.registrationStatus = new HashMap<>();
    }

    /**
     * Gets the list of project IDs the officer is assigned to or registered for.
     * Returns an empty list if the officer is not assigned to any projects.
     * Note: Returns the internal list reference. Consider returning a copy if immutability outside this class is desired.
     *
     * @return The list of project ID strings.
     */
    public List<String> getOfficerProject() {
         // Return a copy or ensure list is initialized
        return (officerProject != null) ? officerProject : new ArrayList<>();
    }

    /**
     * Sets the list of project IDs the officer is assigned to or registered for.
     * Replaces the existing list with the provided one.
     *
     * @param officerProject The list of project ID strings. A copy is stored internally.
     */
    public void setOfficerProject(List<String> officerProject) {
        // Store a copy to prevent external modification of the internal list via the passed reference
        this.officerProject = (officerProject != null) ? new ArrayList<>(officerProject) : new ArrayList<>();
    }

    /**
     * Gets the map containing the officer's registration status for different projects.
     * Returns an empty map if no statuses have been recorded.
     *
     * @return A map where keys are project IDs and values are {@link RegistrationStatus}.
     */
    public Map<String, RegistrationStatus> getRegistrationStatus() {
        // Return a copy or ensure map is initialized
        return (registrationStatus != null) ? registrationStatus : new HashMap<>();
