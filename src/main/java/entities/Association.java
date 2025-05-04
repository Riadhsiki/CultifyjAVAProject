package entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class Association {
    private int id;
    private String nom;
    private double montantDesire;
    private String image;
    private String description;
    private String contact;
    private String but;
    private String siteWeb;

    @OneToMany(mappedBy = "association", cascade = CascadeType.ALL)
    private List<Don> dons = new ArrayList<>();

    public Association() {}

    public Association(int id, String nom, double montantDesire, String description, String contact, String but, String siteWeb) {
        this.id = id;
        this.nom = nom;
        this.montantDesire = montantDesire;
        this.description = description;
        this.contact = contact;
        this.but = but;
        this.siteWeb = siteWeb;
    }

    public Association(String nom, double montantDesire, String description, String contact, String but) {
        this.nom = nom;
        this.montantDesire = montantDesire;
        this.description = description;
        this.contact = contact;
        this.but = but;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getMontantDesire() {
        return montantDesire;
    }

    public void setMontantDesire(double montantDesire) {
        this.montantDesire = montantDesire;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getBut() {
        return but;
    }

    public void setBut(String but) {
        this.but = but;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public List<Don> getDons() {
        return dons;
    }

    public void setDons(List<Don> dons) {
        this.dons = dons;
    }
    public double getPourcentageProgression() {
        if (montantDesire <= 0) {
            return 0;
        }
        return Math.min(100, (getMontantActuel() / montantDesire) * 100);
    }

    public double getMontantActuel() {
        double montantTotal = 0;
        for (Don don : dons) {
            if ("confirme".equals(don.getStatus())) {
                montantTotal += don.getMontant();
            }
        }
        return montantTotal;
    }

    @Override
    public String toString() {
        return "Association{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", montantDesire=" + montantDesire +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", contact='" + contact + '\'' +
                ", but='" + but + '\'' +
                ", siteWeb='" + siteWeb + '\'' +
                ", dons=" + dons +
                '}';
    }

}
