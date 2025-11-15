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
}