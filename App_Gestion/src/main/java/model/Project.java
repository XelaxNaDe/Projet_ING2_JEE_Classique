package model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Projet")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_projet")
    private int idProjet;

    @Column(name = "nom_projet", nullable = false)
    private String nomProjet;

    @Column(name = "date_debut")
    @Temporal(TemporalType.DATE)
    private Date dateDebut;

    @Column(name = "date_fin")
    @Temporal(TemporalType.DATE)
    private Date dateFin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_chef_projet")
    private Employee chefProjet;

    @Column(name = "etat")
    private String etat;

    public Project() {}

    public Project(String nomProjet, Date dateDebut, Date dateFin, Employee chefProjet, String etat) {
        this.nomProjet = nomProjet;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.chefProjet = chefProjet;
        this.etat = etat;
    }

    public int getIdProjet() { return idProjet; }
    public void setIdProjet(int idProjet) { this.idProjet = idProjet; }

    public String getNomProjet() { return nomProjet; }
    public void setNomProjet(String nomProjet) { this.nomProjet = nomProjet; }

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }

    public Employee getChefProjet() { return chefProjet; }
    public void setChefProjet(Employee chefProjet) { this.chefProjet = chefProjet; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}