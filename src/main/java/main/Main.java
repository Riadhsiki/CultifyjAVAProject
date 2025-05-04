package main;
 import entities.Association;
 import entities.Don;
 import entities.User;
 import services.AssociationServices;
 import services.DonServices;
 import services.UserServices;
 import utils.MyDataBase;

 import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        MyDataBase db = MyDataBase.getInstance();
        UserServices us =new UserServices();
        DonServices ds=new DonServices();
        AssociationServices as = new AssociationServices();
        Association association1 = new Association("test1",170,"test java","test1@java.com","test 1lel java");
        Association association2 = new Association(24,"update",175.5,"update descption","t@gmail.com","update but","web");
        try {
            System.out.println("gabel add associtaion : " );
            as.getAll().forEach(assoc -> System.out.println(assoc.toString()));
            as.add(association1);
            System.out.println("\nAprès ajout:");
            as.getAll().forEach(assoc -> System.out.println(assoc.toString()));
            association1.setNom("test update2");
            /*as.update(association2);
            System.out.println("\nAprès update:");
            as.getAll().forEach(assoc -> System.out.println(assoc.toString()));
            System.out.println("get by id :");
            System.out.println(as.getById(24).toString());
            as.delete(association2);
            Association association3 = new Association(19,"update",175.5,"update descption","t@gmail.com","update but","web");
            Association association4 = new Association(20,"update",175.5,"update descption","t@gmail.com","update but","web");
            Association association5 = new Association(21,"update",175.5,"update descption","t@gmail.com","update but","web");
            as.delete(association3);
            as.delete(association4);
            as.delete(association5);
            System.out.println("\n Après delete 19/20/21:");
            as.getAll().forEach(assoc -> System.out.println(assoc.toString()));

            Association association6 = new Association(22,"update",175.5,"update descption","t@gmail.com","update but","web");
            Association association7 = new Association(23,"update",175.5,"update descption","t@gmail.com","update but","web");
            as.delete(association6);
            as.delete(association7);
            */System.out.println("\n Après delete 22/23:");
            Association ass= as.getById(25);
            System.out.println("\nassocaiton : \n");
            as.getAll().forEach(assoc -> System.out.println(assoc.toString()));
            Don d1= new Don(45.0 ,"user","en_attente","don",ass);
            ds.Add(2,d1);
            System.out.println("\n don ajoutina montant 17 user enattente don test1:");
            ds.getAll().forEach(don -> {
                System.out.println("Don: " + don.getMontant() +
                        " | Association: " + don.getAssociation().getNom() +
                        " | But: " + don.getAssociation().getBut());
            });
            System.out.println("\n don 42 \n");
            Don d2 = ds.getDonDetails(42);
            System.out.println(d2.toString());
            System.out.println("\n Don details \n");
            System.out.println("\n don update 1 \n");
            ds.update(d2,1);

            ds.getAll().forEach(don -> {
                System.out.println("Don: " + don.getMontant() +
                        " | Association: " + don.getAssociation().getNom() +
                        " | But: " + don.getAssociation().getBut());
            });
            System.out.println(ds.getDonDetails(42).toString());
            d2.setMontant(80);
            System.out.println("\n don  update2\n");
            ds.update(d2,1);
            ds.getAll().forEach(don -> {
                System.out.println("Don: " + don.getMontant() +
                        " | Association: " + don.getAssociation().getNom() +
                        " | But: " + don.getAssociation().getBut());
            });
            System.out.println("\n don  33\n");
            System.out.println(ds.getDonDetails(42).toString());
            ds.getAll().forEach(don -> {
                System.out.println("Don: " + don.getMontant() +
                        " | Association: " + don.getAssociation().getNom() +
                        " | But: " + don.getAssociation().getBut());
            });
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
