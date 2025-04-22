package entity.request;

// Assuming Requestable interface exists and defines a contract for request objects.
// import entity.request.Requestable; // Uncomment if Requestable interface is used

/**
 * Base entity class representing a generic request within the system.
 * This class holds common attributes shared by all specific request types,
 * such as an ID, type, associated user and project IDs, and the overall processing status.
 * It is intended to be extended by concrete request classes like {@link BTOApplication},
 * {@link Enquiry}, {@link BTOWithdrawal}, and {@link OfficerRegistration}.
 * Implements the {@link Requestable} interface (assumed).
 */
public class Request implements Requestable { // Assuming Requestable interface is implemented

    // --- Attributes ---

    /** The unique identifier for this specific request. */
    private String requestID;
    /** The type of this request, indicating its purpose (e.g., ENQUIRY, BTO_APPLICATION). */
    private RequestType requestType;
    /** The user ID of the user who initiated or is associated with this request. */
    private String userID;
    /** The project ID related to this request, if applicable. */
    private String projectID;
    /** The overall processing status of this request (e.g., PENDING, DONE). */
    private RequestStatus requestStatus;

    // --- Constructors ---

    /**
     * Default constructor.
     * Initializes a new Request instance with default values:
     * empty strings for IDs, {@link RequestType#NONE}, and {@link RequestStatus#PENDING}.
     */
    public Request() {
        this.requestID = "";
        this.requestType = RequestType.NONE; // Default type
        this.userID = "";
        this.projectID = "";
        this.requestStatus = RequestStatus.PENDING; // Default status
    }

    /**
     * Parameterized constructor to create a Request with specified initial values.
     * Used by subclasses to initialize the common request properties.
     *
     * @param requestID     The unique ID for this request.
     * @param requestType   The {@link RequestType} indicating the kind of request.
     * @param userID        The ID of the user associated with this request.
     * @param projectID     The ID of the project related to this request.
     * @param requestStatus The initial {@link RequestStatus} (e.g., PENDING).
     */
    public Request(String requestID, RequestType requestType, String userID, String projectID, RequestStatus requestStatus) {
        this.requestID = requestID;
        this.requestType = requestType;
        this.userID = userID;
        this.projectID = projectID;
        this.requestStatus = requestStatus;
    }

    // --- Getters and Setters ---

    /**
     * Gets the unique identifier for this request.
     * @return The request ID string.
     */
    public String getRequestID() {
        return requestID;
    }

    /**
     * Sets the unique identifier for this request.
     * @param requestID The request ID string.
     */
    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    /**
     * Gets the type of this request.
     * @return The {@link RequestType} enum value.
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * Sets the type of this request.
     * @param requestType The {@link RequestType} enum value.
     */
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    /**
     * Gets the user ID associated with this request.
     * @return The user ID string.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Sets the user ID associated with this request.
     * @param userID The user ID string.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Gets the project ID related to this request.
     * @return The project ID string. Can be null or empty if not applicable.
     */
    public String getProjectID() {
        return projectID;
    }

    /**
     * Sets the project ID related to this request.
     * @param projectID The project ID string.
     */
    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    /**
     * Gets the overall processing status of this request.
     * @return The {@link RequestStatus} enum value (e.g., PENDING, DONE).
     */
    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    /**
     * Sets the overall processing status of this request.
     * @param requestStatus The {@link RequestStatus} enum value.
     */
    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
}
