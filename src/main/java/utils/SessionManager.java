package utils;

import java.time.Instant;
import java.util.prefs.Preferences;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();

    private static final String PREF_SESSION_TOKEN = "sessionToken";
    private static final String PREF_REMEMBER_ME = "rememberMe";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_TIMEOUT = "sessionTimeout";
    private static final String PREF_USER_ROLE = "userRole";
    private static final String TEMP_MESSAGE = "tempMessage";

    private final Preferences prefs;

    private SessionManager() {
        prefs = Preferences.userNodeForPackage(SessionManager.class);
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public void setSessionToken(String token, boolean rememberMe) {
        if (token != null && !token.isEmpty()) {
            prefs.put(PREF_SESSION_TOKEN, token);
            prefs.putBoolean(PREF_REMEMBER_ME, rememberMe);
        } else {
            prefs.remove(PREF_SESSION_TOKEN);
            prefs.remove(PREF_REMEMBER_ME);
        }
        flushPrefs();
    }

    public String getSessionToken() {
        return prefs.get(PREF_SESSION_TOKEN, null);
    }

    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(PREF_REMEMBER_ME, false);
    }

    public void setCurrentUsername(String username) {
        if (username != null && !username.isEmpty()) {
            prefs.put(PREF_USERNAME, username);
        } else {
            prefs.remove(PREF_USERNAME);
        }
        flushPrefs();
    }

    public String getCurrentUsername() {
        return prefs.get(PREF_USERNAME, null);
    }

    public void setUserRole(String role) {
        if (role != null && !role.isEmpty()) {
            prefs.put(PREF_USER_ROLE, role);
        } else {
            prefs.remove(PREF_USER_ROLE);
        }
        flushPrefs();
    }

    public String getUserRole() {
        return prefs.get(PREF_USER_ROLE, null);
    }

    public void setTemporaryMessage(String message) {
        if (message != null) {
            prefs.put(TEMP_MESSAGE, message);
        } else {
            prefs.remove(TEMP_MESSAGE);
        }
        flushPrefs();
    }

    public String getTemporaryMessage() {
        String message = prefs.get(TEMP_MESSAGE, null);
        clearTemporaryMessage(); // Auto-clear after reading
        return message;
    }

    public void clearTemporaryMessage() {
        prefs.remove(TEMP_MESSAGE);
        flushPrefs();
    }

    public void setSessionTimeout(int minutes) {
        if (minutes > 0) {
            long expirationTime = Instant.now().plusSeconds(minutes * 60L).getEpochSecond();
            prefs.putLong(PREF_TIMEOUT, expirationTime);
        } else {
            prefs.remove(PREF_TIMEOUT);
        }
        flushPrefs();
    }

    public boolean isLoggedIn() {
        String token = getSessionToken();
        String username = getCurrentUsername();
        long expirationTime = prefs.getLong(PREF_TIMEOUT, 0);
        boolean hasValidToken = token != null && !token.isEmpty();
        boolean hasUsername = username != null && !username.isEmpty();
        boolean sessionNotExpired = expirationTime == 0 ||
                Instant.now().getEpochSecond() < expirationTime;

        return hasValidToken && hasUsername && sessionNotExpired;
    }

    public void clearSession() {
        prefs.remove(PREF_SESSION_TOKEN);
        prefs.remove(PREF_REMEMBER_ME);
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_TIMEOUT);
        prefs.remove(PREF_USER_ROLE);
        flushPrefs();
    }

    public void refreshSession(int minutesToExtend) {
        if (isLoggedIn() && minutesToExtend > 0) {
            setSessionTimeout(minutesToExtend);
        }
    }

    public void dumpPreferences() {
        System.out.println("--- Session Manager Preferences ---");
        System.out.println("Session Token: " + prefs.get(PREF_SESSION_TOKEN, "null"));
        System.out.println("Remember Me: " + prefs.getBoolean(PREF_REMEMBER_ME, false));
        System.out.println("Username: " + prefs.get(PREF_USERNAME, "null"));
        System.out.println("User Role: " + prefs.get(PREF_USER_ROLE, "null"));
        System.out.println("Timeout: " + prefs.getLong(PREF_TIMEOUT, 0));
        System.out.println("Temporary Message: " + prefs.get(TEMP_MESSAGE, "null"));
        System.out.println("----------------------------------");
    }

    private void flushPrefs() {
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error flushing preferences: " + e.getMessage());
        }
    }
}