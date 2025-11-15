package dao;

import model.Employee;
import model.utils.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; // Assure-toi d'avoir cet import
import java.util.List;      // Assure-toi d'avoir cet import

public class EmployeeDAO {

    /**
     * Tente de trouver un utilisateur par email et mot de passe.
     */
    public Employee findByEmailAndPassword(String email, String password) throws SQLException {
        Employee user = null;
        String sqlEmployee = "SELECT * FROM Employee WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlEmployee)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new Employee(
                            rs.getString("fname"),
                            rs.getString("sname"),
                            rs.getString("gender"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("position"),
                            rs.getString("grade"),
                            rs.getInt("id_departement")
                    );
                    user.setId(rs.getInt("id"));
                    loadUserRoles(conn, user);
                }
            }
        }
        return user;
    }

    /**
     * Méthode privée pour charger les rôles d'un employé.
     */
    private void loadUserRoles(Connection conn, Employee user) throws SQLException {
        String sqlRoles = "SELECT r.nom_role FROM Employee_Role er " +
                "JOIN Role r ON er.id_role = r.id_role " +
                "WHERE er.id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlRoles)) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String roleName = rs.getString("nom_role");
                    try {
                        user.addRole(Role.valueOf(roleName));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Rôle BDD inconnu: " + roleName);
                    }
                }
            }
        }
    }

    /**
     * Récupère la liste de tous les employés (simplifié).
     */
    public List<Employee> getAllEmployees() throws SQLException {
        // 1. On crée la LISTE (au pluriel)
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT id, fname, sname FROM Employee";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // 2. On crée l'employé (au singulier)
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setFname(rs.getString("fname"));
                emp.setSname(rs.getString("sname"));

                // 3. On AJOUTE l'employé 'emp' à la LISTE 'employees'
                employees.add(emp);
            }
        }
        return employees;
    }

    // --- MÉTHODES AJOUTÉES POUR LE PROFIL ---

    /**
     * Vérifie si le mot de passe actuel d'un utilisateur est correct.
     * @return true si le mot de passe correspond, false sinon.
     */
    public boolean checkPassword(int userId, String passwordToCheck) throws SQLException {
        String sql = "SELECT 1 FROM Employee WHERE id = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, passwordToCheck);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true si un enregistrement est trouvé
            }
        }
    }

    /**
     * Met à jour l'email d'un utilisateur dans la BDD.
     */
    public void updateEmail(int userId, String newEmail) throws SQLException {
        String sql = "UPDATE Employee SET email = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newEmail);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Met à jour le mot de passe d'un utilisateur dans la BDD.
     */
    public void updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE Employee SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}