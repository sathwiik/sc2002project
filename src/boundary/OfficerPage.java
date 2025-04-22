package boundary;

import java.util.List;
import controller.AccountController;
import controller.ApplicantController;
import controller.FilterController;
import controller.OfficerRequestController;
import controller.OfficerProjectController;
import entity.list.ApplicantList;
import entity.list.OfficerList;
import entity.list.ProjectList;
import entity.list.RequestList;
import entity.project.FlatType;
import entity.project.Project;
import entity.request.Enquiry;
import entity.request.Request;
import entity.request.RequestStatus;
import entity.user.Applicant;
import entity.user.MaritalStatus;
import exception.ProjectNotFoundException;
import utils.Display;
import utils.IOController;
import utils.UIController;

/**
 * Represents the boundary layer for handling interactions for users logged in as Officers.
 * This page provides a combined menu allowing officers to perform actions both as an applicant
 * (applying for projects, managing personal queries) and as an officer
 * (registering for projects, managing applicant requests/enquiries, booking flats, generating receipts).
 */
public class OfficerPage {
    /**
     * Private constructor to prevent instantiation of this  class.
     * Throwing an error ensures it's not accidentally called via reflection.
     */
    private OfficerPage() {
        // Prevent instantiation
        throw new IllegalStateException("This class should not be instantiated");
    }
    /**
     * Displays the main menu options available to the logged-in officer.
     * This menu includes options for both applicant-related actions and officer-specific duties.
     * Reads the officer's choice and navigates to the corresponding functionality.
     * Handles invalid input and loops back to the main menu or exits the application.
     */
    public static void allOptions() {
        UIController.clearPage();
        System.out.println(UIController.LINE_SEPARATOR);
        // ASCII Art Header
        System.out.println(
                "  __  ____  ____  __  ___  ____  ____    ____   __    ___  ____ \n" + //
                " /  \\(  __)(  __)(  )/ __)(  __)(  _ \\  (  _ \\ / _\\  / __)(  __)\n" + //
                "(  O )) _)  ) _)  )(( (__  ) _)  )   /   ) __//    \\( (_ \\ ) _) \n" + //
                " \\__/(__)  (__)  (__)\\___)(____)(__\\_)  (__)  \\_/\\_/ \\___/(____)\n" + //
                "");
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("Welcome, " + OfficerList.getInstance().getByID(AccountController.getUserID()).getName() + ". Please enter your choice."
                // Combined Applicant & Officer Options
                + "\n\t--- Your Profile & Applicant Actions ---" // Grouping Header
                + "\n\t0. View Profile (Officer & Applicant)"
                + "\n\t1. View Applicable Project (as Applicant)"
                + "\n\t2. View Applied Projects (as Applicant)"
                + "\n\t3. Apply for Project (as Applicant)"
                + "\n\t4. Withdraw Application (as Applicant)"
                + "\n\t5. Make Query (as Applicant)"
                + "\n\t6. View Your Query (as Applicant)"
                + "\n\t7. Edit Your Query (as Applicant)"
                + "\n\t8. Delete Your Query (as Applicant)"
                + "\n\t--- Officer Duties ---" // Grouping Header
                + "\n\t9. View Registrable Project List"
                + "\n\t10. View Your Registered Projects"
                + "\n\t11. View Your Registration Application Status"
                + "\n\t12. Register for Project as Officer"
                + "\n\t13. View All Enquiries (Assigned/Related)"
                + "\n\t14. View Enquiry by Project ID"
                + "\n\t15. Answer Enquiries"
                + "\n\t16. View Applicant Application Status"
                + "\n\t17. Book Flat for Applicant"
                + "\n\t18. Generate General Receipt"
                + "\n\t19. Generate Receipt by Applicant ID"
                + "\n\t20. Generate Receipt by Project ID"
                + "\n\t--- Filters & System ---" // Grouping Header
                + "\n\t21. Set up Project Filter"
                + "\n\t22. View Your Current Filter"
                + "\n\t23. Sign out"
                + "\n\t24. Exit");
        System.out.print("Your choice (0-24): ");
        int option = IOController.nextInt();
        switch (option) {
            // Profile
            case 0 -> {
                // Display both Officer and Applicant details if applicable
                Display.displayOfficer(OfficerList.getInstance().getByID(AccountController.getUserID()));
                // Assuming officer might also have an applicant profile linked by same ID
                Display.displayApplicant(ApplicantList.getInstance().getByID(AccountController.getUserID()), true);
                UIController.loopOfficer();
            }
            // Applicant Actions
            case 1 -> viewApplicableProject();
            case 2 -> viewAppliedProject();
            case 3 -> applyProject();
            case 4 -> withdrawApplication();
            case 5 -> query();
            case 6 -> viewQuery();
            case 7 -> editQuery();
            case 8 -> deleteQuery();
            // Officer Project Registration/Status
            case 9 -> viewRegistrableProject();
            case 10 -> viewRegisteredProject();
            case 11 -> viewRegistrationStatus();
            case 12 -> registerProject();
            // Officer Enquiry/Request Management
            case 13 -> viewEnquiries();
            case 14 -> viewEnquiriesByProject();
            case 15 -> answerEnquiry();
            // Officer Applicant/Flat Management
            case 16 -> viewApplicantApplicationStatus();
            case 17 -> bookFlat();
            // Officer Receipt Generation
            case 18 -> generateReceipt();
            case 19 -> generateReceiptByApplicant();
            case 20 -> generateReceiptByProject();
            // Filters & System
            case 21 -> {
                FilterController.setup();
                UIController.loopOfficer();
            }
            case 22 -> {
                FilterController.displayFilter();
                UIController.loopOfficer();
            }
            case 23 -> AccountController.logout();
            case 24 -> UIController.exit();
            default -> {
                System.out.println("Invalid choice. Press ENTER to try again.");
                IOController.nextLine(); // Consume leftover newline
                allOptions();
            }
        }
    }

    // ========================================
    // Applicant Actions (using ApplicantController)
    // These methods allow the officer to act as an applicant.
    // ========================================

    /**
     * Displays projects that the officer (acting as an applicant) is eligible to apply for.
     * Delegates logic to {@link ApplicantController#viewApplicableProject()}.
     * Loops back to the officer menu.
     */
    public static void viewApplicableProject() {
        ApplicantController.viewApplicableProject();
        UIController.loopOfficer();
    }

    /**
     * Displays projects the officer (acting as an applicant) has applied for.
     * Delegates logic to {@link ApplicantController#viewAppliedProject()}.
     * Loops back to the officer menu.
     */
    public static void viewAppliedProject() {
        ApplicantController.viewAppliedProject();
        UIController.loopOfficer();
    }

    /**
     * Handles the process for the officer (acting as an applicant) to apply for a specific project.
     * Checks eligibility (if already applied, age, marital status). Prompts for project ID and flat type.
     * Delegates application logic to {@link ApplicantController#applyProject(String, FlatType)}.
     * Handles {@link ProjectNotFoundException}.
     * Loops back to the officer menu.
     */
    public static void applyProject() {
        Applicant applicant = ApplicantList.getInstance().getByID(AccountController.getUserID());
        if (applicant.getProject() != null) {
            System.out.println("You (as an applicant) are allowed to apply for only one project.");
            UIController.loopOfficer();
            return;
        }
        System.out.print("Enter the project ID to apply for (as an applicant): ");
        String projectID = IOController.nextLine();
        try {
            Project project = ProjectList.getInstance().getByID(projectID);
            if (project == null) throw new ProjectNotFoundException();
            // Eligibility check (copied from ApplicantPage logic)
            int able = 0; // 0=Not eligible, 1=Eligible for 2-room, 2=Eligible for 2/3-room
            if (applicant.getAge() >= 35 && applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
                able = 1;
            } else if (applicant.getAge() >= 21 && applicant.getMaritalStatus() == MaritalStatus.MARRIED) {
                able = 2;
            }
            if (able == 0) {
                System.out.println("You are not eligible to apply for a project based on age/marital status.");
                UIController.loopOfficer();
                return;
            }
            System.out.println("Select flat type to apply for:");
            System.out.println("\t1. Two Room");
            if (able == 2) System.out.println("\t2. Three Room");
            System.out.print("Your choice: ");
            int option = IOController.nextInt();
            while (option > able || option < 1) {
                System.out.print("Please enter a valid choice: ");
                option = IOController.nextInt();
            }
            FlatType applyFlat = option == 1 ? FlatType.TWO_ROOM : FlatType.THREE_ROOM;
            ApplicantController.applyProject(projectID, applyFlat);
            System.out.println("Application submitted successfully."); // Confirmation
            // No return needed here as loopOfficer is called next
        } catch (ProjectNotFoundException e) {
            System.out.println(e.getMessage());
        }
        UIController.loopOfficer(); // Loop back in both success and exception cases (after catch)
    }

    /**
     * Handles the process for the officer (acting as an applicant) to withdraw their project application.
     * Prompts for the project ID.
     * Delegates withdrawal logic to {@link ApplicantController#withdrawApplication(String)}.
     * Handles {@link ProjectNotFoundException}.
     * Loops back to the officer menu.
     */
    public static void withdrawApplication() {
        System.out.print("Enter the project ID to withdraw your application from: ");
        String projectID = IOController.nextLine();
        try {
            ApplicantController.withdrawApplication(projectID);
            System.out.println("Application withdrawn successfully."); // Confirmation
        } catch (ProjectNotFoundException e) {
            System.out.println(e.getMessage());
        }
        // Ensure loopOfficer is called regardless of try/catch outcome
        UIController.loopOfficer();
    }

    /**
     * Allows the officer (acting as an applicant) to submit a personal query about a specific project.
     * Prompts for project ID and query text. Checks if project exists.
     * Delegates query submission to {@link ApplicantController#query(String, String)}.
     * Loops back to the officer menu.
     */
    public static void query() {
        System.out.print("Enter the project ID to make a personal enquiry about: ");
        String projectID = IOController.nextLine();
        if (ProjectList.getInstance().getByID(projectID) == null) {
            System.out.println(new ProjectNotFoundException().getMessage());
            UIController.loopOfficer();
            return;
        }
        System.out.print("Enter your query text: ");
        String question = IOController.nextLine();
        ApplicantController.query(projectID, question);
        System.out.println("Your query has been submitted."); // Confirmation
        UIController.loopOfficer();
    }

    /**
     * Displays the personal queries submitted by the officer (acting as an applicant).
     * Delegates display logic to {@link ApplicantController#viewQuery()}.
     * Loops back to the officer menu.
     */
    public static void viewQuery() {
        ApplicantController.viewQuery();
        UIController.loopOfficer();
    }

    /**
     * Allows the officer (acting as an applicant) to edit one of their own submitted queries.
     * Prompts for the request ID of the query. Checks if the query belongs to the officer.
     * Prompts for the new query text.
     * Delegates editing logic to {@link ApplicantController#editQuery(String, String)}.
     * Loops back to the officer menu.
     */
    public static void editQuery() {
        System.out.print("Enter the request ID of your query to edit: ");
        String requestID = IOController.nextLine();
        // Check if the query exists and belongs to the current user (acting as applicant)
        if (!ApplicantController.checkQuery(requestID)) {
            // Assuming checkQuery prints an error message if not found/invalid
            UIController.loopOfficer();
            return;
        }
        System.out.print("Enter the new query text: ");
        String newQuery = IOController.nextLine();
        ApplicantController.editQuery(requestID, newQuery);
        System.out.println("Query updated successfully."); // Confirmation
        UIController.loopOfficer();
    }

    /**
     * Allows the officer (acting as an applicant) to delete one of their own submitted queries.
     * Prompts for the request ID of the query.
     * Delegates deletion logic to {@link ApplicantController#deleteQuery(String)}.
     * Loops back to the officer menu.
     */
    public static void deleteQuery() {
        System.out.print("Enter the request ID of your query to delete: ");
        String requestID = IOController.nextLine();
        ApplicantController.deleteQuery(requestID);
        System.out.println("Query deleted successfully (if it existed and belonged to you)."); // Confirmation
        UIController.loopOfficer();
    }


    // ========================================
    // Officer Duties - Project Registration & Status
    // ========================================

    /**
     * Allows the officer to register their interest in working on a specific project.
     * Prompts for the project ID.
     * Delegates registration logic to {@link OfficerRequestController#registerProject(String)}.
     * Loops back to the officer menu.
     */
    public static void registerProject() {
        System.out.print("Enter the project ID to register for as an officer: ");
        String projectID = IOController.nextLine();
        OfficerRequestController.registerProject(projectID); // Assumes controller handles confirmation/error messages
        UIController.loopOfficer();
    }

    /**
     * Displays the projects the officer is currently registered to work on.
     * Delegates display logic to {@link OfficerRequestController#viewRegisteredProject()}.
     * Loops back to the officer menu.
     */
    public static void viewRegisteredProject() {
        OfficerRequestController.viewRegisteredProject();
        UIController.loopOfficer();
    }

    /**
     * Displays the status of the officer's applications to register for projects.
     * Delegates display logic to {@link OfficerRequestController#viewRegistrationStatus()}.
     * Loops back to the officer menu.
     */
    public static void viewRegistrationStatus() {
        OfficerRequestController.viewRegistrationStatus();
        UIController.loopOfficer();
    }

    /**
     * Displays projects that are currently available for officers to register for.
     * Delegates display logic to {@link OfficerProjectController#viewRegistrableProject()}.
     * Loops back to the officer menu.
     */
    public static void viewRegistrableProject() {
        OfficerProjectController.viewRegistrableProject();
        UIController.loopOfficer();
    }

    // ========================================
    // Officer Duties - Enquiry & Request Management
    // ========================================

    /**
     * Displays enquiries assigned to or related to the currently logged-in officer.
     * Delegates display logic to {@link OfficerRequestController#viewEnquiries()}.
     * Loops back to the officer menu.
     */
    public static void viewEnquiries() {
        OfficerRequestController.viewEnquiries();
        UIController.loopOfficer();
    }

    /**
     * Displays enquiries filtered by a specific project ID.
     * Prompts for the project ID. Checks if the project exists.
     * Delegates display logic to {@link OfficerRequestController#viewEnquiries(String)}.
     * Handles {@link ProjectNotFoundException}.
     * Loops back to the officer menu.
     */
    public static void viewEnquiriesByProject() {
        System.out.print("Enter the project ID to view enquiries for: ");
        String projectID = IOController.nextLine();
        try {
            Project project = ProjectList.getInstance().getByID(projectID);
            if (project == null) throw new ProjectNotFoundException();
            OfficerRequestController.viewEnquiries(projectID); // Call the overloaded method
        } catch (ProjectNotFoundException e) {
            System.out.println(e.getMessage());
        }
        UIController.loopOfficer();
    }

    /**
     * Handles the process for an officer to answer a specific enquiry.
     * Prompts for the request ID. Performs validation checks:
     * - Request existence.
     * - Request type (must be Enquiry).
     * - Enquiry status (must not be DONE).
     * - Officer's association with the project (must be registered for the project).
     * Prompts for the answer text if valid.
     * Delegates answering logic to {@link OfficerRequestController#answerEnquiry(String, String)}.
     * Loops back to the officer menu.
     */
    public static void answerEnquiry() {
        System.out.print("Enter the request ID of the enquiry to answer: ");
        String requestID = IOController.nextLine();
        Request query = RequestList.getInstance().getByID(requestID);

        // --- Input Validations ---
        if (query == null) {
            System.out.println("This request ID does not exist.");
            UIController.loopOfficer();
            return;
        }
        if (!(query instanceof Enquiry)) {
            System.out.println("This request ID is not for an enquiry.");
            UIController.loopOfficer();
            return;
        }
        if (query.getRequestStatus() == RequestStatus.DONE) {
            System.out.println("This enquiry has already been answered.");
            UIController.loopOfficer();
            return;
        }
        // Check if officer is assigned to the project associated with the enquiry
        List<String> projects = OfficerList.getInstance().getByID(AccountController.getUserID()).getOfficerProject();
        if (!projects.contains(query.getProjectID())) { // Check project associated with the enquiry
            System.out.println("You are not registered for the project associated with this enquiry (Project ID: " + query.getProjectID() + ").");
            UIController.loopOfficer();
            return;
        }
        // --- End Validations ---

        System.out.print("Enter your answer: ");
        String answer = IOController.nextLine();
        OfficerRequestController.answerEnquiry(requestID, answer);
        System.out.println("Enquiry answered successfully."); // Confirmation
        UIController.loopOfficer();
    }

    // ========================================
    // Officer Duties - Applicant/Flat Management
    // ========================================

    /**
     * Displays the application status of applicants, likely filtered for projects the officer manages.
     * Delegates display logic to {@link OfficerProjectController#viewApplicantApplicationStatus()}.
     * Loops back to the officer menu.
     */
    public static void viewApplicantApplicationStatus() {
        OfficerProjectController.viewApplicantApplicationStatus();
        UIController.loopOfficer();
    }

    /**
     * Initiates the process for booking a flat for a specific applicant.
     * Prompts for the applicant's ID.
     * Delegates booking logic to {@link OfficerProjectController#bookFlat(String)}.
     * Loops back to the officer menu.
     */
    public static void bookFlat() {
        System.out.print("Enter the applicant ID to book a flat for: ");
        String applicantID = IOController.nextLine();
        OfficerProjectController.bookFlat(applicantID); // Assumes controller handles logic and messages
        UIController.loopOfficer();
    }

    // ========================================
    // Officer Duties - Receipt Generation
    // ========================================

    /**
     * Generates a general receipt (details depend on controller implementation).
     * Delegates generation logic to {@link OfficerProjectController#generateReceipt()}.
     * Loops back to the officer menu.
     */
    public static void generateReceipt() {
        OfficerProjectController.generateReceipt(); // Assumes controller handles logic and messages
        UIController.loopOfficer();
    }

    /**
     * Generates a receipt specifically for an applicant.
     * Prompts for the applicant's ID.
     * Delegates generation logic to {@link OfficerProjectController#generateReceiptByApplicant(String)}.
     * Loops back to the officer menu.
     */
    public static void generateReceiptByApplicant() {
        System.out.print("Enter the applicant ID to generate receipt for: ");
        String applicantID = IOController.nextLine();
        OfficerProjectController.generateReceiptByApplicant(applicantID); // Assumes controller handles logic and messages
        UIController.loopOfficer();
    }

    /**
     * Generates receipts related to a specific project.
     * Prompts for the project ID.
     * Delegates generation logic to {@link OfficerProjectController#generateReceiptByProject(String)}.
     * Loops back to the officer menu.
     */
    public static void generateReceiptByProject() {
        System.out.print("Enter the project ID to generate receipts for: ");
        String projectID = IOController.nextLine();
        OfficerProjectController.generateReceiptByProject(projectID); // Assumes controller handles logic and messages
        UIController.loopOfficer();
    }
}
