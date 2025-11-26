package controller;

import dao.DepartementDAO;
import dao.EmployeeDAO;
import dao.ProjetDAO;
import model.Project;
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
            // 1. Récupérer l'objet département directement depuis l'utilisateur (Hibernate l'a chargé)
            // Plus besoin de departementDAO.findById(id) ici si l'objet est déjà dans user
            req.setAttribute("departement", user.getDepartement());

            // 2. Récupérer les projets de l'utilisateur
            List<Project> projects = projetDAO.getProjectsByEmployeeId(user.getId());
            req.setAttribute("projets", projects);

            // 3. Récupérer TOUS les départements pour le menu <select>
            List<Departement> allDepartments = departementDAO.getAllDepartments();
            req.setAttribute("allDepartments", allDepartments);

        } catch (Exception e) {
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
                String newEmail = req.getParameter("newEmail1");
                String confirmEmail = req.getParameter("newEmail2");
                String password = req.getParameter("currentPassword");

                if(newEmail != null && newEmail.equals(confirmEmail) && employeeDAO.checkPassword(user.getId(), password)) {
                    employeeDAO.updateEmail(user.getId(), newEmail);
                    user.setEmail(newEmail); // Mise à jour session
                    session.setAttribute("successMessage", "Email mis à jour.");
                } else {
                    session.setAttribute("errorMessage", "Erreur dans le formulaire email.");
                }

            } else if ("updatePassword".equals(action)) {
                String newPwd = req.getParameter("newPassword1");
                String confirmPwd = req.getParameter("newPassword2");
                String oldPwd = req.getParameter("oldPassword");

                if(newPwd != null && newPwd.equals(confirmPwd) && employeeDAO.checkPassword(user.getId(), oldPwd)) {
                    employeeDAO.updatePassword(user.getId(), newPwd);
                    user.setPassword(newPwd); // Mise à jour session
                    session.setAttribute("successMessage", "Mot de passe mis à jour.");
                } else {
                    session.setAttribute("errorMessage", "Erreur dans le formulaire mot de passe.");
                }

            } else if ("updateDepartment".equals(action)) {

                int newDeptId = Integer.parseInt(req.getParameter("idDepartement"));

                // 1. Mettre à jour la BDD (Le DAO gère l'int via getReference)
                employeeDAO.setEmployeeDepartment(user.getId(), newDeptId);

                // 2. (CORRECTION MAJEURE) Mettre à jour l'objet en SESSION
                // Comme Employee stocke un objet Departement, on doit le récupérer
                if (newDeptId > 0) {
                    Departement newDeptObj = departementDAO.findById(newDeptId);
                    user.setDepartement(newDeptObj);
                } else {
                    user.setDepartement(null);
                }

                session.setAttribute("currentUser", user); // Force la sauvegarde en session
                session.setAttribute("successMessage", "Département mis à jour avec succès.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur SQL : " + e.getMessage());
        }

        // On redirige vers le doGet (pattern PRG)
        resp.sendRedirect(req.getContextPath() + "/profil");
    }
}