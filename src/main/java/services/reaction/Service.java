package services.reaction;

import models.Reaction;

import java.sql.SQLException;
import java.util.List;

public interface Service<T> {
    void add(Reaction reaction) throws SQLException;

    boolean update(Reaction reaction) throws SQLException;

    void delete(Reaction reaction) throws SQLException;

    List<Reaction> getAll() throws SQLException;
}
