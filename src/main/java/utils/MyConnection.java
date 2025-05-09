package Utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private Connection connection;
    private final String url = "jdbc:mysql://localhost:3306/cultifybd";
    private final String user = "root";
    private final String password = "";
    private static MyConnection myConnection;

    private MyConnection() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection established");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static  MyConnection getMyConnection() {
        if(myConnection == null)
            myConnection = new MyConnection();
        return myConnection;
    }

    public Connection getConnection() {
        return connection;
    }

}
