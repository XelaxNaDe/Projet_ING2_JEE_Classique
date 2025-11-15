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

@WebServlet(name = "DetailProjetServlet", urlPatterns = "/detail-projet")
public class DetailProjetServlet extends HttpServlet {

    private ProjetDAO projetDAO;
    private EmployeeDAO employeeDAO;

    @Override
    public void init() {
        this.projetDAO = new ProjetDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Affiche la page de détail d'un projet.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/connexion.jsp");
            return;
        }

        try {
            int id = Integer.parseInt(req.getParameter("id"));

            // 1. Récupérer le projet
            Projet projet = projetDAO.getProjectById(id);
            if (projet == null) {
                resp.sendRedirect(req.getContextPath() + "/projets");
                return;
            }
            req.setAttribute("projet", projet);

            // 2. Récupérer tous les employés (pour le <select> du chef)
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            req.setAttribute("allEmployees", allEmployees);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/projets");
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }

        req.getRequestDispatcher("/DetailProjet.jsp").forward(req, resp);
    }

    /**
     * Gère la mise à jour (UPDATE) du projet.
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
        int idProjet = Integer.parseInt(req.getParameter("id"));

        if ("update".equals(action)) {
            try {
                // Récupérer tous les champs du formulaire
                String nom = req.getParameter("nomProjet");
                String etat = req.getParameter("etat");
                String dateDebutStr = req.getParameter("dateDebut");
                String dateFinStr = req.getParameter("dateFin");

                // Seul l'admin peut changer le chef, les autres gardent l'ancien
                int idChef;
                if (user.hasRole(Role.ADMINISTRATOR)) {
                    idChef = Integer.parseInt(req.getParameter("idChefProjet"));
                } else {
                    idChef = Integer.parseInt(req.getParameter("originalIdChefProjet")); // On utilise le champ caché
                }

                // Parser les dates
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateDebut = formatter.parse(dateDebutStr);
                Date dateFin = formatter.parse(dateFinStr);

                // Créer l'objet et mettre à jour
                Projet projet = new Projet(idProjet, nom, dateDebut, dateFin, idChef, etat);
                projetDAO.updateProject(projet);

                session.setAttribute("successMessage", "Projet mis à jour avec succès.");

            } catch (SQLException | ParseException | NumberFormatException e) {
                session.setAttribute("errorMessage", "Erreur lors de la mise à jour : " + e.getMessage());
            }
        }

        // Rediriger vers le doGet de cette servlet
        resp.sendRedirect(req.getContextPath() + "/detail-projet?id=" + idProjet);
    }
}