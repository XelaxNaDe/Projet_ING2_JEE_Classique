package controller;

import dao.DepartementDAO;
import dao.EmployeeDAO;
import dao.ProjetDAO;
import model.Projet;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Departement;
import model.Employee;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ProfilServlet", urlPatterns = "/profil")
public class ProfilServlet extends HttpServlet {

    private DepartementDAO departementDAO;
    private EmployeeDAO employeeDAO;
    private ProjetDAO projetDAO;

    @Override
    public void init() {
        this.departementDAO = new DepartementDAO();
        this.employeeDAO = new EmployeeDAO();
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
            // 1. Récupérer le nom de son département
            Departement dept = departementDAO.findById(user.getIdDepartement());
            req.setAttribute("departement", dept);

            // 2. Récupérer les projets de l'utilisateur
            List<Projet> projets = projetDAO.getProjectsByEmployeeId(user.getId());
            req.setAttribute("projets", projets);

            // 3. Récupérer TOUS les départements pour le menu <select>
            List<Departement> allDepartments = departementDAO.getAllDepartments();
            req.setAttribute("allDepartments", allDepartments);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Impossible de charger les détails du profil.");
        }

        req.getRequestDispatcher("/Profil.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        String action = req.getParameter("action");

        try {
            if ("updateEmail".equals(action)) {
                // ... (Logique Email) ...

            } else if ("updatePassword".equals(action)) {
                // ... (Logique Mot de passe) ...

                // BLOC DE MISE À JOUR DU DÉPARTEMENT
            } else if ("updateDepartment".equals(action)) {

                int newDeptId = Integer.parseInt(req.getParameter("idDepartement"));

                // 1. Mettre à jour la BDD
                employeeDAO.setEmployeeDepartment(user.getId(), newDeptId);

                // 2. (CORRECTION) Mettre à jour l'objet en session
                user.setIdDepartement(newDeptId);
                session.setAttribute("currentUser", user); // Force la sauvegarde

                session.setAttribute("successMessage", "Département mis à jour avec succès.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur SQL : " + e.getMessage());
        }

        // On redirige vers le doGet (pattern PRG)
        resp.sendRedirect(req.getContextPath() + "/profil");
    }
}