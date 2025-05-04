package entities;

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
    private String role;
    private Float montantAPayer;


    public User(String nom, String prenom, String username, String numTel, String email, String gender, Date datedenaissance, String profilePicture, String password, String roles, Float montantAPayer) {
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.numTel = numTel;
        this.email = email;
        this.gender = gender;
        this.datedenaissance = datedenaissance;
        this.profilePicture = profilePicture;
        this.password = password;
        this.role = roles;
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
        this.role = roles;
        this.montantAPayer = montantAPayer;
    }

    public int getId() {
        return id;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDatedenaissance() {
        return datedenaissance;
    }

    public void setDatedenaissance(Date datedenaissance) {
        this.datedenaissance = datedenaissance;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String roles) {
        this.role = roles;
    }

    public Float getMontantAPayer() {
        return montantAPayer;
    }

    public void setMontantAPayer(Float montantAPayer) {
        this.montantAPayer = montantAPayer;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String getName() {
        return nom;
    }

    public void setName(String name) {
        this.nom = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
                ", roles=" + role +
                ", montantAPayer=" + montantAPayer +
                '}';
    }


}
