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

@WebServlet(name = "GestionDepartementServlet", urlPatterns = "/departements")
public class GestionDepartementServlet extends HttpServlet {

    private DepartementDAO departementDAO;
    private EmployeeDAO employeeDAO;

    @Override
    public void init() {
        this.departementDAO = new DepartementDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ... (Ton code doGet est correct) ...
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        try {
            List<Departement> listeDepartements = departementDAO.getAllDepartments();
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            req.setAttribute("listeDepartements", listeDepartements);
            req.setAttribute("allEmployees", allEmployees);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }

        req.getRequestDispatcher("/GestionDepartement.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null || !user.hasRole(Role.ADMINISTRATOR)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                String nom = req.getParameter("nomDepartement");
                int idChef = Integer.parseInt(req.getParameter("idChefDepartement"));

                if (nom == null || nom.trim().isEmpty()) {
                    session.setAttribute("errorMessage", "Le nom du département est requis.");
                } else {

                    // --- LOGIQUE CORRIGÉE ---
                    // 1. D'ABORD, on libère le chef de son ancien poste (s'il en a un)
                    if (idChef > 0) {
                        departementDAO.removeAsChiefFromAnyDepartment(idChef);
                    }

                    // 2. ENSUITE, on crée le nouveau département
                    int newDeptId = departementDAO.createDepartment(nom, idChef);

                    if (idChef > 0) {
                        // 3. On assigne le rôle ET le département
                        employeeDAO.assignHeadDepartementRole(idChef);
                        employeeDAO.setEmployeeDepartment(idChef, newDeptId);
                    }
                    // --- FIN LOGIQUE ---

                    session.setAttribute("successMessage", "Département créé avec succès.");
                }

            } else if ("delete".equals(action)) {
                // ... (La logique de suppression est correcte) ...
                int id = Integer.parseInt(req.getParameter("deptId"));
                int oldChefId = 0;
                Departement deptASupprimer = departementDAO.findById(id);
                if (deptASupprimer != null) {
                    oldChefId = deptASupprimer.getIdChefDepartement();
                }
                departementDAO.deleteDepartment(id);
                if (oldChefId > 0) {
                    employeeDAO.checkAndRemoveHeadDepartementRole(oldChefId);
                }
                session.setAttribute("successMessage", "Département supprimé avec succès.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur SQL : " + e.getMessage());
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID invalide.");
        }

        resp.sendRedirect(req.getContextPath() + "/departements");
    }
}