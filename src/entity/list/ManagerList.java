package entity.list;

import entity.user.Manager;

/**
 * Manages a list of {@link Manager} objects, handling data persistence and retrieval.
 * This class extends the generic {@link ModelList} to specialize in managing manager data,
 * likely loaded from and saved to a CSV file specified by {@code FILE_PATH}.
 * It provides methods to access the list instance and retrieve managers by their ID.
 * Uses a static factory method {@code getInstance()} for convenient access.
 */
public class ManagerList extends ModelList<Manager> {

    /**
     * The default file path for storing and retrieving manager data (CSV format).
     */
    private static final String FILE_PATH = "data_csv/ManagerList.csv";

    /**
     * Constructs a ManagerList instance associated with a specific file path.
     * Calls the superclass constructor to initialize the list, providing the file path
     * and the {@code Manager.class} type for CSV data mapping.
     * Typically accessed via the static {@link #getInstance()} method using the default path.
     *
     * @param filePath The path to the CSV file used for data persistence.
     */
    public ManagerList(String filePath) { // Changed parameter name for clarity
        super(filePath, Manager.class); // Pass Manager class for reflection/CSV handling
    }

    /**
     * Provides a static factory method to get an instance of ManagerList.
     * This method creates a new instance using the default {@code FILE_PATH}.
     * Note: This implementation creates a new instance on each call, potentially reloading data.
     * Consider implementing a true Singleton pattern if a single shared instance is desired.
     *
     * @return A new instance of {@code ManagerList} initialized with the default file path.
     */
    public static ManagerList getInstance() {
        // Creates a new instance each time, might reload data from CSV
        return new ManagerList(FILE_PATH);
    }

    /**
     * Gets the file path associated with this ManagerList instance,
     * indicating where the manager data is persisted.
     *
     * @return The file path string (e.g., "data_csv/ManagerList.csv").
     */
    public String getFilePath() {
        // Returns the path used by this specific instance (inherited from ModelList or set via constructor)
        // Assuming superclass has a getter or stores the path. If not, return the static constant.
        // return super.getFilePath(); // If superclass provides it
        return FILE_PATH; // Assuming it uses the static path defined here
    }

    /**
     * Retrieves a {@link Manager} from the list based on their unique user ID.
     * Iterates through the list maintained by the superclass ({@link #getAll()})
     * and returns the first manager matching the provided ID.
     *
     * @param ID The user ID of the manager to find.
     * @return The {@link Manager} object if found, otherwise {@code null}.
     */
    public Manager getByID(String ID) {
        // Input validation could be added (e.g., check if ID is null or empty)
        if (ID == null || ID.isEmpty()) {
            return null;
        }
        // Uses this.getAll() which is equivalent to getAll() for instance methods
        for (Manager manager : this.getAll()) { // getAll() is inherited from ModelList
            if (ID.equals(manager.getUserID())) { // Use equals for string comparison
                return manager;
            }
        }
        return null; // Return null if no manager with the given ID is found
    }
}
