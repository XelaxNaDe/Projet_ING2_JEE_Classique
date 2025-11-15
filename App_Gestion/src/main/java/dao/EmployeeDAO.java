package dao;

import model.Employee;
import model.utils.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDAO {

    /**
     * Tente de trouver un utilisateur par email et mot de passe.
     * Si trouvé, charge aussi ses rôles.
     */
    public Employee findByEmailAndPassword(String email, String password) throws SQLException {
        Employee user = null;

        // Requête sur la table Employee (utilise les noms de ta BDD)
        String sqlEmployee = "SELECT * FROM Employee WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlEmployee)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. L'utilisateur existe, on crée l'objet
                    user = new Employee(
                            rs.getString("fname"),
                            rs.getString("sname"),
                            rs.getString("gender"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("position")
                    );

                    // 2. On utilise 'id' (de la BDD) et 'setId' (du modèle)
                    user.setId(rs.getInt("id"));

                    // 3. On charge les rôles de cet utilisateur (MÉTHODE CORRIGÉE CI-DESSOUS)
                    loadUserRoles(conn, user);
                }
            }
        }
        return user; // Renvoie null si l'utilisateur n'est pas trouvé
    }

    /**
     * Méthode privée pour charger les rôles d'un employé.
     * (VERSION CORRIGÉE QUI LIT LE NOM DU RÔLE)
     */
    private void loadUserRoles(Connection conn, Employee user) throws SQLException {

        // CORRECTION : On fait une jointure pour récupérer le NOM du rôle,
        // pas seulement son ID.
        String sqlRoles = "SELECT r.nom_role FROM Employee_Role er " +
                "JOIN Role r ON er.id_role = r.id_role " +
                "WHERE er.id = ?"; // 'er.id' est la clé étrangère vers Employee

        try (PreparedStatement ps = conn.prepareStatement(sqlRoles)) {

            ps.setInt(1, user.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // 1. On récupère le nom du rôle (ex: "ADMINISTRATOR")
                    String roleName = rs.getString("nom_role");

                    try {
                        // 2. On convertit ce String en Enum (Role.ADMINISTRATOR)
                        user.addRole(Role.valueOf(roleName));
                    } catch (IllegalArgumentException e) {
                        // Si le rôle "ADMINISTRATOR" n'existe pas dans ton Enum 'Role.java'
                        System.err.println("Rôle BDD inconnu ou non défini dans l'Enum Role: " + roleName);
                    }
                }
            }
        }
    }
}