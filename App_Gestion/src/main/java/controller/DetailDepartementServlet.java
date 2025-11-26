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
import model.utils.RoleEnum;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DetailDepartementServlet", urlPatterns = "/detail-departement")
public class DetailDepartementServlet extends HttpServlet {

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
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        try {
            String idStr = req.getParameter("id");
            if (idStr == null) {
                resp.sendRedirect(req.getContextPath() + "/departements");
                return;
            }
            int id = Integer.parseInt(idStr);

            Departement dept = departementDAO.findById(id);
            if (dept == null) {
                resp.sendRedirect(req.getContextPath() + "/departements");
                return;
            }
            req.setAttribute("departement", dept);

            // Liste complète pour le choix du chef
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            req.setAttribute("allEmployees", allEmployees);

            // Liste des membres actuels pour l'affichage du tableau
            List<Employee> assignedEmployees = employeeDAO.getEmployeesByDepartmentId(id);
            req.setAttribute("assignedEmployees", assignedEmployees);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/departements");
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur: " + e.getMessage());
        }

        req.getRequestDispatcher("/DetailDepartement.jsp").forward(req, resp);
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
        int idDept = 0;

        try {
            idDept = Integer.parseInt(req.getParameter("id"));

            // --- 1. MISE À JOUR DU DÉPARTEMENT (Admin uniquement) ---
            if ("update".equals(action) && user.hasRole(RoleEnum.ADMINISTRATOR)) {
                String nom = req.getParameter("nomDepartement");
                int newChefId = Integer.parseInt(req.getParameter("idChefDepartement"));

                // Récupérer l'ancien chef avant la modification pour gérer les rôles
                Departement currentDept = departementDAO.findById(idDept);
                Employee oldChef = currentDept.getChefDepartement();

                // A. Mise à jour de la table DEPARTEMENT
                departementDAO.updateDepartment(idDept, nom, newChefId);

                // B. Gestion des Rôles et de l'Affectation (Employee)
                // Si le chef a changé
                if (oldChef != null && oldChef.getId() != newChefId) {
                    // On retire le rôle "HEADDEPARTEMENT" à l'ancien chef
                    employeeDAO.checkAndRemoveHeadDepartementRole(oldChef.getId());
                }

                if (newChefId > 0) {
                    // On donne le rôle "HEADDEPARTEMENT" au nouveau chef
                    employeeDAO.assignHeadDepartementRole(newChefId);

                    // --- C'EST ICI QUE SE FAIT LA MAGIE ---
                    // "Le chef de département soit un membre du département"
                    // On force l'affectation de l'employé à ce département
                    employeeDAO.setEmployeeDepartment(newChefId, idDept);
                }

                session.setAttribute("successMessage", "Département mis à jour et chef affecté.");

                // --- 2. AJOUTER UN MEMBRE (Admin ou Chef de ce Dept) ---
            } else if ("assignEmployee".equals(action)) {
                int idEmploye = Integer.parseInt(req.getParameter("idEmploye"));

                // --- VERIFICATION ---
                String deptDirige = departementDAO.getDepartmentNameIfChef(idEmploye);

                // Si l'employé est chef, et que le département qu'il dirige n'est PAS celui actuel (idDept)
                // Note: departementDAO.findById(idDept).getNomDepartement() pour comparer les noms ou les IDs
                if (deptDirige != null) {
                    // On vérifie s'il est chef DU département actuel (ce qui serait bizarre ici car il serait déjà dedans, mais bon)
                    // Le cas critique : Il est chef de "RH" et on veut le mettre dans "Informatique"

                    // On charge l'employé pour voir son département actuel exact
                    Employee empCheck = employeeDAO.findEmployeeById(idEmploye);
                    int currentDeptId = (empCheck.getDepartement() != null) ? empCheck.getDepartement().getId() : 0;

                    if (currentDeptId != idDept) {
                        session.setAttribute("errorMessage",
                                "Impossible d'affecter cet employé : Il est actuellement Chef du département '" + deptDirige + "'.");
                        resp.sendRedirect(req.getContextPath() + "/detail-departement?id=" + idDept);
                        return;
                    }
                }
                // --------------------

                // Si tout va bien, on l'affecte
                employeeDAO.setEmployeeDepartment(idEmploye, idDept);
                session.setAttribute("successMessage", "Employé ajouté au département.");

                // --- 3. RETIRER UN MEMBRE (Admin ou Chef de ce Dept) ---
            } else if ("removeEmployee".equals(action)) {
                int idEmploye = Integer.parseInt(req.getParameter("idEmploye"));

                // On vérifie qu'on n'essaie pas de virer le chef lui-même !
                Departement currentDept = departementDAO.findById(idDept);
                if (currentDept.getChefDepartement() != null && currentDept.getChefDepartement().getId() == idEmploye) {
                    session.setAttribute("errorMessage", "Impossible de retirer le chef du département. Changez le chef d'abord.");
                } else {
                    // On retire l'employé (id_departement = null/0)
                    employeeDAO.setEmployeeDepartment(idEmploye, 0);
                    session.setAttribute("successMessage", "Employé retiré du département.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/detail-departement?id=" + idDept);
    }
}