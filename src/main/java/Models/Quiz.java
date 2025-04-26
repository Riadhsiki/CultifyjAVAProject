package Models;

import java.util.Date;

public class Quiz {
    private int id_quiz;
    private int contenu_id;
    private String titre_quiz;
    private Date date_quiz;
    private int score_quiz;
    private String reponse_choisit;

    public Quiz() {
    }

    public Quiz(int contenu_id, String titre_quiz, Date date_quiz, int score_quiz, String reponse_choisit) {
        this.contenu_id = contenu_id;
        this.titre_quiz = titre_quiz;
        this.date_quiz = date_quiz;
        this.score_quiz = score_quiz;
        this.reponse_choisit = reponse_choisit;
    }

    public Quiz(int id_quiz, int contenu_id, String titre_quiz, Date date_quiz, int score_quiz, String reponse_choisit) {
        this.id_quiz = id_quiz;
        this.contenu_id = contenu_id;
        this.titre_quiz = titre_quiz;
        this.date_quiz = date_quiz;
        this.score_quiz = score_quiz;
        this.reponse_choisit = reponse_choisit;
    }

    // Getters and Setters
    public int getId_quiz() {
        return id_quiz;
    }

    public void setId_quiz(int id_quiz) {
        this.id_quiz = id_quiz;
    }

    public int getContenu_id() {
        return contenu_id;
    }

    public void setContenu_id(int contenu_id) {
        this.contenu_id = contenu_id;
    }

    public String getTitre_quiz() {
        return titre_quiz;
    }

    public void setTitre_quiz(String titre_quiz) {
        this.titre_quiz = titre_quiz;
    }

    public Date getDate_quiz() {
        return date_quiz;
    }

    public void setDate_quiz(Date date_quiz) {
        this.date_quiz = date_quiz;
    }

    public int getScore_quiz() {
        return score_quiz;
    }

    public void setScore_quiz(int score_quiz) {
        this.score_quiz = score_quiz;
    }

    public String getReponse_choisit() {
        return reponse_choisit;
    }

    public void setReponse_choisit(String reponse_choisit) {
        this.reponse_choisit = reponse_choisit;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id_quiz=" + id_quiz +
                ", contenu_id=" + contenu_id +
                ", titre_quiz='" + titre_quiz + '\'' +
                ", date_quiz=" + date_quiz +
                ", score_quiz=" + score_quiz +
                ", reponse_choisit='" + reponse_choisit + '\'' +
                '}';
    }
}