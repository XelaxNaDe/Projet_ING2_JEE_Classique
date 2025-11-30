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

@WebServlet(name = "DetailEmployeServlet", urlPatterns = "/detail-employe")
public class DetailEmployeServlet extends HttpServlet {

    private EmployeeDAO employeeDAO;
    private DepartementDAO departementDAO;

    @Override
    public void init() {
        this.employeeDAO = new EmployeeDAO();
        this.departementDAO = new DepartementDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null ||
                !((Employee) session.getAttribute("currentUser")).hasRole(RoleEnum.ADMINISTRATOR)) {
            resp.sendRedirect(req.getContextPath() + "/accueil");
            return;
        }

        try {
            List<Departement> listeDepartements = departementDAO.getAllDepartments();
            req.setAttribute("listeDepartements", listeDepartements);

            String idStr = req.getParameter("id");
            if (idStr != null) {
                int id = Integer.parseInt(idStr);
                Employee emp = employeeDAO.findEmployeeById(id);
                if (emp != null) req.setAttribute("employe", emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }
        req.getRequestDispatcher("/DetailEmploye.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee loggedInUser = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (loggedInUser == null || !loggedInUser.hasRole(RoleEnum.ADMINISTRATOR)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        int employeeId = 0;

        int targetDeptId = Integer.parseInt(req.getParameter("idDepartement"));

        try {
            String fname = req.getParameter("fname");
            String sname = req.getParameter("sname");
            String gender = req.getParameter("gender");
            String position = req.getParameter("position");
            int idDepartement = Integer.parseInt(req.getParameter("idDepartement"));

            Departement deptObj = null;
            if (idDepartement > 0) {
                deptObj = departementDAO.findById(idDepartement);
            }

            Employee emp;

            if ("create".equals(action)) {
                String email = req.getParameter("email");
                String password = req.getParameter("password");

                if (password == null || password.isEmpty() || email == null || email.isEmpty()) {
                    session.setAttribute("errorMessage", "Email/Mot de passe requis.");
                    doGet(req, resp);
                    return;
                }

                emp = new Employee(fname, sname, gender, email, password, position, deptObj);
                employeeDAO.createEmployee(emp);

                Employee createdEmp = employeeDAO.findByEmailAndPassword(email, password);
                if(createdEmp != null) employeeId = createdEmp.getId();

                session.setAttribute("successMessage", "Employé créé.");

            } else if ("update".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                employeeId = id;
                Employee existingEmp = employeeDAO.findEmployeeById(id);

                if (existingEmp != null) {
                    emp = new Employee(fname, sname, gender, existingEmp.getEmail(), existingEmp.getPassword(), position, deptObj);
                    emp.setId(id);
                    employeeDAO.updateEmployee(emp);
                    session.setAttribute("successMessage", "Employé mis à jour.");
                }
            }

            if ("update".equals(action)) {
                int idToUpdate = Integer.parseInt(req.getParameter("id"));

                String deptDirige = departementDAO.getDepartmentNameIfChef(idToUpdate);

                Departement deptActuel = null;
                if (deptDirige != null) {
                    Employee existing = employeeDAO.findEmployeeById(idToUpdate);
                    if (existing.getDepartement() != null) {
                        deptActuel = existing.getDepartement();
                    }
                }

                if (deptDirige != null && (deptActuel == null || targetDeptId != deptActuel.getId())) {

                    session.setAttribute("errorMessage",
                            "Action impossible : " + fname + " " + sname +
                                    " est actuellement Chef du département '" + deptDirige +
                                    "'. Vous devez changer le chef de ce département avant de déplacer cet employé.");

                    resp.sendRedirect(req.getContextPath() + "/detail-employe?id=" + idToUpdate);
                    return;
                }

                Employee existingEmp = employeeDAO.findEmployeeById(idToUpdate);
                if (existingEmp != null) {
                    Departement targetDeptObj = null;
                    if (targetDeptId > 0) targetDeptObj = departementDAO.findById(targetDeptId);

                    emp = new Employee(fname, sname, gender, existingEmp.getEmail(), existingEmp.getPassword(), position, targetDeptObj);
                    emp.setId(idToUpdate);
                    employeeDAO.updateEmployee(emp);

                    session.setAttribute("successMessage", "Employé mis à jour.");
                }

                employeeId = idToUpdate;
            }

            if (employeeId > 0) {
                boolean isAdmin = req.getParameter("isAdmin") != null;
                employeeDAO.manageAdminRole(employeeId, isAdmin);
                if (loggedInUser.getId() == employeeId) {
                    session.setAttribute("currentUser", employeeDAO.findEmployeeById(employeeId));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur : " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/employes");
    }
}