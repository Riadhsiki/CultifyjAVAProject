package Models;

import java.util.Date;

public class ContenuMultiMedia {
    private int id_contenu;
    private String titre_media;
    private String text_media;
    private String photo_media;
    private String categorie_media;
    private Date date_media;

    public ContenuMultiMedia() {
    }

    public ContenuMultiMedia(String titre_media, String text_media, String photo_media, String categorie_media, Date date_media) {
        this.titre_media = titre_media;
        this.text_media = text_media;
        this.photo_media = photo_media;
        this.categorie_media = categorie_media;
        this.date_media = date_media;
    }

    public ContenuMultiMedia(int id_contenu, String titre_media, String text_media, String photo_media, String categorie_media, Date date_media) {
        this.id_contenu = id_contenu;
        this.titre_media = titre_media;
        this.text_media = text_media;
        this.photo_media = photo_media;
        this.categorie_media = categorie_media;
        this.date_media = date_media;
    }

    // Getters and Setters
    public int getId_contenu() {
        return id_contenu;
    }

    public void setId_contenu(int id_contenu) {
        this.id_contenu = id_contenu;
    }

    public String getTitre_media() {
        return titre_media;
    }

    public void setTitre_media(String titre_media) {
        this.titre_media = titre_media;
    }

    public String getText_media() {
        return text_media;
    }

    public void setText_media(String text_media) {
        this.text_media = text_media;
    }

    public String getPhoto_media() {
        return photo_media;
    }

    public void setPhoto_media(String photo_media) {
        this.photo_media = photo_media;
    }

    public String getCategorie_media() {
        return categorie_media;
    }

    public void setCategorie_media(String categorie_media) {
        this.categorie_media = categorie_media;
    }

    public Date getDate_media() {
        return date_media;
    }

    public void setDate_media(Date date_media) {
        this.date_media = date_media;
    }

    @Override
    public String toString() {
        return "ContenuMultiMedia{" +
                "id_contenu=" + id_contenu +
                ", titre_media='" + titre_media + '\'' +
                ", text_media='" + text_media + '\'' +
                ", photo_media='" + photo_media + '\'' +
                ", categorie_media='" + categorie_media + '\'' +
                ", date_media=" + date_media +
                '}';
    }
}