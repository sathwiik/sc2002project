package entity.list;

import entity.project.Project;

/**
 * Manages a list of {@link Project} objects, handling data persistence and retrieval.
 * This class extends the generic {@link ModelList} to specialize in managing project data,
 * likely loaded from and saved to a CSV file specified by the internal {@code FILE_PATH}.
 * It provides methods to access the list instance and retrieve projects by their ID.
 * Uses a static factory method {@code getInstance()} for convenient access.
 */
public class ProjectList extends ModelList<Project> {

    /**
     * The file path for storing and retrieving project data (CSV format).
     */
    private static final String FILE_PATH = "data_csv/ProjectList.csv";

    /**
     * Constructs a ProjectList instance.
     * Calls the superclass constructor using the default {@code FILE_PATH}
     * and passing the {@code Project.class} type for CSV data mapping.
     * Typically accessed via the static {@link #getInstance()} method.
     */
    public ProjectList() {
        super(FILE_PATH, Project.class); // Use static FILE_PATH and Project class
    }

    /**
     * Provides a static factory method to get an instance of ProjectList.
     * This method creates a new instance using the default {@code FILE_PATH}.
     * Note: This implementation creates a new instance on each call, potentially reloading data.
     * Consider implementing a true Singleton pattern if a single shared instance is desired.
     *
     * @return A new instance of {@code ProjectList} initialized with the default file path.
     */
    public static ProjectList getInstance() {
        // Creates a new instance each time, might reload data from CSV
        return new ProjectList();
    }

    /**
     * Gets the file path associated with this ProjectList instance,
     * indicating where the project data is persisted.
     *
     * @return The file path string (e.g., "data_csv/ProjectList.csv").
     */
    @Override // Override annotation added, assuming getFilePath is abstract in ModelList
    public String getFilePath() {
        // Returns the path used by this specific instance
        return FILE_PATH; // Returns the static path defined here
    }

    /**
     * Retrieves a {@link Project} from the list based on its unique project ID.
     * Iterates through the list maintained by the superclass ({@link #getAll()})
     * and returns the first project matching the provided ID.
     *
     * @param ID The project ID (e.g., "PRJ001") of the project to find.
     * @return The {@link Project} object if found, otherwise {@code null}.
     */
    @Override // Override annotation added, assuming getByID is abstract in ModelList
    public Project getByID(String ID) {
        // Input validation could be added (e.g., check if ID is null or empty)
        if (ID == null || ID.isEmpty()) {
            return null;
        }
        // Uses this.getAll() which is equivalent to getAll() for instance methods
        for (Project project : this.getAll()) { // getAll() is inherited from ModelList
            // Ensure comparison uses the correct ID field from Project class
            if (ID.equals(project.getProjectID())) { // Use equals for string comparison
                return project;
            }
        }
        return null; // Return null if no project with the given ID is found
    }
}
