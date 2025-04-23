import boundary.LoginPage;
import utils.IDController;

/**
 * The main entry point class for the housing application system.
 * Initializes necessary controllers and starts the user interface flow.
 */
public class Main {
    /**
     * Private constructor to prevent instantiation of this  class.
     * Throwing an error ensures it's not accidentally called via reflection.
     */
    private Main() {
        // Prevent instantiation
        throw new IllegalStateException("This class should not be instantiated");
    }
    /**
     * The main method that serves as the entry point for the application execution.
     * It first initializes the ID generation system by calling {@link utils.IDController#init()}
     * to ensure unique IDs can be generated based on existing data.
     * Then, it displays the initial welcome/login screen to the user by calling
     * {@link boundary.LoginPage#welcome()}.
     *
     * @param args Command line arguments (not used in this application).
     */
    public static void main(String[] args) {
        // Initialize the ID Controller to load the highest existing IDs
        // This should be done once at the start before generating any new IDs.
        IDController.init();

        // Start the application by displaying the main login/welcome page.
        LoginPage.welcome();
    }
}
