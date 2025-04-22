package entity.request;

/**
 * Represents the overall processing status of a {@link Request} within the system.
 * Indicates whether a request is still awaiting action or has been completed.
 * This is distinct from specific approval statuses like {@link ApprovedStatus}.
 */
public enum RequestStatus {
    /**
     * The request is currently awaiting processing, review, or further action.
     */
    PENDING,

    /**
     * The request has been fully processed and is considered complete.
     * No further action is required (e.g., an enquiry has been answered, an application approved/rejected).
     */
    DONE;

    /**
     * Returns a string representation of the status, formatted with ANSI escape codes
     * for colored console output.
     * <ul>
     * <li>PENDING: Yellow</li>
     * <li>DONE: Green</li>
     * </ul>
     * Note: Color display depends on the console supporting ANSI escape codes.
     *
     * @return A colorized string representation of the enum constant name.
     */
    public String coloredString() {
        return switch (this) {
            // Yellow for Pending
            case PENDING -> "\u001B[33m" + this + "\u001B[0m";
            // Green for Done
            case DONE -> "\u001B[32m" + this + "\u001B[0m";
            // Default case added for robustness, though should not be reached with current enum values
            // default -> this.toString();
        };
    }
}
