package controller;

// Imports NÉCESSAIRES pour le DAO et le modèle
import dao.ProjetDAO;
import dao.EmployeeDAO; // 1. IMPORTER LE DAO DES EMPLOYÉS
import model.Projet;
import java.util.List;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
// Fin des imports nécessaires

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import model.utils.Role;

import java.io.IOException;

@WebServlet(name = "GestionProjetServlet", urlPatterns = "/projets")
public class GestionProjetServlet extends HttpServlet {

    private ProjetDAO projetDAO;
    private EmployeeDAO employeeDAO; // 2. AJOUTER L'INSTANCE DU DAO

    @Override
    public void init() {
        // On initialise les DAO une seule fois au démarrage de la servlet
        this.projetDAO = new ProjetDAO();
        this.employeeDAO = new EmployeeDAO(); // 3. INITIALISER LE DAO
    }

    /**
     * Gère l'affichage de la page (accès direct ou redirection après POST).
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
            // 4. RÉCUPÉRER LES DEUX LISTES
            List<Projet> listeProjets = projetDAO.getAllProjects();
            List<Employee> allEmployees = employeeDAO.getAllEmployees(); // Pour le <select>

            // 5. METTRE LES DEUX LISTES DANS LA REQUÊTE
            req.setAttribute("listeProjets", listeProjets);
            req.setAttribute("allEmployees", allEmployees); // Transmettre au JSP

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD lors de la récupération des projets: " + e.getMessage());
        }

        // 4. Afficher la page JSP (forward)
        req.getRequestDispatcher("/GestionProjet.jsp").forward(req, resp);
    }

    /**
     * Gère la création de projet depuis le formulaire (méthode POST).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null || !user.hasRole(Role.ADMINISTRATOR)) {
            resp.sendRedirect(req.getContextPath() + "/connexion.jsp");
            return;
        }

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                // 1. Récupérer les données
                String projectName = req.getParameter("projectName");
                String dateDebutStr = req.getParameter("dateDebut");
                String dateFinStr = req.getParameter("dateFin");

                // ... (validation) ...
                if (projectName == null || projectName.trim().isEmpty() || dateDebutStr == null || dateFinStr.isEmpty() || dateFinStr == null || dateFinStr.isEmpty()) {
                    session.setAttribute("errorMessage", "Tous les champs sont obligatoires.");
                    resp.sendRedirect(req.getContextPath() + "/projets");
                    return;
                }

                // 2. Logique Métier
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateDebut = formatter.parse(dateDebutStr);
                Date dateFin = formatter.parse(dateFinStr);

                // 6. MODIFICATION : On lit le chef de projet depuis le formulaire
                int idChefProjet = Integer.parseInt(req.getParameter("idChefProjet"));

                Projet nouveauProjet = new Projet(projectName, dateDebut, dateFin, idChefProjet, "En cours");
                projetDAO.createProject(nouveauProjet);

                session.setAttribute("successMessage", "Projet '" + projectName + "' créé avec succès!");

            } else if ("delete".equals(action)) {
                // 1. Récupérer l'ID du projet à supprimer
                int idProjet = Integer.parseInt(req.getParameter("projectId"));

                // 2. Appeler le DAO pour supprimer
                projetDAO.deleteProject(idProjet);

                // 3. Mettre un message de succès
                session.setAttribute("successMessage", "Projet (ID: " + idProjet + ") supprimé avec succès!");

            } else {
                // Gérer les actions inconnues
                session.setAttribute("errorMessage", "Action non reconnue.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur SQL : " + e.getMessage());
        } catch (ParseException e) { // Pour le "create"
            e.printStackTrace();
            session.setAttribute("errorMessage", "Format de date invalide.");
        } catch (NumberFormatException e) { // Pour le "create" (idChefProjet) ou "delete" (projectId)
            e.printStackTrace();
            session.setAttribute("errorMessage", "ID invalide.");
        }

        // Redirection PRG (Post/Redirect/Get) à la fin
        resp.sendRedirect(req.getContextPath() + "/projets");
    }
}