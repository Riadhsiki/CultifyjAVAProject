package models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

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
    private String roles;
    private Float montantAPayer;
    private String firstName;
    private String lastName;
    private String profileImage;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Don> dons = new ArrayList<>();
    public User(String nom, String prenom, String username, String numTel, String email, String gender, Date datedenaissance, String profilePicture, String password, String roles, Float montantAPayer) {
        this.nom = nom;
        this.prenom = prenom;
        this.username = username; // Fixed typo: removed "awaits"
        this.numTel = numTel;
        this.email = email;
        this.gender = gender;
        this.datedenaissance = datedenaissance;
        this.profilePicture = profilePicture;
        this.password = password;
        this.roles = roles;
        this.montantAPayer = montantAPayer;
    }

    public User() {}

    public User(String nom, String prenom, String username, int num, String email, String gender, String roles, String profilePic, String password, Date sqlDate, Float montantAPayer) {
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.numTel = String.valueOf(num);
        this.email = email;
        this.gender = gender;
        this.datedenaissance = sqlDate;
        this.profilePicture = profilePic;
        this.password = password;
        this.roles = roles;
        this.montantAPayer = montantAPayer;
    }

    public void setDons(List<Don> dons) {
        this.dons = dons;
    }
    public void addDon(Don don) {
        dons.add(don);
        don.setUser(this);
    }

    public void removeDon(Don don) {
        dons.remove(don);
        don.setUser(null);
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

    public List<Don> getDons() {
        return dons;
    }


    public Float getMontantAPayer() {
        return montantAPayer;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfileImage() {
        return profileImage;
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

    public void setMontantAPayer(Float montantAPayer) {
        this.montantAPayer = montantAPayer;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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
                ", montantAPayer=" + montantAPayer +
                '}';
    }
}