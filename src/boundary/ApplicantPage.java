package boundary;

import controller.AccountController;
import controller.ApplicantController;
import controller.FilterController;
import entity.list.ApplicantList;
import entity.list.ProjectList;
import entity.project.FlatType;
import entity.project.Project;
import entity.user.Applicant;
import entity.user.MaritalStatus;
import exception.ProjectNotFoundException;
import utils.Display;
import utils.IOController;
import utils.UIController;

/**
 * Represents the boundary layer for handling applicant interactions and displaying the applicant menu.
 * This class provides static methods to navigate different applicant functionalities like viewing projects,
 * applying, managing applications, handling queries, and setting filters.
 */
public class ApplicantPage {

    /**
     * Displays the main menu options available to the logged-in applicant.
     * Reads the applicant's choice and navigates to the corresponding functionality.
     * Handles invalid input and loops back to the main menu or exits the application.
     */
    public static void allOptions() {
        UIController.clearPage();
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println(
                "  __  ____ ____ __   __ ___  __  __ _ ____    ____  __   ___ ____ \n" + //
                " / _\\(  _ (  _ (  ) (  / __)/ _\\(  ( (_  _)  (  _ \\/ _\\ / __(  __)\n" + //
                "/    \\) __/) __/ (_/\\)( (__/    /    / )(     ) __/    ( (_ \\) _) \n" + //
                "\\_/\\_(__) (__) \\____(__\\___\\_/\\_\\_)__)(__)   (__) \\_/\\_/\\___(____)\n" + //
                "");
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("Welcome, " + ApplicantList.getInstance().getByID(AccountController.getUserID()).getName() + ". Please enter your choice."
                + "\n\t0. View Profile"
                + "\n\t1. View Applicable Project"
                + "\n\t2. View Applied Projects"
                + "\n\t3. View Applied Applications"
                + "\n\t4. Apply for Project"
                + "\n\t5. Withdraw Application"
                + "\n\t6. Make Query"
                + "\n\t7. View Query"
                + "\n\t8. Edit Query"
                + "\n\t9. Delete Query"
                + "\n\t10. Set up Project Filter"
                + "\n\t11. View Your Current Filter"
                + "\n\t12. Sign out"
                + "\n\t13. Exit");
        System.out.print("Your choice (0-13): ");
        int option = IOController.nextInt();
        switch (option) {
            case 0 -> {
                Display.displayApplicant(ApplicantList.getInstance().getByID(AccountController.getUserID()), true);
                UIController.loopApplicant();
            }
            case 1 -> viewApplicableProject();
            case 2 -> viewAppliedProject();
            case 3 -> viewAppliedApplication();
            case 4 -> applyProject();
            case 5 -> withdrawApplication();
            case 6 -> query();
            case 7 -> viewQuery();
            case 8 -> editQuery();
            case 9 -> deleteQuery();
            case 10 -> {
                FilterController.setup();
                UIController.loopApplicant();
            }
            case 11 -> {
                FilterController.displayFilter();
                UIController.loopApplicant();
            }
            case 12 -> AccountController.logout();
            case 13 -> UIController.exit();
            default -> {
                System.out.println("Invalid choice. Press ENTER to try again.");
                IOController.nextLine();
                allOptions();
            }
        }
    }


    /**
     * Displays projects that the currently logged-in applicant is eligible to apply for,
     * potentially based on filters or eligibility criteria handled by the ApplicantController.
     * After displaying, loops back to the applicant menu.
     */
    public static void viewApplicableProject() {
        ApplicantController.viewApplicableProject();
        UIController.loopApplicant();
    }

    /**
     * Displays the projects for which the currently logged-in applicant has submitted an application.
     * After displaying, loops back to the applicant menu.
     */
    public static void viewAppliedProject() {
        ApplicantController.viewAppliedProject();
        UIController.loopApplicant();
    }

    /**
     * Displays details of the applications submitted by the currently logged-in applicant.
     * After displaying, loops back to the applicant menu.
     */
    public static void viewAppliedApplication(){
        ApplicantController.viewAppliedApplication();
        UIController.loopApplicant();
    }

    /**
     * Handles the process for an applicant to apply for a specific project.
     * Checks if the applicant has already applied for a project.
     * Prompts for the project ID, checks project existence and applicant eligibility (age, marital status).
     * Prompts for the desired flat type based on eligibility.
     * Delegates the actual application logic to ApplicantController.
     * Handles potential ProjectNotFoundException if the entered project ID is invalid.
     * Loops back to the applicant menu afterwards.
     */
    public static void applyProject() {
        Applicant applicant = ApplicantList.getInstance().getByID(AccountController.getUserID());
        if (applicant.getProject() != null) {
            System.out.println("You are allowed to apply for only one project.");
            UIController.loopApplicant();
            return;
        }
        System.out.print("Enter the project ID to apply: ");
        String projectID = IOController.nextLine();
        try {
            Project project = ProjectList.getInstance().getByID(projectID);
            if (project == null) throw new ProjectNotFoundException();
            int able = 0; // Eligibility flag: 0=Not eligible, 1=Eligible for 2-room, 2=Eligible for 2 or 3-room
            if (applicant.getAge() >= 35 && applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
                able = 1;
            }
            else if (applicant.getAge() >= 21 && applicant.getMaritalStatus() == MaritalStatus.MARRIED) {
                able = 2;
            }
            if (able == 0) {
                System.out.println("You are not eligible to apply for a project.");
                UIController.loopApplicant();
                return;
            }
            System.out.println("Enter flat type: ");
            System.out.println("\t1. Two Room");
            if (able == 2) System.out.println("\t2. Three Room");
            System.out.print("Your choice: ");
            int option = IOController.nextInt();
            // Validate flat type choice based on eligibility
            while (option > able || option < 1) {
                System.out.print("Please enter valid choice: ");
                option = IOController.nextInt();
            }
            FlatType applyFlat = option == 1 ? FlatType.TWO_ROOM : FlatType.THREE_ROOM;
            ApplicantController.applyProject(projectID, applyFlat);
            UIController.loopApplicant();
            // Removed redundant return as loopApplicant is called
        } catch (ProjectNotFoundException e) {
            System.out.println(e.getMessage());
            UIController.loopApplicant(); // Ensure loop back even on exception
        }
        // Removed redundant UIController.loopApplicant() as it's called in both try and catch blocks
    }

    /**
     * Handles the process for an applicant to withdraw their application from a project.
     * Prompts for the project ID from which to withdraw.
     * Delegates the withdrawal logic to ApplicantController.
     * Handles potential ProjectNotFoundException if the entered project ID is invalid or the applicant
     * hasn't applied to that project.
     * Loops back to the applicant menu afterwards.
     */
    public static void withdrawApplication() {
        System.out.print("Enter the project ID to withdraw application from: "); // Clarified prompt
        String projectID = IOController.nextLine();
        try {
            ApplicantController.withdrawApplication(projectID);
            UIController.loopApplicant(); // Loop back on success
        } catch (ProjectNotFoundException e) {
            System.out.println(e.getMessage());
            UIController.loopApplicant(); // Loop back on exception
        }
        // Removed redundant UIController.loopApplicant()
    }

    /**
     * Allows the applicant to submit a query regarding a specific project.
     * Prompts for the project ID and checks if it exists.
     * Prompts for the query text.
     * Delegates the query submission logic to ApplicantController.
     * Loops back to the applicant menu afterwards.
     */
    public static void query() {
        System.out.print("Enter the project ID to enquiry about: "); // Clarified prompt
        String projectID = IOController.nextLine();
        if (ProjectList.getInstance().getByID(projectID) == null) {
            System.out.println(new ProjectNotFoundException().getMessage()); // Use standard message
            UIController.loopApplicant();
            return;
        }
        System.out.print("Enter your query: ");
        String question = IOController.nextLine();
        ApplicantController.query(projectID, question);
        UIController.loopApplicant();
    }

    /**
     * Displays the queries submitted by the currently logged-in applicant.
     * Delegates the display logic to ApplicantController.
     * Loops back to the applicant menu afterwards.
     */
    public static void viewQuery() {
        ApplicantController.viewQuery();
        UIController.loopApplicant();
    }

    /**
     * Allows the applicant to edit an existing query they submitted.
     * Prompts for the request ID (query ID) of the query to edit.
     * Validates if the query exists and belongs to the applicant via ApplicantController.
     * Prompts for the new query text.
     * Delegates the editing logic to ApplicantController.
     * Loops back to the applicant menu afterwards.
     */
    public static void editQuery() {
        System.out.print("Enter the request ID (query ID) to edit: "); // Clarified prompt
        String requestID = IOController.nextLine();
        // Check if the query exists and belongs to the user before proceeding
        if (!ApplicantController.checkQuery(requestID)) {
            // Error message likely handled within checkQuery or should be added here
            System.out.println("Query not found or does not belong to you."); // Example error message
            UIController.loopApplicant();
            return;
        }
        System.out.print("Enter the new query text: "); // Clarified prompt
        String newQuery = IOController.nextLine();
        ApplicantController.editQuery(requestID, newQuery);
        UIController.loopApplicant();
    }

    /**
     * Allows the applicant to delete an existing query they submitted.
     * Prompts for the request ID (query ID) of the query to delete.
     * Delegates the deletion logic (including necessary checks) to ApplicantController.
     * Loops back to the applicant menu afterwards.
     */
    public static void deleteQuery() {
        System.out.print("Enter the request ID (query ID) to delete: "); // Clarified prompt
        String requestID = IOController.nextLine();
        ApplicantController.deleteQuery(requestID); // Assumes controller handles checks/errors
        UIController.loopApplicant();
    }
}
