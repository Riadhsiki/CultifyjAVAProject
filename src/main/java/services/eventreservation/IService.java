package services.eventreservation;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void add(T t) throws SQLException;
    void update(T t) throws SQLException;
    void delete(T t) throws SQLException;
    List<T> getAll() throws SQLException;
    List<T> select() throws SQLException; // Même type de retour que getAll()
}