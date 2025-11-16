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

// J'ai corrigé le 'name' de l'annotation pour qu'il corresponde à ta classe
@WebServlet(name = "GestionDepartementServlet", urlPatterns = "/departements")
public class GestionDepartementServlet extends HttpServlet {

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
        // ... (ton code doGet est correct) ...
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

        req.getRequestDispatcher("/GestionDepartement.jsp").forward(req, resp); // Corrigé: GestionDepartements.jsp
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
                int idChef = Integer.parseInt(req.getParameter("idChefDepartement"));

                if (nom == null || nom.trim().isEmpty()) {
                    session.setAttribute("errorMessage", "Le nom du département est requis.");
                } else {
                    departementDAO.createDepartment(nom, idChef);

                    // Assigner le rôle au nouveau chef
                    if (idChef > 0) {
                        employeeDAO.assignHeadDepartementRole(idChef);
                    }

                    session.setAttribute("successMessage", "Département créé avec succès.");
                }

            } else if ("delete".equals(action)) {

                // 1. Récupérer l'ID du département à supprimer
                int id = Integer.parseInt(req.getParameter("deptId"));

                // 2. (NOUVEAU) Avant de supprimer, trouver qui est le chef
                int oldChefId = 0;
                Departement deptASupprimer = departementDAO.findById(id);
                if (deptASupprimer != null) {
                    oldChefId = deptASupprimer.getIdChefDepartement();
                }

                // 3. Appeler le DAO pour supprimer
                departementDAO.deleteDepartment(id);

                // 4. (NOUVEAU) Vérifier le statut de l'ancien chef
                if (oldChefId > 0) {
                    employeeDAO.checkAndRemoveHeadDepartementRole(oldChefId);
                }

                // 5. Mettre un message de succès
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