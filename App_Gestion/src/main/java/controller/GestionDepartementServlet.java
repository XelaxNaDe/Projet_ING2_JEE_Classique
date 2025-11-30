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

@WebServlet(name = "GestionDepartementServlet", urlPatterns = "/departements")
public class GestionDepartementServlet extends HttpServlet {

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
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        try {
            List<Departement> listeDepartements = departementDAO.getAllDepartments();
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            req.setAttribute("listeDepartements", listeDepartements);
            req.setAttribute("allEmployees", allEmployees);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }

        req.getRequestDispatcher("/GestionDepartement.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null || !user.hasRole(RoleEnum.ADMINISTRATOR)) {
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
                    if (idChef > 0) {
                        departementDAO.removeAsChiefFromAnyDepartment(idChef);
                    }

                    int newDeptId = departementDAO.createDepartment(nom, idChef);
                    if (idChef > 0) {
                        employeeDAO.assignHeadDepartementRole(idChef);
                        employeeDAO.setEmployeeDepartment(idChef, newDeptId);
                    }

                    session.setAttribute("successMessage", "Département créé avec succès.");
                }

            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("deptId"));
                Employee oldChef = null;
                Departement deptASupprimer = departementDAO.findById(id);

                if (deptASupprimer != null) {
                    oldChef = deptASupprimer.getChefDepartement();
                }
                departementDAO.deleteDepartment(id);

                if (oldChef != null) {
                    employeeDAO.checkAndRemoveHeadDepartementRole(oldChef.getId());
                }

                session.setAttribute("successMessage", "Département supprimé avec succès.");
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID invalide.");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur lors de l'opération : " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/departements");
    }
}