package controller;

import dao.EmployeeDAO;
import dao.ProjetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import model.Projet;
import model.utils.Role;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map; // IMPORTER MAP

@WebServlet(name = "DetailProjetServlet", urlPatterns = "/detail-projet")
public class DetailProjetServlet extends HttpServlet {

    private ProjetDAO projetDAO;
    private EmployeeDAO employeeDAO;

    @Override
    public void init() {
        this.projetDAO = new ProjetDAO();
        this.employeeDAO = new EmployeeDAO();
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
            int id = Integer.parseInt(req.getParameter("id"));
            Projet projet = projetDAO.getProjectById(id);
            if (projet == null) {
                resp.sendRedirect(req.getContextPath() + "/projets");
                return;
            }
            req.setAttribute("projet", projet);

            // Récupérer la liste de TOUS les employés (pour le menu <select>)
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            req.setAttribute("allEmployees", allEmployees);

            // NOUVEAU : Récupérer l'équipe AFFECTÉE au projet
            Map<Employee, String> assignedTeam = projetDAO.getAssignedEmployees(id);
            req.setAttribute("assignedTeam", assignedTeam);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/projets");
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }

        req.getRequestDispatcher("/DetailProjet.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        int idProjet = Integer.parseInt(req.getParameter("id"));

        // --- Vérification des permissions (inchangée) ---
        boolean canModify = false;
        int oldChefId = 0;
        try {
            Projet projet = projetDAO.getProjectById(idProjet);
            if (projet == null) { /* ... gestion erreur ... */ }
            oldChefId = projet.getIdChefProjet();

            boolean isAdmin = user.hasRole(Role.ADMINISTRATOR);
            boolean isProjectManager = user.hasRole(Role.PROJECTMANAGER);
            boolean isThisProjectsManager = (isProjectManager && user.getId() == projet.getIdChefProjet());

            canModify = isAdmin || isThisProjectsManager;
        } catch (SQLException e) { /* ... gestion erreur ... */ }
        if (!canModify) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // --- Fin de la vérification ---

        try {
            if ("update".equals(action)) {
                String nom = req.getParameter("nomProjet");
                String etat = req.getParameter("etat");
                String dateDebutStr = req.getParameter("dateDebut");
                String dateFinStr = req.getParameter("dateFin");
                int newChefId = user.hasRole(Role.ADMINISTRATOR) ? Integer.parseInt(req.getParameter("idChefProjet")) : oldChefId;

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateDebut = formatter.parse(dateDebutStr);
                Date dateFin = formatter.parse(dateFinStr);

                Projet projetMisAJour = new Projet(idProjet, nom, dateDebut, dateFin, newChefId, etat);
                projetDAO.updateProject(projetMisAJour);

                // --- LOGIQUE DES RÔLES ET AFFECTATIONS (MODIFIÉE) ---
                if (newChefId != oldChefId && user.hasRole(Role.ADMINISTRATOR)) {
                    // 1. Gérer les rôles (promotion/rétrogradation)
                    if (newChefId > 0) employeeDAO.assignProjectManagerRole(newChefId);
                    if (oldChefId > 0) employeeDAO.checkAndRemoveProjectManagerRole(oldChefId);

                    // 2. Gérer l'affectation à l'équipe du projet
                    if (newChefId > 0) {
                        projetDAO.assignEmployeeToProject(idProjet, newChefId, "Chef de Projet");
                    }
                    if (oldChefId > 0) {
                        projetDAO.removeEmployeeFromProject(idProjet, oldChefId);
                    }
                }
                // --- FIN LOGIQUE ---

                session.setAttribute("successMessage", "Projet mis à jour.");

            } else if ("assignEmployee".equals(action)) {
                int idEmploye = Integer.parseInt(req.getParameter("idEmploye"));
                String roleDansProjet = req.getParameter("role_dans_projet");

                projetDAO.assignEmployeeToProject(idProjet, idEmploye, roleDansProjet);
                session.setAttribute("successMessage", "Employé affecté au projet.");

            } else if ("removeEmployee".equals(action)) {
                int idEmploye = Integer.parseInt(req.getParameter("idEmploye"));

                projetDAO.removeEmployeeFromProject(idProjet, idEmploye);
                session.setAttribute("successMessage", "Employé retiré du projet.");
            }

        } catch (SQLException | ParseException | NumberFormatException e) {
            session.setAttribute("errorMessage", "Erreur lors de l'opération : " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/detail-projet?id=" + idProjet);
    }
}