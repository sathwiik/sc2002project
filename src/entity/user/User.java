package entity.user;

/**
 * Defines the common contract for all user types within the system (e.g., Applicant, Officer, Manager).
 * Specifies methods required to access and modify fundamental user attributes such as
 * user ID, name, hashed password, age, and marital status.
 */
public interface User {

    /**
     * Gets the unique identifier for this user.
     *
     * @return The user ID string.
     */
    public String getUserID();

    /**
     * Sets the unique identifier for this user.
     *
     * @param userID The user ID string to set.
     */
    public void setUserID(String userID);

    /**
     * Gets the name of this user.
     *
     * @return The user's name string.
     */
    public String getName();

    /**
     * Sets the name of this user.
     *
     * @param name The user's name string to set.
     */
    public void setName(String name);

    /**
     * Gets the securely hashed password for this user's account.
     *
     * @return The Base64 encoded hashed password string.
     */
    public String getHashedPassword();

    /**
     * Sets the securely hashed password for this user's account.
     * Note: There is a typo in the method name ("Passoword"). It should ideally be "setPassword" or "setHashedPassword".
     *
     * @param hashedPassword The Base64 encoded hashed password string to set.
     */
    public void setHashedPassoword(String hashedPassword); // Original name kept, typo noted

    /**
     * Gets the age of this user.
     *
     * @return The user's age as an integer.
     */
    public int getAge();

    /**
     * Sets the age of this user.
     *
     * @param age The user's age as an integer.
     */
    public void setAge(int age);

    /**
     * Gets the marital status of this user.
     *
     * @return The user's {@link MaritalStatus}.
     */
    public MaritalStatus getMaritalStatus();

    /**
     * Sets the marital status of this user.
     *
     * @param maritalStatus The user's {@link MaritalStatus}.
     */
    public void setMaritalStatus(MaritalStatus maritalStatus);

}
