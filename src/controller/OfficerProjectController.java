package controller;

import java.util.List;

import entity.list.ApplicantList;
import entity.list.OfficerList;
import entity.list.ProjectList;
import entity.project.FlatType;
import entity.project.Project;
import entity.user.Applicant;
import entity.user.ApplicationStatus;
import entity.user.Manager; // Note: Imported but not directly used in methods shown
import entity.user.Officer;
import entity.user.User;   // Note: Imported but not directly used in methods shown
import entity.user.UserType;
import utils.Display;

/**
 * Controller responsible for handling project-related operations performed by an Officer.
 * This includes viewing projects available for registration, viewing applicant statuses within assigned projects,
 * booking flats for successful applicants, and generating receipts/reports related to booked flats.
 * It operates using the context of the currently logged-in officer's ID.
 */
public class OfficerProjectController {

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
     * Displays a list of projects that the current officer may be eligible to register for.
     * Filters the global project list using {@link FilterController}.
     * Further filters based on:
     * - Project visibility.
     * - Officer is not already an applicant for the project.
     * - The project's dates do not overlap with any project the officer is already registered for.
     */
    public static void viewRegistrableProject() {
        List<Project> list = ProjectList.getInstance().getAll();
        list = FilterController.filteredList(list); // Apply general filters first
        Officer currentOfficer = OfficerList.getInstance().getByID(officerID);
        if (currentOfficer == null) {
             System.out.println("Error: Could not find officer details for ID: " + officerID);
             return;
        }
        List<String> officerProjectIDs = currentOfficer.getOfficerProject(); // Projects officer is already registered for
        boolean hasRegistrable = false; // Track if any registrable projects are found

        System.out.println("--- Projects Available for Officer Registration ---"); // Header
        for (Project potentialProject : list) {
            // Check basic conditions: visible and officer is not an applicant
            if (potentialProject.getVisibility() && !potentialProject.getApplicantID().contains(officerID)) {
                boolean canRegister = true; // Assume registrable unless overlap found
                // Check for date overlaps with already registered projects
                if (officerProjectIDs != null) {
                    for (String registeredProjectID : officerProjectIDs) {
                        Project registeredProject = ProjectList.getInstance().getByID(registeredProjectID);
                        if (registeredProject != null) {
                            // Check for overlap: !(registered_ends < potential_starts || potential_ends < registered_starts)
                            if (!(registeredProject.getCloseDate().isBefore(potentialProject.getOpenDate()) ||
                                  potentialProject.getCloseDate().isBefore(registeredProject.getOpenDate()))) {
                                canRegister = false; // Found overlap, cannot register
                                break; // No need to check other registered projects
                            }
                        }
                    }
                }
                // If no overlaps found, display the project
                if (canRegister) {
                    hasRegistrable = true;
                    // Display using Officer view, show all flat types
                    Display.displayProject(potentialProject, UserType.OFFICER, null);
                }
            }
        }
        if (!hasRegistrable) {
            System.out.println("There are no projects currently available for you to register for (considering filters and date overlaps).");
        }
    }

    /**
     * Displays the application status for all applicants across all projects the current officer is registered for.
     * Checks if the officer is registered for any projects before proceeding.
     */
    public static void viewApplicantApplicationStatus() {
        Officer officer = OfficerList.getInstance().getByID(officerID);
        if (officer == null) {
            System.out.println("Error: Could not find officer details for ID: " + officerID);
            return;
        }
        List<String> registeredProjectIDs = officer.getOfficerProject();

        if (!checkValidProject(registeredProjectIDs)) return; // Checks if list is empty and prints message

        System.out.println("--- Applicant Statuses for Your Registered Projects ---"); // Header
        for (String projectID : registeredProjectIDs) {
            Project project = ProjectList.getInstance().getByID(projectID);
            if (project != null) {
                System.out.println("\n--- Project: " + project.getName() + " (ID: " + projectID + ") ---");
                boolean foundApplicants = false;
                // Iterate through all applicants to find those associated with this project
                for (Applicant applicant : ApplicantList.getInstance().getAll()) {
                    // Check if applicant applied to this specific project
                    if (applicant.getProject() != null && applicant.getProject().equals(projectID)) {
                        ApplicationStatus status = applicant.getApplicationStatusByID(projectID);
                        System.out.println("\nApplicant: " + applicant.getName() + " (ID: " + applicant.getUserID() + ")");
                        System.out.println("Status: " + (status != null ? status.coloredString() : "N/A"));
                        Display.displayApplicant(applicant, false); // Display basic applicant info
                        foundApplicants = true;
                    }
                }
                if (!foundApplicants) {
                     System.out.println("No applicants found for this project.");
                }
            } else {
                 System.err.println("Warning: Could not find project details for registered project ID: " + projectID);
            }
        }
    }

    /**
     * Displays the application status for all applicants associated with a specific project ID.
     * Note: This method does not explicitly check if the current officer is assigned to this project.
     *
     * @param projectID The ID of the project for which to view applicant statuses.
     */
    public static void viewApplicantApplicationStatus(String projectID) {
        Project project = ProjectList.getInstance().getByID(projectID);
        if (project == null) {
             System.out.println("Error: Project with ID '" + projectID + "' not found.");
             return;
        }

        System.out.println("--- Applicant Statuses for Project: " + project.getName() + " (ID: " + projectID + ") ---"); // Header
        Display.displayProject(project, UserType.OFFICER, null); // Display project details
        boolean foundApplicants = false;
        for (Applicant applicant : ApplicantList.getInstance().getAll()) {
            if (applicant.getProject() != null && applicant.getProject().equals(projectID)) {
                 ApplicationStatus status = applicant.getApplicationStatusByID(projectID);
                 System.out.println("\nApplicant: " + applicant.getName() + " (ID: " + applicant.getUserID() + ")");
                 System.out.println("Status: " + (status != null ? status.coloredString() : "N/A"));
                 Display.displayApplicant(applicant, false);
                 foundApplicants = true;
            }
        }
         if (!foundApplicants) {
              System.out.println("No applicants found associated with this project.");
         }
    }

    /**
     * Displays applicants with a specific {@link ApplicationStatus} across all projects
     * the current officer is registered for.
     *
     * @param status The specific {@link ApplicationStatus} to filter by.
     */
    public static void viewApplicantApplicationStatus(ApplicationStatus status) {
        Officer officer = OfficerList.getInstance().getByID(officerID);
         if (officer == null) {
             System.out.println("Error: Could not find officer details for ID: " + officerID);
             return;
         }
        List<String> registeredProjectIDs = officer.getOfficerProject();

        if (!checkValidProject(registeredProjectIDs)) return; // Checks if list is empty and prints message

        System.out.println("--- Applicants with Status '" + status + "' in Your Registered Projects ---"); // Header
        boolean foundAnyMatching = false;
        for (String projectID : registeredProjectIDs) {
            Project project = ProjectList.getInstance().getByID(projectID);
             if (project != null) {
                 boolean foundInProject = false;
                 // Buffer output for this project to avoid printing project header if no matches
                 StringBuilder projectOutput = new StringBuilder();
                 projectOutput.append("\n--- Project: ").append(project.getName()).append(" (ID: ").append(projectID).append(") ---\n");

                for (Applicant applicant : ApplicantList.getInstance().getAll()) {
                     // Check project match AND status match
                    if (applicant.getProject() != null &&
                        applicant.getProject().equals(projectID) &&
                        applicant.getApplicationStatusByID(projectID) == status) {

                        projectOutput.append("\nApplicant: ").append(applicant.getName()).append(" (ID: ").append(applicant.getUserID()).append(")\n");
                        // Display.displayApplicant might print directly, adjust if needed for buffering
                         // For now, assume Display prints details not needed here, just list names/IDs
                         // Display.displayApplicant(applicant, false);
                        foundInProject = true;
                        foundAnyMatching = true;
                    }
                }
                 // Print project info only if matching applicants were found
                 if (foundInProject) {
                      System.out.println(projectOutput.toString());
                 }
             } else {
                  System.err.println("Warning: Could not find project details for registered project ID: " + projectID);
             }
        }
         if (!foundAnyMatching) {
             System.out.println("No applicants found with status '" + status + "' in your registered projects.");
         }
    }

    /**
     * Displays applicants with a specific {@link ApplicationStatus} for a specific project ID.
     * Note: This method does not explicitly check if the current officer is assigned to this project.
     *
     * @param projectID The ID of the project to filter by.
     * @param status    The specific {@link ApplicationStatus} to filter by.
     */
    public static void viewApplicantApplicationStatus(String projectID, ApplicationStatus status) {
        Project project = ProjectList.getInstance().getByID(projectID);
        if (project == null) {
             System.out.println("Error: Project with ID '" + projectID + "' not found.");
             return;
        }

        System.out.println("--- Applicants with Status '" + status + "' for Project: " + project.getName() + " (ID: " + projectID + ") ---"); // Header
        Display.displayProject(project, UserType.OFFICER, null); // Display project details
        boolean foundMatching = false;
        for (Applicant applicant : ApplicantList.getInstance().getAll()) {
            // Check project match AND status match
            if (applicant.getProject() != null &&
                applicant.getProject().equals(projectID) &&
                applicant.getApplicationStatusByID(projectID) == status) {

                System.out.println("\nApplicant: " + applicant.getName() + " (ID: " + applicant.getUserID() + ")");
                // Display.displayApplicant(applicant, false); // Display basic info
                foundMatching = true;
            }
        }
        if (!foundMatching) {
            System.out.println("No applicants found with status '" + status + "' for this project.");
        }
    }

    /**
     * Books a flat for a specified applicant.
     * Performs checks:
     * - Applicant ID is valid and corresponds to an Applicant (not Manager/Officer).
     * - Applicant has applied to a project.
     * - The current officer is registered for the applicant's project.
     * - Applicant's status is SUCCESSFUL (not already BOOKED or PENDING/UNSUCCESSFUL).
     * - The flat type applied for has available units.
     * If checks pass, updates the project's available units, adds the applicant to the project's booked list,
     * and updates the applicant's status to BOOKED.
     *
     * @param applicantID The ID of the applicant for whom to book the flat.
     */
    public static void bookFlat(String applicantID) {
        // Note: Comment in original code says "no need to pass project because applicant can has only 1 project"
        User user = ApplicantList.getInstance().getByID(applicantID); // Check ApplicantList first

        // Check 1: Is it a valid Applicant?
        if (!(user instanceof Applicant applicant)) { // Use pattern variable binding
            System.out.println("Booking failed: Invalid Applicant ID '" + applicantID + "' or user is not an Applicant.");
            return;
        }

        // Check 2: Has the applicant applied to a project?
        String projectID = applicant.getProject();
        if (projectID == null) {
            System.out.println("Booking failed: Applicant " + applicantID + " has not applied to any project.");
            return;
        }

        // Check 3: Is the current officer registered for this project?
        Officer officer = OfficerList.getInstance().getByID(officerID);
         if (officer == null || officer.getOfficerProject() == null || !officer.getOfficerProject().contains(projectID)) {
            System.out.println("Booking failed: You are not registered for project " + projectID + " which applicant " + applicantID + " applied to.");
            return;
        }

        // Check 4: Is the application status SUCCESSFUL?
        ApplicationStatus currentStatus = applicant.getApplicationStatusByID(projectID);
        if (currentStatus == ApplicationStatus.BOOKED) {
            System.out.println("Booking failed: A flat has already been booked for applicant " + applicantID + ".");
            return;
        } else if (currentStatus != ApplicationStatus.SUCCESSFUL) {
            System.out.println("Booking failed: Applicant " + applicantID + "'s application status is " + currentStatus + ", not SUCCESSFUL.");
            return;
        }

        // Check 5: Flat type and availability
        Project project = ProjectList.getInstance().getByID(projectID);
        FlatType flat = applicant.getAppliedFlatByID(projectID);
         if (project == null) {
              System.out.println("Booking failed: Project " + projectID + " not found.");
              return;
         }
        if (flat == null) {
            System.out.println("Booking failed: Could not determine the flat type applied for by applicant " + applicantID + ".");
            return;
        }
        int availableUnit = project.getAvailableUnit().getOrDefault(flat, 0);
        if (availableUnit <= 0) {
            System.out.println("Booking failed: No available units of type " + flat + " left in project " + projectID + ".");
            return;
        }

        // All checks passed, proceed with booking
        // Update Project: Decrement available units, add applicant to booked list
        project.setAvailableUnit(flat, availableUnit - 1);
        project.addApplicantID(applicantID); // Add applicant to the project's list of successful/booked applicants
        ProjectList.getInstance().update(projectID, project);

        // Update Applicant: Set status to BOOKED
        applicant.setApplicationStatusByID(projectID, ApplicationStatus.BOOKED);
        ApplicantList.getInstance().update(applicantID, applicant);

        System.out.println("Successfully booked a " + flat + " flat for applicant " + applicantID + " in project " + projectID + ".");
    }

    /**
     * Generates a 'receipt' by displaying details of all applicants who have successfully BOOKED a flat
     * across all projects the current officer is registered for.
     * Also displays the project details for context.
     */
    public static void generateReceipt() {
        Officer officer = OfficerList.getInstance().getByID(officerID);
        if (officer == null) {
             System.out.println("Error: Could not find officer details for ID: " + officerID);
             return;
        }
        List<String> registeredProjectIDs = officer.getOfficerProject();

        if (!checkValidProject(registeredProjectIDs)) return; // Checks if list is empty

        System.out.println("--- Receipt Generation: Booked Applicants in Your Projects ---"); // Header
        boolean foundAnyBooked = false;
        for (String projectID : registeredProjectIDs) {
            Project project = ProjectList.getInstance().getByID(projectID);
            if (project != null) {
                List<String> bookedApplicantIDs = project.getApplicantID(); // Get list of booked applicants from project
                if (bookedApplicantIDs != null && !bookedApplicantIDs.isEmpty()) {
                     boolean foundInProject = false;
                     StringBuilder projectReceipt = new StringBuilder();
                     projectReceipt.append("\n--- Project: ").append(project.getName()).append(" (ID: ").append(projectID).append(") ---\n");
                     // Display.displayProject(project, UserType.OFFICER, null); // Optional: Display full project details

                    for (String bookedApplicantID : bookedApplicantIDs) {
                        Applicant applicant = ApplicantList.getInstance().getByID(bookedApplicantID);
                        // Double-check status is indeed BOOKED
                        if (applicant != null && applicant.getApplicationStatusByID(projectID) == ApplicationStatus.BOOKED) {
                            projectReceipt.append("\n-- Applicant: ").append(applicant.getName()).append(" (ID: ").append(bookedApplicantID).append(") --\n");
                            // Display relevant details for receipt
                            Display.displayApplicant(applicant, false); // Basic info
                             FlatType bookedFlat = applicant.getAppliedFlatByID(projectID);
                             Integer price = project.getPrice().get(bookedFlat);
                             projectReceipt.append("Booked Flat Type: ").append(bookedFlat != null ? bookedFlat : "N/A").append("\n");
                             projectReceipt.append("Price: ").append(price != null ? price : "N/A").append("\n");
                            foundInProject = true;
                            foundAnyBooked = true;
                        }
                    }
                     // Print project receipt only if booked applicants were found
                     if (foundInProject) {
                          System.out.println(projectReceipt.toString());
                     }
                }
            } else {
                 System.err.println("Warning: Could not find project details for registered project ID: " + projectID);
            }
        }
         if (!foundAnyBooked) {
              System.out.println("No applicants with booked flats found in your registered projects.");
         }
    }

    /**
     * Generates a 'receipt' for a specific applicant ID.
     * Displays details if the applicant has BOOKED status and is associated with a project
     * the current officer is registered for.
     *
     * @param applicantID The ID of the applicant for whom to generate the receipt.
     */
    public static void generateReceiptByApplicant(String applicantID) {
        Officer officer = OfficerList.getInstance().getByID(officerID);
        if (officer == null) {
             System.out.println("Error: Could not find officer details for ID: " + officerID);
             return;
        }
        List<String> registeredProjectIDs = officer.getOfficerProject();

        if (!checkValidProject(registeredProjectIDs)) return; // Checks if list is empty

        Applicant applicant = ApplicantList.getInstance().getByID(applicantID);
        if (applicant == null) {
             System.out.println("Applicant with ID '" + applicantID + "' not found.");
             return;
        }

        String projectID = applicant.getProject(); // Project applicant applied to/booked in
        if (projectID == null) {
             System.out.println("Applicant " + applicantID + " is not associated with any project.");
             return;
        }

        // Check if the officer manages this project AND if the applicant has BOOKED status
        if (registeredProjectIDs.contains(projectID) && applicant.getApplicationStatusByID(projectID) == ApplicationStatus.BOOKED) {
            Project project = ProjectList.getInstance().getByID(projectID);
            if (project != null) {
                 System.out.println("--- Receipt for Applicant ID: " + applicantID + " ---"); // Header
                 Display.displayProject(project, UserType.OFFICER, null); // Show project context
                 System.out.println("\n-- Applicant Details --");
                 Display.displayApplicant(applicant, false); // Show applicant details
                 FlatType bookedFlat = applicant.getAppliedFlatByID(projectID);
                 Integer price = project.getPrice().get(bookedFlat);
                 System.out.println("Booking Status: " + applicant.getApplicationStatusByID(projectID));
                 System.out.println("Booked Flat Type: " + (bookedFlat != null ? bookedFlat : "N/A"));
                 System.out.println("Price: " + (price != null ? price : "N/A"));
                 return; // Receipt generated, exit method
            } else {
                  System.err.println("Warning: Could not find project details for project ID: " + projectID);
            }
        }

        // If loop completes without returning, the applicant wasn't found or didn't meet criteria
        System.out.println("Could not generate receipt: Applicant '" + applicantID + "' not found with BOOKED status in your registered projects.");
    }

    /**
     * Generates a 'receipt' by displaying details of all applicants who have successfully BOOKED a flat
     * for a specific project ID, provided the current officer is registered for that project.
     *
     * @param projectID The ID of the project for which to generate receipts.
     */
    public static void generateReceiptByProject(String projectID) {
        Officer officer = OfficerList.getInstance().getByID(officerID);
        if (officer == null) {
             System.out.println("Error: Could not find officer details for ID: " + officerID);
             return;
        }
        List<String> registeredProjectIDs = officer.getOfficerProject();

        // Check if officer is registered for the requested project
        if (registeredProjectIDs == null || !registeredProjectIDs.contains(projectID)) {
            System.out.println("Cannot generate receipt: You are not registered for project ID '" + projectID + "'.");
            return;
        }

        Project project = ProjectList.getInstance().getByID(projectID);
        if (project == null) {
             System.out.println("Error: Project with ID '" + projectID + "' not found.");
             return;
        }

        System.out.println("--- Receipt Generation: Booked Applicants for Project: " + project.getName() + " (ID: " + projectID + ") ---"); // Header
        Display.displayProject(project, UserType.OFFICER, null); // Display project details

        List<String> bookedApplicantIDs = project.getApplicantID(); // Get booked applicants from project
        boolean foundBooked = false;
        if (bookedApplicantIDs != null && !bookedApplicantIDs.isEmpty()) {
            for (String bookedApplicantID : bookedApplicantIDs) {
                Applicant applicant = ApplicantList.getInstance().getByID(bookedApplicantID);
                // Ensure applicant exists and status is BOOKED
                if (applicant != null && applicant.getApplicationStatusByID(projectID) == ApplicationStatus.BOOKED) {
                     System.out.println("\n-- Applicant: " + applicant.getName() + " (ID: " + bookedApplicantID + ") --");
                     Display.displayApplicant(applicant, false); // Basic info
                     FlatType bookedFlat = applicant.getAppliedFlatByID(projectID);
                     Integer price = project.getPrice().get(bookedFlat);
                     System.out.println("Booked Flat Type: " + (bookedFlat != null ? bookedFlat : "N/A"));
                     System.out.println("Price: " + (price != null ? price : "N/A"));
                    foundBooked = true;
                }
            }
        }

        if (!foundBooked) {
            System.out.println("\nNo applicants with booked flats found for this project.");
        }
    }

    /**
     * Helper method to check if the officer is associated with any projects.
     * Prints a message to the console if the officer is not registered for any projects.
     * Suggestion: Could be made private if only used within this class.
     *
     * @param projectIds The list of project IDs the officer is registered for.
     * @return true if the list is not null and not empty, false otherwise.
     */
    public static boolean checkValidProject(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            System.out.println("Action cannot be performed: You are not registered for any projects.");
            return false;
        }
        return true;
    }
}
