package model;

// POJO simple pour la table Departement
public class Departement {
    private int id;
    private String nomDepartement;
    private int idChefDepartement;

    // Constructeurs, Getters, Setters...
    public Departement() {}

    public Departement(int id, String nomDepartement, int idChefDepartement) {
        this.id = id;
        this.nomDepartement = nomDepartement;
        this.idChefDepartement = idChefDepartement;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomDepartement() { return nomDepartement; }
    public void setNomDepartement(String nomDepartement) { this.nomDepartement = nomDepartement; }
    public int getIdChefDepartement() { return idChefDepartement; }
    public void setIdChefDepartement(int idChefDepartement) { this.idChefDepartement = idChefDepartement; }
}