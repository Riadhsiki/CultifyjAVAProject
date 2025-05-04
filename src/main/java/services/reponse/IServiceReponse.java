package services.reponse;

import java.sql.SQLException;
import java.util.List;

public interface IServiceReponse <T>{
        void add(T t) throws SQLException;
        void update(T t) throws SQLException;
        void delete(T t) throws SQLException;
        List<T> getAll() throws SQLException;
        T getById(int id) throws SQLException;


}
