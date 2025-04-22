package entity.list;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import entity.request.Request; // Base class for requests
import utils.Converter;    // Utility for object<->string conversion

/**
 * Manages a list of {@link Request} objects and their potential subclasses (e.g., Enquiry, BTOApplication),
 * handling data persistence and retrieval.
 * This class extends the generic {@link ModelList} to specialize in managing request data,
 * likely loaded from and saved to a CSV file specified by {@code FILE_PATH}.
 * It overrides the {@code load} method to handle the polymorphic nature of Request objects during deserialization.
 * Provides methods to access the list instance and retrieve requests by their ID.
 * Uses a static factory method {@code getInstance()} for convenient access.
 */
public class RequestList extends ModelList<Request> {

    /**
     * The default file path for storing and retrieving request data (CSV format).
     */
    private static final String FILE_PATH = "data_csv/RequestList.csv";

    /**
     * Constructs a RequestList instance associated with a specific file path.
     * Calls the superclass constructor to initialize the list, providing the file path
     * and the base {@code Request.class} type. Note that the overridden {@link #load} method
     * handles determining specific subclasses during loading.
     * Typically accessed via the static {@link #getInstance()} method using the default path.
     *
     * @param filePath The path to the CSV file used for data persistence.
     */
    public RequestList(String filePath) { // Changed parameter name for clarity
        super(filePath, Request.class); // Pass Request base class
    }

    /**
     * Provides a static factory method to get an instance of RequestList.
     * This method creates a new instance using the default {@code FILE_PATH}.
     * Note: This implementation creates a new instance on each call, potentially reloading data.
     * Consider implementing a true Singleton pattern if a single shared instance is desired.
     *
     * @return A new instance of {@code RequestList} initialized with the default file path.
     */
    public static RequestList getInstance() {
        // Creates a new instance each time, might reload data from CSV
        return new RequestList(FILE_PATH);
    }

    /**
     * Gets the file path associated with this RequestList instance,
     * indicating where the request data is persisted.
     *
     * @return The file path string (e.g., "data_csv/RequestList.csv").
     */
    @Override // Override annotation added, assuming getFilePath is abstract in ModelList
    public String getFilePath() {
        // Returns the path used by this specific instance
        return FILE_PATH; // Returns the static path defined here
    }

    /**
     * Retrieves a {@link Request} (or one of its subclasses) from the list based on its unique request ID.
     * Iterates through the list maintained by the superclass ({@link #getAll()})
     * and returns the first request matching the provided ID.
     *
     * @param requestID The unique ID of the request to find.
     * @return The {@link Request} object (or subclass instance) if found, otherwise {@code null}.
     */
    @Override // Override annotation added, assuming getByID is abstract in ModelList
    public Request getByID(String requestID) {
        // Input validation could be added (e.g., check if requestID is null or empty)
        if (requestID == null || requestID.isEmpty()) {
            return null;
        }
        // Uses this.getAll() which is equivalent to getAll() for instance methods
        for (Request request : this.getAll()) { // getAll() is inherited from ModelList
            if (requestID.equals(request.getRequestID())) { // Use equals for string comparison
                return request;
            }
        }
        return null; // Return null if no request with the given ID is found
    }

    /**
     * Overrides the load method to handle polymorphism within the Request hierarchy.
     * Reads data from the specified CSV file, determines the specific subclass of {@link Request}
     * for each line using {@link Converter#getRequestClass(String)}, deserializes the line
     * into an object of that specific subclass using {@link Converter#stringtoObj(String, Class)},
     * and adds the resulting object to the internal list using the superclass's add method.
     * Note: Calling {@code super.add(val)} might trigger the superclass's save mechanism
     * repeatedly during load, which could be inefficient. Consider loading all objects first,
     * then adding them to the internal list directly if performance is critical.
     *
     * @param filePath  The path to the file from which to load data.
     * @param hasHeader If true, the first line of the file is skipped as a header.
     */
    @Override
    public void load(String filePath, boolean hasHeader) { // Matched param name to superclass
        List<String> data = new ArrayList<>();
        File file = new File(filePath); // Use consistent variable name

        // Ensure the directory structure exists
         File parentDir = file.getParentFile();
         if (parentDir != null && !parentDir.exists()) {
              if (!parentDir.mkdirs()) {
                   System.err.println("Error: Could not create directory structure for file: " + filePath);
                   return; // Cannot proceed if directory cannot be made
              }
         }


        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) { // Use file object
                if (hasHeader && file.length() > 0) { // Check length before reading header
                    br.readLine(); // Skip header
                }

                String line;
                while ((line = br.readLine()) != null) {
                     if (!line.trim().isEmpty()) { // Ignore empty lines
                         data.add(line);
                     }
                }

                // Process loaded data lines
                for (String d : data) {
                     try {
                         // Determine the specific Request subclass from the data line
                        Class<? extends Request> specificRequestClass = Converter.getRequestClass(d);
                         if (specificRequestClass != null) {
                             // Convert the string data to an object of the determined subclass
                            Request val = Converter.stringtoObj(d, specificRequestClass);
                             if (val != null) {
                                 // Add the deserialized object to the list using super.add()
                                 // WARNING: super.add() calls save(), potentially saving on every line loaded.
                                 // Consider adding directly to 'this.list' instead for efficiency:
                                 // this.list.add(val);
                                 super.add(val); // Uses the provided implementation
                             } else {
                                  System.err.println("Warning: Converter returned null for data line: " + d + " with class " + specificRequestClass.getSimpleName());
                             }
                         } else {
                              System.err.println("Warning: Could not determine Request class type for data line: " + d);
                         }
                     } catch (Exception conversionError) { // Catch potential errors during conversion/class determination
                          System.err.println("Error processing data line '" + d + "': " + conversionError.getMessage());
                          // Optionally skip this line and continue, or re-throw
                     }
                }
                 System.out.println("Request data loaded successfully from: " + filePath); // Optional confirmation

            } catch (IOException e) {
                System.err.println("Error loading the request data file '" + filePath + "': " + e.getMessage());
                // e.printStackTrace(); // Optionally print stack trace for debugging
            }
        } else {
            // Create file if it doesn't exist
            try {
                if (file.createNewFile()) {
                     System.out.println("Request data file created successfully: " + filePath);
                     // Optionally write header here if known
                } else {
                     System.err.println("Request data file already existed (concurrent creation?): " + filePath);
                }
            } catch (IOException e) {
                System.err.println("Error creating the request data file '" + filePath + "': " + e.getMessage());
                // e.printStackTrace();
            }
        }
         // If loading directly into 'this.list', save once here after loop:
         // this.save(filePath);
    }
}
