package utils;

import java.util.List;

import entity.list.ProjectList;
import entity.list.RequestList;
import entity.project.Project;
import entity.request.Request;

/**
 * Utility class responsible for generating unique, sequential identifiers (IDs)
 * for new {@link entity.project.Project} and {@link entity.request.Request} entities.
 * It maintains static counters for each entity type and initializes them based on
 * the highest existing ID found in the current data lists to ensure continuity.
 * <p>
 * Note: This implementation assumes IDs follow a pattern like "P0001" or "R0001"
 * where the numeric part starts from index 1.
 */
public class IDController {

    /** Internal counter to track the last used numeric part for Project IDs. */
    private static int projectCount;
    /** Internal counter to track the last used numeric part for Request IDs. (Note typo: reqeust -> request) */
    private static int reqeustCount; // Typo in variable name

    /**
     * Initializes the static ID counters by scanning the existing Project and Request lists.
     * It finds the maximum numeric value currently used in existing IDs (assuming format Prefix + Number)
     * and sets the internal counters to that maximum value. This ensures that subsequently generated IDs
     * are unique and sequential. Should typically be called once at application startup after loading initial data.
     */
    public static void init() {
        int maxProjectNum = 0; // Use more descriptive variable name
        try {
            List<Project> projects = ProjectList.getInstance().getAll();
            for (Project project : projects) {
                try {
                     // Assumes ID format like Pxxxx; extracts number after prefix
                    String idNumPart = project.getProjectID().substring(1);
                    int currentNum = Integer.parseInt(idNumPart);
                    maxProjectNum = Math.max(maxProjectNum, currentNum);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                     System.err.println("Warning: Could not parse numeric part from Project ID: " + project.getProjectID() + " - " + e.getMessage());
                     // Continue scanning other IDs
                }
            }
        } catch (Exception e) {
             System.err.println("Error accessing ProjectList during ID initialization: " + e.getMessage());
             // Counters might not be accurately initialized
        }
        setProjectCount(maxProjectNum); // Set counter to the highest found number
        System.out.println("IDController initialized Project counter to: " + projectCount);


        int maxRequestNum = 0; // Use more descriptive variable name
        try {
            List<Request> requests = RequestList.getInstance().getAll();
            for (Request request : requests) {
                 try {
                     // Assumes ID format like Rxxxx; extracts number after prefix
                    String idNumPart = request.getRequestID().substring(1);
                    int currentNum = Integer.parseInt(idNumPart);
                    maxRequestNum = Math.max(maxRequestNum, currentNum);
                 } catch (NumberFormatException | IndexOutOfBoundsException e) {
                      System.err.println("Warning: Could not parse numeric part from Request ID: " + request.getRequestID() + " - " + e.getMessage());
                      // Continue scanning other IDs
                 }
            }
        } catch (Exception e) {
             System.err.println("Error accessing RequestList during ID initialization: " + e.getMessage());
             // Counters might not be accurately initialized
        }
        setRequestCount(maxRequestNum); // Set counter to the highest found number
        System.out.println("IDController initialized Request counter to: " + reqeustCount);
    }

    /**
     * Manually sets the internal counter used for generating the next project ID.
     * Useful primarily for testing or specific reset scenarios. Use with caution.
     *
     * @param count The value to set the project counter to.
     */
    public static void setProjectCount(int count) {
        projectCount = count;
    }

    /**
     * Manually sets the internal counter used for generating the next request ID.
     * Useful primarily for testing or specific reset scenarios. Use with caution.
     * Note: There is a typo in the method name and the corresponding static variable (`reqeustCount`).
     *
     * @param count The value to set the request counter to.
     */
    public static void setRequestCount(int count) {
        reqeustCount = count; // Typo in variable name
    }

    /**
     * Generates a new, unique project ID sequentially.
     * Increments the internal project counter and formats the ID as "P" followed by
     * a 4-digit number, zero-padded on the left (e.g., "P0001", "P0012", "P0123").
     *
     * @return A new unique project ID string.
     */
    public static String newProjectID() {
        // Increment first, then format
        String projectNumStr = Integer.toString(++projectCount);
        // Pad with leading zeros until length is 4
        while (projectNumStr.length() < 4) {
            projectNumStr = "0" + projectNumStr;
        }
        return "P" + projectNumStr;
    }

    /**
     * Generates a new, unique request ID sequentially.
     * Increments the internal request counter (`reqeustCount` - typo noted) and formats the ID as "R"
     * followed by a 4-digit number, zero-padded on the left (e.g., "R0001", "R0012", "R0123").
     * Note: Uses the static variable `reqeustCount` which has a typo.
     *
     * @return A new unique request ID string.
     */
    public static String newRequestID() {
         // Increment first (using variable with typo), then format
        String requestNumStr = Integer.toString(++reqeustCount); // Typo in variable name
        // Pad with leading zeros until length is 4
        while (requestNumStr.length() < 4) {
            requestNumStr = "0" + requestNumStr;
        }
        return "R" + requestNumStr;
    }
}
