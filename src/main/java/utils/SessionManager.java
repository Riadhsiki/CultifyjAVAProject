package utils;

import java.util.prefs.Preferences;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();

    private static final String PREF_SESSION_TOKEN = "sessionToken";
    private static final String PREF_REMEMBER_ME = "rememberMe";
    private static final String PREF_USERNAME = "username";
    private static final String TEMP_MESSAGE = "tempMessage";

    private final Preferences prefs;

    // Private constructor to enforce singleton pattern
    private SessionManager() {
        prefs = Preferences.userNodeForPackage(SessionManager.class);
    }

    // Get the singleton instance
    public static SessionManager getInstance() {
        return instance;
    }

    // Set the session token
    public void setSessionToken(String token, boolean rememberMe) {
        if (token != null && !token.isEmpty()) {
            prefs.put(PREF_SESSION_TOKEN, token);
            prefs.putBoolean(PREF_REMEMBER_ME, rememberMe);
            // Debug output
            System.out.println("Session token set: " + token);
        } else {
            prefs.remove(PREF_SESSION_TOKEN);
            prefs.remove(PREF_REMEMBER_ME);
            System.out.println("Session token cleared");
        }

        // Flush to ensure data is saved immediately
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get the session token
    public String getSessionToken() {
        String token = prefs.get(PREF_SESSION_TOKEN, null);
        // Debug output
        System.out.println("Retrieved session token: " + (token != null ? token : "null"));
        return token;
    }

    // Check if "remember me" is enabled
    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(PREF_REMEMBER_ME, false);
    }

    // Set the current username
    public void setCurrentUsername(String username) {
        if (username != null && !username.isEmpty()) {
            prefs.put(PREF_USERNAME, username);
            System.out.println("Username set: " + username);
        } else {
            prefs.remove(PREF_USERNAME);
            System.out.println("Username cleared");
        }

        // Flush to ensure data is saved immediately
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get the current username
    public String getCurrentUsername() {
        String username = prefs.get(PREF_USERNAME, null);
        System.out.println("Retrieved username: " + (username != null ? username : "null"));
        return username;
    }

    // Set a temporary message (for passing messages between screens)
    public void setTemporaryMessage(String message) {
        if (message != null) {
            prefs.put(TEMP_MESSAGE, message);
        } else {
            prefs.remove(TEMP_MESSAGE);
        }

        // Flush to ensure data is saved immediately
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get the temporary message
    public String getTemporaryMessage() {
        return prefs.get(TEMP_MESSAGE, null);
    }

    // Clear the temporary message
    public void clearTemporaryMessage() {
        prefs.remove(TEMP_MESSAGE);

        // Flush to ensure data is saved immediately
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if a user is logged in
    public boolean isLoggedIn() {
        String token = getSessionToken();
        String username = getCurrentUsername();
        boolean hasSession = token != null && !token.isEmpty();
        boolean hasUsername = username != null && !username.isEmpty();

        System.out.println("Session check - Has token: " + hasSession + ", Has username: " + hasUsername);

        return hasSession && hasUsername;
    }

    // Clear the entire session
    public void clearSession() {
        System.out.println("Clearing session");
        prefs.remove(PREF_SESSION_TOKEN);
        prefs.remove(PREF_REMEMBER_ME);
        prefs.remove(PREF_USERNAME);

        // Flush to ensure data is saved immediately
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // For debugging: output all stored preferences
    public void dumpPreferences() {
        System.out.println("--- Session Manager Preferences ---");
        System.out.println("Session Token: " + prefs.get(PREF_SESSION_TOKEN, "null"));
        System.out.println("Remember Me: " + prefs.getBoolean(PREF_REMEMBER_ME, false));
        System.out.println("Username: " + prefs.get(PREF_USERNAME, "null"));
        System.out.println("Temporary Message: " + prefs.get(TEMP_MESSAGE, "null"));
        System.out.println("----------------------------------");
    }

    public void setSessionTimeout(int sessionTimeoutMinutes) {
    }
}