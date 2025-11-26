package controller;

import dao.PayrollDAO;
import dao.EmployeeDAO;
import model.Employee;
import model.Payroll;
import model.utils.IntStringPayroll;
import model.utils.RoleEnum;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "GestionPayrollServlet", urlPatterns = "/payroll")
public class GestionPayrollServlet extends HttpServlet {

    private PayrollDAO payrollDAO;
    private EmployeeDAO employeeDAO;

    @Override
    public void init(){
        payrollDAO = new PayrollDAO();
        employeeDAO = new EmployeeDAO();
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
            String searchId = req.getParameter("search_employee");
            String dateDebut = req.getParameter("date_debut");
            String dateFin = req.getParameter("date_fin");
            List<Payroll> listePayrolls;

            // Vérification du rôle (adaptez RoleEnum.ADMINISTRATOR selon votre projet)
            boolean isAdmin = user.hasRole(RoleEnum.ADMINISTRATOR);

            if (isAdmin) {
                boolean hasSearchCriteria = (searchId != null && !searchId.isEmpty()) ||
                        (dateDebut != null && !dateDebut.isEmpty()) ||
                        (dateFin != null && !dateFin.isEmpty());

                if (hasSearchCriteria) {
                    listePayrolls = payrollDAO.searchPayrolls(searchId, dateDebut, dateFin);
                } else {
                    listePayrolls = payrollDAO.getAllPayrolls();
                }
            } else {
                listePayrolls = payrollDAO.findPayrollByEmployee(user.getId());
            }

            // Récupérer la liste des employés pour le filtre (Admin uniquement)
            List<Employee> allEmployees = new ArrayList<>();
            if (isAdmin) {
                allEmployees = employeeDAO.getAllEmployees();
            } else {
                allEmployees.add(user);
            }

            req.setAttribute("listePayrolls", listePayrolls);
            req.setAttribute("allEmployees", allEmployees);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        req.getRequestDispatcher("/GestionPayroll.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null || !user.hasRole(RoleEnum.ADMINISTRATOR)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                // 1. Données principales
                int idEmployee = Integer.parseInt(req.getParameter("id_employee"));
                String dateStr = req.getParameter("date");
                int salary = Integer.parseInt(req.getParameter("salary"));
                double netPay = Double.parseDouble(req.getParameter("netPay"));

                Employee emp = employeeDAO.findEmployeeById(idEmployee);
                Payroll payroll = new Payroll(emp, LocalDate.parse(dateStr), salary, netPay);

                // 2. Lignes (Primes/Déductions)
                String[] reqAmount = req.getParameterValues("Amount");
                String[] reqLabel = req.getParameterValues("Label");
                String[] reqType = req.getParameterValues("Type");
                int lineCount = (reqAmount != null) ? reqAmount.length : 0;

                for (int i = 0; i < lineCount; i++) {
                    try {
                        int amount = Integer.parseInt(reqAmount[i]);
                        String label = (reqLabel != null && i < reqLabel.length) ? reqLabel[i] : "";
                        String type = (reqType != null && i < reqType.length) ? reqType[i] : "Prime";

                        // Ajout à l'objet Payroll (la liaison parent-enfant se fait dans addDetail)
                        payroll.addDetail(new IntStringPayroll(amount, label, type));
                    } catch (NumberFormatException ignored) {}
                }

                // 3. Sauvegarde via Hibernate (cascade)
                int idPay = payrollDAO.createPayroll(payroll);
                session.setAttribute("successMessage", "Fiche de paie (ID: " + idPay + ") créée avec succès!");

            } else if ("update".equals(action)) {
                int idPayroll = Integer.parseInt(req.getParameter("id_payroll"));
                int idEmployee = Integer.parseInt(req.getParameter("id_employee"));
                String dateStr = req.getParameter("date");
                int salary = Integer.parseInt(req.getParameter("salary"));
                double netPay = Double.parseDouble(req.getParameter("netPay"));

                // Pour l'update, on crée un objet temporaire ou on récupère l'existant.
                // Ici, on passe les nouvelles données au DAO
                Employee emp = employeeDAO.findEmployeeById(idEmployee);
                Payroll pUpdate = new Payroll(emp, LocalDate.parse(dateStr), salary, netPay);
                pUpdate.setId(idPayroll);

                // Reconstitution de la liste des lignes
                List<IntStringPayroll> newLines = new ArrayList<>();
                String[] reqAmount = req.getParameterValues("Amount");
                String[] reqLabel = req.getParameterValues("Label");
                String[] reqType = req.getParameterValues("Type");
                int lineCount = (reqAmount != null) ? reqAmount.length : 0;

                for (int i = 0; i < lineCount; i++) {
                    try {
                        int amount = Integer.parseInt(reqAmount[i]);
                        String label = (reqLabel != null) ? reqLabel[i] : "";
                        String type = (reqType != null) ? reqType[i] : "Prime";
                        newLines.add(new IntStringPayroll(amount, label, type));
                    } catch (NumberFormatException ignored) {}
                }

                payrollDAO.updatePayroll(pUpdate, newLines);
                session.setAttribute("successMessage", "Fiche de paie mise à jour.");

            } else if ("delete".equals(action)) {
                String idPayrollStr = req.getParameter("IdPayroll");
                if (idPayrollStr != null && !idPayrollStr.isEmpty()) {
                    payrollDAO.deletePayroll(Integer.parseInt(idPayrollStr));
                    session.setAttribute("successMessage", "Fiche de paie supprimée.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/payroll");
    }
}