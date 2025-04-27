package services.comment;

import models.Comment;

import java.sql.SQLException;
import java.util.List;

public interface Service <T>{
    void add(Comment comment) throws SQLException;

    boolean update(Comment comment) throws SQLException;

    void delete(Comment comment) throws SQLException;

    List<Comment> getAll() throws SQLException;
}
