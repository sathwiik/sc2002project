package entity.request;

/**
 * Defines the different categories or types of requests that can be initiated
 * and processed within the housing application system.
 * This helps differentiate the purpose and handling logic for various requests.
 */
public enum RequestType {
    /**
     * Represents a request submitted by an applicant to apply for a Build-To-Order (BTO) housing project.
     * Typically associated with the {@link BTOApplication} class.
     */
    BTO_APPLICATION,

    /**
     * Represents a request submitted by an applicant to withdraw a previously submitted BTO application.
     * Typically associated with the {@link BTOWithdrawal} class.
     */
    BTO_WITHDRAWAL,

    /**
     * Represents a request submitted by an officer to register for assignment to a specific project.
     * Typically associated with the {@link OfficerRegistration} class.
     */
    REGISTRATION,

    /**
     * Represents a request submitted by a user (e.g., applicant) asking a question or seeking information, usually about a project.
     * Typically associated with the {@link Enquiry} class.
     */
    ENQUIRY,

    /**
     * Represents an unspecified, default, or null request type.
     * May be used for initialization or to indicate an absence of a specific request type.
     */
    NONE,
}
