package dao;

import model.Employee;
import model.Payroll;
import model.utils.IntStringPayroll;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {

    // --- 1. MÉTHODES DE CONNEXION / RÉCUPÉRATION (GET) ---

    public List<IntStringPayroll> getAllPayrollsDetails(int payrollId, String type){
        List<IntStringPayroll> detailspayrolls = new ArrayList<>();
        String sql = "SELECT * FROM IntStringPayroll WHERE id_payroll = ? AND type_list = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, payrollId);
            ps.setString(2, type);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detailspayrolls.add(new IntStringPayroll(
                            rs.getInt("id_line"),
                            rs.getInt("id_payroll"),
                            rs.getInt("amount"),
                            rs.getString("label"),
                            rs.getString("type_list")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des détails de paie.", e);
        }
        return detailspayrolls;
    }

    /**
     * CORRECTION MAJEURE: Utilisation d'un JOIN SQL pour éviter de fermer la connexion
     * en appelant EmployeeDAO dans la boucle.
     */
    public List<Payroll> getAllPayrolls() throws SQLException {
        List<Payroll> payrolls = new ArrayList<>();

        // On joint Payroll et Employee pour tout récupérer en une seule requête
        String sql = "SELECT p.*, e.fname, e.sname, e.gender, e.email, e.password, " +
                "e.position, e.grade, e.id_departement " +
                "FROM Payroll p " +
                "JOIN Employee e ON p.id = e.id " + // p.id est la FK vers Employee
                "ORDER BY p.date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // 1. Reconstruire l'objet Employee manuellement depuis le ResultSet courant
                Employee emp = mapEmployeeFromResultSet(rs, rs.getInt("id")); // "id" dans Payroll est l'ID employé

                // 2. Créer l'objet Payroll
                Payroll payroll = new Payroll(
                        rs.getInt("id_payroll"),
                        emp,
                        rs.getDate("date").toLocalDate(),
                        rs.getInt("salary"),
                        rs.getDouble("netPay")
                );
                payrolls.add(payroll);
            }
        }

        // 3. Charger les détails (Primes/Déductions) APRÈS avoir fermé la connexion principale
        populatePayrollDetails(payrolls);

        return payrolls;
    }

    public Payroll findPayrollById(int idPayroll) throws SQLException {
        String sql = "SELECT p.*, e.fname, e.sname, e.gender, e.email, e.password, " +
                "e.position, e.grade, e.id_departement " +
                "FROM Payroll p " +
                "JOIN Employee e ON p.id = e.id " +
                "WHERE p.id_payroll = ?";

        Payroll payroll = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPayroll);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee emp = mapEmployeeFromResultSet(rs, rs.getInt("id"));

                    payroll = new Payroll(
                            rs.getInt("id_payroll"),
                            emp,
                            rs.getDate("date").toLocalDate(),
                            rs.getInt("salary"),
                            rs.getDouble("netPay")
                    );
                }
            }
        }

        // Si trouvé, on charge les détails (nouvelle connexion gérée par la méthode)
        if (payroll != null) {
            payroll.setBonusesList(getAllPayrollsDetails(payroll.getId(), "Prime"));
            payroll.setDeductionsList(getAllPayrollsDetails(payroll.getId(), "Déduction"));
        }

        return payroll;
    }

    public ArrayList<Payroll> findPayrollByEmployee(int employeeId) throws SQLException {
        String sql = "SELECT p.*, e.fname, e.sname, e.gender, e.email, e.password, " +
                "e.position, e.grade, e.id_departement " +
                "FROM Payroll p " +
                "JOIN Employee e ON p.id = e.id " +
                "WHERE p.id = ?";

        ArrayList<Payroll> payrolls = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee emp = mapEmployeeFromResultSet(rs, employeeId);

                    payrolls.add(new Payroll(
                            rs.getInt("id_payroll"),
                            emp,
                            rs.getDate("date").toLocalDate(),
                            rs.getInt("salary"),
                            rs.getDouble("netPay")
                    ));
                }
            }
        }

        populatePayrollDetails(payrolls);
        return payrolls;
    }

    /**
     * Recherche avancée multi-critères
     */
    public List<Payroll> searchPayrolls(String empIdStr, String dateDebutStr, String dateFinStr) throws SQLException {
        List<Payroll> payrolls = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, e.fname, e.sname, e.gender, e.email, e.password, " +
                        "e.position, e.grade, e.id_departement " +
                        "FROM Payroll p " +
                        "JOIN Employee e ON p.id = e.id " +
                        "WHERE 1=1 "); // 1=1 permet d'ajouter des AND dynamiquement

        List<Object> params = new ArrayList<>();

        // Filtre par Employé
        if (empIdStr != null && !empIdStr.isEmpty()) {
            sql.append("AND p.id = ? ");
            params.add(Integer.parseInt(empIdStr));
        }

        // Filtre par Date de début
        if (dateDebutStr != null && !dateDebutStr.isEmpty()) {
            sql.append("AND p.date >= ? ");
            params.add(Date.valueOf(dateDebutStr));
        }

        // Filtre par Date de fin
        if (dateFinStr != null && !dateFinStr.isEmpty()) {
            sql.append("AND p.date <= ? ");
            params.add(Date.valueOf(dateFinStr));
        }

        sql.append("ORDER BY p.date DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Remplissage des paramètres
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee emp = mapEmployeeFromResultSet(rs, rs.getInt("id"));
                    Payroll payroll = new Payroll(
                            rs.getInt("id_payroll"),
                            emp,
                            rs.getDate("date").toLocalDate(),
                            rs.getInt("salary"),
                            rs.getDouble("netPay")
                    );
                    payrolls.add(payroll);
                }
            }
        }

        // Charger les détails (Primes/Déductions)
        populatePayrollDetails(payrolls);

        return payrolls;
    }

    // --- HELPER METHODS (Pour éviter la duplication de code) ---

    /**
     * Reconstruit un objet Employee à partir du ResultSet courant (grâce au JOIN).
     */
    private Employee mapEmployeeFromResultSet(ResultSet rs, int empId) throws SQLException {
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
        emp.setId(empId);
        return emp;
    }

    /**
     * Parcourt une liste de Payrolls et remplit les primes/déductions.
     * Cette méthode ouvre et ferme ses propres connexions proprement.
     */
    private void populatePayrollDetails(List<Payroll> payrolls) {
        for (Payroll p : payrolls) {
            // Note: getAllPayrollsDetails gère sa propre connexion (Open/Close)
            // Comme nous ne sommes plus dans un ResultSet ouvert, c'est sécurisé.
            p.setBonusesList(getAllPayrollsDetails(p.getId(), "Prime"));
            p.setDeductionsList(getAllPayrollsDetails(p.getId(), "Déduction"));
        }
    }

    // --- 2. MÉTHODES DE MODIFICATION (CREATE, UPDATE, DELETE) ---
    // Ces méthodes ne changeaient pas, mais sont incluses pour garder la classe complète.

    public int createPayrollLine(int idPay, int amount, String label, String type) throws SQLException {
        String sql = "INSERT INTO IntStringPayroll (id_payroll, amount, label, type_list) VALUES (?, ?, ?, ?)";
        int newId = 0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPay);
            ps.setInt(2, amount);
            ps.setString(3, label);
            ps.setString(4, type);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) newId = rs.getInt(1);
            }
        }
        return newId;
    }

    public void updatePayrollLine(int idLine, int amount, String label, String type) throws SQLException {
        String sql = "UPDATE IntStringPayroll SET amount = ?, label = ?, type_list = ? WHERE id_line = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setString(2, label);
            ps.setString(3, type);
            ps.setInt(4, idLine);
            ps.executeUpdate();
        }
    }

    public void deletePayrollLine(int id) throws SQLException {
        String sql = "DELETE FROM IntStringPayroll WHERE id_line = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Crée une nouvelle fiche de paie avec le NetPay directement inclus.
     */
    public int createPayroll(int idEmployee, Date date, int salaire, double netPay) throws SQLException {
        // MODIFICATION : Ajout de la colonne netPay dans l'INSERT
        String sql = "INSERT INTO Payroll (id, `date`, salary, netPay) VALUES (?, ?, ?, ?)";
        int newId = 0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idEmployee);
            ps.setDate(2, date);
            ps.setInt(3, salaire);
            ps.setDouble(4, netPay); // Insertion de la valeur reçue depuis le JSP

            ps.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    newId = rs.getInt(1);
                }
            }
        }
        return newId;
    }

    public void createPayrollNetPay(int idPayroll, int salary) throws SQLException {
        // Logique optimisée : lecture et mise à jour séparées ou via une procédure,
        // mais ici on garde la logique simple : récupération puis mise à jour.

        int bonus = 0;
        int deductions = 0;

        // Étape 1: Calculer
        String selectSql = "SELECT amount, type_list FROM IntStringPayroll WHERE id_payroll = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, idPayroll);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("Prime".equals(rs.getString("type_list"))) {
                        bonus += rs.getInt("amount");
                    } else if ("Déduction".equals(rs.getString("type_list"))) {
                        deductions += rs.getInt("amount");
                    }
                }
            }
        }

        // Étape 2: Mise à jour
        double netPay = (double) salary + bonus - deductions;
        String updateSql = "UPDATE Payroll SET netPay = ? WHERE id_payroll = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setDouble(1, netPay);
            ps.setInt(2, idPayroll);
            ps.executeUpdate();
        }
    }

    public void updatePayroll(Payroll pay) throws SQLException {
        String sql = "UPDATE Payroll SET id = ?, `date` = ?, salary = ?, netPay = ? WHERE id_payroll = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pay.getEmployeeId());
            ps.setDate(2, Date.valueOf(pay.getDate()));
            ps.setInt(3, pay.getSalary());
            ps.setDouble(4, pay.getNetPay());
            ps.setInt(5, pay.getId());
            ps.executeUpdate();
        }
    }

    public void deletePayroll(int id) throws SQLException {
        String sqlPayroll = "DELETE FROM Payroll WHERE id_payroll = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlPayroll)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}