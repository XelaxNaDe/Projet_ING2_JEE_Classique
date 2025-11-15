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
            
            // 1. Récupérer le département
            Departement dept = departementDAO.findById(id);
            if (dept == null) {
                resp.sendRedirect(req.getContextPath() + "/departements");
                return;
            }
            req.setAttribute("departement", dept);

            // 2. Récupérer tous les employés (pour le <select> du chef)
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            req.setAttribute("allEmployees", allEmployees);

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
     * Gère la mise à jour (UPDATE) du département.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        // Sécurité : Seul un Admin peut mettre à jour
        if (user == null || !user.hasRole(Role.ADMINISTRATOR)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        int id = Integer.parseInt(req.getParameter("id")); // ID du département à mettre à jour

        if ("update".equals(action)) {
            try {
                String nom = req.getParameter("nomDepartement");
                int idChef = Integer.parseInt(req.getParameter("idChefDepartement"));
                
                departementDAO.updateDepartment(id, nom, idChef);
                session.setAttribute("successMessage", "Département mis à jour avec succès.");
                
            } catch (SQLException e) {
                session.setAttribute("errorMessage", "Erreur SQL : " + e.getMessage());
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "Format d'ID invalide.");
            }
        }
        
        // On redirige vers le doGet de cette même servlet pour rafraîchir la page
        resp.sendRedirect(req.getContextPath() + "/detail-departement?id=" + id);
    }
}