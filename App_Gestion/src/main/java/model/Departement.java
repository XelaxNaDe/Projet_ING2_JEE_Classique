package model;

import jakarta.persistence.*;

@Entity
@Table(name = "Departement")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement")
    private int id;

    @Column(name = "nom_departement", nullable = false)
    private String nomDepartement;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_chef_departement")
    private Employee chefDepartement;

    public Departement() {}

    public Departement(String nomDepartement, Employee chefDepartement) {
        this.nomDepartement = nomDepartement;
        this.chefDepartement = chefDepartement;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomDepartement() { return nomDepartement; }
    public void setNomDepartement(String nomDepartement) { this.nomDepartement = nomDepartement; }

    public Employee getChefDepartement() { return chefDepartement; }
    public void setChefDepartement(Employee chefDepartement) { this.chefDepartement = chefDepartement; }
}