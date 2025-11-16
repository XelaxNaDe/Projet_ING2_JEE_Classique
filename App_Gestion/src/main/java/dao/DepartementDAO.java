package dao;

import model.Departement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class DepartementDAO {

    public Departement findById(int id) throws SQLException {
        // ... (ton code findById est correct) ...
        Departement dept = null;
        String sql = "SELECT * FROM Departement WHERE id_departement = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dept = new Departement(
                            rs.getInt("id_departement"),
                            rs.getString("nom_departement"),
                            rs.getInt("id_chef_departement")
                    );
                }
            }
        }
        return dept;
    }

    /**
     * Récupère TOUS les départements de la BDD.
     */
    public List<Departement> getAllDepartments() throws SQLException {
        List<Departement> departements = new ArrayList<>();
        // On va aussi chercher le nom du chef de département (LEFT JOIN)
        String sql = "SELECT d.*, e.fname, e.sname FROM Departement d " +
                "LEFT JOIN Employee e ON d.id_chef_departement = e.id";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Departement dept = new Departement(
                        rs.getInt("id_departement"),
                        rs.getString("nom_departement"),
                        rs.getInt("id_chef_departement")
                );

                // ***** BLOC SUPPRIMÉ *****
                // Le code qui appelait setNomChefProjet était ici.
                // On ajoute juste le département tel quel.

                departements.add(dept);
            }
        }
        return departements;
    }

    /**
     * Crée un nouveau département (sans chef pour l'instant).
     */
    public int createDepartment(String nom, int idChef) throws SQLException {
        String sql = "INSERT INTO Departement (nom_departement, id_chef_departement) VALUES (?, ?)";
        int newId = 0; // Pour stocker l'ID généré

        // On doit utiliser un try-with-resources plus complexe pour récupérer les clés
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nom);

            if (idChef == 0) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, idChef);
            }

            ps.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    newId = rs.getInt(1);
                }
            }
        }
        return newId; // Renvoie l'ID (ou 0 si l'insertion a échoué)
    }

    /**
     * Supprime un département de la BDD.
     */
    public void deleteDepartment(int id) throws SQLException {
        // ... (ton code deleteDepartment est correct) ...
        String sql = "DELETE FROM Departement WHERE id_departement = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    public void updateDepartment(int id, String nom, int idChef) throws SQLException {
        String sql = "UPDATE Departement SET nom_departement = ?, id_chef_departement = ? WHERE id_departement = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);

            // --- CORRECTION ICI ---
            // Si l'ID est 0, on met NULL. Sinon, on met l'ID.
            if (idChef == 0) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, idChef);
            }
            // --- FIN CORRECTION ---

            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void removeAsChiefFromAnyDepartment(int idChef) throws SQLException {

        // --- CORRECTION ICI ---
        // On met NULL (Non assigné) au lieu de 0
        String sql = "UPDATE Departement SET id_chef_departement = NULL WHERE id_chef_departement = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idChef);
            ps.executeUpdate();
        }
    }
}