package Entities;

import java.util.Date;

public class Reponse {
    private int id_reponse;
    private Date reponsedate;
    private String titre;
    private String contenu;
    private String offre;
    private Reclamation reclamation; // Référence à la réclamation

    public Reponse() {}

    public Reponse(int id_reponse, Date reponsedate, String titre,
                   String contenu, String offre, Reclamation reclamation) {
        this.id_reponse = id_reponse;
        this.reponsedate = reponsedate;
        this.titre = titre;
        this.contenu = contenu;
        this.offre = offre;
        this.reclamation = reclamation;
    }

    public Reponse(Date reponsedate, String titre, String contenu, String offre) {
        this.reponsedate = reponsedate;
        this.titre = titre;
        this.contenu = contenu;
        this.offre = offre;
    }

    // Getters et Setters
    public int getId_reponse() {
        return id_reponse;
    }

    public void setId_reponse(int id_reponse) {
        this.id_reponse = id_reponse;
    }

    public Date getReponsedate() {
        return reponsedate;
    }

    public void setReponsedate(Date reponsedate) {
        this.reponsedate = reponsedate;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getOffre() {
        return offre;
    }

    public void setOffre(String offre) {
        this.offre = offre;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "id_reponse=" + id_reponse +
                ", reponsedate=" + reponsedate +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", offre='" + offre + '\'' +
                ", reclamation=" + (reclamation != null ? reclamation.getId_reclamation() : "null") +
                '}';
    }
}