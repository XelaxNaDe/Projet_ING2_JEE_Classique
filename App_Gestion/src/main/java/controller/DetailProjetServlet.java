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
import java.util.Map;

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
            Project project = projetDAO.getProjectById(id);
            if (project == null) {
                resp.sendRedirect(req.getContextPath() + "/projects");
                return;
            }
            req.setAttribute("projet", project);

            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            req.setAttribute("allEmployees", allEmployees);

            Map<Employee, String> assignedTeam = projetDAO.getAssignedEmployees(id);
            req.setAttribute("assignedTeam", assignedTeam);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/projects");
            return;
        } catch (Exception e) {
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
        int idProjet;
        try {
            idProjet = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/projects");
            return;
        }

        boolean canModify = false;
        Employee oldChef = null;
        try {
            Project project = projetDAO.getProjectById(idProjet);
            if (project != null) {
                oldChef = project.getChefProjet();

                boolean isAdmin = user.hasRole(RoleEnum.ADMINISTRATOR);
                boolean isProjectManager = user.hasRole(RoleEnum.PROJECTMANAGER);
                boolean isThisProjectsManager = false;

                if (isProjectManager && oldChef != null) {
                    isThisProjectsManager = (user.getId() == oldChef.getId());
                }
                canModify = isAdmin || isThisProjectsManager;
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (!canModify) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            if ("update".equals(action)) {
                String nom = req.getParameter("nomProjet");
                String etat = req.getParameter("etat");
                String dateDebutStr = req.getParameter("dateDebut");
                String dateFinStr = req.getParameter("dateFin");

                int newChefId = 0;
                if (user.hasRole(RoleEnum.ADMINISTRATOR)) {
                    try { newChefId = Integer.parseInt(req.getParameter("idChefProjet")); } catch (Exception e) {}
                } else {
                    newChefId = (oldChef != null) ? oldChef.getId() : 0;
                }

                Employee newChefEmployee = null;
                if (newChefId > 0) newChefEmployee = employeeDAO.findEmployeeById(newChefId);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateDebut = formatter.parse(dateDebutStr);
                Date dateFin = formatter.parse(dateFinStr);

                Project projectMisAJour = new Project(nom, dateDebut, dateFin, newChefEmployee, etat);
                projectMisAJour.setIdProjet(idProjet);
                projetDAO.updateProject(projectMisAJour);

                int oldChefId = (oldChef != null) ? oldChef.getId() : 0;

                if (newChefId != oldChefId && user.hasRole(RoleEnum.ADMINISTRATOR)) {
                    if (oldChefId > 0) employeeDAO.checkAndRemoveProjectManagerRole(oldChefId);
                    if (newChefId > 0) employeeDAO.assignProjectManagerRole(newChefId);

                    if (newChefId > 0) {
                        projetDAO.assignEmployeeToProject(idProjet, newChefId, "Chef de Projet");
                    }
                    if (oldChefId > 0) {
                        projetDAO.removeEmployeeFromProject(idProjet, oldChefId);
                    }
                }

                session.setAttribute("successMessage", "Projet mis à jour.");

            } else if ("assignEmployee".equals(action)) {
                int idEmploye = Integer.parseInt(req.getParameter("idEmploye"));
                String roleDansProjet = req.getParameter("role_dans_projet");

                projetDAO.assignEmployeeToProject(idProjet, idEmploye, roleDansProjet);
                session.setAttribute("successMessage", "Employé affecté au projet.");

            } else if ("removeEmployee".equals(action)) {
                int idEmploye = Integer.parseInt(req.getParameter("idEmploye"));

                Project currentProject = projetDAO.getProjectById(idProjet);
                boolean isChef = (currentProject.getChefProjet() != null && currentProject.getChefProjet().getId() == idEmploye);

                if (isChef) {
                    session.setAttribute("errorMessage", "Impossible de retirer cet employé : c'est le Chef de Projet actuel. Changez le chef avant de le retirer.");
                } else {
                    projetDAO.removeEmployeeFromProject(idProjet, idEmploye);
                    session.setAttribute("successMessage", "Employé retiré du projet.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/detail-projet?id=" + idProjet);
    }
}