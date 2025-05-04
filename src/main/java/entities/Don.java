package entities;
import jakarta.persistence.*;



public class Don {


    private int id;
    private double montant;
    private String donorType;
    private String status;
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id")
    private Association association;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_id")
    private User user;

    public Don() {}

    public Don(double montant, String donorType, String status, String type, Association association, User user) {
        this.montant = montant;
        this.donorType = donorType;
        this.status = status;
        this.type = type;
        this.association = association;
        this.user = user;
    }

    public Don(int id, double montant, String donorType, String status, String type) {
        this.id = id;
        this.montant = montant;
        this.donorType = donorType;
        this.status = status;
        this.type = type;
    }
    public Don(double montant, String donorType, String status, String type, Association association) {
        this.montant = montant;
        this.donorType = donorType;
        this.status = status;
        this.type = type;
        this.association = association;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDonorType() {
        return donorType;
    }

    public void setDonorType(String donorType) {
        this.donorType = donorType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Association getAssociation() {
        return association;
    }

    public void setAssociation(Association association) {
        this.association = association;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Don{" +
                "id=" + id +
                ", montant=" + montant +
                ", donorType='" + donorType + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", association=" + association +
                '}';
    }
}
