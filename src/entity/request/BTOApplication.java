package entity.request;

/**
 * Represents a specific type of request for a Build-To-Order (BTO) housing project application.
 * Extends the base {@link Request} class and adds an approval status specific to the application outcome.
 *
 * @see Request
 * @see ApprovedStatus
 */
public class BTOApplication extends Request {

    /**
     * Stores the approval status (e.g., PENDING, SUCCESSFUL, UNSUCCESSFUL) of this BTO application.
     * This indicates the outcome decided by the approving authority (e.g., Manager).
     */
    private ApprovedStatus applicationStatus;

    /**
     * Default constructor.
     * Initializes a new BTOApplication instance by calling the default {@link Request} constructor.
     * The specific {@code applicationStatus} will likely be null or require explicit setting later.
     */
    public BTOApplication() {
        super(); // Calls Request() constructor
        // applicationStatus will be null by default unless initialized here
    }

    /**
     * Parameterized constructor to create a BTOApplication with specified initial details.
     * Calls the {@link Request} constructor to initialize common request fields (ID, type, user, project, status).
     * Sets the initial {@code applicationStatus} for this BTO application to {@link ApprovedStatus#PENDING}.
     *
     * @param requestID     The unique ID for this request.
     * @param requestType   The type of request (should be {@link RequestType#BTO_APPLICATION}).
     * @param userID        The ID of the applicant submitting the application.
     * @param projectID     The ID of the project being applied for.
     * @param requestStatus The initial overall processing status (e.g., {@link RequestStatus#PENDING}).
     */
    public BTOApplication(String requestID, RequestType requestType, String userID, String projectID, RequestStatus requestStatus) {
        super(requestID, requestType, userID, projectID, requestStatus); // Initialize base Request fields
        this.applicationStatus = ApprovedStatus.PENDING; // Set initial approval status to PENDING
    }

    /**
     * Gets the current approval status of this BTO application.
     *
     * @return The {@link ApprovedStatus} (PENDING, SUCCESSFUL, or UNSUCCESSFUL).
     */
    public ApprovedStatus getApplicationStatus() {
        return applicationStatus;
    }

    /**
     * Sets the approval status of this BTO application.
     * Typically called by a Manager or relevant controller when processing the application.
     *
     * @param applicationStatus The new {@link ApprovedStatus} to set.
     */
    public void setApplicationStatus(ApprovedStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }
}
