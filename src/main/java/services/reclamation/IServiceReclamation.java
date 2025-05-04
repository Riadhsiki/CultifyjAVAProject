package services.reclamation;

import java.sql.SQLException;
import java.util.List;

public interface IServiceReclamation<T> {
    void add(T t) throws SQLException;
    void update(T t) throws SQLException;
    void delete(T t) throws SQLException;
    List<T> getAll() throws SQLException;
    T getById(int id) throws SQLException;

}