package controller;

import dao.DepartementDAO;
import dao.EmployeeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Departement;
import model.Employee;
import model.utils.Role;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "DetailDepartementServlet", urlPatterns = "/detail-departement")
public class DetailDepartementServlet extends HttpServlet {

    private DepartementDAO departementDAO;
    private EmployeeDAO employeeDAO;
    // PAS de ProjetDAO ici

    @Override
    public void init() {
        this.departementDAO = new DepartementDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Affiche la page de détail d'un département.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        try {
            int id = Integer.parseInt(req.getParameter("id"));

            Departement dept = departementDAO.findById(id);
            if (dept == null) {
                resp.sendRedirect(req.getContextPath() + "/departements");
                return;
            }
            req.setAttribute("departement", dept);

            // Pour le <select> du Chef
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            req.setAttribute("allEmployees", allEmployees);

            // Récupérer les MEMBRES ACTUELS du département
            List<Employee> assignedEmployees = employeeDAO.getEmployeesByDepartmentId(id);
            req.setAttribute("assignedEmployees", assignedEmployees);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/departements");
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }

        req.getRequestDispatcher("/DetailDepartement.jsp").forward(req, resp);
    }

    /**
     * Gère la mise à jour (UPDATE) et les affectations.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        int idDept = Integer.parseInt(req.getParameter("id"));
        int oldChefId = 0;

        // --- Vérification des permissions (inchangée) ---
        boolean canModify = false;
        try {
            Departement dept = departementDAO.findById(idDept);
            if (dept == null) { /* ... gestion erreur ... */ }
            oldChefId = dept.getIdChefDepartement();
            boolean isAdmin = user.hasRole(Role.ADMINISTRATOR);
            boolean isHead = user.hasRole(Role.HEADDEPARTEMENT);
            boolean isThisDeptsHead = (isHead && user.getId() == dept.getIdChefDepartement());
            canModify = isAdmin || isThisDeptsHead;
        } catch (SQLException e) { /* ... gestion erreur ... */ }
        if (!canModify) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // --- Fin de la vérification ---

        try {
            if ("update".equals(action)) {
                String nom = req.getParameter("nomDepartement");
                int newChefId = user.hasRole(Role.ADMINISTRATOR) ? Integer.parseInt(req.getParameter("idChefDepartement")) : oldChefId;

                // --- LOGIQUE CORRIGÉE ---
                // 1. Si le chef a changé ET que c'est un admin...
                if (newChefId != oldChefId && user.hasRole(Role.ADMINISTRATOR)) {
                    // 1a. On libère le NOUVEAU chef de son poste actuel (s'il en a un)
                    if (newChefId > 0) {
                        departementDAO.removeAsChiefFromAnyDepartment(newChefId);
                    }
                }

                // 2. On met à jour le département
                departementDAO.updateDepartment(idDept, nom, newChefId);

                // 3. Gérer les rôles et l'affectation
                if (newChefId != oldChefId && user.hasRole(Role.ADMINISTRATOR)) {
                    if (newChefId > 0) {
                        employeeDAO.assignHeadDepartementRole(newChefId);
                        employeeDAO.setEmployeeDepartment(newChefId, idDept); // Affecte au nouveau dept
                    }
                    if (oldChefId > 0) {
                        employeeDAO.checkAndRemoveHeadDepartementRole(oldChefId);
                    }
                }
                // --- FIN LOGIQUE ---

                session.setAttribute("successMessage", "Département mis à jour.");

            } else if ("assignEmployee".equals(action)) {
                int idEmploye = Integer.parseInt(req.getParameter("idEmploye"));
                employeeDAO.setEmployeeDepartment(idEmploye, idDept);
                session.setAttribute("successMessage", "Employé affecté.");

            } else if ("removeEmployee".equals(action)) {
                int idEmploye = Integer.parseInt(req.getParameter("idEmploye"));
                employeeDAO.setEmployeeDepartment(idEmploye, 0); // 0 = Non assigné
                session.setAttribute("successMessage", "Employé retiré.");
            }

        } catch (SQLException | NumberFormatException e) {
            session.setAttribute("errorMessage", "Erreur lors de l'opération : " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/detail-departement?id=" + idDept);
    }
}