package utils;

import java.time.Instant;
import java.util.prefs.Preferences;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();

    private static final String PREF_SESSION_TOKEN = "sessionToken";
    private static final String PREF_REMEMBER_ME = "rememberMe";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_TIMEOUT = "sessionTimeout";
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
            System.out.println("Session token set: " + token);
        } else {
            prefs.remove(PREF_SESSION_TOKEN);
            prefs.remove(PREF_REMEMBER_ME);
            System.out.println("Session token cleared");
        }
        flushPrefs();
    }

    public String getSessionToken() {
        String token = prefs.get(PREF_SESSION_TOKEN, null);
        System.out.println("Retrieved session token: " + (token != null ? token : "null"));
        return token;
    }

    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(PREF_REMEMBER_ME, false);
    }

    public void setCurrentUsername(String username) {
        if (username != null && !username.isEmpty()) {
            prefs.put(PREF_USERNAME, username);
            System.out.println("Username set: " + username);
        } else {
            prefs.remove(PREF_USERNAME);
            System.out.println("Username cleared");
        }
        flushPrefs();
    }

    public String getCurrentUsername() {
        String username = prefs.get(PREF_USERNAME, null);
        System.out.println("Retrieved username: " + (username != null ? username : "null"));
        return username;
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
        return prefs.get(TEMP_MESSAGE, null);
    }

    public void clearTemporaryMessage() {
        prefs.remove(TEMP_MESSAGE);
        flushPrefs();
    }

    public void setSessionTimeout(int minutes) {
        if (minutes > 0) {
            long expirationTime = Instant.now().plusSeconds(minutes * 60L).toEpochMilli();
            prefs.putLong(PREF_TIMEOUT, expirationTime);
            System.out.println("Session timeout set to: " + minutes + " minutes (expires at: " + expirationTime + ")");
        } else {
            prefs.remove(PREF_TIMEOUT);
            System.out.println("Session timeout cleared");
        }
        flushPrefs();
    }

    public boolean isLoggedIn() {
        String token = getSessionToken();
        String username = getCurrentUsername();
        long expirationTime = prefs.getLong(PREF_TIMEOUT, 0);
        boolean hasSession = token != null && !token.isEmpty();
        boolean hasUsername = username != null && !username.isEmpty();
        boolean isValidSession = hasSession && hasUsername && (expirationTime == 0 || Instant.now().toEpochMilli() < expirationTime);

        System.out.println("Session check - Has token: " + hasSession + ", Has username: " + hasUsername +
                ", Valid session: " + isValidSession + ", Expiration: " + expirationTime);

        return isValidSession;
    }

    public void clearSession() {
        System.out.println("Clearing session at: " + new java.util.Date());
        prefs.remove(PREF_SESSION_TOKEN);
        prefs.remove(PREF_REMEMBER_ME);
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_TIMEOUT);
        flushPrefs();
    }

    public void dumpPreferences() {
        System.out.println("--- Session Manager Preferences ---");
        System.out.println("Session Token: " + prefs.get(PREF_SESSION_TOKEN, "null"));
        System.out.println("Remember Me: " + prefs.getBoolean(PREF_REMEMBER_ME, false));
        System.out.println("Username: " + prefs.get(PREF_USERNAME, "null"));
        System.out.println("Timeout: " + prefs.getLong(PREF_TIMEOUT, 0));
        System.out.println("Temporary Message: " + prefs.get(TEMP_MESSAGE, "null"));
        System.out.println("----------------------------------");
    }

    private void flushPrefs() {
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error flushing preferences: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
