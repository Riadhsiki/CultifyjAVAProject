
package models;

import java.io.File;
import java.sql.Date;
import java.util.List;

public class Event {
    private int idE;
    private String titre;
    private String description;
    private Date dateE;
    private String organisation;
    private int capacite;
    private int nbplaces;
    private String categorie;
    private float prix;
    private String image;
    private List<Reservation> reservations;


    public Event() {
    }

    public Event(String titre, String description, Date dateE, String organisation,
                 int capacite, int nbplaces, String categorie, String image, float prix) {
        this.titre = titre;
        this.description = description;
        this.dateE = dateE;
        this.organisation = organisation;
        this.capacite = capacite;
        this.nbplaces = nbplaces;
        this.categorie = categorie;
        this.image = image;
        this.prix = prix;
    }

    // Getters et Setters (inchangés)
    public int getIdE() { return idE; }
    public void setIdE(int idE) { this.idE = idE; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getDateE() { return dateE; }
    public void setDateE(Date dateE) { this.dateE = dateE; }
    public String getOrganisation() { return organisation; }
    public void setOrganisation(String organisation) { this.organisation = organisation; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public int getNbplaces() { return nbplaces; }
    public void setNbplaces(int nbplaces) { this.nbplaces = nbplaces; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public float getPrix() { return prix; }
    public void setPrix(float prix) { this.prix = prix; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }

    @Override
    public String toString() {
        return "Event{" +
                "idE=" + idE +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dateE=" + dateE +
                ", organisation='" + organisation + '\'' +
                ", capacite=" + capacite +
                ", nbplaces=" + nbplaces +
                ", categorie='" + categorie + '\'' +
                ", prix=" + prix +
                ", image='" + image + '\'' +
                '}';

    }

    public String getImagePath() {
        // Si l'image est stockée comme chemin absolu
        if (this.image != null && !this.image.isEmpty()) {
            // Vérifier si c'est déjà un chemin absolu
            File file = new File(this.image);
            if (file.exists()) {
                return this.image;
            }

            // Sinon, construire le chemin depuis le dossier de ressources
            String resourcePath = "/images/events/" + this.image;
            try {
                // Vérifier si la ressource existe
                if (getClass().getResource(resourcePath) != null) {
                    return getClass().getResource(resourcePath).toExternalForm();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }

        // Retourner une image par défaut si aucune image n'est spécifiée
        return getClass().getResource("/images/events/default.png").toExternalForm();
    }


    private String imageUrl;
    private float price;

}