package controller;

import java.util.List;

import entity.list.OfficerList;
import entity.list.ProjectList;
import entity.list.RequestList;
import entity.project.Project;
import entity.request.Enquiry;
import entity.request.OfficerRegistration;
import entity.request.Request;
import entity.request.RequestStatus;
import entity.request.RequestType;
import entity.user.Officer;
import entity.user.RegistrationStatus;
import entity.user.UserType;
import utils.Display;
import utils.IDController;
import utils.UIController;

/**
 * Controller responsible for handling request-related operations specific to Officers.
 * This includes registering for projects, viewing registration status and assigned projects,
 * viewing enquiries for assigned projects, and answering those enquiries.
 * It operates using the context of the currently logged-in officer's ID.
 */
public class OfficerRequestController {

    /**
     * Stores the user ID of the officer currently interacting with the system.
     * This ID is typically set by the {@link AccountController} upon successful login of an Officer.
     */
    private static String officerID;

    /**
     * Sets the officer ID for the current session context.
     * Subsequent officer-specific operations in this controller will use this ID.
     *
     * @param ID The user ID of the currently logged-in officer.
     */
    public static void setOfficerID(String ID) {
        officerID = ID;
    }

    /**
     * Submits a registration request for the current officer to join a specific project.
     * Performs checks:
     * - Project exists and is visible.
     * - Officer is not already an applicant for the project.
     * - The project's dates do not overlap with any project the officer is already registered for.
     * If checks pass, updates the officer's status for this project to PENDING and creates
     * a new {@link OfficerRegistration} request in the {@link RequestList}.
     *
     * @param projectID The ID of the project the officer wants to register for.
     */
    public static void registerProject(String projectID) {
        Project project = ProjectList.getInstance().getByID(projectID);
        // Basic project check
        if (project == null) {
            System.out.println("Registration failed: Project with ID '" + projectID + "' not found.");
            return;
        }
        Officer currentOfficer = OfficerList.getInstance().getByID(officerID);
        if (currentOfficer == null) {
             System.out.println("Registration failed: Officer details not found for ID: " + officerID);
             return;
        }
        List<String> officerRegisteredProjectIDs = currentOfficer.getOfficerProject(); // Projects officer is already registered for

        // Check project visibility and if officer is already an applicant for it
        if (!project.getApplicantID().contains(officerID) && project.getVisibility()) {
            boolean canRegister = true; // Assume registrable unless overlap found
            // Check for date overlaps with already registered projects
             if (officerRegisteredProjectIDs != null) {
                 for (String registeredProjectID : officerRegisteredProjectIDs) {
                     Project registeredProject = ProjectList.getInstance().getByID(registeredProjectID);
                     if (registeredProject != null) {
                         // Check for overlap: !(registered_ends < potential_starts || potential_ends < registered_starts)
                         if (!(registeredProject.getCloseDate().isBefore(project.getOpenDate()) ||
                               project.getCloseDate().isBefore(registeredProject.getOpenDate()))) {
                             canRegister = false; // Found overlap
                             System.out.println("Registration failed: This project's dates overlap with project ID '" + registeredProjectID + "' which you are already registered for.");
                             break; // No need to check further
                         }
                     }
                 }
             }

            // If no overlaps, proceed with registration request
            if (canRegister) {
                // Update officer's local status for this project to PENDING
                currentOfficer.setRegistrationStatusByID(projectID, RegistrationStatus.PENDING);
                OfficerList.getInstance().update(officerID, currentOfficer); // Save the status update

                // Add the registration request to the central list for manager approval
                RequestList.getInstance().add(new OfficerRegistration(IDController.newRequestID(), RequestType.REGISTRATION, officerID, projectID, RequestStatus.PENDING));
                System.out.println("Successfully submitted registration request for project ID: " + projectID + ".");
                return; // Exit after successful submission
            }
        } else if (!project.getVisibility()) {
             System.out.println("Registration failed: Project '" + projectID + "' is not currently visible.");
        } else if (project.getApplicantID().contains(officerID)) {
             System.out.println("Registration failed: You cannot register as an officer for a project you have applied to as an applicant.");
        }

        // If function reaches here, registration was not allowed for reasons printed above or implicit failure
        // System.out.println("You are not allowed to apply for this project."); // This is a bit vague, specific reasons printed earlier
    }

    /**
     * Displays a list of projects the current officer is successfully registered for.
     * Applies filters set in {@link FilterController} to the list before display.
     */
    public static void viewRegisteredProject() {
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                            Your Registered Projects");
        System.out.println(UIController.LINE_SEPARATOR);
        boolean hasProjects = false;
        Officer officer = OfficerList.getInstance().getByID(officerID);
        if (officer == null) {
             System.out.println("Error: Could not find officer details for ID: " + officerID);
             return;
        }
        List<String> projectIDs = officer.getOfficerProject(); // Get IDs of projects officer is assigned to

        if (projectIDs != null && !projectIDs.isEmpty()) {
             // Apply filters to the list of registered projects
            List<Project> list = FilterController.filteredListFromID(projectIDs);
             if (!list.isEmpty()) {
                  FilterController.displayFilter(); // Show active filters
                 for (Project project : list) {
                     hasProjects = true;
                     // Display using Officer view, show all flat types
                     Display.displayProject(project, UserType.OFFICER, null);
                 }
             }
        }
        if (!hasProjects) {
            System.out.println("You are not registered for any projects matching the current filters.");
        }
    }

    /**
     * Displays the history of project registration requests submitted by the current officer.
     * Shows the status of each request (e.g., PENDING, APPROVED, REJECTED).
     */
    public static void viewRegistrationStatus() {
        List<Request> list = RequestList.getInstance().getAll();
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                       Your Project Registration History");
        System.out.println(UIController.LINE_SEPARATOR);
        boolean hasHistory = false; // Track if any registration requests exist for this officer
        for (Request request : list) {
            // Check if it's an OfficerRegistration request AND belongs to the current officer
            if (request instanceof OfficerRegistration r && r.getUserID().equals(officerID)) {
                hasHistory = true;
                Display.displayRequest(request, UserType.OFFICER); // Display the request details
            }
        }
        if (!hasHistory) {
            System.out.println("You have not submitted any project registration requests.");
        }
    }

    /**
     * Displays all enquiries associated with all projects the current officer is registered for.
     */
    public static void viewEnquiries() {
        List<Request> list = RequestList.getInstance().getAll();
        Officer officer = OfficerList.getInstance().getByID(officerID);
        if (officer == null) {
             System.out.println("Error: Could not find officer details for ID: " + officerID);
             return;
        }
        List<String> registeredProjectIDs = officer.getOfficerProject(); // Get projects officer works on

        if (registeredProjectIDs == null || registeredProjectIDs.isEmpty()) {
             System.out.println("You are not registered for any projects. No enquiries to display.");
             return;
        }

        boolean hasEnquiries = false; // Track if relevant enquiries found
        System.out.println("--- Enquiries for Your Registered Projects ---"); // Header
        for (Request request : list) {
            // Check if it's an Enquiry AND its project ID is in the officer's list
            if (request instanceof Enquiry && registeredProjectIDs.contains(request.getProjectID())) {
                hasEnquiries = true;
                Display.displayRequest(request, UserType.OFFICER); // Display using Officer view
            }
        }
        if (!hasEnquiries) {
            System.out.println("There are no enquiries for the projects you are registered for.");
        }
    }

    /**
     * Displays enquiries associated with a specific project ID,
     * but only if the current officer is registered for that project.
     *
     * @param projectID The ID of the project for which to view enquiries.
     */
    public static void viewEnquiries(String projectID) {
        Officer officer = OfficerList.getInstance().getByID(officerID);
        if (officer == null) {
             System.out.println("Error: Could not find officer details for ID: " + officerID);
             return;
        }
        List<String> registeredProjectIDs = officer.getOfficerProject();

        // Check if the officer is actually registered for the requested project
        if (registeredProjectIDs == null || !registeredProjectIDs.contains(projectID)) {
            System.out.println("Access Denied: You are not registered for project ID '" + projectID + "' and cannot view its enquiries.");
            return;
        }

        // Proceed to display enquiries for the validated project
        List<Request> list = RequestList.getInstance().getAll();
        boolean hasEnquiries = false; // Track if enquiries exist for this specific project
        System.out.println("--- Enquiries for Project ID: " + projectID + " ---"); // Header
        for (Request request : list) {
            // Check if it's an Enquiry AND matches the specific project ID
            if (request instanceof Enquiry && request.getProjectID().equals(projectID)) {
                hasEnquiries = true;
                Display.displayRequest(request, UserType.OFFICER); // Display using Officer view
            }
        }
        if (!hasEnquiries) {
            System.out.println("There are no enquiries specifically for project ID: " + projectID + ".");
        }
    }

    /**
     * Records an answer provided by the officer for a specific enquiry.
     * Updates the {@link Enquiry} object with the answer text and sets its status to DONE.
     * Note: This method assumes the {@code requestID} corresponds to a valid Enquiry
     * and performs no validation checks itself (checks might be done in the calling boundary class).
     *
     * @param requestID The ID of the {@link Enquiry} request to answer.
     * @param answerText The text of the answer provided by the officer.
     */
    public static void answerEnquiry(String requestID, String answerText) {
        Request request = RequestList.getInstance().getByID(requestID);

        // Basic validation: Check if request exists and is an Enquiry
        if (!(request instanceof Enquiry enquiry)) { // Use pattern variable binding
             System.out.println("Failed to answer: Request ID '" + requestID + "' not found or is not an enquiry.");
             return;
        }

        // Update the enquiry object
        enquiry.setAnswer(answerText);
        enquiry.setRequestStatus(RequestStatus.DONE); // Mark as answered

        // Save the updated enquiry back to the list
        RequestList.getInstance().update(requestID, enquiry);
        System.out.println("Enquiry ID '" + requestID + "' answered successfully.");
    }
}
