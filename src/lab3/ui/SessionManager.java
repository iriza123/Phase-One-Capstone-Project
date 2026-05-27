package lab3.ui;

import lab1.model.Customer;

// Holds the currently logged-in user for the session
public class SessionManager {
    private static Customer currentUser;

    public static void login(Customer c)  { currentUser = c; }
    public static void logout()           { currentUser = null; }
    public static Customer getUser()      { return currentUser; }
    public static boolean isLoggedIn()    { return currentUser != null; }
    public static boolean isAdmin()       { return currentUser != null && currentUser.isAdmin(); }
}
