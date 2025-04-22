package boundary;

import java.util.*;
import controller.AccountController;
import controller.FilterController;
import controller.ManagerProjectController;
import controller.ManagerRequestController;
import controller.OfficerRequestController; // Note: Used in answerEnquiries, might be intended or ManagerRequestController expected
import entity.list.ManagerList;
import entity.list.OfficerList; // Note: Used in welcome message, ensure this is intended for Manager context
import entity.list.ProjectList;
import entity.list.RequestList;
import entity.project.FlatType;
import entity.project.Project;
import entity.request.ApprovedStatus;
import entity.request.Enquiry;
import entity.request.Request;
import entity.request.RequestStatus;
import entity.user.UserType;
import exception.ProjectNotFoundException;
import utils.Display;
import utils.IDController;
import utils.IOController;
import utils.UIController;
import java.time.*;

/**
 * Represents the boundary layer for handling manager interactions and displaying the manager menu.
 * This class provides static methods for managers to manage projects (create, edit, delete, toggle visibility),
 * handle requests and enquiries (view, change status, answer), manage filters, view officer status,
 * and generate reports.
 */
public class ManagerPage {

    /**
     * Instance variable for request ID. Note: This variable is not used by any static methods
     * within this class as provided and might be leftover code or intended for future non-static use.
     */
    String requestID; // TODO: Review usage - Seems unused in static context.

    /**
     * Displays the main menu options available to the logged-in manager.
     * Reads the manager's choice and navigates to the corresponding functionality.
     * Handles invalid input and loops back to the main menu or exits the application.
     */
    public static void allOptions() {
        UIController.clearPage();
        System.out.println(UIController.LINE_SEPARATOR);
        // ASCII Art Header
        System.out.println(
                " _  _   __   __ _   __    ___  ____  ____    ____   __    ___  ____ \n" + //
                "( \\/ ) / _\\ (  ( \\ / _\\  / __)(  __)(  _ \\  (  _ \\ / _\\  / __)(  __)\n" + //
                "/ \\/ \\/    \\/    //    \\( (_ \\ ) _)  )   /   ) __//    \\( (_ \\ ) _) \n" + //
                "\\_)(_/\\_/\\_/\\_)__)\\_/\\_/ \\___/(____)(__\\_)  (__)  \\_/\\_/ \\___/(____)\n" + //
                "");
        System.out.println(UIController.LINE_SEPARATOR);
        // Welcome message - Note: Uses OfficerList, might intend ManagerList
        System.out.println("Welcome, " + OfficerList.getInstance().getByID(AccountController.getUserID()).getName() + ". Please enter your choice."
                + "\n\t0. View Profile"
                + "\n\t1. View Project List"
                + "\n\t2. Create Project"
                + "\n\t3. Edit Project"
                + "\n\t4. Delete Project"
                + "\n\t5. Toggle Visibility"
                + "\n\t6. View Requests"
                + "\n\t7. View Officer Registration Status"
                + "\n\t8. Change Application Status"
                + "\n\t9. View Enquiries"
                + "\n\t10. View All Enquiries"
                + "\n\t11. Answer Enquiries"
                + "\n\t12. Generate Report"
                + "\n\t13. Set up Project Filter"
                + "\n\t14. View Your Current Filter"
                + "\n\t15. Sign out"
                + "\n\t16. Exit");
        System.out.print("Your choice (0-16): ");
        int option = IOController.nextInt();
        switch (option) {
            case 0 -> {
                // Display manager profile
                Display.displayManager(ManagerList.getInstance().getByID(AccountController.getUserID()));
                UIController.loopManager();
            }
            case 1 -> viewProjectList();
            case 2 -> createProject();
            case 3 -> editProject();
            case 4 -> deleteProject();
            case 5 -> toggleVisibility();
            case 6 -> viewRequest();
            case 7 -> viewOfficerRegistrationStatus();
            case 8 -> changeApplicationStatus();
            case 9 -> viewEnquiries();
            case 10 -> viewAllEnquiries();
            case 11 -> answerEnquiries();
            case 12 -> generateReport();
            case 13 -> {
                // Setup project filter
                FilterController.setup();
                UIController.loopManager();
            }
            case 14 -> {
                // Display current filter settings
                FilterController.displayFilter();
                UIController.loopManager();
            }
            case 15 -> AccountController.logout(); // Sign out
            case 16 -> UIController.exit();     // Exit application
            default -> {
                // Handle invalid choice
                System.out.println("Invalid choice. Press ENTER to try again.");
                IOController.nextLine(); // Consume leftover newline
                allOptions(); // Show options again
            }
        }
    }

    /**
     * Displays enquiries specifically assigned to or related to the current manager's projects.
     * Delegates the display logic to {@link ManagerRequestController#viewEnquiries()}.
     * Loops back to the manager menu afterwards.
     */
    public static void viewEnquiries() {
        ManagerRequestController.viewEnquiries();
        UIController.loopManager();
    }

    /**
     * Handles the process for a manager to answer a specific enquiry.
     * Prompts for the request ID of the enquiry.
     * Performs checks:
     * - If the request ID exists.
     * - If the request is actually an Enquiry.
     * - If the enquiry has already been answered (status is DONE).
     * - If the enquiry belongs to a project managed by the current manager.
     * Prompts for the answer text if all checks pass.
     * Delegates the action of answering the enquiry to {@link OfficerRequestController#answerEnquiry(String, String)}.
     * Note: Uses OfficerRequestController; consider if ManagerRequestController is intended.
     * Loops back to the manager menu afterwards.
     */
    public static void answerEnquiries() {
        System.out.print("Enter the request ID to answer: ");
        String requestID = IOController.nextLine();
        // Fetch projects managed by the current manager
        List<String> projects = ManagerList.getInstance().getByID(AccountController.getUserID()).getProject();
        Request query = RequestList.getInstance().getByID(requestID);

        // --- Input Validations ---
        if (query == null) {
            System.out.println("This request ID does not exist.");
            UIController.loopManager();
            return;
        }
        if (!(query instanceof Enquiry)) {
            System.out.println("This request ID is not for an enquiry.");
            UIController.loopManager();
            return;
        }
        if (query.getRequestStatus() == RequestStatus.DONE) {
            System.out.println("This enquiry has already been answered.");
            UIController.loopManager();
            return;
        }
        // Check if the enquiry's project ID is in the list of projects managed by this manager
        if (!projects.contains(query.getProjectID())) { // Assuming Enquiry has getProjectID()
            System.out.println("You are not allowed to answer enquiries for projects you do not manage.");
            UIController.loopManager();
            return;
        }
        // --- End Validations ---

        System.out.print("Enter your answer: ");
        String answer = IOController.nextLine();
        // Delegate to controller - Note: Using OfficerRequestController
        OfficerRequestController.answerEnquiry(requestID, answer);
        System.out.println("Enquiry answered successfully."); // Added confirmation
        UIController.loopManager();
    }

    /**
     * Displays the list of projects created by a specific manager.
     * Prompts for the manager ID. If left empty, defaults to the currently logged-in manager.
     * Delegates the display logic to {@link ManagerProjectController#viewProjectList(String)}.
     * Catches and handles {@link ProjectNotFoundException} if no projects are found for the manager.
     * Loops back to the manager menu afterwards.
     */
    public static void viewProjectList() {
        System.out.print("Enter the manager ID to view their created projects (Press ENTER to view yours): ");
        String managerID = IOController.nextLine();
        if (managerID.isEmpty()) {
            managerID = AccountController.getUserID(); // Default to current user
        }
        try {
            ManagerProjectController.viewProjectList(managerID);
        } catch (ProjectNotFoundException e) {
            System.out.println("No projects found for manager ID: " + managerID); // More specific message
        }
        UIController.loopManager();
    }

    /**
     * Displays requests (applications, etc.) associated with the projects managed by the current manager.
     * Delegates the display logic to {@link ManagerRequestController#viewRequest()}.
     * Loops back to the manager menu afterwards.
     */
    public static void viewRequest() {
        ManagerRequestController.viewRequest();
        UIController.loopManager();
    }

    /**
     * Allows the manager to change the approval status of an application request.
     * Prompts for the request ID.
     * Performs checks:
     * - If the request ID exists.
     * - If the request is not an Enquiry.
     * - If the request belongs to a project managed by the current manager.
     * Displays the request details and prompts for the new status (Pending, Successful, Unsuccessful).
     * Delegates the status change to {@link ManagerRequestController#changeApplicationStatus(String, ApprovedStatus)}.
     * Loops back to the manager menu afterwards.
     */
    public static void changeApplicationStatus() {
        System.out.print("Enter the request ID: ");
        String requestID = IOController.nextLine();
        Request request = RequestList.getInstance().getByID(requestID);

        // --- Input Validations ---
        if (request == null) {
            System.out.println("No request found with this ID.");
            UIController.loopManager();
            return;
        }
        if (request instanceof Enquiry) {
            System.out.println("Invalid request type. This request ID is for an enquiry. Use 'Answer Enquiries' instead.");
            UIController.loopManager();
            return;
        }
        // Fetch projects managed by the current manager
        List<String> projects = ManagerList.getInstance().getByID(AccountController.getUserID()).getProject();
        // Check if the request's project ID is in the list of projects managed by this manager
        if (!projects.contains(request.getProjectID())) {
            System.out.println("You are not allowed to change the application status for projects you do not manage.");
            UIController.loopManager();
            return;
        }
        // --- End Validations ---

        Display.displayRequest(request, UserType.MANAGER); // Show details before asking for change
        System.out.println("Enter new status:");
        System.out.println("\t1. " + ApprovedStatus.PENDING);
        System.out.println("\t2. " + ApprovedStatus.SUCCESSFUL);
        System.out.println("\t3. " + ApprovedStatus.UNSUCCESSFUL);
        System.out.print("Your choice (1-3): "); // Added prompt hint
        int option = IOController.nextInt();
        // Validate status choice
        while (option < 1 || option > 3) {
            System.out.print("Please enter a valid choice (1-3): ");
            option = IOController.nextInt();
        }
        ApprovedStatus status = null;
        switch (option) {
            case 1 -> status = ApprovedStatus.PENDING;
            case 2 -> status = ApprovedStatus.SUCCESSFUL;
            case 3 -> status = ApprovedStatus.UNSUCCESSFUL;
        }
        ManagerRequestController.changeApplicationStatus(requestID, status);
        System.out.println("Application status updated successfully."); // Added confirmation
        UIController.loopManager();
    }

    /**
     * Displays all enquiries across all projects (presumably those the manager has access to view).
     * Delegates the display logic to {@link ManagerRequestController#viewAllEnquiries()}.
     * Loops back to the manager menu afterwards.
     */
    public static void viewAllEnquiries() {
        ManagerRequestController.viewAllEnquiries();
        UIController.loopManager();
    }

    /**
     * Handles the creation of a new project by the manager.
     * Generates a new project ID using {@link IDController#newProjectID()}.
     * Prompts the manager for all required project details:
     * - Name
     * - Neighbourhoods (list)
     * - Available units and price for Two Room and Three Room flats
     * - Open and Close dates for applications
     * - Number of available officer slots (1-10)
     * Delegates the project creation logic to {@link ManagerProjectController#createProject(String, String, List, Map, Map, LocalDate, LocalDate, int)}.
     * Loops back to the manager menu afterwards.
     */
    public static void createProject() {
        String tmp; // Temporary string variable
        int tmpint; // Temporary integer variable
        String projectID = IDController.newProjectID(); // Generate unique project ID

        System.out.println("--- Create New Project (ID: " + projectID + ") ---"); // Header

        System.out.print("Project Name: ");
        String name = IOController.nextLine();

        // Collect Neighbourhoods
        System.out.print("Number of nearby neighbourhoods (enter 0 if none): ");
        List<String> neighbourhood = new ArrayList<>();
        tmpint = IOController.nextInt();
        while (tmpint < 0) {
            System.out.print("Please enter a valid number (0 or more): ");
            tmpint = IOController.nextInt();
        }
        if (tmpint > 0) {
            System.out.println("Enter names of nearby neighbourhoods:");
            for (int i = 0; i < tmpint; i++) { // Corrected loop logic
                System.out.print("\tNeighbourhood " + (i + 1) + ": ");
                tmp = IOController.nextLine();
                neighbourhood.add(tmp);
            }
        }

        // Collect Flat Details
        Map<FlatType, Integer> availableUnits = new HashMap<>();
        Map<FlatType, Integer> price = new HashMap<>();

        System.out.println("Two Room Flat Details:");
        System.out.print("\tNumber of units: ");
        tmpint = IOController.nextInt();
        availableUnits.put(FlatType.TWO_ROOM, tmpint);
        System.out.print("\tPrice per unit: ");
        tmpint = IOController.nextInt();
        price.put(FlatType.TWO_ROOM, tmpint);

        System.out.println("Three Room Flat Details:");
        System.out.print("\tNumber of units: ");
        tmpint = IOController.nextInt();
        availableUnits.put(FlatType.THREE_ROOM, tmpint);
        System.out.print("\tPrice per unit: ");
        tmpint = IOController.nextInt();
        price.put(FlatType.THREE_ROOM, tmpint);

        // Collect Dates
        System.out.println("Application Open Date:");
        LocalDate openDate = IOController.nextDate(); // Assumes nextDate handles format prompting
        System.out.println("Application Close Date:");
        LocalDate closeDate = IOController.nextDate(); // Assumes nextDate handles format prompting

        // Collect Available Officer Slots
        System.out.print("Number of Available Officer Slots (1-10): ");
        int availableOfficer = IOController.nextInt();
        while (availableOfficer < 1 || availableOfficer > 10) {
            System.out.print("Please enter a valid number between 1 and 10: ");
            availableOfficer = IOController.nextInt();
        }

        // Delegate creation to controller
        ManagerProjectController.createProject(projectID, name, neighbourhood, availableUnits, price, openDate, closeDate, availableOfficer);
        System.out.println("Project created successfully (ID: " + projectID + ")."); // Added confirmation
        UIController.loopManager();
    }

    /**
     * Handles the editing of an existing project by the manager.
     * Prompts for the project ID to edit.
     * If the project exists, prompts for new values for each field, allowing the user to press ENTER to skip
     * and keep the existing value.
     * Collects potentially updated details for:
     * - Name, Neighbourhoods, Units/Prices for flat types, Open/Close dates, Officer slots, Visibility.
     * Constructs a new {@link Project} object with potentially mixed old and new data.
     * Delegates the update logic to {@link ManagerProjectController#editProject(String, Project)}.
     * Loops back to the manager menu afterwards.
     */
    public static void editProject() {
        String tmp; // Temporary string variable
        int tmpint; // Temporary integer variable

        System.out.print("Enter the Project ID to edit: ");
        String projectID = IOController.nextLine();
        Project project = ProjectList.getInstance().getByID(projectID); // Get existing project

        // Check if project exists
        if (project == null) {
            System.out.println("No project found with this ID.");
            UIController.loopManager();
            return;
        }

        System.out.println("--- Editing Project (ID: " + projectID + ") ---");
        System.out.println("Press ENTER to keep the current value.");

        // Edit Name
        System.out.print("Name [" + project.getName() + "]: ");
        String name = IOController.nextLine();
        if (name.isEmpty()) {
            name = project.getName(); // Keep old value if skipped
        }

        // Edit Neighbourhoods
        System.out.print("Number of nearby neighbourhoods (current: " + project.getNeighborhood().size() + ") [Enter new number or ENTER to keep]: ");
        List<String> neighbourhood;
        tmp = IOController.nextLine();
        if (tmp.isEmpty()) {
            neighbourhood = project.getNeighborhood(); // Keep old list
        } else {
            try {
                tmpint = Integer.parseInt(tmp);
                neighbourhood = new ArrayList<>(); // Create new list
                while (tmpint < 0) {
                    System.out.print("Please enter a valid number (0 or more): ");
                    tmpint = IOController.nextInt();
                }
                if (tmpint > 0) {
                    System.out.println("Enter new names of nearby neighbourhoods:");
                    for (int i = 0; i < tmpint; i++) { // Corrected loop
                        System.out.print("\tNeighbourhood " + (i + 1) + ": ");
                        tmp = IOController.nextLine();
                        neighbourhood.add(tmp);
                    }
                }
            } catch (NumberFormatException e) {
                 System.out.println("Invalid number entered. Keeping original neighbourhoods.");
                 neighbourhood = project.getNeighborhood();
            }
        }

        // Edit Flat Details
        Map<FlatType, Integer> availableUnits = new HashMap<>(project.getAvailableUnit()); // Start with old map
        Map<FlatType, Integer> price = new HashMap<>(project.getPrice()); // Start with old map

        System.out.println("Two Room Flat Details:");
        System.out.print("\tNumber of units [" + availableUnits.get(FlatType.TWO_ROOM) + "]: ");
        tmp = IOController.nextLine();
        if (!tmp.isEmpty()) {
             try { availableUnits.put(FlatType.TWO_ROOM, Integer.parseInt(tmp)); } catch (NumberFormatException e) { /* Keep old */ }
        }
        System.out.print("\tPrice per unit [" + price.get(FlatType.TWO_ROOM) + "]: ");
        tmp = IOController.nextLine();
        if (!tmp.isEmpty()) {
             try { price.put(FlatType.TWO_ROOM, Integer.parseInt(tmp)); } catch (NumberFormatException e) { /* Keep old */ }
        }

        System.out.println("Three Room Flat Details:");
        System.out.print("\tNumber of units [" + availableUnits.get(FlatType.THREE_ROOM) + "]: ");
        tmp = IOController.nextLine();
        if (!tmp.isEmpty()) {
             try { availableUnits.put(FlatType.THREE_ROOM, Integer.parseInt(tmp)); } catch (NumberFormatException e) { /* Keep old */ }
        }
        System.out.print("\tPrice per unit [" + price.get(FlatType.THREE_ROOM) + "]: ");
        tmp = IOController.nextLine();
        if (!tmp.isEmpty()) {
             try { price.put(FlatType.THREE_ROOM, Integer.parseInt(tmp)); } catch (NumberFormatException e) { /* Keep old */ }
        }

        // Edit Dates
        System.out.println("Application Open Date [" + project.getOpenDate() + "] (YYYY-MM-DD or ENTER to keep):");
        LocalDate openDate = project.getOpenDate();
        tmp = IOController.nextLine(); // Read line first
        if (!tmp.isEmpty()) {
             try { openDate = LocalDate.parse(tmp); } catch (Exception e) { System.out.println("Invalid date format. Keeping original.");} // Use try-catch for parsing
        }

        System.out.println("Application Close Date [" + project.getCloseDate() + "] (YYYY-MM-DD or ENTER to keep):");
        LocalDate closeDate = project.getCloseDate();
        tmp = IOController.nextLine(); // Read line first
         if (!tmp.isEmpty()) {
             try { closeDate = LocalDate.parse(tmp); } catch (Exception e) { System.out.println("Invalid date format. Keeping original.");} // Use try-catch for parsing
        }

        // Edit Available Officer Slots
        System.out.print("Number of Available Officer Slots (1-10) [" + project.getAvailableOfficer() + "]: ");
        int availableOfficer = project.getAvailableOfficer();
        tmp = IOController.nextLine();
        if (!tmp.isEmpty()) {
            try {
                 tmpint = Integer.parseInt(tmp);
                 if (tmpint >= 1 && tmpint <= 10) {
                      availableOfficer = tmpint;
                 } else {
                      System.out.println("Number must be between 1 and 10. Keeping original value.");
                 }
            } catch (NumberFormatException e) {
                 System.out.println("Invalid number entered. Keeping original value.");
            }
        }

        // Edit Visibility
        System.out.println("Visibility [" + (project.getVisibility() ? "Visible" : "Not Visible") + "]: ");
        System.out.println("\t1. Visible");
        System.out.println("\t2. Not visible");
        System.out.print("Your choice (1-2 or ENTER to keep): ");
        boolean visibility = project.getVisibility();
        tmp = IOController.nextLine();
        if (!tmp.isEmpty()) {
            try {
                 tmpint = Integer.parseInt(tmp);
                 if (tmpint == 1) {
                      visibility = true;
                 } else if (tmpint == 2) {
                      visibility = false;
                 } else {
                     System.out.println("Invalid choice. Keeping original visibility.");
                 }
            } catch (NumberFormatException e) {
                 System.out.println("Invalid input. Keeping original visibility.");
            }
        }

        // Create the updated project object (using existing manager ID)
        Project updatedProject = new Project(projectID, name, neighbourhood, availableUnits, price, openDate, closeDate, project.getManagerID(), availableOfficer, visibility);

        // Delegate edit to controller
        ManagerProjectController.editProject(projectID, updatedProject);
        System.out.println("Project updated successfully."); // Added confirmation
        UIController.loopManager();
    }

    /**
     * Handles the deletion of a project by the manager.
     * Prompts for the project ID to delete.
     * Delegates the deletion logic to {@link ManagerProjectController#deleteProject(String)}.
     * Catches and displays a message for {@link ProjectNotFoundException} if the project ID is invalid.
     * Loops back to the manager menu afterwards.
     */
    public static void deleteProject() {
        System.out.print("Enter the Project ID to delete: ");
        String projectID = IOController.nextLine();
        try {
            ManagerProjectController.deleteProject(projectID);
            System.out.println("Project deleted successfully."); // Added confirmation
        } catch (ProjectNotFoundException e) {
            System.out.println(e.getMessage());
        }
        UIController.loopManager();
    }

    /**
     * Toggles the visibility status of a project (Visible <-> Not Visible).
     * Prompts for the project ID.
     * Delegates the toggle logic to {@link ManagerProjectController#toggleVisibility(String)}.
     * Catches and displays a message for {@link ProjectNotFoundException} if the project ID is invalid.
     * Loops back to the manager menu afterwards.
     */
    public static void toggleVisibility() {
        System.out.print("Enter the Project ID to toggle visibility: ");
        String projectID = IOController.nextLine();
        try {
            ManagerProjectController.toggleVisibility(projectID);
            System.out.println("Project visibility toggled successfully."); // Added confirmation
        } catch (ProjectNotFoundException e) {
            System.out.println(e.getMessage());
        }
        UIController.loopManager();
    }

    /**
     * Displays the registration status of officers (details depend on controller implementation).
     * Delegates the display logic to {@link ManagerProjectController#viewOfficerRegistrationStatus()}.
     * Loops back to the manager menu afterwards.
     */
    public static void viewOfficerRegistrationStatus() {
        ManagerProjectController.viewOfficerRegistrationStatus();
        UIController.loopManager();
    }

    /**
     * Generates a report based on project or request data (specifics depend on controller implementation).
     * Delegates the report generation logic to {@link ManagerProjectController#generateReport()}.
     * Loops back to the manager menu afterwards.
     */
    public static void generateReport() {
        ManagerProjectController.generateReport();
        System.out.println("Report generation process initiated."); // Added confirmation
        UIController.loopManager();
    }
}
