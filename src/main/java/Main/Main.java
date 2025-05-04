package Main;
<<<<<<< HEAD

import java.sql.Connection;

import utils.DataSource;

public class Main {

    public static void main(String[] args) {
    Connection connection=DataSource.getInstance().getConnection();
    Connection connection1=DataSource.getInstance().getConnection();

        System.out.println(connection);
        System.out.println(connection1);
}
=======
import Entities.Reclamation;
import Entities.Reponse;
import Services.ReclamationService;
import Services.ReponseService;
import Utils.MyDataBase;

import java.sql.SQLException;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        MyDataBase db = MyDataBase.getInstance();

        Reclamation Reclamtion = new Reclamation("LOL","aaaa","zzzzz","eeeeeee","bbbbb","yyyyy");
        Reclamation Reclamtion1 = new Reclamation("LOL25","aasaa","zzzz5742z","eaeeeeee","bbbbfb","fyyyyy");
        ReclamationService ReclamationService = new ReclamationService();
        ReponseService reponseService = new ReponseService();

    }
>>>>>>> origin/GestionReclamation
}