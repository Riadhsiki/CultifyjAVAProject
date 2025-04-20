package Entities;

public class Reclamation {
    private int id_reclamation;
    private String type;
    private String titre;
    private String description;
    private String statut;
    private String priorite;
    private String email;
    private Reponse reponse; // Référence à la réponse

    public Reclamation(int id_reclamation, String type, String titre, String description, String statut, String priorite, String email) {
        this.id_reclamation = id_reclamation;
        this.type = type;
        this.titre = titre;
        this.description = description;
        this.statut = statut;
        this.priorite = priorite;
        this.email = email;
    }

    public Reclamation(int id_reclamation, String type, String titre, String description,
                       String statut, String priorite, String email, Reponse reponse) {
        this.id_reclamation = id_reclamation;
        this.type = type;
        this.titre = titre;
        this.description = description;
        this.statut = statut;
        this.priorite = priorite;
        this.email = email;
        this.reponse = reponse;
    }

    public Reclamation(String type, String titre, String description,
                       String statut, String priorite, String email) {
        this.type = type;
        this.titre = titre;
        this.description = description;
        this.statut = statut;
        this.priorite = priorite;
        this.email = email;
    }

    public Reclamation() {}

    // Getters et Setters
    public int getId_reclamation() {
        return id_reclamation;
    }

    public void setId_reclamation(int id_reclamation) {
        this.id_reclamation = id_reclamation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Reponse getReponse() {
        return reponse;
    }

    public void setReponse(Reponse reponse) {
        this.reponse = reponse;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id_reclamation=" + id_reclamation +
                ", type='" + type + '\'' +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", statut='" + statut + '\'' +
                ", priorite='" + priorite + '\'' +
                ", email='" + email + '\'' +
                ", reponse=" + (reponse != null ? reponse.toString() : "null") +
                '}';
    }
}