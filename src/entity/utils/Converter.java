package utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.request.BTOApplication;
import entity.request.BTOWithdrawal;
import entity.request.Enquiry;
import entity.request.OfficerRegistration;
import entity.request.Request;
import entity.request.RequestType;

/**
 * Utility class providing static methods for converting objects to and from string representations,
 * primarily designed for CSV serialization and deserialization.
 * Uses reflection to handle object fields, including common types like String, Integer, Boolean,
 * Enums, and complex types like {@code List<String>}, {@code Map<K, V>}, and {@link LocalDate}.
 * Defines specific separators for serializing collection types. Includes special handling
 * for determining subclasses of {@link Request}.
 *
 * Note: The generic type {@code <T>} on the class declaration itself appears unused as all methods are static.
 *
 * @param <T> Unused generic type parameter on class declaration.
 */
public class Converter<T> {

    /** Separator used within a CSV field to delimit elements of a List. */
    public static final String LIST_SEPARATOR = "::LIST::";
    /** Separator used within a CSV field to delimit parts of a LocalDate representation. */
    public static final String DATE_SEPARATOR = "::DATE::";
    /** Separator used within a Map entry string to separate the key from the value. */
    public static final String MAP_SEPARATOR = "::MAP::";

    /**
     * Deserializes a single line of comma-separated string data into an object of the specified class.
     * Uses reflection to instantiate the object and set its fields based on the string values.
     * Handles fields inherited from up to two levels of superclasses.
     * Supports conversion for String, Integer, Double, Boolean, Enum, LocalDate, List&lt;String&gt;, and Map&lt;?,?&gt; types.
     * Assumes custom formats for List, Map, and LocalDate using defined separators.
     * Treats the literal string "null" as a null value for fields.
     *
     * @param <T>   The type of the object to create.
     * @param line  The comma-separated string representing the object's data.
     * @param clazz The {@code Class} object of the type {@code T}.
     * @return An object of type {@code T} populated with data from the string, or {@code null} if an error occurs.
     */
    public static <T> T stringtoObj(String line, Class<T> clazz) {
        if (line == null || line.isEmpty() || clazz == null) {
             System.err.println("Error in stringtoObj: Input line or class is null/empty.");
             return null;
        }
        try {
            // Create a new instance of the class
            T obj = clazz.getDeclaredConstructor().newInstance();

            // Gather fields from the class and its superclasses (up to 2 levels)
            Field[] f = clazz.getDeclaredFields();
            List<Field> fields = new ArrayList<>();
            Class<?> current = clazz.getSuperclass();
             // Gather fields from first superclass
            if (current != null && current != Object.class) {
                Field[] ff = current.getDeclaredFields();
                 // Gather fields from second superclass
                 Class<?> grandparent = current.getSuperclass();
                if (grandparent != null && grandparent != Object.class) {
                    Field[] fff = grandparent.getDeclaredFields();
                    fields.addAll(Arrays.asList(fff));
                }
                fields.addAll(Arrays.asList(ff));
            }
            fields.addAll(Arrays.asList(f)); // Add fields from the target class last

            // Split the input line into values
            String[] values = line.split(",", -1); // Use -1 limit to keep trailing empty strings
            List<String> lineData = new ArrayList<>(Arrays.asList(values));

            // Check if the number of values matches the number of fields
            if (lineData.size() != fields.size()) {
                System.err.println("Error in stringtoObj for " + clazz.getSimpleName() + ": Mismatched number of fields (" + fields.size() + ") and CSV values (" + lineData.size() + "). Line: " + line);
                 // Optionally skip this line or handle partial data? Currently returns null.
                return null;
            }


            // Iterate through fields and set values
            int idx = 0; // values iterator
            for (Field field : fields) {
                field.setAccessible(true); // Allow access to private fields
                Class<?> fieldType = field.getType();
                 String rawValue = lineData.get(idx).trim(); // Get the raw string value for this field

                try {
                    // Handle special types (List, Map, LocalDate)
                    if (List.class.isAssignableFrom(fieldType)) {
                        List<String> ls = stringToList(rawValue);
                        field.set(obj, ls);
                    } else if (Map.class.isAssignableFrom(fieldType)) {
                        // Determine generic types for Map
                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType pt) {
                            // Ensure types are Classes before casting
                            if (pt.getActualTypeArguments()[0] instanceof Class && pt.getActualTypeArguments()[1] instanceof Class) {
                                Class<?> keyClass = (Class<?>) pt.getActualTypeArguments()[0];
                                Class<?> valueClass = (Class<?>) pt.getActualTypeArguments()[1];
                                Map<?, ?> mp = stringToMap(rawValue, keyClass, valueClass);
                                field.set(obj, mp);
                            } else {
                                 System.err.println("Error in stringtoObj: Map field '" + field.getName() + "' in " + clazz.getSimpleName() + " has non-class generic types.");
                                // Cannot proceed with this field
                            }
                        } else {
                             System.err.println("Error in stringtoObj: Map field '" + field.getName() + "' in " + clazz.getSimpleName() + " is not parameterized.");
                             // Cannot determine key/value types
                        }
                    } else if (fieldType.equals(LocalDate.class)) {
                         if ("null".equalsIgnoreCase(rawValue) || rawValue.isEmpty()) {
                              field.set(obj, null);
                         } else {
                              LocalDate date = stringToDate(rawValue);
                              field.set(obj, date);
                         }
                    }
                    // Handle primitive/wrapper/enum/String types
                    else {
                         if ("null".equalsIgnoreCase(rawValue)) {
                            // Set object types to null, leave primitives as default (requires careful handling if primitive cannot be null)
                             if (!fieldType.isPrimitive()) {
                                 field.set(obj, null);
                             } else {
                                  System.err.println("Warning in stringtoObj: Cannot set primitive field '" + field.getName() + "' in " + clazz.getSimpleName() + " to null. Leaving default value.");
                             }
                        } else {
                            Object value = convert(rawValue, fieldType); // Use helper for conversion
                            field.set(obj, value);
                        }
                    }
                 } catch (Exception fieldError) { // Catch errors during parsing/setting individual fields
                      System.err.println("Error setting field '" + field.getName() + "' in " + clazz.getSimpleName() + " from value '" + rawValue + "': " + fieldError.getMessage());
                      // Optionally continue to next field or return null/throw?
                 }
                idx++;
            }
            return obj;
        } catch (Exception e) { // Catch errors during instantiation or general processing
            System.err.println("Error creating object of type " + clazz.getSimpleName() + " from line: " + line);
            e.printStackTrace(); // Print stack trace for debugging
            return null;
        }
    }

    /**
     * Serializes an object into a single comma-separated string representation.
     * Uses reflection to access object fields (including inherited ones up to two levels).
     * Handles List&lt;String&gt;, Map&lt;?,?&gt;, LocalDate, primitives, enums, and Strings using helper methods
     * and defined separators. Null field values are represented by the literal string "null".
     *
     * @param <T> The type of the object to serialize.
     * @param obj The object to convert to a string.
     * @return A comma-separated string representing the object's data, or {@code null} if an error occurs.
     */
    public static <T> String objToString(T obj) {
        if (obj == null) return null; // Handle null input object

        StringBuilder ret = new StringBuilder(); // More efficient than string concatenation
        try {
            Class<?> clazz = obj.getClass();
            // Gather fields from class and superclasses (up to 2 levels)
            Field[] f = clazz.getDeclaredFields();
            List<Field> fields = new ArrayList<>();
            Class<?> current = clazz.getSuperclass();
             // Gather fields from first superclass
            if (current != null && current != Object.class) {
                Field[] ff = current.getDeclaredFields();
                 // Gather fields from second superclass
                 Class<?> grandparent = current.getSuperclass();
                if (grandparent != null && grandparent != Object.class) {
                    Field[] fff = grandparent.getDeclaredFields();
                    fields.addAll(Arrays.asList(fff));
                }
                fields.addAll(Arrays.asList(ff));
            }
            fields.addAll(Arrays.asList(f)); // Add fields from the target class last


            // Iterate through fields and append values
            boolean firstField = true;
            for (Field field : fields) {
                field.setAccessible(true); // Allow access to private fields
                Object val = field.get(obj); // Get field value
                Class<?> fieldType = field.getType();
                 String valueString; // String representation of the value

                // Handle special types
                if (List.class.isAssignableFrom(fieldType)) {
                    // Suppress warning for unchecked cast (assuming field is List<String>)
                    @SuppressWarnings("unchecked")
                    List<String> lsval = (List<String>) val;
                    valueString = listToString(lsval);
                } else if (Map.class.isAssignableFrom(fieldType)) {
                     // Suppress warning for unchecked cast (assuming field is Map<?,?>)
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> mpval = (Map<Object, Object>) val;
                    valueString = mapToString(mpval);
                } else if (fieldType.equals(LocalDate.class)) {
                    LocalDate dateval = (LocalDate) val;
                    valueString = dateToString(dateval);
                }
                // Handle primitive/wrapper/enum/String types
                else {
                    if (val == null) {
                        valueString = "null"; // Represent null fields as "null"
                    } else {
                        valueString = val.toString(); // Use standard toString for others
                    }
                }

                // Append value to result string with comma separator
                 if (!firstField) {
                      ret.append(",");
                 }
                 ret.append(valueString != null ? valueString : "null"); // Append "null" if helper returned null
                 firstField = false;
            }
        } catch (Exception e) { // Catch reflection errors
            System.err.println("Error converting object to string: " + obj);
            e.printStackTrace();
            return null;
        }
        return ret.toString();
    }

    /**
     * Converts a string representation (using {@code LIST_SEPARATOR}) back into a List of Strings.
     * If the input data is the literal string "null" or empty, returns an empty list.
     *
     * @param data The string data, potentially containing elements separated by {@code LIST_SEPARATOR}.
     * @return A {@link List} of strings. Returns an empty list if input is "null" or empty.
     */
    public static List<String> stringToList(String data) {
        // Handle null or empty input string representations gracefully
        if (data == null || data.isEmpty() || "null".equalsIgnoreCase(data)) {
             return new ArrayList<>();
        }
        // Split the string by the defined separator
        String[] values = data.split(LIST_SEPARATOR, -1); // Keep trailing empty strings if needed
        // Convert array to list
        return new ArrayList<>(Arrays.asList(values));
    }

    /**
     * Converts a List of Strings into a single string representation using {@code LIST_SEPARATOR}.
     * If the input list is null or empty, returns the literal string "null".
     *
     * @param data The {@link List} of strings to convert.
     * @return A single string with elements joined by {@code LIST_SEPARATOR}, or "null" if the list is empty/null.
     */
    public static String listToString(List<String> data) {
        // Handle null or empty list
        if (data == null || data.isEmpty()) {
            return "null";
        }
        // Join list elements with the separator
        return String.join(LIST_SEPARATOR, data);
         /* Manual joining implementation:
         StringBuilder ret = new StringBuilder();
         for (int i = 0; i < data.size(); i++) {
             ret.append(data.get(i));
             if (i < data.size() - 1) {
                 ret.append(LIST_SEPARATOR);
             }
         }
         return ret.toString();
         */
    }

    /**
     * Parses a string representation (using {@code DATE_SEPARATOR}) into a {@link LocalDate} object.
     * Expects the format "M::DATE::dd::DATE::yyyy".
     *
     * @param data The string representation of the date (e.g., "4::DATE::22::DATE::2025").
     * @return The parsed {@link LocalDate} object, or null if parsing fails or input is null/empty.
     */
    public static LocalDate stringToDate(String data) {
        if (data == null || data.isEmpty() || "null".equalsIgnoreCase(data)) {
             return null;
        }
        try {
             // Define the specific formatter expected
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M'" + DATE_SEPARATOR + "'dd'" + DATE_SEPARATOR + "'yyyy");
            return LocalDate.parse(data, formatter);
        } catch (java.time.format.DateTimeParseException e) {
             System.err.println("Error parsing date string '" + data + "': " + e.getMessage());
             return null; // Return null on parsing error
        }
    }

    /**
     * Formats a {@link LocalDate} object into a specific string representation using {@code DATE_SEPARATOR}.
     * The output format is "M::DATE::dd::DATE::yyyy".
     *
     * @param date The {@link LocalDate} object to format.
     * @return The formatted date string, or "null" if the input date is null.
     */
    public static String dateToString(LocalDate date) {
        if (date == null) {
            return "null"; // Represent null date as "null"
        }
        // Define the specific formatter for output
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M'" + DATE_SEPARATOR + "'dd'" + DATE_SEPARATOR + "'yyyy");
        return date.format(formatter);
    }

    /**
     * Converts a string representation (using {@code LIST_SEPARATOR} for entries and {@code MAP_SEPARATOR} within entries)
     * back into a {@code Map<A, B>}.
     * Uses the {@link #convert(String, Class)} helper method to convert keys and values to their target types.
     * If the input data is the literal string "null" or empty, returns an empty map.
     *
     * @param <A>       The type of the keys in the map.
     * @param <B>       The type of the values in the map.
     * @param data      The serialized string representation of the map.
     * @param keyType   The {@code Class} object for the key type {@code A}.
     * @param valueType The {@code Class} object for the value type {@code B}.
     * @return A {@link Map} populated from the string data. Returns an empty map if input is "null" or empty.
     */
    public static <A, B> Map<A, B> stringToMap(String data, Class<A> keyType, Class<B> valueType) {
        Map<A, B> ret = new HashMap<>();
        // Handle null or empty input representation
        if (data == null || data.isEmpty() || "null".equalsIgnoreCase(data)) {
             return ret; // Return empty map
        }

        // Split the string into key-value pair strings
        String[] entries = data.split(LIST_SEPARATOR, -1); // Keep trailing empty if needed

        for (String entry : entries) {
            if (entry == null || entry.isEmpty()) continue; // Skip empty entries

            // Split each entry into key and value strings
            String[] pair = entry.split(MAP_SEPARATOR, 2); // Split into exactly two parts
            if (pair.length == 2) {
                try {
                     // Convert key and value strings to their respective types
                    A key = convert(pair[0], keyType);
                    B val = convert(pair[1], valueType);
                    // Add to map if conversion was successful (convert doesn't throw checked exceptions)
                     if (key != null) { // Basic check, convert might return null for "null" strings depending on type
                         ret.put(key, val);
                     } else {
                          System.err.println("Warning in stringToMap: Null key encountered for entry '" + entry + "'");
                     }
                 } catch (Exception e) { // Catch potential errors during conversion
                     System.err.println("Error parsing map entry '" + entry + "': " + e.getMessage());
                     // Skip this entry and continue
                 }
            } else {
                 System.err.println("Warning in stringToMap: Malformed map entry skipped: '" + entry + "'");
            }
        }
        return ret;
    }

    /**
     * Converts a {@code Map<A, B>} into a single string representation.
     * Entries are separated by {@code LIST_SEPARATOR}. Keys and values within an entry
     * are separated by {@code MAP_SEPARATOR}. Keys and values are converted using their
     * {@code toString()} method.
     * If the input map is null or empty, returns the literal string "null".
     *
     * @param <A> The type of the keys in the map.
     * @param <B> The type of the values in the map.
     * @param mp  The {@link Map} to convert.
     * @return A single string representation of the map, or "null" if the map is empty/null.
     */
    public static <A, B> String mapToString(Map<A, B> mp) {
        // Handle null or empty map
        if (mp == null || mp.isEmpty()) {
            return "null";
        }

        StringBuilder ret = new StringBuilder();
        int cnt = 0;
        for (Map.Entry<A, B> entry : mp.entrySet()) {
             // Convert key and value to string (handle nulls)
             String keyString = (entry.getKey() == null) ? "null" : entry.getKey().toString();
             String valueString = (entry.getValue() == null) ? "null" : entry.getValue().toString();

            // Append entry: key<MAP_SEP>value
             ret.append(keyString).append(MAP_SEPARATOR).append(valueString);

            // Append list separator if not the last entry
            if (cnt < mp.size() - 1) {
                ret.append(LIST_SEPARATOR);
            }
            cnt++;
        }
        return ret.toString();
    }

    /**
     * Converts a string value to a specified target type using basic conversion logic.
     * Handles Integer, Double, Boolean (case-insensitive "true"), and Enum types.
     * If the type is not explicitly handled, it returns the original string value.
     *
     * @param <T>   The target type.
     * @param value The string value to convert.
     * @param type  The {@code Class} object of the target type {@code T}.
     * @return The converted value as type {@code T}, or the original string if no specific conversion applies.
     * Returns null if the input value is null. May throw runtime exceptions on parsing errors (e.g., NumberFormatException).
     * @throws NumberFormatException If conversion to Integer or Double fails.
     * @throws IllegalArgumentException If conversion to Enum fails (value not found).
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) // Suppress warnings for Enum casting
    private static <T> T convert(String value, Class<T> type) {
        if (value == null) return null; // Handle null input value

        // Handle common types
        if (type == Integer.class || type == Integer.TYPE) {
             // Trim whitespace before parsing integer
             return (T) Integer.valueOf(value.trim());
        }
        if (type == Double.class || type == Double.TYPE) { // Added Double primitive type
            // Trim whitespace before parsing double
             return (T) Double.valueOf(value.trim());
        }
        if (type == Boolean.class || type == Boolean.TYPE) {
            // Case-insensitive boolean conversion
            return (T) Boolean.valueOf("true".equalsIgnoreCase(value.trim()));
        }
        // Handle Enums: Use Enum.valueOf, requires Class<Enum>
        if (type.isEnum()) {
            try {
                 // Trim whitespace before Enum parsing
                 return (T) Enum.valueOf((Class<Enum>) type.asSubclass(Enum.class), value.trim().toUpperCase()); // Convert to uppercase for robustness
            } catch (IllegalArgumentException e) {
                 System.err.println("Error converting value '" + value + "' to Enum type " + type.getSimpleName() + ": " + e.getMessage());
                 // Return null or throw? Returning null might hide errors. Rethrowing might be better.
                 throw e; // Rethrowing standard exception
                 // return null;
            }
        }
        // Default: Assume it's a String or requires no conversion
        return (T) value; // Fallback to returning the original string cast to T (works if T is String)
    }

    /**
     * Generates a comma-separated string of field names for a given object's class,
     * including fields inherited from superclasses (up to two levels).
     * This is typically used to create a header row for a CSV file.
     *
     * @param <T> The type of the object.
     * @param obj An instance of the object whose class's fields are to be retrieved.
     * (Can be null if only class structure is needed, but instance is safer for non-static fields).
     * @return A comma-separated string of field names, or {@code null} if an error occurs.
     */
    public static <T> String getField(T obj) {
        if (obj == null) return null; // Need an object instance to get class

        StringBuilder ret = new StringBuilder();
        try {
            Class<?> clazz = obj.getClass();
            // Gather fields from class and superclasses (up to 2 levels)
            Field[] f = clazz.getDeclaredFields();
            List<Field> fields = new ArrayList<>();
            Class<?> current = clazz.getSuperclass();
             // Gather fields from first superclass
            if (current != null && current != Object.class) {
                Field[] ff = current.getDeclaredFields();
                 // Gather fields from second superclass
                Class<?> grandparent = current.getSuperclass();
                if (grandparent != null && grandparent != Object.class) {
                    Field[] fff = grandparent.getDeclaredFields();
                    fields.addAll(Arrays.asList(fff));
                }
                fields.addAll(Arrays.asList(ff));
            }
            fields.addAll(Arrays.asList(f)); // Add fields from the target class last

            // Append field names
             boolean firstField = true;
            for (Field field : fields) {
                // No need for setAccessible just to get the name
                 if (!firstField) {
                     ret.append(",");
                 }
                ret.append(field.getName());
                 firstField = false;
            }
        } catch (Exception e) { // Catch potential SecurityExceptions etc.
            System.err.println("Error getting fields for class " + obj.getClass().getSimpleName());
            e.printStackTrace();
            return null;
        }
        return ret.toString();
    }

    /**
     * Determines the specific subclass of {@link Request} based on a serialized string line.
     * It assumes the second comma-separated value in the line corresponds to a {@link RequestType} enum name.
     *
     * @param s The comma-separated string line representing a serialized Request object.
     * @return The specific {@code Class} object extending {@code Request} (e.g., {@code Enquiry.class}),
     * or the base {@code Request.class} if the type is NONE.
     * @throws IllegalArgumentException If the request type string derived from the input is unknown or invalid.
     */
    public static Class<? extends Request> getRequestClass(String s) {
        if (s == null || s.isEmpty()) {
             throw new IllegalArgumentException("Input string cannot be null or empty for getRequestClass.");
        }
        String[] parts = s.split(",", -1); // Keep trailing empty strings
        // Assumes RequestType is the second field (index 1) in the CSV representation
        if (parts.length > 1) {
            String typeString = parts[1].trim(); // Get the type string
             try {
                 RequestType type = RequestType.valueOf(typeString.toUpperCase()); // Convert string to enum
                 // Return the corresponding class based on the enum value
                 return switch (type) {
                    case ENQUIRY -> Enquiry.class;
                    case BTO_APPLICATION -> BTOApplication.class;
                    case BTO_WITHDRAWAL -> BTOWithdrawal.class;
                    case REGISTRATION -> OfficerRegistration.class;
                    case NONE -> Request.class; // Default or base class case
                    // Default case for the switch should ideally not be reached if enum covers all types
                    // default -> throw new IllegalArgumentException("Unhandled RequestType in switch: " + type);
                };
             } catch (IllegalArgumentException e) {
                 // Throw a more informative exception if the string doesn't match any RequestType
                 throw new IllegalArgumentException("Unknown or invalid RequestType string found in data: '" + typeString + "' from line: " + s, e);
             }
        } else {
             // Throw exception if the line doesn't have enough parts to determine the type
             throw new IllegalArgumentException("Cannot determine RequestType from data line (not enough fields): " + s);
        }
    }
}
