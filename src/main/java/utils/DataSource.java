package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private Connection connection;
    private final String url = "jdbc:mysql://localhost:3306/userdb";
    private final String user = "root";
    private final String pws = "";

    private static DataSource instance;
    private DataSource() {
        try {
            connection= DriverManager.getConnection(url,user,pws);
            System.out.println("connecter a la base de données");
        } catch (SQLException e) {
            System.err.println(e.getMessage());    }
    }

    public static DataSource getInstance(){
        if (instance==null){
            instance= new DataSource();

        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
