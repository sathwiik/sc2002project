package controller;

import java.util.List;

import entity.list.ApplicantList;
import entity.list.ManagerList;
import entity.list.OfficerList;
import entity.list.ProjectList;
import entity.list.RequestList;
import entity.project.FlatType;
import entity.project.Project;
import entity.request.ApprovedStatus;
import entity.request.BTOApplication;
import entity.request.BTOWithdrawal;
import entity.request.Enquiry;
import entity.request.OfficerRegistration;
import entity.request.Request;
import entity.request.RequestStatus;
import entity.request.RequestType;
import entity.user.Applicant;
import entity.user.ApplicationStatus;
import entity.user.Manager;
import entity.user.Officer;
import entity.user.RegistrationStatus;
import entity.user.UserType;
import utils.Display;

/**
 * Controller responsible for handling request-related operations from a Manager's perspective.
 * This includes viewing various request types (applications, withdrawals, registrations),
 * viewing enquiries associated with the manager's projects, viewing all enquiries,
 * and changing the status of different request types, often involving complex cascading logic.
 * It operates using the context of the currently logged-in manager's ID.
 */
public class ManagerRequestController {
    /**
     * Private constructor to prevent instantiation of this  class.
     * Throwing an error ensures it's not accidentally called via reflection.
     */
    private ManagerRequestController() {
        // Prevent instantiation
        throw new IllegalStateException("This class should not be instantiated");
    }
    /**
     * Stores the user ID of the manager currently interacting with the system.
     * This ID is typically set by the {@link AccountController} upon successful login of a Manager
     * and is used to filter requests relevant to this manager (e.g., enquiries for their projects).
     */
    private static String managerID;

    /**
     * Sets the manager ID for the current session context.
     * Subsequent manager-specific operations in this controller will use this ID.
     *
     * @param ID The user ID of the currently logged-in manager.
     */
    public static void setManagerID(String ID) {
        managerID = ID;
    }

    /**
     * Displays all requests from the {@link RequestList}, excluding Enquiries.
     * Uses the {@link UserType#MANAGER} display format.
     */
    public static void viewRequest() {
        List<Request> list = RequestList.getInstance().getAll();
        boolean found = false;
        System.out.println("--- All Requests (excluding Enquiries) ---"); // Header
        for (Request request : list) {
            if (request.getRequestType() != RequestType.ENQUIRY) {
                Display.displayRequest(request, UserType.MANAGER);
                found = true;
            }
        }
         if (!found) {
              System.out.println("No requests (excluding enquiries) found.");
         }
    }

    /**
     * Displays specific types of requests using the APPLICANT display format.
     * The behavior depends on the boolean parameter:
     * - If {@code applicant} is true, displays BTO Applications and BTO Withdrawals.
     * - If {@code applicant} is false, displays Officer Registrations.
     * Note: The use of {@code UserType.APPLICANT} for display might be inconsistent, especially for Officer Registrations.
     *
     * @param applicant If true, display applicant-related requests (BTO Application/Withdrawal);
     * if false, display officer-related requests (Registration).
     */
    public static void viewRequest(boolean applicant) {
        List<Request> list = RequestList.getInstance().getAll();
        boolean found = false;
         String type = applicant ? "Applicant BTO Applications/Withdrawals" : "Officer Registrations";
         System.out.println("--- Viewing " + type + " (Displayed as Applicant View) ---"); // Header

        for (Request request : list) {
            boolean displayApplicantRequests = applicant && (request.getRequestType() == RequestType.BTO_APPLICATION || request.getRequestType() == RequestType.BTO_WITHDRAWAL);
            boolean displayOfficerRequests = !applicant && request.getRequestType() == RequestType.REGISTRATION;

            if (displayApplicantRequests || displayOfficerRequests) {
                 // Uses Applicant display format regardless of request type based on the code.
                Display.displayRequest(request, UserType.APPLICANT);
                found = true;
            }
        }
         if (!found) {
              System.out.println("No requests of the specified type found.");
         }
    }

    /**
     * Changes the overall status (e.g., PENDING, DONE) of a specific request.
     *
     * @param requestID The ID of the request to update.
     * @param status    The new {@link RequestStatus} to set.
     */
    public static void changeRequestStatus(String requestID, RequestStatus status) {
        Request request = RequestList.getInstance().getByID(requestID);
        if (request != null) {
            request.setRequestStatus(status);
            RequestList.getInstance().update(requestID, request);
            System.out.println("Request " + requestID + " status updated to " + status + ".");
        } else {
             System.out.println("Failed to update request status: Request ID '" + requestID + "' not found.");
        }
    }

    /**
     * Changes the approval status (PENDING, SUCCESSFUL, UNSUCCESSFUL) for specific types of applications
     * (BTO Application, BTO Withdrawal, Officer Registration) and triggers related cascading actions.
     * Also updates the overall request status to DONE if the approval status is not PENDING.
     *
     * @param requestID The ID of the application request to update.
     * @param status    The new {@link ApprovedStatus} to set.
     */
    public static void changeApplicationStatus(String requestID, ApprovedStatus status) {
        Request request = RequestList.getInstance().getByID(requestID);
         if (request == null) {
              System.out.println("Failed to change application status: Request ID '" + requestID + "' not found.");
              return;
         }

        // --- Handle BTO Application ---
        if (request instanceof BTOApplication application) {
            String projectID = application.getProjectID();
            String applicantUserID = application.getUserID();
            Applicant applicant = ApplicantList.getInstance().getByID(applicantUserID);

            if (applicant != null) {
                // Update applicant's application status based on approval
                if (status == ApprovedStatus.SUCCESSFUL) {
                     applicant.setApplicationStatusByID(projectID, ApplicationStatus.SUCCESSFUL);
                } else if (status == ApprovedStatus.UNSUCCESSFUL) {
                     applicant.setApplicationStatusByID(projectID, ApplicationStatus.UNSUCCESSFUL);
                     applicant.setProject(null); // Unlink project if unsuccessful
                     applicant.setAppliedFlatByID(projectID, null); // Clear applied flat
                } else if (status == ApprovedStatus.PENDING) {
                     applicant.setApplicationStatusByID(projectID, ApplicationStatus.PENDING);
                }
                ApplicantList.getInstance().update(applicantUserID, applicant); // Save applicant changes
            } else {
                 System.err.println("Warning: Applicant " + applicantUserID + " not found while processing BTO Application " + requestID);
            }
            // Update the request itself
            application.setApplicationStatus(status);
            RequestList.getInstance().update(requestID, application);
            System.out.println("BTO Application " + requestID + " status changed to " + status + ".");

        // --- Handle BTO Withdrawal ---
        } else if (request instanceof BTOWithdrawal withdrawal) {
            // Update the withdrawal request status
            withdrawal.setWithdrawalStatus(status);
            RequestList.getInstance().update(requestID, withdrawal);

            // If withdrawal is successful, handle consequences
            if (status == ApprovedStatus.SUCCESSFUL) {
                 String applicantUserID = withdrawal.getUserID();
                 String projectID = withdrawal.getProjectID();
                 Applicant applicant = ApplicantList.getInstance().getByID(applicantUserID);
                 Project project = ProjectList.getInstance().getByID(projectID);

                 // Find the original BTO Application for this user/project
                Request originalBTOApplication = null;
                for (Request r : RequestList.getInstance().getAll()) {
                    if (r.getProjectID().equals(projectID) && r.getUserID().equals(applicantUserID) && r instanceof BTOApplication) {
                        originalBTOApplication = r;
                        break; // Assume only one active BTO application per user/project
                    }
                }

                // If original application found, mark it unsuccessful
                if (originalBTOApplication != null) {
                    changeApplicationStatus(originalBTOApplication.getRequestID(), ApprovedStatus.UNSUCCESSFUL);
                    // The above call already unlinks project/flat and sets status in Applicant obj
                } else {
                     // If no original BTO found, still ensure applicant is unlinked
                     if (applicant != null) {
                          applicant.setProject(null);
                          applicant.setApplicationStatusByID(projectID, ApplicationStatus.UNSUCCESSFUL); // Ensure status reflects withdrawal
                          applicant.setAppliedFlatByID(projectID, null);
                          ApplicantList.getInstance().update(applicantUserID, applicant);
                     }
                     System.err.println("Warning: Could not find original BTO application for successful withdrawal " + requestID);
                }

                // If applicant had already booked a flat, release the unit
                if (applicant != null && project != null && applicant.getApplicationStatusByID(projectID) == ApplicationStatus.BOOKED) {
                    List<String> applicantsInProject = project.getApplicantID();
                    if (applicantsInProject != null && applicantsInProject.contains(applicantUserID)) {
                         applicantsInProject.remove(applicantUserID);
                         project.setApplicantID(applicantsInProject);
                         FlatType appliedFlat = applicant.getAppliedFlatByID(projectID);
                         if (appliedFlat != null) { // Increment unit count if flat type known
                              project.setAvailableUnit(appliedFlat, project.getAvailableUnit().getOrDefault(appliedFlat, 0) + 1);
                         }
                         ProjectList.getInstance().update(projectID, project); // Save project changes
                         System.out.println("Released booked flat for applicant " + applicantUserID + " in project " + projectID);
                    }
                }
                System.out.println("BTO Withdrawal " + requestID + " approved. Applicant status updated.");
            } else {
                 System.out.println("BTO Withdrawal " + requestID + " status changed to " + status + ".");
            }

        // --- Handle Officer Registration ---
        } else if (request instanceof OfficerRegistration registration) {
            String officerUserID = registration.getUserID();
            String projectID = registration.getProjectID();
            Officer officer = OfficerList.getInstance().getByID(officerUserID);
            Project project = ProjectList.getInstance().getByID(projectID);

            if (officer != null && project != null) {
                boolean projectUpdated = false;
                boolean officerUpdated = false;

                // Logic for SUCCESSFUL registration
                if (status == ApprovedStatus.SUCCESSFUL) {
                    officer.setRegistrationStatusByID(projectID, RegistrationStatus.APPROVED);
                    officerUpdated = true;
                    // Check vacancy before adding officer
                    if (project.getAvailableOfficer() > 0) {
                        List<String> officersInProject = project.getOfficerID();
                        if (officersInProject == null) officersInProject = new java.util.ArrayList<>();
                        // Add officer to project if not already present
                        if (!officersInProject.contains(officerUserID)) {
                            officersInProject.add(officerUserID);
                            project.setOfficerID(officersInProject);
                            project.setAvailableOfficer(project.getAvailableOfficer() - 1); // Decrement available slot
                            projectUpdated = true;
                        }
                        // Add project to officer's list if not already present
                        List<String> projectsForOfficer = officer.getOfficerProject();
                        if (projectsForOfficer == null) projectsForOfficer = new java.util.ArrayList<>();
                        if (!projectsForOfficer.contains(projectID)) {
                            projectsForOfficer.add(projectID);
                            officer.setOfficerProject(projectsForOfficer);
                            // officerUpdated is already true
                        }
                         System.out.println("Officer Registration " + requestID + " approved.");
                    } else {
                        System.out.println("Officer Registration " + requestID + " approved, but no vacancy available in project " + projectID + ". Officer not assigned.");
                        // Set status back? Or keep approved but not assigned? Current logic keeps approved.
                        officer.setRegistrationStatusByID(projectID, RegistrationStatus.APPROVED); // Still approved request-wise
                    }
                }
                // Logic for UNSUCCESSFUL registration
                else if (status == ApprovedStatus.UNSUCCESSFUL) {
                    officer.setRegistrationStatusByID(projectID, RegistrationStatus.REJECTED);
                    officerUpdated = true;
                    // Remove officer from project if present
                    List<String> officersInProject = project.getOfficerID();
                    if (officersInProject != null && officersInProject.contains(officerUserID)) {
                        officersInProject.remove(officerUserID);
                        project.setOfficerID(officersInProject);
                        project.setAvailableOfficer(project.getAvailableOfficer() + 1); // Increment available slot
                        projectUpdated = true;
                    }
                    // Remove project from officer's list if present
                    List<String> projectsForOfficer = officer.getOfficerProject();
                    if (projectsForOfficer != null && projectsForOfficer.contains(projectID)) {
                        projectsForOfficer.remove(projectID);
                        officer.setOfficerProject(projectsForOfficer);
                        // officerUpdated is already true
                    }
                     System.out.println("Officer Registration " + requestID + " rejected.");
                }
                 // Logic for PENDING registration (usually just sets status)
                 else if (status == ApprovedStatus.PENDING) {
                      officer.setRegistrationStatusByID(projectID, RegistrationStatus.PENDING);
                      officerUpdated = true;
                      System.out.println("Officer Registration " + requestID + " status set to Pending.");
                 }


                // Update entities if changes occurred
                if (officerUpdated) OfficerList.getInstance().update(officerUserID, officer);
                if (projectUpdated) ProjectList.getInstance().update(projectID, project);

            } else {
                 System.err.println("Warning: Officer " + officerUserID + " or Project " + projectID + " not found while processing Officer Registration " + requestID);
            }
            // Update the request itself
            registration.setRegistrationStatus(status);
            RequestList.getInstance().update(requestID, registration);

        } else {
             System.out.println("Cannot change application status: Request ID '" + requestID + "' is not an application/withdrawal/registration type.");
             return; // Exit if not a relevant type
        }

        // Update overall request status if approval status is decisive
        if (status != ApprovedStatus.PENDING) {
            changeRequestStatus(requestID, RequestStatus.DONE);
        } else {
            changeRequestStatus(requestID, RequestStatus.PENDING);
        }
    }

    /**
     * Displays all enquiries associated with the projects managed by the currently set manager.
     * Iterates through all requests, checks if it's an Enquiry, and if its project ID
     * is in the current manager's list of managed projects.
     */
    public static void viewEnquiries() {
        List<Request> list = RequestList.getInstance().getAll();
        Manager manager = ManagerList.getInstance().getByID(managerID);
        if (manager == null) {
             System.out.println("Cannot view enquiries: Manager ID " + managerID + " not found.");
             return;
        }
        List<String> managedProjectIDs = manager.getProject();
        if (managedProjectIDs == null || managedProjectIDs.isEmpty()) {
             System.out.println("You are not currently managing any projects. No enquiries to display.");
             return;
        }

        boolean hasEnquiries = false; // Track if relevant enquiries are found
        System.out.println("--- Enquiries for Projects Managed by " + manager.getName() + " ---"); // Header
        for (Request request : list) {
            // Check if it's an enquiry AND if its project ID is managed by this manager
            if (request instanceof Enquiry && managedProjectIDs.contains(request.getProjectID())) {
                hasEnquiries = true;
                Display.displayRequest(request, UserType.MANAGER); // Display using Manager view
            }
        }
        if (!hasEnquiries) {
            System.out.println("There are no enquiries for the projects you manage.");
        }
    }

    /**
     * Displays all Enquiry requests present in the system, regardless of the project or manager.
     * Uses the {@link UserType#MANAGER} display format.
     */
    public static void viewAllEnquiries() {
        List<Request> list = RequestList.getInstance().getAll();
        boolean hasEnquiries = false; // Track if any enquiries exist
        System.out.println("--- All Enquiries in the System ---"); // Header
        for (Request request : list) {
            if (request instanceof Enquiry) { // Check if it's an Enquiry type
                hasEnquiries = true;
                Display.displayRequest(request, UserType.MANAGER); // Display using Manager view
            }
        }
         if (!hasEnquiries) {
             System.out.println("No enquiries found in the system.");
         }
    }
}
