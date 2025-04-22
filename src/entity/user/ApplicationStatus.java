package entity.user;

/**
 * Represents the status of an applicant's application for a specific housing project.
 * Defines the possible states throughout the application lifecycle.
 */
public enum ApplicationStatus {
    /**
     * The application has been submitted but is awaiting review or processing.
     */
    PENDING,
    /**
     * The application has been reviewed and approved; the applicant is eligible to proceed (e.g., book a flat).
     */
    SUCCESSFUL,
    /**
     * The application has been reviewed and rejected, or potentially withdrawn by the applicant after approval.
     */
    UNSUCCESSFUL,
    /**
     * The applicant has successfully booked a specific flat unit after their application was approved.
     */
    BOOKED;

    /**
     * Returns a string representation of the status, formatted with ANSI escape codes
     * for colored console output.
     * <ul>
     * <li>PENDING: Yellow</li>
     * <li>SUCCESSFUL: Green</li>
     * <li>UNSUCCESSFUL: Red</li>
     * <li>BOOKED: Blue</li>
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
            // Blue for Booked
            case BOOKED -> "\u001B[34m" + this + "\u001B[0m";
            // Default case added for robustness, though should not be reached with current enum values
            // default -> this.toString(); // Or throw an exception
        };
    }
}
