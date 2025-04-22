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
        if (token != null) {
            prefs.put(PREF_SESSION_TOKEN, token);
            prefs.putBoolean(PREF_REMEMBER_ME, rememberMe);
        } else {
            prefs.remove(PREF_SESSION_TOKEN);
            prefs.remove(PREF_REMEMBER_ME);
        }
    }

    // Get the session token
    public String getSessionToken() {
        return prefs.get(PREF_SESSION_TOKEN, null);
    }

    // Check if "remember me" is enabled
    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(PREF_REMEMBER_ME, false);
    }

    // Set the current username
    public void setCurrentUsername(String username) {
        if (username != null) {
            prefs.put(PREF_USERNAME, username);
        } else {
            prefs.remove(PREF_USERNAME);
        }
    }

    // Get the current username
    public String getCurrentUsername() {
        return prefs.get(PREF_USERNAME, null);
    }

    // Set a temporary message (for passing messages between screens)
    public void setTemporaryMessage(String message) {
        if (message != null) {
            prefs.put(TEMP_MESSAGE, message);
        } else {
            prefs.remove(TEMP_MESSAGE);
        }
    }

    // Get the temporary message
    public String getTemporaryMessage() {
        return prefs.get(TEMP_MESSAGE, null);
    }

    // Clear the temporary message
    public void clearTemporaryMessage() {
        prefs.remove(TEMP_MESSAGE);
    }

    // Clear the entire session
    public void clearSession() {
        prefs.remove(PREF_SESSION_TOKEN);
        prefs.remove(PREF_REMEMBER_ME);
        prefs.remove(PREF_USERNAME);
    }
}
