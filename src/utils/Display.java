package utils;

import java.util.Map;

import entity.list.ManagerList;
import entity.project.FlatType;
import entity.project.Project;
import entity.request.BTOApplication;
import entity.request.BTOWithdrawal;
import entity.request.Enquiry;
import entity.request.OfficerRegistration;
import entity.request.Request;
import entity.request.RequestStatus;
import entity.user.Applicant;
import entity.user.ApplicationStatus;
import entity.user.Manager;
import entity.user.Officer;
import entity.user.RegistrationStatus;
import entity.user.UserType;

/**
 * Utility class containing static methods for displaying formatted information
 * about various entity objects (Applicants, Officers, Managers, Projects, Requests)
 * to the standard console output. Provides different views based on context (e.g., user type).
 */
public class Display {

    /**
     * Displays formatted information about an Applicant.
     * If {@code profile} is true, it includes details about the applied project and application status history.
     * Always shows basic details like name, ID, age, marital status, and applied flat type if applicable.
     *
     * @param user    The {@link Applicant} object to display.
     * @param profile If true, display extended profile information including application history;
     * if false, display basic information.
     */
    public static void displayApplicant(Applicant user, boolean profile) {
        if (user == null) {
             System.out.println("Cannot display null Applicant.");
             return;
        }
        System.out.println("------------------------- Applicant Info --------------------------");
        System.out.println("Name: " + user.getName());
        System.out.println("NRIC: " + user.getUserID()); // Assumes NRIC is used as UserID
        System.out.println("Age: " + user.getAge());
        System.out.println("Marital Status: " + user.getMaritalStatus());

        // Display application details conditionally
        String currentProjectID = user.getProject();
        if (profile && currentProjectID != null) {
            // Display detailed application status map only in profile view
            System.out.println("Currently Applied Project ID: " + currentProjectID);
            System.out.println("Application Status History:");
             Map<String, ApplicationStatus> statusMap = user.getApplicationStatus();
             if (statusMap != null && !statusMap.isEmpty()) {
                 for (Map.Entry<String, ApplicationStatus> entry : statusMap.entrySet()) {
                     // Ensure value is not null before calling coloredString
                     String statusString = (entry.getValue() != null) ? entry.getValue().coloredString() : "N/A";
                     System.out.println("  Project " + entry.getKey() + " = " + statusString);
                 }
             } else {
                  System.out.println("  No application status history found.");
             }
        }

        // Display applied flat type for the current project, if any
        if (currentProjectID != null) {
             FlatType appliedFlat = user.getAppliedFlatByID(currentProjectID);
             System.out.println("Applied Flat Type (Project " + currentProjectID + "): " + (appliedFlat != null ? appliedFlat : "N/A"));
        } else if (!profile) {
             // If not profile view and no current project, mention it.
             System.out.println("Currently Applied Project: None");
        }

        System.out.println("-------------------------------------------------------------------");
    }

    /**
     * Displays formatted information about an Officer.
     * Includes basic details, list of registered project IDs, and registration status history.
     *
     * @param user The {@link Officer} object to display.
     */
    public static void displayOfficer(Officer user) {
         if (user == null) {
              System.out.println("Cannot display null Officer.");
              return;
         }
        System.out.println("-------------------------- Officer Info ---------------------------");
        System.out.println("Name: " + user.getName());
        System.out.println("NRIC: " + user.getUserID()); // Assumes NRIC is used as UserID
        System.out.println("Age: " + user.getAge());
        System.out.println("Marital Status: " + user.getMaritalStatus());

        // Display registered projects
        List<String> projects = user.getOfficerProject();
        if (projects != null && !projects.isEmpty()) {
             System.out.println("Registered Projects: " + String.join(", ", projects));
        } else {
             System.out.println("Registered Projects: None");
        }

        // Display registration status history
        System.out.println("Registration Status History:");
        Map<String, RegistrationStatus> regStatusMap = user.getRegistrationStatus();
        if (regStatusMap != null && !regStatusMap.isEmpty()) {
            for (Map.Entry<String, RegistrationStatus> entry : regStatusMap.entrySet()) {
                // Ensure value is not null before calling coloredString
                 String statusString = (entry.getValue() != null) ? entry.getValue().coloredString() : "N/A";
                System.out.println("  Project " + entry.getKey() + " = " + statusString);
            }
        } else {
             System.out.println("  No registration status history found.");
        }
        System.out.println("-------------------------------------------------------------------");
    }

    /**
     * Displays formatted information about a Manager.
     * Includes basic details and a list of project IDs they created/manage.
     *
     * @param user The {@link Manager} object to display.
     */
    public static void displayManager(Manager user) {
         if (user == null) {
              System.out.println("Cannot display null Manager.");
              return;
         }
        System.out.println("-------------------------- Manager Info ---------------------------");
        System.out.println("Name: " + user.getName());
        System.out.println("NRIC: " + user.getUserID()); // Assumes NRIC is used as UserID
        System.out.println("Age: " + user.getAge());
        System.out.println("Marital Status: " + user.getMaritalStatus());

        // Display managed projects
         List<String> projects = user.getProject();
         if (projects != null && !projects.isEmpty()) {
             System.out.println("Managed Projects: " + String.join(", ", projects));
         } else {
              System.out.println("Managed Projects: None");
         }
        System.out.println("-------------------------------------------------------------------");
    }

    /**
     * Displays formatted information about a Project.
     * The level of detail depends on the {@code user} type viewing the project.
     * The {@code flatType} parameter can filter the display of units and prices
     * (e.g., if viewing as an applicant only eligible for TWO_ROOM, THREE_ROOM details might be hidden).
     *
     * @param project  The {@link Project} object to display.
     * @param user     The {@link UserType} of the user viewing the project (determines detail level).
     * @param flatType A {@link FlatType} used for filtering display (e.g., hide details irrelevant to user eligibility).
     * If null, typically all flat type details are shown.
     */
    public static void displayProject(Project project, UserType user, FlatType flatType) {
        if (project == null) {
             System.out.println("Cannot display null Project.");
             return;
        }
        System.out.println("------------------------- Project Info ---------------------------");
        System.out.println("Project ID: " + project.getProjectID());
        System.out.println("Name: " + project.getName());

        List<String> neighborhoods = project.getNeighborhood();
        if (neighborhoods != null && !neighborhoods.isEmpty()) {
            System.out.println("Neighborhood(s): " + String.join(", ", neighborhoods));
        } else {
             System.out.println("Neighborhood(s): None Specified");
        }

        // Display Available Units, potentially filtered by flatType parameter
        System.out.println("Available Units:");
        Map<FlatType, Integer> units = project.getAvailableUnit();
        if (units != null && !units.isEmpty()) {
            for (Map.Entry<FlatType, Integer> entry : units.entrySet()) {
                // Logic seems intended to hide THREE_ROOM info if flatType param is TWO_ROOM
                if (entry.getKey() == FlatType.THREE_ROOM && flatType == FlatType.TWO_ROOM) continue;
                System.out.println("  " + entry.getKey() + " = " + entry.getValue());
            }
        } else {
             System.out.println("  No unit information available.");
        }

        // Display Prices, potentially filtered by flatType parameter
        System.out.println("Price:");
        Map<FlatType, Integer> prices = project.getPrice();
        if (prices != null && !prices.isEmpty()) {
            for (Map.Entry<FlatType, Integer> entry : prices.entrySet()) {
                // Logic seems intended to hide THREE_ROOM info if flatType param is TWO_ROOM
                if (entry.getKey() == FlatType.THREE_ROOM && flatType == FlatType.TWO_ROOM) continue;
                System.out.println("  " + entry.getKey() + " = $" + entry.getValue());
            }
        } else {
             System.out.println("  No price information available.");
        }

        System.out.println("Open Date: " + project.getOpenDate());
        System.out.println("Close Date: " + project.getCloseDate());

        // Display Manager Name (requires lookup)
        Manager manager = ManagerList.getInstance().getByID(project.getManagerID());
        System.out.println("Manager Name: " + (manager != null ? manager.getName() : "N/A (ID: " + project.getManagerID() + ")"));

        // Display Officer/Applicant details only if viewer is not an Applicant
        if (user != UserType.APPLICANT) {
            System.out.println("Available Officer Slots: " + project.getAvailableOfficer());
             List<String> officers = project.getOfficerID();
             System.out.println("Assigned Officer IDs: " + (officers != null && !officers.isEmpty() ? String.join(", ", officers) : "None"));
             List<String> applicants = project.getApplicantID();
             System.out.println("Booked Applicant IDs: " + (applicants != null && !applicants.isEmpty() ? String.join(", ", applicants) : "None"));
        }

        // Display visibility only if viewer is a Manager
        if (user == UserType.MANAGER) {
            System.out.println("Visible to Public? " + (project.getVisibility() ? "Yes" : "No"));
        }
        System.out.println("-------------------------------------------------------------------");
    }

    /**
     * Displays formatted information about a Request.
     * Adjusts displayed details based on the {@code userType} viewing the request
     * (e.g., hides User ID for Applicants).
     * Displays specific status or query/answer details based on the actual subclass of the Request
     * (OfficerRegistration, BTOApplication, BTOWithdrawal, Enquiry).
     *
     * @param request  The {@link Request} object (or a subclass instance) to display.
     * @param userType The {@link UserType} of the user viewing the request.
     */
    public static void displayRequest(Request request, UserType userType) {
         if (request == null) {
              System.out.println("Cannot display null Request.");
              return;
         }
        System.out.println("------------------------- Request Info ---------------------------");
        System.out.println("Request ID: " + request.getRequestID());
        System.out.println("Type: " + request.getRequestType());
        System.out.println("Project ID: " + request.getProjectID());

        // Hide User ID if viewed by an Applicant
        if (userType != UserType.APPLICANT) {
            System.out.println("User ID: " + request.getUserID());
        }

        // Display specific details based on the concrete Request type
        switch (request.getRequestType()) {
            case REGISTRATION:
                 // Use pattern matching with instanceof
                if (request instanceof OfficerRegistration registration) {
                     RegistrationStatus status = registration.getRegistrationStatus();
                    System.out.println("Registration status: " + (status != null ? status.coloredString() : "N/A"));
                }
                break;
            case BTO_APPLICATION:
                if (request instanceof BTOApplication application) {
                     ApprovedStatus status = application.getApplicationStatus();
                    System.out.println("Application status: " + (status != null ? status.coloredString() : "N/A"));
                }
                break;
            case BTO_WITHDRAWAL:
                if (request instanceof BTOWithdrawal withdrawal) { // Changed variable name
                     ApprovedStatus status = withdrawal.getWithdrawalStatus();
                    System.out.println("Withdrawal status: " + (status != null ? status.coloredString() : "N/A"));
                }
                break;
            case ENQUIRY:
                if (request instanceof Enquiry enquiry) {
                     RequestStatus status = enquiry.getRequestStatus();
                    System.out.println("Status: " + (status != null ? status.coloredString() : "N/A"));
                    System.out.println("Query: " + enquiry.getQuery());
                    // Display answer only if the request is processed (not PENDING)
                    if (enquiry.getRequestStatus() != RequestStatus.PENDING && enquiry.getAnswer() != null) {
                        System.out.println("Answer: " + enquiry.getAnswer());
                    } else if (enquiry.getRequestStatus() != RequestStatus.PENDING && enquiry.getAnswer() == null) {
                         System.out.println("Answer: (Not provided)");
                    }
                }
                break;
            case NONE: // Handle NONE case if necessary
                 System.out.println("Status: Request type is NONE.");
                 break;
            default:
                // Should not happen if all enum cases are handled
                System.out.println("Status: Unknown request type or status.");
                break;
        }
        // Original code had the bottom line commented out, kept it that way. Added newline instead.
        //System.out.println("-------------------------------------------------------------------");
        System.out.println(); // Add a blank line for separation
    }
}
