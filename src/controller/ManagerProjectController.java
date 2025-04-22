package controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import entity.list.ApplicantList;
import entity.list.ManagerList;
import entity.list.OfficerList;
import entity.list.ProjectList;
import entity.list.RequestList;
import entity.project.FlatType;
import entity.project.Project;
import entity.request.OfficerRegistration;
import entity.request.Request;
import entity.user.Applicant;
import entity.user.ApplicationStatus;
import entity.user.Manager;
import entity.user.MaritalStatus;
import entity.user.Officer;
import entity.user.RegistrationStatus;
import entity.user.UserType;
import exception.ProjectNotFoundException;
import utils.Display;
import utils.IOController;
import utils.UIController;

/**
 * Controller responsible for handling project-related operations initiated by a Manager.
 * This includes creating, editing, deleting projects, toggling project visibility,
 * viewing projects associated with a manager, viewing officer registration requests,
 * and generating applicant reports based on various criteria.
 * It operates using the context of the currently logged-in manager's ID.
 */
public class ManagerProjectController {
    /**
     * Private constructor to prevent instantiation of this  class.
     * Throwing an error ensures it's not accidentally called via reflection.
     */
    private ManagerProjectController() {
        // Prevent instantiation
        throw new IllegalStateException("This class should not be instantiated");
    }
    /**
     * Stores the user ID of the manager currently interacting with the system.
     * This ID is typically set by the {@link AccountController} upon successful login of a Manager.
     */
    private static String managerID;

    /**
     * Sets the manager ID for the current session context.
     * All subsequent manager-specific operations in this controller will be performed for this manager.
     *
     * @param ID The user ID of the currently logged-in manager.
     */
    public static void setManagerID(String ID) {
        managerID = ID;
    }

    /**
     * Creates a new project and associates it with the current manager.
     * Performs pre-checks:
     * - Ensures the project name is unique across all existing projects.
     * - Ensures the manager does not already have another active project with overlapping dates.
     * If checks pass, adds the project to the {@link ProjectList} and updates the manager's record
     * in {@link ManagerList} to include the new project ID.
     *
     * @param projectID        The unique ID generated for the new project.
     * @param name             The name of the new project.
     * @param neighbourhood    A list of neighbourhood names associated with the project.
     * @param availableUnit    A map defining the number of available units for each {@link FlatType}.
     * @param price            A map defining the price for each {@link FlatType}.
     * @param openDate         The date when project applications open.
     * @param closeDate        The date when project applications close.
     * @param availableOfficer The number of officer slots available for this project.
     */
    public static void createProject(String projectID, String name, List<String> neighbourhood, Map<FlatType, Integer> availableUnit, Map<FlatType, Integer> price, LocalDate openDate, LocalDate closeDate, int availableOfficer) {
        // Pre-creation checks
        for (Project p : ProjectList.getInstance().getAll()) {
            // Check for duplicate project name
            if (p.getName().equals(name)) {
                System.out.println("Project creation failed: A project with the name '" + name + "' already exists.");
                return;
            }
            // Check if the current manager already has an overlapping active project
            // Overlap occurs if !(existing_close < new_open || existing_open > new_close)
            if (p.getManagerID().equals(managerID) && !(p.getCloseDate().isBefore(openDate) || p.getOpenDate().isAfter(closeDate))) {
                System.out.println("Project creation failed: You cannot create a new project while you have an active project (ID: " + p.getProjectID() + ") with overlapping dates.");
                return;
            }
        }

        // Create and add the new project
        Project newProject = new Project(projectID, name, neighbourhood, availableUnit, price, openDate, closeDate, managerID, availableOfficer, true); // Default visibility is true
        ProjectList.getInstance().add(newProject);

        // Update the manager's list of associated projects
        Manager manager = ManagerList.getInstance().getByID(managerID);
        if (manager != null) { // Ensure manager exists
            List<String> p = manager.getProject(); // Get existing list (might be null if first project)
            if (p == null) {
                p = new java.util.ArrayList<>(); // Initialize if null
            }
            p.add(projectID);
            manager.setProject(p);
            ManagerList.getInstance().update(managerID, manager);
            System.out.println("Successfully created project (ProjectID: " + projectID + ") and assigned to manager " + managerID + ".");
        } else {
             System.err.println("Error: Manager with ID " + managerID + " not found while updating project list.");
             // Consider if project should be rolled back if manager update fails
        }
    }

    /**
     * Updates an existing project's details in the {@link ProjectList}.
     * Replaces the project with the given {@code projectID} with the data from the provided {@code project} object.
     *
     * @param projectID The ID of the project to update.
     * @param project   The {@link Project} object containing the updated details.
     */
    public static void editProject(String projectID, Project project) {
        // Basic check: does the project to update exist?
        if (ProjectList.getInstance().getByID(projectID) == null) {
             System.out.println("Edit failed: Project with ID '" + projectID + "' not found.");
             return;
        }
        // Consider adding more validation here if needed (e.g., ensuring managerID doesn't change)
        ProjectList.getInstance().update(projectID, project);
        System.out.println("Successfully edited project (ProjectID: " + projectID + ").");
    }

    /**
     * Deletes a project and handles cascading updates/cleanups.
     * Actions performed:
     * 1. Removes the project from {@link ProjectList}.
     * 2. Removes the project ID from the list of projects managed by the associated manager in {@link ManagerList}.
     * 3. Deletes all related requests (applications, withdrawals, enquiries) from {@link RequestList}.
     * 4. Unlinks the project from any applicants who applied (sets their project to null) and updates their application status to UNSUCCESSFUL in {@link ApplicantList}.
     * 5. Removes the project ID from the list of projects officers are registered for and updates their registration status to REJECTED in {@link OfficerList}.
     *
     * @param projectID The ID of the project to delete.
     * @throws ProjectNotFoundException If no project with the given ID exists.
     */
    public static void deleteProject(String projectID) throws ProjectNotFoundException {
        Project project = ProjectList.getInstance().getByID(projectID);
        if (project == null) throw new ProjectNotFoundException();

        // 1. Delete project from ProjectList
        ProjectList.getInstance().delete(projectID);

        // 2. Update associated Manager
        // Assuming only one manager per project based on createProject logic
        Manager manager = ManagerList.getInstance().getByID(project.getManagerID());
         if (manager != null) {
             List<String> p = manager.getProject();
             if (p != null && p.contains(projectID)) {
                 p.remove(projectID);
                 manager.setProject(p);
                 ManagerList.getInstance().update(manager.getUserID(), manager);
             }
         }

        // 3. Delete related Requests
        // Need to collect IDs first to avoid ConcurrentModificationException if deleting while iterating list directly
         List<String> requestIdsToDelete = new java.util.ArrayList<>();
        for (Request r : RequestList.getInstance().getAll()) {
            if (r.getProjectID().equals(projectID)) {
                requestIdsToDelete.add(r.getRequestID());
            }
        }
        for (String reqId : requestIdsToDelete) {
             RequestList.getInstance().delete(reqId);
        }


        // 4. Update associated Applicants
        for (Applicant a : ApplicantList.getInstance().getAll()) {
            if (a.getProject() != null && a.getProject().equals(projectID)) {
                a.setProject(null); // Unlink project
                // Update status - may need refinement based on exact application logic
                a.setApplicationStatusByID(projectID, ApplicationStatus.UNSUCCESSFUL);
                 a.setAppliedFlatByID(projectID, null); // Clear applied flat type
                ApplicantList.getInstance().update(a.getUserID(), a);
            }
        }

        // 5. Update associated Officers
        for (Officer o : OfficerList.getInstance().getAll()) {
            if (o.getOfficerProject() != null && o.getOfficerProject().contains(projectID)) {
                List<String> p = o.getOfficerProject();
                p.remove(projectID);
                o.setOfficerProject(p);
                // Update registration status for this project
                o.setRegistrationStatusByID(projectID, RegistrationStatus.REJECTED);
                OfficerList.getInstance().update(o.getUserID(), o);
            }
        }
        System.out.println("Successfully deleted project (ProjectID: " + projectID + ") and performed related cleanups.");
    }

    /**
     * Toggles the visibility status of the specified project.
     * If the project is currently visible, it becomes not visible, and vice versa.
     *
     * @param projectID The ID of the project whose visibility to toggle.
     * @throws ProjectNotFoundException If no project with the given ID exists.
     */
    public static void toggleVisibility(String projectID) throws ProjectNotFoundException {
        Project project = ProjectList.getInstance().getByID(projectID);
        if (project == null) throw new ProjectNotFoundException();

        boolean currentVisibility = project.getVisibility();
        project.setVisibility(!currentVisibility); // Toggle the visibility flag
        ProjectList.getInstance().update(projectID, project); // Save the updated project

        System.out.println("Successfully toggled visibility of project (ProjectID: " + projectID + ") to " + (project.getVisibility() ? "Visible" : "Not Visible") + ".");
    }

    /**
     * Displays all {@link OfficerRegistration} requests found in the {@link RequestList}.
     * This allows the manager to view pending officer applications for projects.
     */
    public static void viewOfficerRegistrationStatus() {
        List<Request> list = RequestList.getInstance().getAll();
        boolean hasRegistrations = false; // Track if any registration requests exist

        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                    Officer Project Registration Requests");
        System.out.println(UIController.LINE_SEPARATOR);

        for (Request request : list) {
            if (request instanceof OfficerRegistration) { // Check if the request is of the correct type
                hasRegistrations = true;
                Display.displayRequest(request, UserType.MANAGER); // Display using Manager view
            }
        }
        if (!hasRegistrations) {
            System.out.println("There are no pending officer registration applications.");
        }
    }

    /**
     * Displays a list of projects managed by the specified manager ID, after applying current filters.
     * Retrieves the list of project IDs associated with the manager, gets the corresponding Project objects,
     * applies filters using {@link FilterController}, and displays the resulting projects.
     *
     * @param managerID The ID of the manager whose projects are to be viewed.
     * @throws ProjectNotFoundException If the manager manages no projects, or if after filtering, no projects remain.
     */
    public static void viewProjectList(String managerID) throws ProjectNotFoundException {
        Manager manager = ManagerList.getInstance().getByID(managerID);
        // Check if manager exists
        if (manager == null) {
             System.out.println("No manager found with ID: " + managerID);
             // Throwing exception might be too strong, maybe just return?
             throw new ProjectNotFoundException();
        }

        List<String> projectIDs = manager.getProject();
        // Check if manager has any projects associated
        if (projectIDs == null || projectIDs.isEmpty()) {
            System.out.println("Manager " + managerID + " is not currently managing any projects.");
            throw new ProjectNotFoundException(); // Or simply return without printing if exception isn't desired
        }

        // Filter the projects associated with the manager
        List<Project> list = FilterController.filteredListFromID(projectIDs);

        // Check if any projects remain after filtering
        if (list.isEmpty()) {
            System.out.println("No projects managed by " + managerID + " match the current filter criteria.");
            throw new ProjectNotFoundException(); // Or return without printing
        }

        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("          Projects Managed by " + manager.getName() + " (ID: " + managerID + ")");
        System.out.println("                 (Matching current filters)");
        System.out.println(UIController.LINE_SEPARATOR);
        FilterController.displayFilter(); // Show active filters for context

        // Display the filtered list
        for (Project project : list) {
            Display.displayProject(project, UserType.MANAGER, null); // Use Manager view, show all flat types
        }
    }

    /**
     * Generates and displays a report of applicants for a specific project based on criteria provided by the user.
     * Prompts the user for Project ID, age range, marital status, and applied flat type.
     * Filters the {@link ApplicantList} based on these criteria using Java Streams and displays the matching applicants.
     */
    public static void generateReport() {
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                      Generate Applicant Report");
        System.out.println(UIController.LINE_SEPARATOR);

        // Get Report Criteria from User
        System.out.print("Enter Project ID to report on: ");
        String projectID = IOController.nextLine();
         // Validate project exists
         if (ProjectList.getInstance().getByID(projectID) == null) {
              System.out.println("Error: Project with ID '" + projectID + "' not found.");
              return;
         }

        System.out.println("Enter desired applicant age range:");
        System.out.print("\tMinimum age (from): ");
        int fromAge = IOController.nextInt();
        System.out.print("\tMaximum age (to): ");
        int toAge = IOController.nextInt();
         if (fromAge < 0 || toAge < 0 || toAge < fromAge) {
              System.out.println("Error: Invalid age range entered.");
              return;
         }


        System.out.println("Enter desired marital status:");
        System.out.println("\t1. Single");
        System.out.println("\t2. Married");
        System.out.print("Your choice (1-2): ");
        MaritalStatus maritalStatus = null;
        while (maritalStatus == null) {
            int maritalChoice = IOController.nextInt();
            switch (maritalChoice) {
                case 1 -> maritalStatus = MaritalStatus.SINGLE;
                case 2 -> maritalStatus = MaritalStatus.MARRIED;
                default -> System.out.print("Invalid choice (1-2). Please try again: ");
            }
        }
        // Use final variable for lambda expression
        final MaritalStatus finalStatus = maritalStatus;

        System.out.println("Enter desired applied Flat Type:");
        System.out.println("\t1. Two Room");
        System.out.println("\t2. Three Room");
        System.out.print("Your choice (1-2): ");
        FlatType flatType = null;
        while (flatType == null) {
            int flatChoice = IOController.nextInt();
            switch (flatChoice) {
                case 1 -> flatType = FlatType.TWO_ROOM;
                case 2 -> flatType = FlatType.THREE_ROOM;
                default -> System.out.print("Invalid choice (1-2). Please try again: ");
            }
        }
        // Use final variable for lambda expression
        final FlatType finalFlatType = flatType;

        // Filter Applicants using Streams
        List<Applicant> report = ApplicantList.getInstance().getAll().stream()
                // Filter 1: Applied to the specified project? (Check non-null project ID first)
                .filter(a -> a.getProject() != null && a.getProject().equals(projectID))
                // Filter 2: Within age range?
                .filter(a -> a.getAge() >= fromAge && a.getAge() <= toAge)
                // Filter 3: Matches marital status?
                .filter(a -> a.getMaritalStatus() == finalStatus)
                // Filter 4: Applied for the specified flat type? (Check map contains key)
                .filter(a -> a.getAppliedFlat() != null && a.getAppliedFlat().get(a.getProject()) == finalFlatType)
                .collect(Collectors.toList()); // Collect matching applicants

        // Display Report Header
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("          Report for Project ID: " + projectID);
        System.out.println("          Criteria: Age " + fromAge + "-" + toAge +
                           ", Status: " + finalStatus + ", Flat Type: " + finalFlatType);
        System.out.println(UIController.LINE_SEPARATOR);

        // Display Applicants or 'No results' message
        if (report.isEmpty()) {
             System.out.println("No applicants found matching the specified criteria for this project.");
        } else {
            System.out.println("Found " + report.size() + " matching applicant(s):");
            for (Applicant applicant : report) {
                // Display applicant details (false likely means basic view)
                Display.displayApplicant(applicant, false);
                 System.out.println("---"); // Separator between applicants
            }
        }
        System.out.println(UIController.LINE_SEPARATOR);
    }
}
