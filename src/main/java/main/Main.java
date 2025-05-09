package Main;

import java.sql.Connection;

import utils.DataSource;

public class Main {

    public static void main(String[] args) {
        Connection connection=DataSource.getInstance().getConnection();
        Connection connection1=DataSource.getInstance().getConnection();

        System.out.println(connection);
        System.out.println(connection1);
    }
}