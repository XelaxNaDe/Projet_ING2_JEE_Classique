package dao;

import model.Projet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}