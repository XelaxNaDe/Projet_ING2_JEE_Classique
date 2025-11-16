package controller;



import dao.DepartementDAO;

import dao.EmployeeDAO;

import dao.RoleDAO; // 1. IMPORTER LE NOUVEAU DAO

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

import java.util.Map; // 2. IMPORTER MAP



@WebServlet(name = "DetailEmployeServlet", urlPatterns = "/detail-employe")

public class DetailEmployeServlet extends HttpServlet {



    private EmployeeDAO employeeDAO;

    private DepartementDAO departementDAO;

    private RoleDAO roleDAO; // 3. AJOUTER L'INSTANCE



    @Override

    public void init() {

        this.employeeDAO = new EmployeeDAO();

        this.departementDAO = new DepartementDAO();

        this.roleDAO = new RoleDAO(); // 4. INITIALISER

    }



    /**

     * Affiche le formulaire (soit vide pour "Ajouter", soit pré-rempli pour "Modifier").

     */

    @Override

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("currentUser") == null ||

                !((Employee) session.getAttribute("currentUser")).hasRole(Role.ADMINISTRATOR)) {

            resp.sendRedirect(req.getContextPath() + "/accueil"); // Corrigé: /accueil

            return;

        }



        try {

            // 5. CHARGER LES DÉPARTEMENTS ET LES RÔLES

            List<Departement> listeDepartements = departementDAO.getAllDepartments();

            Map<Integer, String> allRoles = roleDAO.getAllRoles();

            req.setAttribute("listeDepartements", listeDepartements);

            req.setAttribute("allRoles", allRoles); // Transmettre au JSP



            // Vérifier si on est en mode "Modifier" (un ID est passé)

            String idStr = req.getParameter("id");

            if (idStr != null) {

                int id = Integer.parseInt(idStr);

                Employee emp = employeeDAO.findEmployeeById(id);

                if (emp != null) {

                    req.setAttribute("employe", emp); // Passer l'employé à modifier

                }

            }

            // Si pas d'ID, "employe" reste null, c'est le mode "Ajouter"



        } catch (SQLException | NumberFormatException e) {

            e.printStackTrace();

            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());

        }



        req.getRequestDispatcher("/DetailEmploye.jsp").forward(req, resp);

    }



    /**

     * Gère la création (Create) ou la mise à jour (Update).

     */

    @Override

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        // On récupère l'utilisateur connecté AVANT

        Employee loggedInUser = (session != null) ? (Employee) session.getAttribute("currentUser") : null;



        if (loggedInUser == null || !loggedInUser.hasRole(Role.ADMINISTRATOR)) {

            resp.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;

        }



        String action = req.getParameter("action");

        int employeeId = 0; // Pour stocker l'ID de l'employé créé ou mis à jour



        try {

            // 1. Récupérer les champs (SANS "grade")

            String fname = req.getParameter("fname");

            String sname = req.getParameter("sname");

            String gender = req.getParameter("gender");

            String position = req.getParameter("position");

            int idDepartement = Integer.parseInt(req.getParameter("idDepartement"));



            Employee emp;



            if ("create".equals(action)) {

                String email = req.getParameter("email");

                String password = req.getParameter("password");



                if (password == null || password.isEmpty() || email == null || email.isEmpty()) {

                    session.setAttribute("errorMessage", "L'email et le mot de passe sont requis pour la création.");

                    doGet(req, resp); // Recharge le formulaire avec les listes

                    return;

                }



                emp = new Employee(fname, sname, gender, email, password, position, null, idDepartement);

                employeeDAO.createEmployee(emp);



                // Récupérer l'ID du nouvel employé

                Employee createdEmp = employeeDAO.findByEmailAndPassword(email, password);

                employeeId = createdEmp.getId();



                session.setAttribute("successMessage", "Employé créé avec succès.");



            } else if ("update".equals(action)) {

                int id = Integer.parseInt(req.getParameter("id"));

                employeeId = id; // On a l'ID



                Employee existingEmp = employeeDAO.findEmployeeById(id);

                if (existingEmp == null) { /* ... gestion erreur ... */ }



                emp = new Employee(fname, sname, gender, existingEmp.getEmail(), existingEmp.getPassword(),

                        position, existingEmp.getGrade(), idDepartement);

                emp.setId(id);



                employeeDAO.updateEmployee(emp);

                session.setAttribute("successMessage", "Employé mis à jour avec succès.");

            }



            // --- GESTION DES RÔLES (POUR CREATE ET UPDATE) ---

            if (employeeId > 0) {

                // D'abord, on supprime tous les anciens rôles

                employeeDAO.clearEmployeeRoles(employeeId);



                // Ensuite, on ajoute les nouveaux

                String[] selectedRoleIds = req.getParameterValues("roles");



                if (selectedRoleIds != null) {

                    for (String roleIdStr : selectedRoleIds) {

                        int roleId = Integer.parseInt(roleIdStr);

                        employeeDAO.addEmployeeRole(employeeId, roleId);

                    }

                }



                // ***** BLOC DE CORRECTION AJOUTÉ *****

                // Si l'admin vient de modifier SON PROPRE profil...

                if (loggedInUser.getId() == employeeId) {

                    // ...on doit recharger son objet depuis la BDD

                    // pour mettre à jour ses droits en session.

                    Employee updatedSelf = employeeDAO.findEmployeeById(employeeId);

                    session.setAttribute("currentUser", updatedSelf);

                }

                // ***** FIN DU BLOC *****

            }



        } catch (SQLException | NumberFormatException e) {

            e.printStackTrace();

            session.setAttribute("errorMessage", "Erreur SQL : " + e.getMessage());

        }



        resp.sendRedirect(req.getContextPath() + "/employes");

    }

}