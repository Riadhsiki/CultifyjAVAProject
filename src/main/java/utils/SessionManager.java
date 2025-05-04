package utils;

import entities.User;

/**
 * Singleton class to manage user session across the application
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for singleton pattern
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : 0;
    }

    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    public void clearSession() {
        this.currentUser = null;
    }
}