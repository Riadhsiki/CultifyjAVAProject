package models;

import java.time.LocalDate;

public class Reservation {
    private int idR;
    private String etat;
    private LocalDate dateR;
    private String theme;
    private String url;
    private int nbTickets;
    private Event event;

    public Reservation() {
        // Initialiser la date à aujourd'hui
        this.dateR = LocalDate.now();
    }

    public Reservation(String etat, LocalDate dateR, String theme, String url, int nbTickets, int idE) {
        this.etat = etat;
        this.dateR = dateR;
        this.theme = theme;
        this.url = url;
        this.nbTickets = nbTickets;
        this.event = new Event();
        this.event.setIdE(idE);
    }


    public int getIdR() {
        return idR;
    }

    public void setIdR(int idR) {
        this.idR = idR;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public LocalDate getDateR() {
        return dateR;
    }

    public void setDateR(LocalDate dateR) {
        this.dateR = dateR;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNbTickets() {
        return nbTickets;
    }

    public void setNbTickets(int nbTickets) {
        this.nbTickets = nbTickets;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idR=" + idR +
                ", etat='" + etat + '\'' +
                ", dateR=" + dateR +
                ", theme='" + theme + '\'' +
                ", url='" + url + '\'' +
                ", nbTickets=" + nbTickets +
                ", event=" + (event != null ? event.getIdE() : "null") +
                '}';
    }
}