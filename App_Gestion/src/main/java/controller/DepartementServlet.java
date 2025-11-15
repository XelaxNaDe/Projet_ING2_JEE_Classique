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

@WebServlet(name = "DepartementServlet", urlPatterns = "/departements")
public class DepartementServlet extends HttpServlet {

    private DepartementDAO departementDAO;
    private EmployeeDAO employeeDAO;

    @Override
    public void init() {

        this.departementDAO = new DepartementDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Affiche la page de gestion des départements (POUR TOUS LES UTILISATEURS)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        try {
            // 4. RÉCUPÉRER LES DEUX LISTES
            List<Departement> listeDepartements = departementDAO.getAllDepartments();
            List<Employee> allEmployees = employeeDAO.getAllEmployees(); // Pour le <select>

            // 5. METTRE LES DEUX LISTES DANS LA REQUÊTE
            req.setAttribute("listeDepartements", listeDepartements);
            req.setAttribute("allEmployees", allEmployees); // Transmettre au JSP

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }

        req.getRequestDispatcher("/GestionDepartement.jsp").forward(req, resp);
    }

    /**
     * Gère la CRÉATION de départements (Admin seulement)
     */
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

                // 6. RÉCUPÉRER L'ID DU CHEF DEPUIS LE FORMULAIRE
                int idChef = Integer.parseInt(req.getParameter("idChefDepartement"));

                if (nom == null || nom.trim().isEmpty()) {
                    session.setAttribute("errorMessage", "Le nom du département est requis.");
                } else {
                    // 7. APPELER LE DAO MIS À JOUR
                    departementDAO.createDepartment(nom, idChef);
                    session.setAttribute("successMessage", "Département créé avec succès.");
                }

            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("deptId"));
                departementDAO.deleteDepartment(id);
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
