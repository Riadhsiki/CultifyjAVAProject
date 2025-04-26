package controllers.Auth;

import java.util.prefs.Preferences;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();

    private static final String PREF_SESSION_TOKEN = "sessionToken";
    private static final String PREF_REMEMBER_ME = "rememberMe";
    private static final String PREF_USERNAME = "username";
    private static final String TEMP_MESSAGE = "tempMessage";
    private static final String SESSION_TIMEOUT = "sessionTimeout";
    private static final String LAST_ACTIVITY = "lastActivity";

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
            updateLastActivity();
        } else {
            clearSession();
        }
    }

    // Get the session token
    public String getSessionToken() {
        if (!isSessionValid()) {
            clearSession();
            return null;
        }
        updateLastActivity();
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

    // Set session timeout
    public void setSessionTimeout(int minutes) {
        prefs.putInt(SESSION_TIMEOUT, minutes);
        updateLastActivity();
    }

    private void updateLastActivity() {
        prefs.putLong(LAST_ACTIVITY, System.currentTimeMillis());
    }

    private boolean isSessionValid() {
        if (!prefs.getBoolean(PREF_REMEMBER_ME, false)) {
            return true; // Session is valid if remember me is not enabled
        }

        long lastActivity = prefs.getLong(LAST_ACTIVITY, 0);
        int timeoutMinutes = prefs.getInt(SESSION_TIMEOUT, 30);

        if (lastActivity == 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - lastActivity);

        return elapsedMinutes < timeoutMinutes;
    }

    public boolean isLoggedIn() {
        return getSessionToken() != null;
    }

    // Clear the entire session
    public void clearSession() {
        prefs.remove(PREF_SESSION_TOKEN);
        prefs.remove(PREF_REMEMBER_ME);
        prefs.remove(PREF_USERNAME);
        prefs.remove(LAST_ACTIVITY);
        prefs.remove(SESSION_TIMEOUT);
    }

    public void dumpPreferences() {
        System.out.println("Session State:");
        System.out.println("Token: " + (prefs.get(PREF_SESSION_TOKEN, null) != null ? "Present" : "Absent"));
        System.out.println("Remember Me: " + prefs.getBoolean(PREF_REMEMBER_ME, false));
        System.out.println("Username: " + prefs.get(PREF_USERNAME, "Not set"));
        System.out.println("Last Activity: " + new Date(prefs.getLong(LAST_ACTIVITY, 0)));
        System.out.println("Timeout: " + prefs.getInt(SESSION_TIMEOUT, 30) + " minutes");
    }
}
