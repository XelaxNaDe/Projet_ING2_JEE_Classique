package controller;

import dao.EmployeeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import model.utils.Role;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "GestionEmployeServlet", urlPatterns = "/employes")
public class GestionEmployeServlet extends HttpServlet {

    private EmployeeDAO employeeDAO;

    @Override
    public void init() {
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Affiche la liste des employés.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/connexion.jsp");
            return;
        }

        try {
            // 1. RÉCUPÉRER LES FILTRES (Listes)
            String filterPoste = req.getParameter("poste");
            String filterRole = req.getParameter("role"); // MODIFIÉ

            // 2. RÉCUPÉRER LES RECHERCHES (Champs texte)
            String searchPrenom = req.getParameter("search_prenom");
            String searchNom = req.getParameter("search_nom");
            String searchMatricule = req.getParameter("search_matricule");
            String searchDepartement = req.getParameter("search_departement");

            // 3. PASSER TOUS LES FILTRES AU DAO (MODIFIÉ)
            List<Employee> listeEmployes = employeeDAO.getAllEmployeesFull(
                    searchPrenom, searchNom, searchMatricule, searchDepartement,
                    filterPoste, filterRole // MODIFIÉ
            );
            req.setAttribute("listeEmployes", listeEmployes);

            // 4. RÉCUPÉRER LES LISTES POUR LES DROPDOWNS (MODIFIÉ)
            List<String> postes = employeeDAO.getDistinctPostes();
            List<String> roles = employeeDAO.getDistinctRoles(); // MODIFIÉ
            req.setAttribute("postes", postes);
            req.setAttribute("roles", roles); // MODIFIÉ

            // 5. RENVOYER TOUS LES FILTRES/RECHERCHES ACTIFS AU JSP
            req.setAttribute("filterPoste", filterPoste);
            req.setAttribute("filterRole", filterRole); // MODIFIÉ
            req.setAttribute("searchPrenom", searchPrenom);
            req.setAttribute("searchNom", searchNom);
            req.setAttribute("searchMatricule", searchMatricule);
            req.setAttribute("searchDepartement", searchDepartement);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }

        req.getRequestDispatcher("/GestionEmployes.jsp").forward(req, resp);
    }

    /**
     * Gère la suppression d'un employé.
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

        if ("delete".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                employeeDAO.deleteEmployee(id);
                session.setAttribute("successMessage", "Employé supprimé avec succès.");
            } catch (SQLException e) {
                session.setAttribute("errorMessage", "Erreur SQL : " + e.getMessage());
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "ID invalide.");
            }
        }
        
        resp.sendRedirect(req.getContextPath() + "/employes");
    }
}