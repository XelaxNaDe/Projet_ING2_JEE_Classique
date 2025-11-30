package controller;

import dao.EmployeeDAO;
import dao.PayrollDAO;
import model.Employee;
import model.Payroll;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "DetailPayrollServlet", urlPatterns = "/detail-payroll")
public class DetailPayrollServlet extends HttpServlet {

    private EmployeeDAO employeeDAO;
    private PayrollDAO payrollDAO;

    @Override
    public void init() {
        employeeDAO = new EmployeeDAO();
        payrollDAO = new PayrollDAO();
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
            List<Employee> employees = employeeDAO.getAllEmployees();
            req.setAttribute("allEmployees", employees);
            String idStr = req.getParameter("id");
            if (idStr != null && !idStr.isEmpty()) {
                int idPayroll = Integer.parseInt(idStr);
                Payroll existingPayroll = payrollDAO.findPayrollById(idPayroll);

                req.setAttribute("payroll", existingPayroll);
            }

            req.getRequestDispatcher("/DetailPayroll.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur serveur : " + e.getMessage());
        }
    }
}