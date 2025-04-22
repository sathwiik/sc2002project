package entity.user;

import java.util.HashMap;
import java.util.Map;

import entity.project.FlatType;

/**
 * Represents an Applicant user in the housing application system.
 * Implements the {@link User} interface.
 * Stores personal details (ID, name, hashed password, age, marital status)
 * and application-specific information, such as the single project currently applied to/booked,
 * application statuses, and flat types applied for across potentially multiple projects (though typically associated with one active application at a time).
 */
public class Applicant implements User {

    /** The unique identifier for the applicant (User ID). */
    private String applicantID;
    /** The name of the applicant. */
    private String name;
    /** The securely hashed password for the applicant's account. */
    private String hashedPassword;
    /** The age of the applicant. */
    private int age;
    /** The marital status of the applicant (e.g., SINGLE, MARRIED). */
    private MaritalStatus maritalStatus;
    /**
     * The project ID the applicant is currently associated with (applied to, booked).
     * Typically holds the ID of the single active application/booking. Null if not applied.
     */
    private String project;
    /**
     * A map storing the applicant's {@link ApplicationStatus} for different projects.
     * Keys are project IDs (String), values are {@link ApplicationStatus}.
     * Allows tracking history or status across multiple interactions.
     */
    private Map<String, ApplicationStatus> applicationStatus;
    /**
     * A map storing the {@link FlatType} the applicant applied for in different projects.
     * Keys are project IDs (String), values are {@link FlatType}.
     */
    private Map<String, FlatType> appliedFlat;

    /**
     * Default constructor.
     * Initializes a new Applicant instance with default values:
     * empty strings, zero age, default marital status (SINGLE), and empty maps
     * for application status and applied flat type.
     */
    public Applicant() {
        this.applicantID = "";
        this.name = "";
        this.hashedPassword = "";
        this.age = 0;
        this.maritalStatus = MaritalStatus.SINGLE; // Default status
        // Initialize maps
        this.applicationStatus = new HashMap<>();
        this.appliedFlat = new HashMap<>();
        this.project = null; // Explicitly null for clarity
    }

    /**
     * Parameterized constructor to create an Applicant with specified initial details.
     * Initializes application-related fields (project, applicationStatus, appliedFlat) as empty/null.
     *
     * @param userID         The unique user ID for the applicant.
     * @param name           The name of the applicant.
     * @param hashedPassword The hashed password for the account.
     * @param age            The age of the applicant.
     * @param maritalStatus  The marital status of the applicant.
     */
    public Applicant(String userID, String name, String hashedPassword, int age, MaritalStatus maritalStatus) {
        this.applicantID = userID;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.age = age;
        this.maritalStatus = maritalStatus;
        // Initialize maps and project field
        this.applicationStatus = new HashMap<>();
        this.appliedFlat = new HashMap<>();
        this.project = null; // No project assigned initially
    }

    /**
     * Gets the applicant's user ID.
     * Implements {@link User#getUserID()}.
     * @return The user ID string.
     */
    @Override
    public String getUserID() {
        return applicantID;
    }

    /**
     * Sets the applicant's user ID.
     * @param userID The user ID string.
     */
    public void setUserID(String userID) {
        this.applicantID = userID;
    }

    /**
     * Gets the applicant's name.
     * Implements {@link User#getName()}.
     * @return The name string.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the applicant's name.
     * @param name The name string.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the applicant's hashed password.
     * Implements {@link User#getHashedPassword()}.
     * @return The Base64 encoded hashed password string.
     */
    @Override
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Sets the applicant's hashed password.
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
     * Gets the applicant's age.
     * @return The age as an integer.
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the applicant's age.
     * @param age The age as an integer.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Gets the applicant's marital status.
     * @return The {@link MaritalStatus} enum value.
     */
    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Sets the applicant's marital status.
     * @param maritalStatus The {@link MaritalStatus} enum value.
     */
    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    /**
     * Gets the ID of the single project the applicant is currently actively associated with
     * (e.g., the one they have applied to or booked).
     * Returns null if the applicant is not currently associated with any active project application/booking.
     *
     * @return The project ID string, or null.
     */
    public String getProject() {
        return project;
    }

    /**
     * Sets the ID of the single project the applicant is currently actively associated with.
     * Setting this typically happens upon successful application submission. Setting to null unlinks them.
     *
     * @param project The project ID string, or null to remove association.
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * Gets the map containing the application status for different projects.
     * Useful for tracking history or status across multiple project interactions.
     * Returns an empty map if no statuses have been recorded.
     *
     * @return A map where keys are project IDs and values are {@link ApplicationStatus}.
     */
    public Map<String, ApplicationStatus> getApplicationStatus() {
        // Return a copy or ensure map is initialized
        return (applicationStatus != null) ? applicationStatus : new HashMap<>();
    }

    /**
     * Gets the application status for a specific project ID from the status map.
     *
     * @param projectID The ID of the project whose status is needed.
     * @return The {@link ApplicationStatus} for the given project ID, or null if not found.
     */
    public ApplicationStatus getApplicationStatusByID(String projectID) {
        if (this.applicationStatus == null || projectID == null) {
            return null; // Or perhaps a default status like NOT_APPLIED?
        }
        return applicationStatus.get(projectID);
    }

    /**
     * Sets or updates the application status for a specific project ID in the status map.
     *
     * @param projectID The ID of the project whose status is being set.
     * @param status    The {@link ApplicationStatus} to set for the project.
     */
    public void setApplicationStatusByID(String projectID, ApplicationStatus status) {
        if (this.applicationStatus == null) {
            this.applicationStatus = new HashMap<>(); // Initialize if null
        }
        if (projectID != null && status != null) {
            applicationStatus.put(projectID, status);
        }
    }

    /**
     * Gets the map containing the flat type applied for in different projects.
     * Returns an empty map if no flat types have been recorded.
     *
     * @return A map where keys are project IDs and values are {@link FlatType}.
     */
    public Map<String, FlatType> getAppliedFlat() {
         // Return a copy or ensure map is initialized
        return (appliedFlat != null) ? appliedFlat : new HashMap<>();
    }

    /**
     * Gets the flat type applied for for a specific project ID from the map.
     *
     * @param projectID The ID of the project whose applied flat type is needed.
     * @return The {@link FlatType} applied for in the given project, or null if not found or not applicable.
     */
    public FlatType getAppliedFlatByID(String projectID) {
         if (this.appliedFlat == null || projectID == null) {
            return null;
        }
        return appliedFlat.get(projectID);
    }

    /**
     * Sets or updates the flat type applied for for a specific project ID in the map.
     * This is typically set when an application is submitted. Can be set to null to clear the entry.
     *
     * @param projectID The ID of the project for which the flat type is being set.
     * @param flat      The {@link FlatType} applied for, or null to remove the entry.
     */
    public void setAppliedFlatByID(String projectID, FlatType flat) {
        if (this.appliedFlat == null) {
            this.appliedFlat = new HashMap<>(); // Initialize if null
        }
        if (projectID != null) {
            if (flat != null) {
                appliedFlat.put(projectID, flat);
            } else {
                 appliedFlat.remove(projectID); // Remove entry if flat is null
            }
        }
    }
}
