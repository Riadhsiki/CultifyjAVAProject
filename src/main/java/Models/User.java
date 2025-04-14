package Models;

import java.sql.Date;

public class User {

    private Integer id;
    private String nom;
    private String prenom;
    private String username;
    private String numTel;
    private String email;
    private String gender;
    private Date datedenaissance;
    private String profilePicture;
    private String password;
    private String roles ;

    public User(String nom, String prenom, String username, String numTel, String email, String gender, Date datedenaissance, String profilePicture, String password, String roles) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.numTel = numTel;
        this.email = email;
        this.gender = gender;
        this.datedenaissance = datedenaissance;
        this.profilePicture = profilePicture;
        this.password = password;
        this.roles = roles;
    }

    public User() {}

    public User(String nom, String prenom, String username, int num, String email, String gender, String roles, String profilePic, String password, Date sqlDate) {
    }


    public Integer getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getUsername() {
        return username;
    }

    public String getNumTel() {
        return numTel;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public Date getDatedenaissance() {
        return datedenaissance;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getPassword() {
        return password;
    }

    public String getRoles() {
        return roles;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDatedenaissance(Date datedenaissance) {
        this.datedenaissance = datedenaissance;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", username='" + username + '\'' +
                ", numTel='" + numTel + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", datedenaissance=" + datedenaissance +
                ", profilePicture='" + profilePicture + '\'' +
                ", password='[PROTECTED]'" +
                ", roles=" + roles +
                '}';
    }

}
