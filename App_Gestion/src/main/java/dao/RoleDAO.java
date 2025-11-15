package dao;

import model.utils.Role; // Assure-toi que c'est le bon chemin pour ton Enum
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RoleDAO {

    /**
     * Récupère tous les rôles de la BDD.
     * Renvoie une Map<Integer, String> (ex: {1: "HEADDEPARTEMENT", 2: "PROJECTMANAGER", ...})
     */
    public Map<Integer, String> getAllRoles() throws SQLException {
        Map<Integer, String> roles = new HashMap<>();
        String sql = "SELECT * FROM Role";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                roles.put(rs.getInt("id_role"), rs.getString("nom_role"));
            }
        }
        return roles;
    }
}