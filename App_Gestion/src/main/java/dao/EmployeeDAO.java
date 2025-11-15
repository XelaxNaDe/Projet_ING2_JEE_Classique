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
    public List<Employee> getAllEmployeesFull(String searchPrenom, String searchNom, String searchMatricule, String searchDepartement,
                                              String filterPoste, String filterRole) throws SQLException {

        List<Employee> employees = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // MODIFIÉ : Ajout des jointures pour les Rôles
        String sql = "SELECT DISTINCT e.*, d.nom_departement FROM Employee e " +
                "LEFT JOIN Departement d ON e.id_departement = d.id_departement " +
                "LEFT JOIN Employee_Role er ON e.id = er.id " +
                "LEFT JOIN Role r ON er.id_role = r.id_role " +
                "WHERE 1=1";

        // --- Champs de RECHERCHE (LIKE) ---
        if (searchPrenom != null && !searchPrenom.trim().isEmpty()) {
            sql += " AND e.fname LIKE ?";
            params.add("%" + searchPrenom + "%");
        }
        if (searchNom != null && !searchNom.trim().isEmpty()) {
            sql += " AND e.sname LIKE ?";
            params.add("%" + searchNom + "%");
        }
        if (searchDepartement != null && !searchDepartement.trim().isEmpty()) {
            sql += " AND d.nom_departement LIKE ?";
            params.add("%" + searchDepartement + "%");
        }

        // --- Champ de RECHERCHE (Matricule = Exact) ---
        if (searchMatricule != null && !searchMatricule.trim().isEmpty()) {
            try {
                sql += " AND e.id = ?";
                params.add(Integer.parseInt(searchMatricule));
            } catch (NumberFormatException e) { /* Ignore */ }
        }

        // --- Champs de FILTRE (Menu déroulant = Exact) ---
        if (filterPoste != null && !filterPoste.trim().isEmpty()) {
            sql += " AND e.position = ?";
            params.add(filterPoste);
        }
        // MODIFIÉ : Filtre sur r.nom_role
        if (filterRole != null && !filterRole.trim().isEmpty()) {
            sql += " AND r.nom_role = ?";
            params.add(filterRole);
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            for (Object param : params) {
                ps.setObject(paramIndex++, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee(
                            rs.getString("fname"),
                            rs.getString("sname"),
                            rs.getString("gender"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("position"),
                            rs.getString("grade"), // On garde grade pour le constructeur
                            rs.getInt("id_departement")
                    );
                    emp.setId(rs.getInt("id"));
                    emp.setNomDepartement(rs.getString("nom_departement"));

                    // On charge les rôles à part pour être sûr de les avoir tous
                    loadUserRoles(conn, emp);
                    employees.add(emp);
                }
            }
        }
        return employees;
    }

    /**
     * Récupère un employé par son ID.
     */
    public Employee findEmployeeById(int id) throws SQLException {
        Employee user = null;
        String sqlEmployee = "SELECT * FROM Employee WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlEmployee)) {

            ps.setInt(1, id);
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
     * Crée un nouvel employé.
     * Note : ne gère pas l'assignation des rôles, c'est plus complexe.
     */
    public void createEmployee(Employee emp) throws SQLException {
        String sql = "INSERT INTO Employee (fname, sname, gender, email, password, position, grade, id_departement) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emp.getFname());
            ps.setString(2, emp.getSname());
            ps.setString(3, emp.getGender());
            ps.setString(4, emp.getEmail());
            ps.setString(5, emp.getPassword()); // Rappel : Hasher le mdp est crucial
            ps.setString(6, emp.getPosition());
            ps.setString(7, emp.getGrade());
            ps.setInt(8, emp.getIdDepartement());

            ps.executeUpdate();
        }
    }

    /**
     * Met à jour un employé (ne touche pas au mot de passe).
     */
    public void updateEmployee(Employee emp) throws SQLException {
        // MODIFIÉ : email = ? a été retiré de la requête
        String sql = "UPDATE Employee SET fname = ?, sname = ?, gender = ?, " +
                "position = ?, grade = ?, id_departement = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emp.getFname());
            ps.setString(2, emp.getSname());
            ps.setString(3, emp.getGender());
            // L'email (paramètre 4) est retiré
            ps.setString(4, emp.getPosition());
            ps.setString(5, emp.getGrade());
            ps.setInt(6, emp.getIdDepartement());
            ps.setInt(7, emp.getId()); // ID pour le WHERE

            ps.executeUpdate();
        }
    }

    /**
     * Supprime un employé.
     */
    public void deleteEmployee(int id) throws SQLException {
        String sql = "DELETE FROM Employee WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<String> getDistinctPostes() throws SQLException {
        List<String> postes = new ArrayList<>();
        String sql = "SELECT DISTINCT position FROM Employee WHERE position IS NOT NULL AND position != '' ORDER BY position";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                postes.add(rs.getString("position"));
            }
        }
        return postes;
    }
    /**
     * Récupère la liste de tous les grades (uniques)
     */
    public List<String> getDistinctRoles() throws SQLException {
        List<String> roles = new ArrayList<>();
        // On lit la table Role
        String sql = "SELECT nom_role FROM Role ORDER BY nom_role";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(rs.getString("nom_role"));
            }
        }
        return roles;
    }

    public void clearEmployeeRoles(int employeeId) throws SQLException {
        String sql = "DELETE FROM Employee_Role WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        }
    }

    public void addEmployeeRole(int employeeId, int roleId) throws SQLException {
        String sql = "INSERT INTO Employee_Role (id, id_role) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }
}