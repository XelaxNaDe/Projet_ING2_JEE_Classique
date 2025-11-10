package model;

public class Employee {
    private int matricule; // Identifiant
    private String prenom;
    private String nom;
    private String sexe;
    private String email;
    private String mdp; // Mot de passe
    private String poste; //
    private String grade;
    private int role;

    // Construction d'un employé avec son rôle, poste et grade
    public Employee(String p, String n, String s, String pt, String gd, int r){
        prenom = p;
        nom = n;
        sexe = s;
        poste = pt;
        grade = gd;
        role = r;
    }

    // Modification/Ajout d'informations
    public void setMatricule(int m){ matricule = m; }
    public void setPrenom(String p){ prenom = p; }
    public void setNom(String n){ nom = n; }
    public void setSexe(String s){sexe = s; }
    public void setEmail(String e){ email = e; }
    public void setGrade(String g){ grade = g; }
    public void setPoste(String pt){ poste = pt; }
    public void setRole(int r){ role = r; }

    // Récupération des informations
    public int getMatricule(){ return matricule; }
    public String getPrenom(){ return prenom ; }
    public String getNom(){ return nom; }
    public String getSexe(){ return sexe; }
    public String getEmail(){ return email; }
    public String getGrade(){ return grade; }
    public String getPoste(){ return poste; }
    public int getRole(){ return role; }
}

