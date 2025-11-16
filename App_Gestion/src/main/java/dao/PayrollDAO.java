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

    /**
     * Récupère toutes les listes de primes ou déductions associées à une fiche de paie
     */
    public List<IntStringPayroll> getAllPayrollsDetails(int payroll, String type){
        List<IntStringPayroll> detailspayrolls = new ArrayList<>();

        // REQUÊTE SIMPLIFIÉE : On prend juste tous les dé dans la table Payroll
        String sql = "SELECT * FROM Intstringpayroll WHERE id_payroll = ? AND type = ";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, payroll);
            ps.setString(2, type);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id_line = rs.getInt("id_line");
                    int id_payroll = rs.getInt("id_payroll");
                    int amount = rs.getInt("amount");
                    String label = rs.getString("label");
                    String typeList = rs.getString("type_list");

                    IntStringPayroll payrollLine = new IntStringPayroll(id_line, id_payroll, amount, label, typeList);
                    detailspayrolls.add(payrollLine);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Si ça plante ici (par ex. "Unknown column 'id_chef_projet'"),
        // l'erreur sera attrapée par la servlet.
        return detailspayrolls;
    }

    public List<Payroll> getAllPayrolls() throws SQLException {
        List<Payroll> payrolls = new ArrayList<>();

        // REQUÊTE SIMPLIFIÉE : On prend juste tout dans la table Payroll
        String sql = "SELECT * FROM Payroll";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_payroll");
                int id_employe = rs.getInt("id");
                LocalDate date = rs.getDate("date").toLocalDate();
                int salary = rs.getInt("salary");
                double netPay = rs.getDouble("netPay");
                List<IntStringPayroll> bonusesList = getAllPayrollsDetails(id, "Prime");
                List<IntStringPayroll> deductionsList = getAllPayrollsDetails(id, "Déduction");
                EmployeeDAO employeeDAO = new EmployeeDAO();
                Employee e = employeeDAO.findEmployeeById(id_employe);

                Payroll payroll = new Payroll(id, e, date, salary, netPay, bonusesList, deductionsList);
                payrolls.add(payroll);
            }
        }
        // Si ça plante ici (par ex. "Unknown column 'salary'"),
        // l'erreur sera attrapée par la servlet.
        return payrolls;
    }

    /**
     * Tente de trouver une fiche de paie en fonction de l'employé par email et mot de passe (pour le login).
     */
    public ArrayList<Payroll> findPayrollByEmployee(Employee e) throws SQLException {
        String sqlEmployee = "SELECT * FROM Payroll WHERE id = ?";
        ArrayList<Payroll> payrolls = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlEmployee)) {
            ps.setInt(1, e.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    payrolls.add(new Payroll(
                            rs.getInt("id_payroll"),
                            e,
                            rs.getDate("date").toLocalDate(),
                            rs.getInt("salary"),
                            rs.getDouble("netPay"),
                            getAllPayrollsDetails(rs.getInt("id_payroll"), "Prime"),
                            getAllPayrollsDetails(rs.getInt("id_payroll"), "Déduction")
                    ));
                }
            }
        }
        return payrolls;
    }

    /**
     * Tente de trouver une fiche de paie en fonction de l'employé par email et mot de passe (pour le login).
     */
    public ArrayList<Payroll> findPayrollByPeriod(LocalDate debut, LocalDate fin) throws SQLException {
        String sqlEmployee = "SELECT * FROM Payroll WHERE `date` BETWEEN ? AND ?";
        ArrayList<Payroll> payrolls = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlEmployee)) {

            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));

            EmployeeDAO e = new EmployeeDAO();
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    payrolls.add(new Payroll(
                            rs.getInt("id_payroll"),
                            e.findEmployeeById(rs.getInt("id")),
                            rs.getDate("date").toLocalDate(),
                            rs.getInt("salary"),
                            rs.getDouble("netPay"),
                            getAllPayrollsDetails(rs.getInt("id_payroll"), "Prime"),
                            getAllPayrollsDetails(rs.getInt("id_payroll"), "Déduction")
                    ));
                }
            }
        }
        return payrolls;
    }

    /**
     * Crée une nouvelle ligne de prime ou déduction fiche de paie.
     */
    public void createPayrollLine(IntStringPayroll payLine) throws SQLException {
        String sql = "INSERT INTO Payroll (id_payroll, amount, label, type) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, payLine.getId_payroll());
            ps.setInt(2, payLine.getAmount());
            ps.setString(3, payLine.getLabel());
            ps.setString(4, payLine.getType());

            ps.executeUpdate();
        }
    }

    /**
     * Met à jour une ligne de prime ou déduction fiche de paie.
     */
    public void updatePayrollLine(IntStringPayroll payLine) throws SQLException {
        String sql = "UPDATE Payroll SET id_payroll = ?, amount = ?, label = ?,type = ? " +
                        "WHERE id_line = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, payLine.getId_payroll());
            ps.setInt(2, payLine.getAmount());
            ps.setString(3, payLine.getLabel());
            ps.setString(4, payLine.getType());
            ps.setInt(5, payLine.getId_line()); // ID pour le WHERE

            ps.executeUpdate();
        }
    }

    /**
     * Supprime une ligne de prime ou déduction fiche de paie.
     */
    public void deletePayrollLine(int id) throws SQLException {
        String sql = "DELETE FROM Payroll WHERE id_line = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Crée une nouvelle fiche de paie.
     */
    public void createPayroll(Payroll pay) throws SQLException {
        String sql = "INSERT INTO Payroll (id, `date`, salary, netPay) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pay.getEmployeeId());
            ps.setDate(2, Date.valueOf(pay.getDate()));
            ps.setInt(3, pay.getSalary());
            ps.setDouble(4, pay.getNetPay());

            ps.executeUpdate();
        }
    }

    /**
     * Met à jour une fiche de paie.
     */
    public void updatePayroll(Payroll pay) throws SQLException {
        String sql = "UPDATE Payroll SET id = ?, `date` = ?, salary = ?," +
                        "netPay = ? WHERE id_payroll = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pay.getEmployeeId());
            ps.setDate(2, Date.valueOf(pay.getDate()));
            ps.setInt(3, pay.getSalary());
            ps.setDouble(4, pay.getNetPay());
            ps.setInt(5, pay.getId()); // ID pour le WHERE

            ps.executeUpdate();
        }
    }

    /**
     * Supprime une fiche de paie.
     */
    public void deletePayroll(int id) throws SQLException {
        String sql = "DELETE FROM Payroll WHERE id_payroll = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
