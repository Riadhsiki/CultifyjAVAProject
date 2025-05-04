package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private final String URl = "jdbc:mysql://localhost:3306/cultifydb";
    private final String UserName = "root";
    private final String Password = "";
    private Connection conn ;
    private static MyDataBase instance;

    public MyDataBase() {
        try {
            conn = DriverManager.getConnection(URl,UserName,Password);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance (){
        if(instance ==null)
            instance = new MyDataBase();
        return instance ;
    }

    public Connection getConn() {
        return conn;
    }
}
