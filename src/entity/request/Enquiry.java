package entity.request;

/**
 * Represents a specific type of request where a user submits an enquiry or question,
 * typically regarding a specific project.
 * Extends the base {@link Request} class and adds fields to store the text of the query
 * and the corresponding answer (if provided).
 *
 * @see Request
 */
public class Enquiry extends Request {

    /**
     * The text content of the user's submitted question or enquiry.
     */
    private String query;
    /**
     * The text content of the answer provided in response to the query.
     * This may be null if the enquiry has not yet been answered.
     */
    private String answer;

    /**
     * Default constructor.
     * Initializes a new Enquiry instance by calling the default {@link Request} constructor.
     * The {@code query} and {@code answer} fields will be null initially.
     */
    public Enquiry() {
        super(); // Calls Request() constructor
        // query and answer fields are null by default
    }

    /**
     * Parameterized constructor to create an Enquiry request with specified initial details.
     * Calls the {@link Request} constructor to initialize common request fields (ID, type, user, project, status).
     * Sets the initial {@code query} text provided by the user. The {@code answer} field is initialized to null.
     *
     * @param requestID     The unique ID for this enquiry request.
     * @param requestType   The type of request (should be {@link RequestType#ENQUIRY}).
     * @param userID        The ID of the user submitting the enquiry.
     * @param projectID     The ID of the project the enquiry pertains to.
     * @param requestStatus The initial overall processing status (e.g., {@link RequestStatus#PENDING}).
     * @param query         The text content of the user's enquiry.
     */
    public Enquiry(String requestID, RequestType requestType, String userID, String projectID, RequestStatus requestStatus, String query) {
        super(requestID, requestType, userID, projectID, requestStatus); // Initialize base Request fields
        this.query = query;
        this.answer = null; // Answer is initially null
    }

    /**
     * Gets the text content of the user's submitted query.
     *
     * @return The query string.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the text content of the user's submitted query.
     * Allows modification, for example, if the user edits their query before it's answered.
     *
     * @param query The new query string.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Gets the text content of the answer provided for this enquiry.
     *
     * @return The answer string, or null if the enquiry has not been answered yet.
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets the text content of the answer for this enquiry.
     * Typically called by an Officer or Manager responding to the query.
     * Setting the answer might also involve changing the overall {@link RequestStatus} to DONE.
     *
     * @param answer The answer string provided.
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
