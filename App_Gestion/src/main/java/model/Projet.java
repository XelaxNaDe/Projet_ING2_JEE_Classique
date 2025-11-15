package model;

import java.util.Date;

// Ce POJO correspond à la table 'Projet'
public class Projet {

    private int idProjet;
    private String nomProjet;
    private Date dateDebut;
    private Date dateFin;
    private int idChefProjet; // Stocke l'ID
    private String etat;

    // Constructeur complet
    public Projet(int idProjet, String nomProjet, Date dateDebut, Date dateFin, int idChefProjet, String etat) {
        this.idProjet = idProjet;
        this.nomProjet = nomProjet;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.idChefProjet = idChefProjet;
        this.etat = etat;
    }

    // Constructeur pour la création (l'ID est auto-incrémenté)
    public Projet(String nomProjet, Date dateDebut, Date dateFin, int idChefProjet, String etat) {
        this.nomProjet = nomProjet;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.idChefProjet = idChefProjet;
        this.etat = etat;
    }

    // Getters et Setters (essentiels)
    public int getIdProjet() { return idProjet; }
    public void setIdProjet(int idProjet) { this.idProjet = idProjet; }
    public String getNomProjet() { return nomProjet; }
    public void setNomProjet(String nomProjet) { this.nomProjet = nomProjet; }
    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }
    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }
    public int getIdChefProjet() { return idChefProjet; }
    public void setIdChefProjet(int idChefProjet) { this.idChefProjet = idChefProjet; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}