package model;

import jakarta.persistence.*;

@Entity
@Table(name = "Departement")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement") // Correspond à ta PK SQL
    private int id;

    @Column(name = "nom_departement", nullable = false)
    private String nomDepartement;

    // RELATION : Au lieu d'un int, on met l'Objet Employee
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_chef_departement") // La colonne FK dans la table Departement
    private Employee chefDepartement;

    // Constructeur vide OBLIGATOIRE
    public Departement() {}

    public Departement(String nomDepartement, Employee chefDepartement) {
        this.nomDepartement = nomDepartement;
        this.chefDepartement = chefDepartement;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomDepartement() { return nomDepartement; }
    public void setNomDepartement(String nomDepartement) { this.nomDepartement = nomDepartement; }

    public Employee getChefDepartement() { return chefDepartement; }
    public void setChefDepartement(Employee chefDepartement) { this.chefDepartement = chefDepartement; }
}