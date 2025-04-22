package utils;

/**
 * Represents the different criteria or fields by which a list of items
 * (e.g., {@link entity.project.Project} objects) can be sorted.
 * Used by controllers or display utilities to determine the sort order.
 */
public enum SortType {
    /**
     * Indicates that sorting should be performed based on the name attribute of the items.
     */
    NAME,

    /**
     * Indicates that sorting should be performed based on a price attribute of the items.
     * The specific price (e.g., for a particular flat type) might be determined by the context where sorting is applied.
     */
    PRICE,

    /**
     * Indicates that sorting should be performed based on a date attribute of the items,
     * typically the project's open date or another relevant date.
     */
    DATE,
}
