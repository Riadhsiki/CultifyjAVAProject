package Main;
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
}