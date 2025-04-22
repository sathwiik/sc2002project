package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import entity.list.ProjectList;
import entity.project.FlatType;
import entity.project.Project;
import utils.IOController;
import utils.SortType;
import utils.UIController;

/**
 * Controller responsible for managing filter and sort criteria for Project lists.
 * It holds the current filter settings as static variables and provides methods
 * to initialize, set up (via user input), display, and apply these filters to lists of Projects.
 */
public class FilterController {

    /** List of desired neighbourhood locations for filtering. Null or empty means no location filter. */
    public static List<String> location;
    /** The minimum desired price for filtering. Null means no lower price bound. */
    public static Integer priceLowerBound;
    /** The maximum desired price for filtering. Null means no upper price bound. */
    public static Integer priceUpperBound;
    /** The specific {@link FlatType} to filter by. Null means no flat type filter (show projects with any available units). */
    public static FlatType flatType;
    /** The earliest acceptable project open date for filtering. Null means no start date filter. */
    public static LocalDate startDate;
    /** The latest acceptable project close date for filtering. Null means no end date filter. */
    public static LocalDate endDate;
    /** The criteria used for sorting the filtered list (e.g., by NAME, PRICE, DATE). Defaults to NAME. */
    public static SortType sortType;

    /**
     * Initializes or resets all filter criteria to their default state.
     * Sets location, price bounds, flat type, and dates to null.
     * Sets the default sort type to {@link SortType#NAME}.
     * Typically called at the start of a session or when resetting filters.
     */
    public static void init() {
        location = null;
        priceLowerBound = null;
        priceUpperBound = null;
        flatType = null; // Explicitly set flatType to null on init
        startDate = null;
        endDate = null;
        sortType = SortType.NAME; // Default sort order
    }

    /**
     * Displays the currently active filter and sort settings to the console.
     * Only non-null filter values are displayed.
     */
    public static void displayFilter() {
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                  Your Current Filter Settings");
        System.out.println(UIController.LINE_SEPARATOR);
        boolean hasFilters = false; // Track if any filter is active
        if (location != null && !location.isEmpty()) { // Check if list is not empty too
             System.out.println("Location(s): " + String.join(", ", location));
             hasFilters = true;
        }
        if (priceLowerBound != null) {
             System.out.println("Lowest Price: " + priceLowerBound);
             hasFilters = true;
        }
        if (priceUpperBound != null) {
             System.out.println("Highest Price: " + priceUpperBound);
             hasFilters = true;
        }
        if (flatType != null) {
             System.out.println("Flat Type: " + flatType);
             hasFilters = true;
        }
        if (startDate != null) {
             System.out.println("Minimum Open Date: " + startDate); // Clarified label
             hasFilters = true;
        }
        if (endDate != null) {
             System.out.println("Maximum Close Date: " + endDate); // Clarified label
             hasFilters = true;
        }
        if (sortType != null) { // Always display sort type as it has a default
            System.out.println("Sort By: " + sortType);
        } else {
             System.out.println("Sort By: Default (NAME)"); // Indicate default if somehow null
        }

        if (!hasFilters && sortType == SortType.NAME) { // If only default sort is active
            System.out.println("No active filters. Default sort by Name.");
        }
        System.out.println(UIController.LINE_SEPARATOR);
    }

    /**
     * Retrieves Project objects based on a list of project IDs and then applies the current filters.
     *
     * @param IDList A list of project IDs to retrieve and filter.
     * @return A list of {@link Project} objects corresponding to the IDs, after applying the filters defined in this controller.
     */
    public static List<Project> filteredListFromID(List<String> IDList) {
        List<Project> ret = new ArrayList<>();
        if (IDList == null) return filteredList(ret); // Handle null IDList gracefully

        for (String id : IDList) {
            Project project = ProjectList.getInstance().getByID(id);
            if (project != null) { // Add only if project is found
                ret.add(project);
            } else {
                System.err.println("Warning: Project ID '" + id + "' not found in ProjectList.");
            }
        }
        return filteredList(ret); // Apply filters to the retrieved projects
    }

    /**
     * Applies the currently configured filters and sorting to a given list of Projects.
     * Filters applied include:
     * - Flat Type: Removes projects that have 0 units of the specified {@code flatType}. If {@code flatType} is null, it removes projects with 0 units of BOTH TWO_ROOM and THREE_ROOM.
     * - Location: Keeps projects where at least one specified location name is contained within the project's neighbourhood list.
     * - Price: Keeps projects where both TWO_ROOM and THREE_ROOM prices fall within the {@code priceLowerBound} and {@code priceUpperBound} (if bounds are set).
     * - Date: Keeps projects where the open date is after {@code startDate} and the close date is before {@code endDate} (if dates are set).
     * Sorts the filtered list based on the current {@code sortType}.
     *
     * @param project The initial list of {@link Project} objects to filter and sort.
     * @return A new list containing the filtered and sorted Projects.
     */
    public static List<Project> filteredList(List<Project> project) {
        if (project == null) return new ArrayList<>(); // Handle null input list

        // --- Pre-filter based on FlatType availability (modifies the input list reference effectively) ---
        // Create a copy to iterate over while potentially modifying the original list passed to the stream
        List<Project> projectsToFilter = new ArrayList<>(project);
        List<Project> removalList = new ArrayList<>(); // List projects to remove

        for (Project p : projectsToFilter) {
            Map<FlatType, Integer> mp = p.getAvailableUnit();
            if (flatType == null) {
                // If no specific type filter, remove only if BOTH types have 0 units
                if (mp.getOrDefault(FlatType.TWO_ROOM, 0) == 0 && mp.getOrDefault(FlatType.THREE_ROOM, 0) == 0) {
                    removalList.add(p);
                }
            } else {
                // If a specific type filter is set, remove if that type has 0 units
                if (mp.getOrDefault(flatType, 0) == 0) {
                    removalList.add(p);
                }
            }
        }
        // Remove projects identified for removal from the list that will be streamed
        projectsToFilter.removeAll(removalList);

        // --- Stream-based filtering and sorting ---
        return projectsToFilter.stream()
                // Location Filter: true if no location filter OR project contains any specified location
                .filter(p -> location == null || location.isEmpty() || location.stream().anyMatch(loc -> p.getNeighborhood().contains(loc)))
                // Price Lower Bound Filter: true if no lower bound OR BOTH flat prices meet/exceed bound
                .filter(p -> priceLowerBound == null ||
                           (p.getPrice().getOrDefault(FlatType.TWO_ROOM, Integer.MIN_VALUE) >= priceLowerBound &&
                            p.getPrice().getOrDefault(FlatType.THREE_ROOM, Integer.MIN_VALUE) >= priceLowerBound))
                // Price Upper Bound Filter: true if no upper bound OR BOTH flat prices meet/are below bound
                .filter(p -> priceUpperBound == null ||
                           (p.getPrice().getOrDefault(FlatType.TWO_ROOM, Integer.MAX_VALUE) <= priceUpperBound &&
                            p.getPrice().getOrDefault(FlatType.THREE_ROOM, Integer.MAX_VALUE) <= priceUpperBound))
                // Start Date Filter: true if no start date OR project opens on/after start date
                .filter(p -> startDate == null || !p.getOpenDate().isBefore(startDate)) // Use !isBefore for inclusive start date
                // End Date Filter: true if no end date OR project closes on/before end date
                .filter(p -> endDate == null || !p.getCloseDate().isAfter(endDate)) // Use !isAfter for inclusive end date
                // Sorting
                .sorted((p1, p2) -> {
                    if (sortType == null) return 0; // Handle null sortType gracefully
                    switch (sortType) {
                        case PRICE:
                            // Compare prices, using specified flatType if set, otherwise default to a reasonable comparison (e.g., Two Room)
                            FlatType priceSortKey = (flatType != null) ? flatType : FlatType.TWO_ROOM; // Default to TWO_ROOM if no filter set
                            int price1 = p1.getPrice().getOrDefault(priceSortKey, Integer.MAX_VALUE); // Use default if missing
                            int price2 = p2.getPrice().getOrDefault(priceSortKey, Integer.MAX_VALUE); // Use default if missing
                            return Integer.compare(price1, price2);
                        case DATE:
                            // Compare by open date
                            return p1.getOpenDate().compareTo(p2.getOpenDate());
                        case NAME:
                            // Compare by project name (case-sensitive)
                            return p1.getName().compareTo(p2.getName());
                        default:
                            return 0; // Should not happen with enum, but safe fallback
                    }
                })
                .collect(Collectors.toList()); // Collect the filtered and sorted results into a new list
    }

    /**
     * Provides a console interface for the user to set up or modify the filter criteria.
     * Prompts the user for desired locations, price range, flat type, date range, and sort type.
     * Updates the static filter variables in this controller based on user input.
     * Allows skipping filters by pressing ENTER.
     */
    public static void setup() {
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("                       Set Up Project Filters");
        System.out.println(UIController.LINE_SEPARATOR);
        System.out.println("Leave input blank and press ENTER to skip/clear a filter.");

        // Location Filter Setup
        System.out.print("Number of preferred locations (0 to clear location filter): ");
        List<String> inputLocations = new ArrayList<>();
        String tmp; // Temporary string for input
        int numLocations = IOController.nextInt();
        while (numLocations < 0) { // Validate non-negative input
            System.out.print("Please enter a valid number (0 or more): ");
            numLocations = IOController.nextInt();
        }
        if (numLocations == 0) {
            location = null; // Clear location filter
            System.out.println("Location filter cleared.");
        } else {
            System.out.println("Enter preferred location names:");
            for (int i = 0; i < numLocations; i++) { // Corrected loop
                System.out.print("\tLocation " + (i + 1) + ": ");
                tmp = IOController.nextLine();
                if (!tmp.isEmpty()) { // Add only non-empty locations
                     inputLocations.add(tmp);
                } else {
                     System.out.println("\t(Skipped empty location)");
                     i--; // Decrement counter if input was empty to retry
                }
            }
            location = inputLocations;
             System.out.println("Location filter set to: " + String.join(", ", location));
        }


        // Price Lower Bound Setup
        System.out.print("Enter lowest acceptable price (e.g., 100000) [ENTER to clear]: ");
        tmp = IOController.nextLine();
        if (!tmp.isEmpty()) {
            try {
                priceLowerBound = Integer.parseInt(tmp);
                while (priceLowerBound < 0) { // Validate non-negative
                    System.out.print("Price cannot be negative. Please enter a valid lowest price: ");
                    priceLowerBound = IOController.nextInt(); // Use nextInt after error
                }
                 System.out.println("Lowest price filter set to: " + priceLowerBound);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Lowest price filter cleared.");
                priceLowerBound = null;
            }
        } else {
            priceLowerBound = null;
            System.out.println("Lowest price filter cleared.");
        }

        // Price Upper Bound Setup
        System.out.print("Enter highest acceptable price (e.g., 500000) [ENTER to clear]: ");
        tmp = IOController.nextLine();
        if (!tmp.isEmpty()) {
             try {
                priceUpperBound = Integer.parseInt(tmp);
                while (priceUpperBound < 0) { // Validate non-negative
                    System.out.print("Price cannot be negative. Please enter a valid highest price: ");
                    priceUpperBound = IOController.nextInt();
                }
                // Optional: Check if upper bound is >= lower bound
                if (priceLowerBound != null && priceUpperBound < priceLowerBound) {
                     System.out.println("Warning: Highest price is lower than lowest price. Adjusting highest price filter.");
                     // Decide handling: clear upper, set upper=lower, or just warn? Currently just warns.
                }
                 System.out.println("Highest price filter set to: " + priceUpperBound);
             } catch (NumberFormatException e) {
                 System.out.println("Invalid number format. Highest price filter cleared.");
                 priceUpperBound = null;
             }
        } else {
            priceUpperBound = null;
             System.out.println("Highest price filter cleared.");
        }

        // Flat Type Filter Setup
        System.out.println("Filter by Flat Type:");
        System.out.println("\t1. Two Room");
        System.out.println("\t2. Three Room");
        System.out.println("\t3. No Flat Type Filter (Show All)");
        System.out.print("Your choice (1-3): ");
        int flatTypeChoice = IOController.nextInt();
        while (flatTypeChoice < 1 || flatTypeChoice > 3) { // Validate choice
            System.out.print("Please enter a valid number (1-3): ");
            flatTypeChoice = IOController.nextInt();
        }
        switch (flatTypeChoice) {
            case 1 -> { flatType = FlatType.TWO_ROOM; System.out.println("Filtering by Two Room flats."); }
            case 2 -> { flatType = FlatType.THREE_ROOM; System.out.println("Filtering by Three Room flats."); }
            case 3 -> { flatType = null; System.out.println("Flat type filter cleared."); }
        }

        // Start Date Filter Setup
        System.out.print("Filter by minimum project Open Date? (Y/N): ");
        tmp = IOController.nextLine().toUpperCase(); // Read input and convert to uppercase
        while (!tmp.equals("Y") && !tmp.equals("N")) {
            System.out.print("Please enter a valid option (Y/N): ");
            tmp = IOController.nextLine().toUpperCase();
        }
        if (tmp.equals("Y")) {
            System.out.println("Enter minimum Open Date (YYYY-MM-DD):");
            startDate = IOController.nextDate(); // Assumes nextDate handles parsing
            System.out.println("Minimum open date filter set to: " + startDate);
        } else {
            startDate = null;
             System.out.println("Minimum open date filter cleared.");
        }

        // End Date Filter Setup
        System.out.print("Filter by maximum project Close Date? (Y/N): ");
        tmp = IOController.nextLine().toUpperCase(); // Read input and convert to uppercase
        while (!tmp.equals("Y") && !tmp.equals("N")) {
            System.out.print("Please enter a valid option (Y/N): ");
            tmp = IOController.nextLine().toUpperCase();
        }
        if (tmp.equals("Y")) {
            System.out.println("Enter maximum Close Date (YYYY-MM-DD):");
            endDate = IOController.nextDate(); // Assumes nextDate handles parsing
             // Optional: Check if end date is >= start date
             if (startDate != null && endDate.isBefore(startDate)) {
                 System.out.println("Warning: Maximum close date is before minimum open date. Adjusting dates.");
                 // Decide handling: clear end, set end=start, or just warn? Currently just warns.
             }
            System.out.println("Maximum close date filter set to: " + endDate);
        } else {
            endDate = null;
             System.out.println("Maximum close date filter cleared.");
        }

        // Sort Type Setup
        System.out.println("Sort results by:");
        System.out.println("\t1. Project Name");
        System.out.println("\t2. Price"); // Consider clarifying which price (e.g., Two Room)
        System.out.println("\t3. Open Date");
        System.out.print("Your choice (1-3): ");
        int sortChoice = IOController.nextInt();
        while (sortChoice < 1 || sortChoice > 3) { // Validate choice
            System.out.print("Please enter a valid number (1-3): ");
            sortChoice = IOController.nextInt();
        }
        switch (sortChoice) {
            case 1 -> { sortType = SortType.NAME; System.out.println("Sorting by Name."); }
            case 2 -> { sortType = SortType.PRICE; System.out.println("Sorting by Price."); }
            case 3 -> { sortType = SortType.DATE; System.out.println("Sorting by Open Date."); }
        }

        System.out.println("Filter setup complete.");
        displayFilter(); // Show the configured filters
    }
}
