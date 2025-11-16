package dao;

import model.Employee;
import model.utils.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    // --- 1. MÉTHODES DE CONNEXION / RÉCUPÉRATION (GET) ---

    /**
     * Tente de trouver un utilisateur par email et mot de passe (pour le login).
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
                    // On charge les rôles de l'utilisateur qui se connecte
                    loadUserRoles(conn, user);
                }
            }
        }
        return user;
    }

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
     * Récupère un employé par son ID (pour le formulaire de modification).
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
     * Récupère la liste de tous les employés (simplifié, pour les menus déroulants).
     */
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT id, fname, sname FROM Employee";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setFname(rs.getString("fname"));
                emp.setSname(rs.getString("sname"));
                employees.add(emp);
            }
        }
        return employees;
    }

    /**
     * Récupère TOUS les employés avec filtres ET recherche (pour la page de gestion).
     */
    public List<Employee> getAllEmployeesFull(String searchPrenom, String searchNom, String searchMatricule, String searchDepartement,
                                              String filterPoste, String filterRole) throws SQLException {

        List<Employee> employees = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        String sql = "SELECT DISTINCT e.*, d.nom_departement FROM Employee e " +
                "LEFT JOIN Departement d ON e.id_departement = d.id_departement " +
                "LEFT JOIN Employee_Role er ON e.id = er.id " +
                "LEFT JOIN Role r ON er.id_role = r.id_role " +
                "WHERE 1=1";

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
        if (searchMatricule != null && !searchMatricule.trim().isEmpty()) {
            try {
                sql += " AND e.id = ?";
                params.add(Integer.parseInt(searchMatricule));
            } catch (NumberFormatException e) { /* Ignore */ }
        }
        if (filterPoste != null && !filterPoste.trim().isEmpty()) {
            sql += " AND e.position = ?";
            params.add(filterPoste);
        }
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
                            rs.getString("grade"),
                            rs.getInt("id_departement")
                    );
                    emp.setId(rs.getInt("id"));
                    emp.setNomDepartement(rs.getString("nom_departement"));

                    loadUserRoles(conn, emp);
                    employees.add(emp);
                }
            }
        }
        return employees;
    }

    /**
     * Récupère la liste des employés qui sont dans un département spécifique.
     */
    public List<Employee> getEmployeesByDepartmentId(int idDepartement) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employee WHERE id_departement = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idDepartement);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    employees.add(emp);
                }
            }
        }
        return employees;
    }

    /**
     * Récupère la liste de tous les postes (uniques)
     */
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
     * Récupère la liste de tous les rôles (uniques)
     */
    public List<String> getDistinctRoles() throws SQLException {
        List<String> roles = new ArrayList<>();
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

    // --- 2. MÉTHODES DE MODIFICATION (CREATE, UPDATE, DELETE) ---

    /**
     * Crée un nouvel employé.
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
            ps.setString(5, emp.getPassword());
            ps.setString(6, emp.getPosition());
            ps.setString(7, emp.getGrade());
            ps.setInt(8, emp.getIdDepartement());

            ps.executeUpdate();
        }
    }

    /**
     * Met à jour un employé (ne touche ni à l'email ni au mot de passe).
     */
    public void updateEmployee(Employee emp) throws SQLException {
        String sql = "UPDATE Employee SET fname = ?, sname = ?, gender = ?, " +
                "position = ?, grade = ?, id_departement = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emp.getFname());
            ps.setString(2, emp.getSname());
            ps.setString(3, emp.getGender());
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

    /**
     * Change (ou assigne) le département d'un employé.
     * Si idDepartement = 0, l'employé est "Non assigné".
     */
    public void setEmployeeDepartment(int idEmploye, int idDepartement) throws SQLException {
        String sql = "UPDATE Employee SET id_departement = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (idDepartement == 0) {
                // Si l'ID est 0, on met NULL dans la BDD
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, idDepartement);
            }
            ps.setInt(2, idEmploye);

            ps.executeUpdate();
        }
    }

    // --- 3. MÉTHODES DE GESTION DU PROFIL (Email/MDP) ---

    /**
     * Vérifie si le mot de passe actuel d'un utilisateur est correct.
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

    // --- 4. MÉTHODES DE GESTION DES RÔLES ---

    /**
     * Helper privé pour trouver l'ID d'un rôle par son nom.
     */
    private int getRoleIdByName(String roleName) throws SQLException {
        String sql = "SELECT id_role FROM Role WHERE nom_role = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_role");
                }
            }
        }
        throw new SQLException("Role non trouvé dans la BDD: " + roleName);
    }

    /**
     * Supprime tous les rôles assignés à un employé.
     */
    public void clearEmployeeRoles(int employeeId) throws SQLException {
        String sql = "DELETE FROM Employee_Role WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        }
    }

    /**
     * Ajoute un rôle à un employé.
     */
    public void addEmployeeRole(int employeeId, int roleId) throws SQLException {
        String sql = "INSERT INTO Employee_Role (id, id_role) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    /**
     * Assigne le rôle PROJECTMANAGER.
     */
    public void assignProjectManagerRole(int employeeId) throws SQLException {
        int roleId = getRoleIdByName("PROJECTMANAGER");
        String sqlAssignRole = "INSERT IGNORE INTO Employee_Role (id, id_role) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAssignRole)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    /**
     * Assigne le rôle HEADDEPARTEMENT.
     */
    public void assignHeadDepartementRole(int employeeId) throws SQLException {
        int roleId = getRoleIdByName("HEADDEPARTEMENT");
        String sqlAssignRole = "INSERT IGNORE INTO Employee_Role (id, id_role) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAssignRole)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    /**
     * Vérifie et RETIRE le rôle PROJECTMANAGER si l'employé n'est plus chef.
     */
    public void checkAndRemoveProjectManagerRole(int employeeId) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) AS count FROM Projet WHERE id_chef_projet = ?";
        int projectCount = 0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    projectCount = rs.getInt("count");
                }
            }
        }

        if (projectCount == 0) {
            int roleId = getRoleIdByName("PROJECTMANAGER");
            String sqlDelete = "DELETE FROM Employee_Role WHERE id = ? AND id_role = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
                ps.setInt(1, employeeId);
                ps.setInt(2, roleId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Vérifie et RETIRE le rôle HEADDEPARTEMENT si l'employé n'est plus chef.
     */
    public void checkAndRemoveHeadDepartementRole(int employeeId) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) AS count FROM Departement WHERE id_chef_departement = ?";
        int deptCount = 0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    deptCount = rs.getInt("count");
                }
            }
        }

        if (deptCount == 0) {
            int roleId = getRoleIdByName("HEADDEPARTEMENT");
            String sqlDelete = "DELETE FROM Employee_Role WHERE id = ? AND id_role = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
                ps.setInt(1, employeeId);
                ps.setInt(2, roleId);
                ps.executeUpdate();
            }
        }
    }
}