package controller;

import dao.DepartementDAO;
import dao.EmployeeDAO;
import dao.ProjetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Departement;
import model.Employee;
import model.Project;
import model.utils.RoleEnum;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "GestionEmployeeServlet", urlPatterns = "/employes")
public class GestionEmployeeServlet extends HttpServlet {

    private EmployeeDAO employeeDAO;
    private DepartementDAO departementDAO;
    private ProjetDAO projetDAO;

    @Override
    public void init() {
        this.employeeDAO = new EmployeeDAO();
        this.departementDAO = new DepartementDAO();
        this.projetDAO = new ProjetDAO();
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
            // 1. Récupération des filtres
            String filterPoste = req.getParameter("poste");
            String filterRole = req.getParameter("role");
            String searchPrenom = req.getParameter("search_prenom");
            String searchNom = req.getParameter("search_nom");
            String searchMatricule = req.getParameter("search_matricule");

            // NOUVEAU : Récupération des IDs pour Dept et Projet
            int searchDepartementId = 0;
            try { searchDepartementId = Integer.parseInt(req.getParameter("search_departement")); } catch (Exception e) {}

            int searchProjetId = 0;
            try { searchProjetId = Integer.parseInt(req.getParameter("search_projet")); } catch (Exception e) {}

            // 2. Logique filtre Projet (On récupère les IDs des employés du projet)
            List<Integer> projectEmployeeIds = null;
            if (searchProjetId > 0) {
                projectEmployeeIds = projetDAO.getEmployeeIdsByProject(searchProjetId);
            }

            // 3. APPEL DAO (Nouvelle signature)
            List<Employee> listeEmployes = employeeDAO.getAllEmployeesFull(
                    searchPrenom, searchNom, searchMatricule,
                    searchDepartementId, projectEmployeeIds,
                    filterPoste, filterRole
            );
            req.setAttribute("listeEmployes", listeEmployes);

            // 4. CHARGEMENT DES LISTES POUR LES DROPDOWNS
            List<String> postes = employeeDAO.getDistinctPostes();
            List<String> roles = employeeDAO.getDistinctRoles();
            List<Departement> allDepts = departementDAO.getAllDepartments(); // Pour le select
            List<Project> allProjets = projetDAO.getAllProjects();           // Pour le select

            req.setAttribute("postes", postes);
            req.setAttribute("roles", roles);
            req.setAttribute("listeDepartements", allDepts);
            req.setAttribute("listeProjets", allProjets);

            // 5. Renvoi des valeurs sélectionnées (pour garder le filtre actif)
            req.setAttribute("filterPoste", filterPoste);
            req.setAttribute("filterRole", filterRole);
            req.setAttribute("searchPrenom", searchPrenom);
            req.setAttribute("searchNom", searchNom);
            req.setAttribute("searchMatricule", searchMatricule);
            req.setAttribute("searchDepartementId", searchDepartementId);
            req.setAttribute("searchProjetId", searchProjetId);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur lors du chargement : " + e.getMessage());
        }

        req.getRequestDispatcher("/GestionEmployes.jsp").forward(req, resp);
    }

    /**
     * Gère la suppression d'un employé.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee loggedInUser = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (loggedInUser == null || !loggedInUser.hasRole(RoleEnum.ADMINISTRATOR)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");

        if ("delete".equals(action)) {
            try {
                int idToDelete = Integer.parseInt(req.getParameter("id"));

                // 1. Supprimer via Hibernate
                employeeDAO.deleteEmployee(idToDelete);
                session.setAttribute("successMessage", "Employé supprimé avec succès.");

                // 2. Si l'admin s'est auto-supprimé
                if (loggedInUser.getId() == idToDelete) {
                    session.invalidate();
                    resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
                    return;
                }

            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "ID invalide.");
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("errorMessage", "Erreur lors de la suppression : " + e.getMessage());
            }
        }

        resp.sendRedirect(req.getContextPath() + "/employes");

        super.doPost(req, resp);
    }
}