package model;

import jakarta.persistence.*;
import model.utils.Role;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Employee") // Doit correspondre exactement au nom de la table SQL
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Nom de la colonne PK dans le SQL
    private int id;

    @Column(name = "fname", nullable = false)
    private String fname;

    @Column(name = "sname", nullable = false)
    private String sname;

    @Column(name = "gender")
    private String gender;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "position")
    private String position;

    @Column(name = "grade")
    private String grade;

    @Column(name = "id_departement")
    private int idDepartement;

    // --- CHAMP AJOUTÉ ---
    // Pas d'annotation @Column, c'est un champ "virtuel"
    // que le DAO remplit pour nous.
    private String nomDepartement;
    // --------------------

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "Employee_Role", // Nom de la table de liaison SQL
            joinColumns = @JoinColumn(name = "id") // CORRIGÉ : Doit correspondre à ta PK
    )
    @Column(name = "id_role") // Nom de la colonne qui stocke le rôle dans la table de liaison
    @Enumerated(EnumType.ORDINAL) // Stocke l'index (0, 1, 2...) pour matcher le type INT du SQL
    private Set<Role> role = new HashSet<>();

    // Constructeur vide OBLIGATOIRE pour Hibernate
    public Employee() {
    }

    // Constructeur avec paramètres (Mis à jour)
    public Employee(String fname, String sname, String gender, String email, String password, String position, String grade, int idDepartement) {
        this.fname = fname;
        this.sname = sname;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.position = position;
        this.grade = grade;
        this.idDepartement = idDepartement;
    }

    // --- Méthodes de Rôle ---
    public void addRole(Role r) {
        this.role.add(r);
    }
    public void removeRole(Role r) {
        this.role.remove(r);
    }
    public boolean hasRole(Role r) {
        return this.role != null && this.role.contains(r);
    }
    public Set<Role> getRoles() {
        return role;
    }
    public void setRoles(Set<Role> roles) {
        this.role = roles;
    }

    // --- Getters et Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getIdDepartement() {
        return idDepartement;
    }

    public void setIdDepartement(int idDepartement) {
        this.idDepartement = idDepartement;
    }

    // --- GETTER/SETTER AJOUTÉS ---
    public String getNomDepartement() {
        return nomDepartement;
    }

    public void setNomDepartement(String nomDepartement) {
        this.nomDepartement = nomDepartement;
    }
}