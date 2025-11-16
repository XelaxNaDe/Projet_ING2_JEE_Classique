package dao;

import model.Projet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import model.Employee;

public class ProjetDAO {

    /**
     * Récupère tous les projets de la BDD.
     */
    /**
     * Récupère tous les projets de la BDD. (Version simplifiée SANS jointure)
     */
    public List<Projet> getAllProjects() throws SQLException {
        List<Projet> projets = new ArrayList<>();

        // REQUÊTE SIMPLIFIÉE : On prend juste tout dans la table Projet
        String sql = "SELECT * FROM Projet";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_projet");
                String nom = rs.getString("nom_projet");
                java.util.Date debut = rs.getDate("date_debut");
                java.util.Date fin = rs.getDate("date_fin");
                int idChef = rs.getInt("id_chef_projet");
                String etat = rs.getString("etat");

                Projet projet = new Projet(id, nom, debut, fin, idChef, etat);
                projets.add(projet);
            }
        }
        // Si ça plante ici (par ex. "Unknown column 'id_chef_projet'"),
        // l'erreur sera attrapée par la servlet.
        return projets;
    }

    /**
     * Crée un nouveau projet dans la BDD.
     */
    public void createProject(Projet projet) throws SQLException {
        String sql = "INSERT INTO Projet (nom_projet, date_debut, date_fin, id_chef_projet, etat) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, projet.getNomProjet());

            // Conversion java.util.Date (modèle) en java.sql.Date (BDD)
            ps.setDate(2, new java.sql.Date(projet.getDateDebut().getTime()));
            ps.setDate(3, new java.sql.Date(projet.getDateFin().getTime()));

            ps.setInt(4, projet.getIdChefProjet());
            ps.setString(5, projet.getEtat());

            ps.executeUpdate();
        }
    }
    public void deleteProject(int idProjet) throws SQLException {
        String sql = "DELETE FROM Projet WHERE id_projet = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProjet);
            ps.executeUpdate();
        }
    }

    public Projet getProjectById(int idProjet) throws SQLException {
        Projet projet = null;
        String sql = "SELECT p.*, e.fname, e.sname FROM Projet p " +
                "LEFT JOIN Employee e ON p.id_chef_projet = e.id " +
                "WHERE p.id_projet = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProjet);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_projet");
                    String nom = rs.getString("nom_projet");
                    java.util.Date debut = rs.getDate("date_debut");
                    java.util.Date fin = rs.getDate("date_fin");
                    int idChef = rs.getInt("id_chef_projet");
                    String etat = rs.getString("etat");

                    projet = new Projet(id, nom, debut, fin, idChef, etat);

                    // (BONUS) Stocker le nom du chef de projet s'il est trouvé
                    // Tu devras ajouter ce champ et les getter/setter à ton modèle Projet.java
                    // projet.setNomChefProjet(rs.getString("fname") + " " + rs.getString("sname"));
                }
            }
        }
        return projet; // Renvoie null si non trouvé
    }

    public void updateProject(Projet projet) throws SQLException {
        String sql = "UPDATE Projet SET nom_projet = ?, date_debut = ?, date_fin = ?, etat = ?, id_chef_projet = ? WHERE id_projet = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, projet.getNomProjet());
            ps.setDate(2, new java.sql.Date(projet.getDateDebut().getTime()));
            ps.setDate(3, new java.sql.Date(projet.getDateFin().getTime()));
            ps.setString(4, projet.getEtat());
            ps.setInt(5, projet.getIdChefProjet()); // Le champ ajouté
            ps.setInt(6, projet.getIdProjet()); // Le WHERE

            ps.executeUpdate();
        }
    }
// ... (tes méthodes existantes : getAllProjects, createProject, etc. sont ici) ...

    /**
     * Récupère la liste des employés affectés à un projet et leur rôle.
     * Renvoie une Map<Employee, String> (Employé -> Rôle dans le projet)
     */
    public Map<Employee, String> getAssignedEmployees(int idProjet) throws SQLException {
        Map<Employee, String> team = new HashMap<>();
        // Jointure pour récupérer les infos de l'employé ET son rôle
        String sql = "SELECT e.*, ep.role_dans_projet FROM Employee e " +
                "JOIN Employe_Projet ep ON e.id = ep.id " +
                "WHERE ep.id_projet = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProjet);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Crée l'objet Employee
                    Employee emp = new Employee(
                            rs.getString("fname"),
                            rs.getString("sname"),
                            rs.getString("gender"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("position"),
                            rs.getString("grade"),
                            rs.getInt("id_departement")
                    );
                    emp.setId(rs.getInt("id"));

                    String roleInProject = rs.getString("role_dans_projet");
                    team.put(emp, roleInProject);
                }
            }
        }
        return team;
    }

    /**
     * Ajoute un employé à un projet (ou met à jour son rôle s'il y est déjà).
     */
    public void assignEmployeeToProject(int idProjet, int idEmploye, String role) throws SQLException {
        // ON DUPLICATE KEY UPDATE gère la création et la mise à jour en une fois
        String sql = "INSERT INTO Employe_Projet (id, id_projet, role_dans_projet) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE role_dans_projet = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmploye);
            ps.setInt(2, idProjet);
            ps.setString(3, role);
            ps.setString(4, role); // Pour la partie UPDATE

            ps.executeUpdate();
        }
    }

    /**
     * Retire un employé d'un projet.
     */
    public void removeEmployeeFromProject(int idProjet, int idEmploye) throws SQLException {
        String sql = "DELETE FROM Employe_Projet WHERE id = ? AND id_projet = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmploye);
            ps.setInt(2, idProjet);

            ps.executeUpdate();
        }
    }
    public List<Projet> getProjectsByEmployeeId(int idEmploye) throws SQLException {
        List<Projet> projets = new ArrayList<>();
        // On joint Projet et Employe_Projet
        String sql = "SELECT p.* FROM Projet p " +
                "JOIN Employe_Projet ep ON p.id_projet = ep.id_projet " +
                "WHERE ep.id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmploye);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Projet projet = new Projet(
                            rs.getInt("id_projet"),
                            rs.getString("nom_projet"),
                            rs.getDate("date_debut"),
                            rs.getDate("date_fin"),
                            rs.getInt("id_chef_projet"),
                            rs.getString("etat")
                    );
                    projets.add(projet);
                }
            }
        }
        return projets;
    }
}