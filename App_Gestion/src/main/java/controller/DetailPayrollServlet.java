package controller;

import dao.EmployeeDAO;
import dao.PayrollDAO; // NOUVEAU
import model.Employee;
import model.Payroll;  // NOUVEAU
import model.utils.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "DetailPayrollServlet", urlPatterns = "/detail-payroll")
public class DetailPayrollServlet extends HttpServlet {

    private EmployeeDAO employeeDAO;
    private PayrollDAO payrollDAO; // NOUVEAU

    @Override
    public void init() {
        employeeDAO = new EmployeeDAO();
        payrollDAO = new PayrollDAO(); // NOUVEAU
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        try {
            // 1. Charger la liste des employés pour le menu déroulant
            List<Employee> employees = employeeDAO.getAllEmployees();
            req.setAttribute("allEmployees", employees);

            // 2. LOGIQUE D'ÉDITION (NOUVEAU)
            // On vérifie si un ID est passé dans l'URL (ex: /detail-payroll?id=12)
            String idStr = req.getParameter("id");
            if (idStr != null && !idStr.isEmpty()) {
                int idPayroll = Integer.parseInt(idStr);
                Payroll existingPayroll = payrollDAO.findPayrollById(idPayroll);

                // On envoie la fiche trouvée à la JSP
                req.setAttribute("payroll", existingPayroll);
            }

            req.getRequestDispatcher("/DetailPayroll.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur BDD : " + e.getMessage());
        }
    }
}