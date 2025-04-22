package entity.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Represents a housing project entity.
 * Contains details such as project identification, name, associated neighbourhoods,
 * available units and prices for different flat types, application timeline (open/close dates),
 * associated manager, available officer slots, assigned officers, applicants who have booked units,
 * and the project's visibility status.
 */
public class Project {

    /** Unique identifier for the project (e.g., "PRJ001"). */
    private String projectID;
    /** The official name of the housing project. */
    private String name;
    /** A list of nearby neighbourhood names relevant to the project's location. */
    private List<String> neighborhood;
    /** A map storing the number of available units for each {@link FlatType}. */
    private Map<FlatType, Integer> availableUnit;
    /** A map storing the price for each {@link FlatType}. */
    private Map<FlatType, Integer> price;
    /** The date when applications for this project open. */
    private LocalDate openDate;
    /** The date when applications for this project close. */
    private LocalDate closeDate;
    /** The user ID of the manager responsible for this project. */
    private String managerID;
    /** The number of officer slots still available for assignment to this project. */
    private int availableOfficer;
    /** A list of user IDs of the officers assigned to this project. */
    private List<String> officerID;
    /** A list of user IDs of the applicants who have successfully booked a flat in this project. */
    private List<String> applicantID;
    /** A flag indicating whether the project is currently visible to applicants/officers (e.g., for applications or registration). */
    private boolean visibility;

    /**
     * Default constructor.
     * Initializes a new Project instance with default values:
     * empty strings, empty lists/maps, current date for open/close dates,
     * zero available officers, and false visibility.
     */
    public Project() {
        this.projectID = "";
        this.name = "";
        this.neighborhood = new ArrayList<>();
        this.availableUnit = new HashMap<>();
        this.price = new HashMap<>();
        // Default dates to current date - consider if null or a specific past/future date is more appropriate
        this.openDate = LocalDate.now();
        this.closeDate = LocalDate.now();
        this.managerID = "";
        this.availableOfficer = 0;
        this.officerID = new ArrayList<>(); // Initialize as empty list
        this.applicantID = new ArrayList<>(); // Initialize as empty list
        this.visibility = false;
    }

    /**
     * Parameterized constructor to create a Project with specified initial values.
     * Note: The lists for officer IDs and applicant IDs are initialized as empty by this constructor;
     * they are expected to be populated later.
     *
     * @param projectID        The unique ID for the project.
     * @param name             The name of the project.
     * @param neighbourhood    The list of nearby neighbourhoods.
     * @param availableUnit    The map of available units per flat type.
     * @param price            The map of prices per flat type.
     * @param openDate         The application open date.
     * @param closeDate        The application close date.
     * @param managerID        The ID of the managing user.
     * @param availableOfficer The initial number of available officer slots.
     * @param visibility       The initial visibility status.
     */
    public Project(String projectID, String name, List<String> neighbourhood, Map<FlatType, Integer> availableUnit,
                   Map<FlatType, Integer> price,
                   LocalDate openDate, LocalDate closeDate, String managerID, int availableOfficer, boolean visibility) {

        this.projectID = projectID;
        this.name = name;
        this.neighborhood = (neighbourhood != null) ? new ArrayList<>(neighbourhood) : new ArrayList<>(); // Use copy for safety
        this.availableUnit = (availableUnit != null) ? new HashMap<>(availableUnit) : new HashMap<>(); // Use copy for safety
        this.price = (price != null) ? new HashMap<>(price) : new HashMap<>(); // Use copy for safety
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.managerID = managerID;
        this.availableOfficer = availableOfficer;
        this.officerID = new ArrayList<>(); // Initialize as empty list
        this.applicantID = new ArrayList<>(); // Initialize as empty list
        this.visibility = visibility;
    }

    /**
     * Gets the unique project ID.
     * @return The project ID string.
     */
    public String getProjectID() {
        return projectID;
    }

    /**
     * Sets the unique project ID.
     * @param projectID The project ID string.
     */
    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    /**
     * Gets the name of the project.
     * @return The project name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the project.
     * @param name The project name string.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of nearby neighbourhood names.
     * @return The list of neighbourhood strings. Returns an empty list if none are set.
     */
    public List<String> getNeighborhood() {
        // Return a copy or ensure list is initialized to prevent NullPointerException
        return (neighborhood != null) ? neighborhood : new ArrayList<>();
    }

    /**
     * Sets the list of nearby neighbourhood names.
     * @param neighborhood The list of neighbourhood strings.
     */
    public void setNeighborhood(List<String> neighborhood) {
        this.neighborhood = (neighborhood != null) ? new ArrayList<>(neighborhood) : new ArrayList<>(); // Use copy for safety
    }

    /**
     * Gets the map of available units per flat type.
     * @return The map where keys are {@link FlatType} and values are Integer counts. Returns an empty map if none are set.
     */
    public Map<FlatType, Integer> getAvailableUnit() {
        // Return a copy or ensure map is initialized
        return (availableUnit != null) ? availableUnit : new HashMap<>();
    }

    /**
     * Sets the map of available units per flat type.
     * @param availableUnit The map where keys are {@link FlatType} and values are Integer counts.
     */
    public void setAvailableUnit(Map<FlatType, Integer> availableUnit) {
        this.availableUnit = (availableUnit != null) ? new HashMap<>(availableUnit) : new HashMap<>(); // Use copy for safety
    }

    /**
     * Sets the number of available units for a specific flat type.
     * Adds or updates the entry in the available units map.
     *
     * @param flat          The {@link FlatType} to update.
     * @param availableUnit The number of available units for the specified flat type.
     */
    public void setAvailableUnit(FlatType flat, Integer availableUnit) {
        if (this.availableUnit == null) {
             this.availableUnit = new HashMap<>(); // Initialize if null
        }
        this.availableUnit.put(flat, availableUnit);
    }

    /**
     * Gets the map of prices per flat type.
     * @return The map where keys are {@link FlatType} and values are Integer prices. Returns an empty map if none are set.
     */
    public Map<FlatType, Integer> getPrice() {
         // Return a copy or ensure map is initialized
        return (price != null) ? price : new HashMap<>();
    }

    /**
     * Sets the map of prices per flat type.
     * @param price The map where keys are {@link FlatType} and values are Integer prices.
     */
    public void setPrice(Map<FlatType, Integer> price) {
        this.price = (price != null) ? new HashMap<>(price) : new HashMap<>(); // Use copy for safety
    }

    /**
     * Gets the application open date.
     * @return The open date as a {@link LocalDate}.
     */
    public LocalDate getOpenDate() {
        return openDate;
    }

    /**
     * Sets the application open date.
     * @param openDate The open date as a {@link LocalDate}.
     */
    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    /**
     * Gets the application close date.
     * @return The close date as a {@link LocalDate}.
     */
    public LocalDate getCloseDate() {
        return closeDate;
    }

    /**
     * Sets the application close date.
     * @param closeDate The close date as a {@link LocalDate}.
     */
    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    /**
     * Gets the user ID of the manager responsible for this project.
     * @return The manager's user ID string.
     */
    public String getManagerID() {
        return managerID;
    }

    /**
     * Sets the user ID of the manager responsible for this project.
     * @param managerID The manager's user ID string.
     */
    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }

    /**
     * Gets the number of available officer slots for this project.
     * @return The count of available officer slots.
     */
    public int getAvailableOfficer() {
        return availableOfficer;
    }

    /**
     * Sets the number of available officer slots for this project.
     * @param availableOfficer The count of available officer slots.
     */
    public void setAvailableOfficer(int availableOfficer) {
        this.availableOfficer = availableOfficer;
    }

    /**
     * Gets the list of user IDs of officers assigned to this project.
     * @return The list of officer user ID strings. Returns an empty list if none are set.
     */
    public List<String> getOfficerID() {
         // Return a copy or ensure list is initialized
        return (officerID != null) ? officerID : new ArrayList<>();
    }

    /**
     * Sets the list of user IDs of officers assigned to this project.
     * @param officerID The list of officer user ID strings.
     */
    public void setOfficerID(List<String> officerID) {
        this.officerID = (officerID != null) ? new ArrayList<>(officerID) : new ArrayList<>(); // Use copy for safety
    }

    /**
     * Gets the list of user IDs of applicants who have successfully booked a flat in this project.
     * @return The list of applicant user ID strings. Returns an empty list if none are set.
     */
    public List<String> getApplicantID() {
         // Return a copy or ensure list is initialized
        return (applicantID != null) ? applicantID : new ArrayList<>();
    }

    /**
     * Sets the list of user IDs of applicants who have successfully booked a flat in this project.
     * @param applicantID The list of applicant user ID strings.
     */
    public void setApplicantID(List<String> applicantID) {
        this.applicantID = (applicantID != null) ? new ArrayList<>(applicantID) : new ArrayList<>(); // Use copy for safety
    }

    /**
     * Adds a single applicant ID to the list of applicants who have booked a flat.
     * Typically called when an officer successfully books a flat for an applicant.
     *
     * @param applicantID The user ID of the applicant to add.
     */
    public void addApplicantID(String applicantID) {
         if (this.applicantID == null) {
              this.applicantID = new ArrayList<>(); // Initialize if null
         }
         // Avoid adding duplicates if desired
         if (applicantID != null && !this.applicantID.contains(applicantID)) {
            this.applicantID.add(applicantID);
         }
    }

    /**
     * Gets the visibility status of the project.
     * @return true if the project is visible, false otherwise.
     */
    public boolean getVisibility() {
        return visibility;
    }

    /**
     * Sets the visibility status of the project.
     * @param visibility true to make the project visible, false otherwise.
     */
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    /**
     * Copies all attribute values from another {@code Project} object into this object.
     * This is a shallow copy for primitive types and references (lists/maps point to the same objects as the source).
     * Consider implementing deep copy if independent copies of lists/maps are needed.
     *
     * @param project The source {@code Project} object from which to copy attributes.
     */
    public void setAll(Project project) {
         if (project == null) return; // Guard against null input

        this.projectID = project.getProjectID();
        this.name = project.getName();
        // Use getters which return copies/safe versions if implemented that way
        this.neighborhood = project.getNeighborhood();
        this.availableUnit = project.getAvailableUnit();
        this.price = project.getPrice();
        this.openDate = project.getOpenDate();
        this.closeDate = project.getCloseDate();
        this.managerID = project.getManagerID();
        this.availableOfficer = project.getAvailableOfficer();
        this.officerID = project.getOfficerID();
        this.applicantID = project.getApplicantID();
        this.visibility = project.getVisibility();
    }
}
