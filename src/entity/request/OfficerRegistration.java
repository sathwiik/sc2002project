package entity.request;

/**
 * Represents a specific type of request where an Officer applies to register for
 * or be assigned to a specific project.
 * Extends the base {@link Request} class and adds an approval status
 * (using the {@link ApprovedStatus} enum) to track the outcome of the registration request,
 * typically decided by a Manager.
 *
 * @see Request
 * @see ApprovedStatus
 * @see entity.user.Officer
 */
public class OfficerRegistration extends Request {

    /**
     * Stores the approval status (e.g., PENDING, SUCCESSFUL, UNSUCCESSFUL) of this officer registration request.
     * This reflects the decision made by the approving authority (e.g., Manager).
     */
    private ApprovedStatus registrationStatus;

    /**
     * Default constructor.
     * Initializes a new OfficerRegistration instance by calling the default {@link Request} constructor.
     * The specific {@code registrationStatus} will likely be null or require explicit setting later.
     */
    public OfficerRegistration() {
        super(); // Calls Request() constructor
        // registrationStatus will be null by default unless initialized here
    }

    /**
     * Parameterized constructor to create an OfficerRegistration request with specified initial details.
     * Calls the {@link Request} constructor to initialize common request fields (ID, type, user, project, status).
     * Sets the initial {@code registrationStatus} for this registration request to {@link ApprovedStatus#PENDING}.
     *
     * @param requestID     The unique ID for this registration request.
     * @param requestType   The type of request (should be {@link RequestType#REGISTRATION}).
     * @param userID        The ID of the Officer submitting the registration request.
     * @param projectID     The ID of the project the officer wants to register for.
     * @param requestStatus The initial overall processing status (e.g., {@link RequestStatus#PENDING}).
     */
    public OfficerRegistration(String requestID, RequestType requestType, String userID, String projectID, RequestStatus requestStatus) {
        super(requestID, requestType, userID, projectID, requestStatus); // Initialize base Request fields
        this.registrationStatus = ApprovedStatus.PENDING; // Set initial registration approval status to PENDING
    }

    /**
     * Gets the current approval status of this officer registration request.
     *
     * @return The {@link ApprovedStatus} (PENDING, SUCCESSFUL, or UNSUCCESSFUL).
     */
    public ApprovedStatus getRegistrationStatus() {
        return registrationStatus;
    }

    /**
     * Sets the approval status of this officer registration request.
     * Typically called by a Manager or relevant controller when processing the request.
     *
     * @param registrationStatus The new {@link ApprovedStatus} to set for the registration request.
     */
    public void setRegistrationStatus(ApprovedStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
}
