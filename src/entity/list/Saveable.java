package entity.list;

/**
 * Defines a contract for classes whose state can be persisted (saved to)
 * and loaded from a storage medium, typically a file identified by a path.
 * Implementing classes are expected to handle the specifics of serialization
 * and deserialization for their data.
 */
public interface Saveable {

    /**
     * Loads the object's state from the specified file path.
     * Implementations should handle file reading, data parsing (e.g., CSV),
     * and populating the object's internal fields.
     *
     * @param filePath  The path to the file from which to load the data.
     * @param hasHeader A boolean flag indicating whether the file contains a header row
     * that should be skipped during the loading process.
     */
    public void load(String filePath, boolean hasHeader);

    /**
     * Saves the object's current state to the specified file path.
     * Implementations should handle formatting the data (e.g., to CSV) and writing it
     * to the file, typically overwriting any existing content.
     *
     * @param filePath The path to the file where the data should be saved.
     */
    public void save(String filePath);

}
