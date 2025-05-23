package services.user;

import java.sql.SQLException;
import java.util.List;

public interface Service<T> {
    public void add(T t) throws SQLException;

    void changePassword(String username, String newPassword) throws SQLException;

    public boolean update(T t) throws SQLException;

    public void delete(T t) throws SQLException;

    public List<T> getAll() throws SQLException;

}
