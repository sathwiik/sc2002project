package entity.request;

/**
 * Represents a specific type of request to withdraw a previously submitted
 * Build-To-Order (BTO) housing application.
 * Extends the base {@link Request} class and adds an approval status specific
 * to the outcome of the withdrawal request itself.
 *
 * @see Request
 * @see ApprovedStatus
 */
public class BTOWithdrawal extends Request {

    /**
     * Stores the approval status (e.g., PENDING, SUCCESSFUL, UNSUCCESSFUL) of this withdrawal request.
     * This indicates whether the request to withdraw the original application was approved by the relevant authority.
     */
    private ApprovedStatus withdrawalStatus;

    /**
     * Default constructor.
     * Initializes a new BTOWithdrawal instance by calling the default {@link Request} constructor.
     * The specific {@code withdrawalStatus} will likely be null or require explicit setting later.
     */
    public BTOWithdrawal() {
        super(); // Calls Request() constructor
        // withdrawalStatus will be null by default unless initialized here
    }

    /**
     * Parameterized constructor to create a BTOWithdrawal request with specified initial details.
     * Calls the {@link Request} constructor to initialize common request fields (ID, type, user, project, status).
     * Sets the initial {@code withdrawalStatus} for this withdrawal request to {@link ApprovedStatus#PENDING}.
     *
     * @param requestID     The unique ID for this withdrawal request.
     * @param requestType   The type of request (should be {@link RequestType#BTO_WITHDRAWAL}).
     * @param userID        The ID of the applicant submitting the withdrawal request.
     * @param projectID     The ID of the project from which the application is being withdrawn.
     * @param requestStatus The initial overall processing status (e.g., {@link RequestStatus#PENDING}).
     */
    public BTOWithdrawal(String requestID, RequestType requestType, String userID, String projectID, RequestStatus requestStatus) {
        super(requestID, requestType, userID, projectID, requestStatus); // Initialize base Request fields
        this.withdrawalStatus = ApprovedStatus.PENDING; // Set initial withdrawal approval status to PENDING
    }

    /**
     * Gets the current approval status of this withdrawal request.
     *
     * @return The {@link ApprovedStatus} (PENDING, SUCCESSFUL, or UNSUCCESSFUL).
     */
    public ApprovedStatus getWithdrawalStatus() {
        return withdrawalStatus;
    }

    /**
     * Sets the approval status of this withdrawal request.
     * Typically called by a Manager or relevant controller when processing the withdrawal request.
     *
     * @param withdrawalStatus The new {@link ApprovedStatus} to set for the withdrawal request.
     */
    public void setWithdrawalStatus(ApprovedStatus withdrawalStatus) {
        this.withdrawalStatus = withdrawalStatus;
    }
}
