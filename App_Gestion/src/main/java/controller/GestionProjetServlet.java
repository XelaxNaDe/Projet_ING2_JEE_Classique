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
import model.Project;
import model.utils.RoleEnum;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet(name = "GestionProjetServlet", urlPatterns = "/projects")
public class GestionProjetServlet extends HttpServlet {

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
            List<Project> listeProjects = projetDAO.getAllProjects();
            List<Employee> allEmployees = employeeDAO.getAllEmployees();

            req.setAttribute("listeProjets", listeProjects);
            req.setAttribute("allEmployees", allEmployees);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        req.getRequestDispatcher("/GestionProjet.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null || !user.hasRole(RoleEnum.ADMINISTRATOR)) {
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                String projectName = req.getParameter("projectName");
                String dateDebutStr = req.getParameter("dateDebut");
                String dateFinStr = req.getParameter("dateFin");

                // 1. Récupérer l'ID du chef
                int idChefProjet = 0;
                try {
                    idChefProjet = Integer.parseInt(req.getParameter("idChefProjet"));
                } catch (NumberFormatException ignored) {}

                if (projectName == null || projectName.trim().isEmpty() || dateDebutStr == null || dateFinStr.isEmpty()) {
                    session.setAttribute("errorMessage", "Tous les champs sont obligatoires.");
                    resp.sendRedirect(req.getContextPath() + "/projects");
                    return;
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateDebut = formatter.parse(dateDebutStr);
                Date dateFin = formatter.parse(dateFinStr);

                // 2. Charger l'objet Chef (Hibernate requirement)
                Employee chefObj = null;
                if (idChefProjet > 0) {
                    chefObj = employeeDAO.findEmployeeById(idChefProjet);
                }

                // 3. Créer le projet avec l'objet
                Project nouveauProject = new Project(projectName, dateDebut, dateFin, chefObj, "En cours");

                // 4. Persister (utilisera merge dans le DAO)
                int newProjetId = projetDAO.createProject(nouveauProject);

                if (idChefProjet > 0 && newProjetId > 0) {
                    employeeDAO.assignProjectManagerRole(idChefProjet);
                    projetDAO.assignEmployeeToProject(newProjetId, idChefProjet, "Chef de Projet");
                }

                session.setAttribute("successMessage", "Projet '" + projectName + "' créé avec succès!");

            } else if ("delete".equals(action)) {
                int idProjet = Integer.parseInt(req.getParameter("projectId"));
                int oldChefId = 0;

                Project projectASupprimer = projetDAO.getProjectById(idProjet);

                // Récupération sécurisée de l'ID de l'ancien chef
                if (projectASupprimer != null && projectASupprimer.getChefProjet() != null) {
                    oldChefId = projectASupprimer.getChefProjet().getId();
                }

                projetDAO.deleteProject(idProjet);

                if (oldChefId > 0) {
                    employeeDAO.checkAndRemoveProjectManagerRole(oldChefId);
                }
                session.setAttribute("successMessage", "Projet supprimé avec succès!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur lors de l'opération : " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/projects");
    }
}