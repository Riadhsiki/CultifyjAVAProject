package interfaces;

import entities.Don;

import java.sql.SQLException;
import java.util.List;

public interface IServiceDon <T>{
    void Add(int userId, T t) throws SQLException;
    void update(T t, int newAssociationId) throws SQLException;
    void delete(T t) throws SQLException;
    List<T> getDonsByUser(int userId) throws SQLException;
    List<T> getDonsByAssociation(int associationId) throws SQLException;
    List<T> getAll() throws SQLException;
    T getDonDetails(int donId) throws SQLException;
}
