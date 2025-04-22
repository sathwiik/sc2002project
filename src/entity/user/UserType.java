package entity.user;

/**
 * Represents the different types or roles a user can have within the system.
 * This is used to differentiate between standard applicants and users with administrative
 * or operational responsibilities (Officers, Managers).
 */
public enum UserType {
    /**
     * Represents a standard applicant user seeking housing.
     * Corresponds to the {@link Applicant} class.
     */
    APPLICANT,

    /**
     * Represents an officer user, potentially responsible for processing applications
     * or managing specific aspects of projects.
     * Corresponds to the {@link Officer} class.
     */
    OFFICER,

    /**
     * Represents a manager user, typically with higher-level responsibilities such as
     * creating projects, managing officers, and overseeing application approvals.
     * Corresponds to the {@link Manager} class.
     */
    MANAGER
}
