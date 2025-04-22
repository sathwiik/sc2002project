package entity.list;

import java.util.List;

import utils.Converter; // Assumed utility for CSV conversion

import java.util.ArrayList;
import java.io.*;

/**
 * An abstract generic base class for managing lists of model objects of type {@code T}.
 * Provides common functionality including an internal list storage, basic CRUD operations
 * (add, update, delete, get all), size management, and persistence mechanisms
 * for loading from and saving to a file (presumably CSV format, utilizing a {@link Converter} utility).
 * This class implements the {@link Saveable} interface.
 * <p>
 * Subclasses must implement {@link #getFilePath()} to define the data storage location
 * and {@link #getByID(String)} to provide type-specific retrieval by a unique ID.
 *
 * @param <T> The type of the model objects managed by this list.
 */
public abstract class ModelList<T> implements Saveable {

    /** The Class object representing the type T, used for reflection (e.g., in CSV conversion). */
    private Class<T> clazz;
    /** The internal list holding the model objects of type T. */
    private List<T> list;

    // --- Constructors ---

    /**
     * Initializes the ModelList.
     * Creates an empty internal list, stores the class type {@code T} for potential reflection use,
     * and immediately attempts to load existing data from the specified file path.
     *
     * @param filePath The path to the file used for loading and saving the list data.
     * @param clazz    The {@code Class} object corresponding to the generic type {@code T},
     * required for type-specific operations like CSV conversion.
     */
    public ModelList(String filePath, Class<T> clazz) {
        this.clazz = clazz;
        this.list = new ArrayList<>();
        this.load(filePath, true); // Load data on initialization, assumes CSV has a header
    }

    // --- Abstract methods ---

    /**
     * Gets the specific file path used by the concrete subclass for data persistence.
     * Subclasses must implement this method to define where their data is stored.
     *
     * @return The file path string for loading and saving data.
     */
    public abstract String getFilePath();

    /**
     * Retrieves an item of type {@code T} from the list based on its unique String identifier.
     * Subclasses must implement the specific logic for searching based on their ID structure.
     *
     * @param ID The unique identifier of the item to retrieve.
     * @return The item of type {@code T} if found, otherwise {@code null}.
     */
    public abstract T getByID(String ID);

    // --- Public Accessor and Mutator Methods ---

    /**
     * Returns a defensive copy of the internal list of items.
     * Modifications to the returned list will not affect the internal list managed by this class.
     *
     * @return A new {@link ArrayList} containing all items currently in the list.
     */
    public List<T> getAll() {
        return new ArrayList<>(list); // Return a copy to prevent external modification
    }

    /**
     * Deletes an item from the list based on its ID.
     * Uses the subclass's {@link #getByID(String)} implementation to find the item.
     * If the item is found, it is removed from the list, and the list is saved to the file.
     *
     * @param ID The unique identifier of the item to delete.
     */
    public void delete(String ID) {
        T item = getByID(ID); // Find item using subclass implementation
        if (item != null) {
            boolean removed = list.remove(item);
             if (removed) {
                  save(getFilePath()); // Save the list after removal
                  System.out.println("Item with ID '" + ID + "' deleted successfully."); // Optional confirmation
             } else {
                  // This case might occur if getByID returns an object not considered equal by list.remove()
                  System.err.println("Warning: Item with ID '" + ID + "' found by getByID but remove() failed.");
             }
        } else {
             System.out.println("Item with ID '" + ID + "' not found for deletion."); // Optional info
        }
    }

    /**
     * Updates an item in the list.
     * This implementation performs an update by first removing the old item identified by {@code ID}
     * and then adding the {@code newItem}. The list is saved after the operation.
     * Note: This ensures the item is replaced but might affect order if the list is ordered.
     *
     * @param ID      The unique identifier of the item to replace.
     * @param newItem The new item object to add in place of the old one.
     */
    public void update(String ID, T newItem) {
        T oldItem = getByID(ID); // Find the old item
        if (oldItem != null) {
            list.remove(oldItem); // Remove the old version
        } else {
             System.err.println("Warning: Updating item ID '" + ID + "', but no existing item found to remove.");
        }
        list.add(newItem); // Add the new version
        save(getFilePath()); // Save the updated list
         System.out.println("Item with ID '" + ID + "' updated successfully."); // Optional confirmation
    }

    /**
     * Replaces the entire contents of the current list with the provided list of new items.
     * The internal list is cleared, the new items are added, and the list is saved.
     *
     * @param newItems The list of items that will replace the current contents.
     */
    public void updateAll(List<T> newItems) {
        clear(); // Clear the current internal list
        if (newItems != null) { // Add all items from the provided list
             list.addAll(newItems);
        }
        save(getFilePath()); // Save the new state
    }

    /**
     * Adds a single item to the end of the list and saves the list to the file.
     *
     * @param item The item of type {@code T} to add.
     */
    public void add(T item) {
         if (item != null) { // Basic check
             list.add(item);
             save(getFilePath()); // Save after adding
         } else {
              System.err.println("Warning: Attempted to add a null item.");
         }
    }

    /**
     * Returns the current number of items in the list.
     *
     * @return The size of the internal list.
     */
    public int size() {
        return list.size();
    }

    /**
     * Removes all items from the internal list.
     * Note: This method only clears the in-memory list; it does not automatically save the empty state to the file.
     * A subsequent save operation (e.g., via add, delete, update) is required to persist the cleared state.
     */
    public void clear() {
        list.clear();
    }

    // --- Persistence Methods ---

    /**
     * Loads list data from the specified file path.
     * If the file exists, it reads each line (optionally skipping a header),
     * converts the line to an object of type {@code T} using {@link Converter#stringtoObj(String, Class)},
     * and adds it to the internal list.
     * If the file does not exist, it attempts to create it.
     * Errors during file reading or creation are printed to standard error.
     *
     * @param filePath  The path to the file from which to load data.
     * @param hasHeader If true, the first line of the file is skipped as a header.
     */
    public void load(String filePath, boolean hasHeader) {
        List<String> data = new ArrayList<>();
        File file = new File(filePath);

        // Ensure the directory structure exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
             if (!parentDir.mkdirs()) {
                  System.err.println("Error: Could not create directory structure for file: " + filePath);
                  return; // Cannot proceed if directory cannot be made
             }
        }


        if (file.exists()) {
            // Load data if file exists
            try (BufferedReader br = new BufferedReader(new FileReader(file))) { // Use file object
                if (hasHeader && file.length() > 0) { // Check file length to avoid EOF on empty file header read
                     br.readLine(); // Skip header line
                }
                String line;
                while ((line = br.readLine()) != null) {
                     if (!line.trim().isEmpty()) { // Avoid processing empty lines
                         data.add(line);
                     }
                }
                // Convert loaded string data to objects
                for (String d : data) {
                    try {
                        T val = Converter.stringtoObj(d, this.clazz);
                        if (val != null) {
                             list.add(val);
                        } else {
                             System.err.println("Warning: Converter returned null for data line: " + d);
                        }
                    } catch (Exception conversionError) { // Catch potential errors during conversion
                         System.err.println("Error converting data line '" + d + "' to " + clazz.getSimpleName() + ": " + conversionError.getMessage());
                         // Optionally skip this line and continue, or re-throw
                    }
                }
                System.out.println("Data loaded successfully from: " + filePath); // Optional confirmation
            } catch (IOException e) {
                System.err.println("Error loading the data file '" + filePath + "': " + e.getMessage());
                // e.printStackTrace(); // Optionally print stack trace for debugging
            }
        } else {
            // Create file if it doesn't exist
            try {
                if (file.createNewFile()) {
                     System.out.println("File created successfully: " + filePath);
                } else {
                     System.err.println("File already existed (concurrent creation?): " + filePath);
                }
                // Optionally write header if creating a new file
                 if (hasHeader) {
                     // Need a way to get header string, perhaps from Converter or require subclasses?
                     // For now, just creates empty file. Saving later will add header.
                 }
            } catch (IOException e) {
                System.err.println("Error creating the data file '" + filePath + "': " + e.getMessage());
                // e.printStackTrace();
            }
        }
    }

    /**
     * Saves the current state of the internal list to the specified file path, overwriting existing content.
     * Implements the {@link Saveable#save(String)} method.
     * If the list is not empty, it generates a header line using {@link Converter#getField(Object)}
     * based on the first item, then writes the header followed by each item converted to a string
     * using {@link Converter#objToString(Object)}.
     *
     * @param filePath The path to the file where the data should be saved.
     * @throws RuntimeException if an {@link IOException} occurs during file writing.
     */
    @Override
    public void save(String filePath) {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(filePath))) {
            if (!list.isEmpty()) {
                // Get header from the first item
                T firstItem = list.get(0);
                 String header = null;
                 try {
                     header = Converter.getField(firstItem); // Assumes getField works for type T
                 } catch (Exception e) {
                      System.err.println("Error generating header for type " + clazz.getSimpleName() + ": " + e.getMessage());
                      // Decide whether to proceed without header or throw error
                 }

                if (header != null) {
                     printWriter.println(header); // Write header
                }

                // Write each item as a line
                for (T item : list) {
                     try {
                         String line = Converter.objToString(item); // Assumes objToString works for type T
                         printWriter.println(line);
                     } catch (Exception e) {
                          System.err.println("Error converting item to string for saving: " + item + " - " + e.getMessage());
                          // Decide whether to skip item or throw error
                     }
                }
            } else {
                // If list is empty, write nothing or optionally just the header if a mechanism exists to get it without an item
                // Currently writes an empty file if list is empty.
                 System.out.println("Saving empty list to: " + filePath);
            }
        } catch (IOException e) {
             // Wrap IOExceptions in a RuntimeException for simpler handling in calling code,
             // though specific handling might be preferred in some cases.
            throw new RuntimeException("Data could not be saved to file: " + filePath, e);
        }
    }
}
