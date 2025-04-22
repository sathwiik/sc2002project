package controller;

import java.time.LocalDate;
import java.util.List;

import entity.project.FlatType;
import entity.project.Project;
import entity.request.*;
import entity.user.Applicant;
import entity.user.ApplicationStatus;
import entity.user.MaritalStatus;
import entity.user.UserType;
import exception.ProjectNotFoundException;
import utils.Display;
import utils.IDController;
import utils.UIController;
import entity.list.ApplicantList;
import entity.list.ProjectList;
import entity.list.RequestList;

/**
 * Controller responsible for handling business logic related to actions performed by Applicants.
 * This includes checking project eligibility, viewing project lists, applying for projects,
 * withdrawing applications, and managing personal enquiries (view, create, edit, delete).
 * It operates based on the currently logged-in applicant's ID.
 */
public class ApplicantController {

    /**
     * Stores the user ID of the applicant currently interacting with the system.
     * This ID is typically set by the {@link AccountController} upon successful login.
     */
    private static String applicantID;

    /**
     * Sets the applicant ID for the current session context.
     * All subsequent operations in this controller will be performed for this applicant.
     *
     * @param ID The user ID of the currently logged-in applicant.
     */
    public static void setApplicantID(String ID) {
        applicantID = ID;
    }

    /**
     * Checks if the currently set applicant is eligible to apply for a given project.
     * Eligibility criteria include:
     * - Project visibility is true.
     * - Applicant is not an officer assigned to the project.
     * - Current date is within the project's application open and close dates.
     * - Applicant meets age and marital status requirements for specific flat types:
     * - Age >= 35 and Single: Eligible for TWO_ROOM.
     * - Age >= 21 and Married: Eligible for THREE_ROOM (implicitly includes TWO_ROOM).
     *
     * @param projectID The ID of the project to check eligibility for.
     * @return The maximum {@link FlatType} the applicant is eligible for (THREE_ROOM implies eligibility for TWO_ROOM as well),
     * or null if the applicant is not eligible for the project based on the criteria.
     */
    public static FlatType checkApplicable(String projectID) {
        Project project = ProjectList.getInstance().getByID(projectID);
        // Ensure project exists before proceeding
        if (project == null) {
             System.err.println("Warning: Project not found in checkApplicable for ID: " + projectID);
             return null;
        }
        Applicant applicant = ApplicantList.getInstance().getByID(applicantID);
        // Ensure applicant exists before proceeding
        if (applicant == null) {
             System.err.println("Warning: Applicant not found in checkApplicable for ID: " + applicantID);
             return null;
        }

        // Check basic project conditions
        boolean projectConditionsMet = project.getVisibility() &&
                                      !project.getOfficerID().contains(applicantID) && // Applicant is not an officer for this project
                                      project.getOpenDate().isBefore(LocalDate.now()) && // Application period is open
                                      project.getCloseDate().isAfter(LocalDate.now());

        if (projectConditionsMet) {
            // Check eligibility based on age and marital status
            if (applicant.getAge() >= 21 && applicant.getMaritalStatus() == MaritalStatus.MARRIED) {
                // Eligible for up to Three Room
                return FlatType.THREE_ROOM;
            } else if (applicant.getAge() >= 35 && applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
                 // Eligible for up to Two Room
                return FlatType.TWO_ROOM;
            }
        }
        // Not eligible if conditions aren't met
        return null;
    }

    /**
     * Displays a list of projects that the current applicant is eligible to apply for.
     * The list is first filtered using {@link FilterController}.
     * For each eligible project, it indicates the maximum flat type the applicant can apply for.
     */
    public static void viewApplicableProject() {
        List<Project> list = ProjectList.getInstance().getAll();
        list = FilterController.filteredList(list); // Apply user-defined filters
        boolean hasApplicable = false; // Changed variable name for clarity

        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                     Applicable Projects for You");
        System.out.println(UIController.LINE_SEPARATOR);

        for (Project project : list) {
            FlatType maxEligibleFlatType = checkApplicable(project.getProjectID());
            // Display project if eligible for any flat type
            if (maxEligibleFlatType != null) {
                hasApplicable = true;
                // Display based on eligibility: null means up to 3-room, THREE_ROOM means only 2-room.
                // The display logic seems reversed: if eligible for THREE_ROOM, show all (null). If only TWO_ROOM, hide THREE_ROOM info.
                // Let's adjust the Display call based on this interpretation.
                 if (maxEligibleFlatType == FlatType.THREE_ROOM) {
                      // Eligible for 3-room (and implicitly 2-room)
                      Display.displayProject(project, UserType.APPLICANT, null); // null hides no flat types
                 } else if (maxEligibleFlatType == FlatType.TWO_ROOM) {
                      // Eligible only for 2-room
                      Display.displayProject(project, UserType.APPLICANT, FlatType.THREE_ROOM); // Pass THREE_ROOM to potentially hide it in display
                 }
            }
        }
        if (!hasApplicable) {
            System.out.println("There are no projects currently applicable to you based on criteria and filters.");
        }
    }

    /**
     * Displays the current applicant's pending and historical BTO applications and withdrawals.
     * Separates the display into current pending requests and completed request history.
     */
    public static void viewAppliedApplication(){
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                      Your Current Applications/Withdrawals");
        System.out.println(UIController.LINE_SEPARATOR);
        boolean hasPending = false; // Track if pending requests exist
        List<Request> requests = RequestList.getInstance().getAll();
        // Display Pending BTO Applications or Withdrawals
        for (Request request : requests) {
            if (request.getUserID().equals(applicantID) &&
                request.getRequestStatus() == RequestStatus.PENDING &&
                (request.getRequestType() == RequestType.BTO_APPLICATION || request.getRequestType() == RequestType.BTO_WITHDRAWAL)) {
                hasPending = true;
                Display.displayRequest(request, UserType.APPLICANT);
            }
        }
        if (!hasPending) {
            System.out.println("You have no pending BTO applications or withdrawals.");
        }

        // Display Application/Withdrawal History (Done status)
        boolean hasHistory = false; // Track if history exists
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                      Your Application/Withdrawal History");
        System.out.println(UIController.LINE_SEPARATOR);
        for (Request request : requests) {
            if (request.getUserID().equals(applicantID) &&
                request.getRequestStatus() == RequestStatus.DONE &&
                (request.getRequestType() == RequestType.BTO_APPLICATION || request.getRequestType() == RequestType.BTO_WITHDRAWAL)) {
                hasHistory = true;
                Display.displayRequest(request, UserType.APPLICANT);
            }
        }
        if (!hasHistory) {
            System.out.println("No application or withdrawal history found.");
        }
    }

    /**
     * Displays details of the single project the applicant has currently applied to, if any.
     * Includes the current application status for that project.
     */
    public static void viewAppliedProject() {
        Applicant applicant = ApplicantList.getInstance().getByID(applicantID);
        String currentProjectID = applicant.getProject(); // Get the ID of the project applicant applied to
        Project currentProject = null;
        if (currentProjectID != null) {
             currentProject = ProjectList.getInstance().getByID(currentProjectID);
        }

        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                           Your Applied Project");
        System.out.println(UIController.LINE_SEPARATOR);

        if (currentProject == null) {
            System.out.println("You have not applied for any project currently.");
        } else {
            // Display the status first
            ApplicationStatus status = applicant.getApplicationStatusByID(currentProjectID);
            System.out.println("Your Application Status: " + (status != null ? status.coloredString() : "N/A"));

            // Display project details, adjusting based on what the applicant was eligible for when applying (inferred)
            // We might not know the *original* max eligibility, so display full details.
            Display.displayProject(currentProject, UserType.APPLICANT, null); // Show all flat types
        }
    }

    /**
     * Processes a project application request for the current applicant.
     * Performs checks:
     * - Applicant hasn't already applied for another project.
     * - Project exists.
     * - Applicant is eligible for the specified project (using {@link #checkApplicable(String)}).
     * - The requested flat type has available units.
     * If all checks pass, updates the applicant's record (sets project, applied flat type, status to PENDING)
     * and creates a new {@link BTOApplication} request in the RequestList.
     *
     * @param projectID The ID of the project to apply for.
     * @param applyFlat The {@link FlatType} the applicant wants to apply for.
     * @throws ProjectNotFoundException If the specified projectID does not exist.
     */
    public static void applyProject(String projectID, FlatType applyFlat) throws ProjectNotFoundException {
        Applicant applicant = ApplicantList.getInstance().getByID(applicantID);
        // Check 1: Applicant already applied?
        if (applicant.getProject() != null) {
            System.out.println("Application failed: You have already applied for project ID: " + applicant.getProject());
            return;
        }
        // Check 2: Project exists?
        Project project = ProjectList.getInstance().getByID(projectID);
        if (project == null) throw new ProjectNotFoundException();
        // Check 3: Applicant eligible for this project?
        FlatType maxEligibleType = checkApplicable(projectID);
        if (maxEligibleType == null) {
            System.out.println("Application failed: You are not eligible to apply for this project based on current criteria.");
            return;
        }
        // Check 3b: Applicant eligible for the *specific* flat type requested?
        if (applyFlat == FlatType.THREE_ROOM && maxEligibleType != FlatType.THREE_ROOM) {
             System.out.println("Application failed: You are not eligible to apply for a Three Room flat based on your profile.");
             return;
        }
        // Check 4: Units available?
        if (project.getAvailableUnit().getOrDefault(applyFlat, 0) <= 0) {
            System.out.println("Application failed: There are no available units of the selected flat type (" + applyFlat + ") in this project.");
            return;
        }

        // All checks passed, proceed with application
        applicant.setAppliedFlatByID(projectID, applyFlat); // Record the flat type applied for
        applicant.setProject(projectID); // Link applicant to the project ID
        applicant.setApplicationStatusByID(projectID, ApplicationStatus.PENDING); // Set initial status
        ApplicantList.getInstance().update(applicantID, applicant); // Save changes to applicant

        // Create a corresponding request record
        RequestList.getInstance().add(new BTOApplication(IDController.newRequestID(), RequestType.BTO_APPLICATION, applicantID, projectID, RequestStatus.PENDING));
        System.out.println("Successfully applied for project ID: " + projectID);
    }

    /**
     * Submits a request for the current applicant to withdraw their application from a specific project.
     * Performs checks:
     * - Project exists.
     * - Applicant has a pending application for this project.
     * - Applicant has not already submitted a withdrawal request for this project.
     * If checks pass, creates a new {@link BTOWithdrawal} request in the RequestList.
     *
     * @param projectID The ID of the project from which to withdraw the application.
     * @throws ProjectNotFoundException If the specified projectID does not exist.
     */
    public static void withdrawApplication(String projectID) throws ProjectNotFoundException {
        // Check 1: Project exists?
        if (ProjectList.getInstance().getByID(projectID) == null) throw new ProjectNotFoundException();

        boolean hasApplied = false;       // Flag if an application exists for this user/project
        boolean alreadyWithdrawn = false; // Flag if a withdrawal request already exists

        // Check requests for existing application and withdrawal
        for (Request r : RequestList.getInstance().getAll()) {
            if (r.getProjectID().equals(projectID) && r.getUserID().equals(applicantID)) {
                if (r instanceof BTOApplication && r.getRequestStatus() == RequestStatus.PENDING) {
                    hasApplied = true; // Found a pending application
                } else if (r instanceof BTOWithdrawal && r.getRequestStatus() == RequestStatus.PENDING) {
                    alreadyWithdrawn = true; // Found a pending withdrawal request
                }
            }
        }

        // Check conditions and proceed or report error
        if (hasApplied && !alreadyWithdrawn) {
            // Create a new withdrawal request
            RequestList.getInstance().add(new BTOWithdrawal(IDController.newRequestID(), RequestType.BTO_WITHDRAWAL, applicantID, projectID, RequestStatus.PENDING));
            // Also update applicant status immediately? Or wait for manager approval?
            // Current logic only adds request. Consider if applicant status should change here.
             Applicant applicant = ApplicantList.getInstance().getByID(applicantID);
             applicant.setApplicationStatusByID(projectID, ApplicationStatus.WITHDRAWN); // Set status to withdrawn
             applicant.setProject(null); // Unlink project from applicant
             applicant.setAppliedFlatByID(projectID, null); // Clear applied flat type
             ApplicantList.getInstance().update(applicantID, applicant);
            System.out.println("Withdrawal request submitted successfully for project ID: " + projectID + ". Your application is now marked as withdrawn.");
        } else if (!hasApplied) {
            System.out.println("Withdrawal failed: You do not have a pending application for this project.");
        } else { // Implies (hasApplied && alreadyWithdrawn) or (!hasApplied && alreadyWithdrawn - less likely)
            System.out.println("Withdrawal failed: You have already submitted a withdrawal request for this project.");
        }
    }

    /**
     * Submits an enquiry from the current applicant regarding a specific project.
     * Checks if the applicant is generally eligible (relevant) to enquire about the project using {@link #checkApplicable(String)}.
     * If relevant, creates a new {@link Enquiry} request in the RequestList.
     *
     * @param projectID The ID of the project the enquiry is about.
     * @param text      The text content of the enquiry.
     */
    public static void query(String projectID, String text) {
        // Check if the project is relevant to the applicant (e.g., they are eligible)
        // Using checkApplicable might be too strict if they just want to ask general questions.
        // Consider relaxing this check if needed, e.g., just check if project exists and is visible.
        Project project = ProjectList.getInstance().getByID(projectID);
         if (project == null || !project.getVisibility()) { // Basic check: project exists and is visible
             System.out.println("Unable to submit enquiry: Project not found or is not visible.");
             return;
         }
        // Removed checkApplicable check to allow enquiries on visible projects regardless of strict eligibility.
        // if (checkApplicable(projectID) == null) {
        //     System.out.println("Unable to enquire about an irrelevant project.");
        //     return;
        // }

        // Add the enquiry request
        RequestList.getInstance().add(new Enquiry(IDController.newRequestID(), RequestType.ENQUIRY, applicantID, projectID, RequestStatus.PENDING, text));
        System.out.println("Enquiry submitted successfully for project ID: " + projectID); // Confirmation
    }

    /**
     * Displays all enquiries previously submitted by the current applicant.
     */
    public static void viewQuery() {
        List<Request> list = RequestList.getInstance().getAll();
        boolean hasEnquiries = false; // Track if any enquiries exist

        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                         Your Submitted Enquiries");
        System.out.println(UIController.LINE_SEPARATOR);

        for (Request request : list) {
            // Check if it's an enquiry and belongs to the current applicant
            if (request.getUserID().equals(applicantID) && request instanceof Enquiry) {
                hasEnquiries = true;
                Display.displayRequest(request, UserType.APPLICANT); // Display the enquiry details
            }
        }
        if (!hasEnquiries) {
            System.out.println("You have not submitted any enquiries.");
        }
    }

    /**
     * Checks if the current applicant is allowed to modify (edit/delete) a specific enquiry.
     * Conditions for modification:
     * - Request ID must exist.
     * - Request must be an Enquiry.
     * - Enquiry status must be PENDING (cannot modify answered enquiries).
     * - Enquiry must belong to the current applicant.
     *
     * @param requestID The ID of the enquiry request to check.
     * @return true if the applicant is allowed to modify the enquiry, false otherwise.
     */
    public static boolean checkQuery(String requestID) {
        Request query = RequestList.getInstance().getByID(requestID);
        // Check 1: Exists?
        if (query == null) {
            System.out.println("Error: Request ID '" + requestID + "' not found.");
            return false;
        }
        // Check 2: Is Enquiry?
        if (!(query instanceof Enquiry)) {
            System.out.println("Error: Request ID '" + requestID + "' does not correspond to an enquiry.");
            return false;
        }
        // Check 3: Status is Pending?
        if (query.getRequestStatus() != RequestStatus.PENDING) {
            System.out.println("Error: Cannot modify an enquiry that has already been processed (Status: " + query.getRequestStatus() + ").");
            return false;
        }
        // Check 4: Belongs to user?
        if (!query.getUserID().equals(applicantID)) {
            System.out.println("Error: You are not allowed to modify an enquiry submitted by another user.");
            return false;
        }
        // All checks passed
        return true;
    }

    /**
     * Edits the text content of an existing enquiry submitted by the current applicant.
     * First performs validation using {@link #checkQuery(String)}.
     * If validation passes, updates the enquiry in the RequestList with the new text.
     *
     * @param requestID The ID of the enquiry to edit.
     * @param newText   The new text content for the enquiry.
     */
    public static void editQuery(String requestID, String newText) {
        if (!checkQuery(requestID)) return; // Perform checks first

        // Retrieve the original project ID before creating the new object
        Request originalQuery = RequestList.getInstance().getByID(requestID);
        String projectID = originalQuery.getProjectID(); // Get project ID from original

        // Update the request list with a new Enquiry object containing the updated text
        RequestList.getInstance().update(requestID, new Enquiry(requestID, RequestType.ENQUIRY, applicantID, projectID, RequestStatus.PENDING, newText));
        System.out.println("Enquiry (ID: " + requestID + ") updated successfully.");
    }

    /**
     * Deletes an existing enquiry submitted by the current applicant.
     * First performs validation using {@link #checkQuery(String)}.
     * If validation passes, removes the enquiry from the RequestList.
     *
     * @param requestID The ID of the enquiry to delete.
     */
    public static void deleteQuery(String requestID) {
        if (!checkQuery(requestID)) return; // Perform checks first

        RequestList.getInstance().delete(requestID); // Delete the enquiry
        System.out.println("Enquiry (ID: " + requestID + ") deleted successfully.");
    }
}
