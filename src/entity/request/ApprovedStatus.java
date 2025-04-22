package entity.request;

/**
 * Represents the approval status of a request that requires explicit approval or rejection,
 * such as BTO applications, withdrawals, or officer registrations.
 * This is distinct from the overall processing status (e.g., {@link RequestStatus}).
 */
public enum ApprovedStatus {
    /**
     * The request is awaiting review and an approval decision (e.g., by a manager).
     */
    PENDING,
    /**
     * The request has been explicitly approved.
     */
    SUCCESSFUL,
    /**
     * The request has been explicitly rejected or deemed unsuccessful.
     */
    UNSUCCESSFUL;

    /**
     * Returns a string representation of the approval status, formatted with ANSI escape codes
     * for colored console output.
     * <ul>
     * <li>PENDING: Yellow</li>
     * <li>SUCCESSFUL: Green</li>
     * <li>UNSUCCESSFUL: Red</li>
     * </ul>
     * Note: Color display depends on the console supporting ANSI escape codes.
     *
     * @return A colorized string representation of the enum constant name.
     */
    public String coloredString() {
        return switch (this) {
            // Yellow for Pending
            case PENDING -> "\u001B[33m" + this + "\u001B[0m";
            // Green for Successful
            case SUCCESSFUL -> "\u001B[32m" + this + "\u001B[0m";
            // Red for Unsuccessful
            case UNSUCCESSFUL -> "\u001B[31m" + this + "\u001B[0m";
            // Default case added for robustness, though should not be reached with current enum values
            // default -> this.toString(); // Or throw an exception
        };
    }
}
