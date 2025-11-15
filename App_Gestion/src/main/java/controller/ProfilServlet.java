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

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ProfilServlet", urlPatterns = "/profil")
public class ProfilServlet extends HttpServlet {

    private DepartementDAO departementDAO;
    private EmployeeDAO employeeDAO;

    @Override
    public void init() {
        this.departementDAO = new DepartementDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/connexion.jsp");
            return;
        }

        try {
            // 1. L'utilisateur est déjà dans la session (il est complet grâce au login)
            // 2. On doit juste récupérer le nom de son département
            Departement dept = departementDAO.findById(user.getIdDepartement());
            
            // 3. On passe le département au JSP
            req.setAttribute("departement", dept); 
            
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Impossible de charger le département.");
        }

        // L'utilisateur (currentUser) est déjà dispo pour le JSP via la session
        req.getRequestDispatcher("/Profil.jsp").forward(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/connexion.jsp");
            return;
        }

        // On utilise un champ "action" pour savoir quel formulaire a été soumis
        String action = req.getParameter("action");

        try {
            // --- Logique pour changer l'Email ---
            if ("updateEmail".equals(action)) {
                String newEmail1 = req.getParameter("newEmail1");
                String newEmail2 = req.getParameter("newEmail2");
                String password = req.getParameter("currentPassword");

                if (!newEmail1.equals(newEmail2)) {
                    session.setAttribute("errorMessage", "Les nouveaux emails ne correspondent pas.");
                } else if (!employeeDAO.checkPassword(user.getId(), password)) {
                    session.setAttribute("errorMessage", "Mot de passe actuel incorrect.");
                } else {
                    employeeDAO.updateEmail(user.getId(), newEmail1);
                    user.setEmail(newEmail1); // Mettre à jour l'objet en session !
                    session.setAttribute("currentUser", user);
                    session.setAttribute("successMessage", "Email mis à jour avec succès.");
                }

                // --- Logique pour changer le Mot de Passe ---
            } else if ("updatePassword".equals(action)) {
                String oldPassword = req.getParameter("oldPassword");
                String newPassword1 = req.getParameter("newPassword1");
                String newPassword2 = req.getParameter("newPassword2");

                if (!newPassword1.equals(newPassword2)) {
                    session.setAttribute("errorMessage", "Les nouveaux mots de passe ne correspondent pas.");
                } else if (!employeeDAO.checkPassword(user.getId(), oldPassword)) {
                    session.setAttribute("errorMessage", "Ancien mot de passe incorrect.");
                } else {
                    employeeDAO.updatePassword(user.getId(), newPassword1);
                    user.setPassword(newPassword1); // Mettre à jour l'objet en session !
                    session.setAttribute("currentUser", user);
                    session.setAttribute("successMessage", "Mot de passe mis à jour avec succès.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur SQL : " + e.getMessage());
        }

        // On redirige vers le doGet (pattern PRG)
        resp.sendRedirect(req.getContextPath() + "/profil");
    }
}
