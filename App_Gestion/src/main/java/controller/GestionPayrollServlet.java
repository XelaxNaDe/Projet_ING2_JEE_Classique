package controller;

import dao.PayrollDAO;
import dao.EmployeeDAO;
import model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Payroll;
import model.utils.IntStringPayroll;
import model.utils.Role;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@WebServlet(name = "GestionPayrollServlet", urlPatterns = "/payroll")
public class GestionPayrollServlet extends HttpServlet {

    private PayrollDAO payrollDAO;
    private EmployeeDAO employeeDAO;

    @Override
    public void init(){
        payrollDAO = new PayrollDAO();
        employeeDAO = new EmployeeDAO();
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
            // Récupération des paramètres de recherche
            String searchId = req.getParameter("search_employee");
            String dateDebut = req.getParameter("date_debut");
            String dateFin = req.getParameter("date_fin");

            List<Payroll> listePayrolls;

            // Si au moins un critère est présent, on fait une recherche
            boolean hasSearchCriteria = (searchId != null && !searchId.isEmpty()) ||
                    (dateDebut != null && !dateDebut.isEmpty()) ||
                    (dateFin != null && !dateFin.isEmpty());

            if (hasSearchCriteria) {
                listePayrolls = payrollDAO.searchPayrolls(searchId, dateDebut, dateFin);
            } else {
                // Sinon, on affiche tout
                listePayrolls = payrollDAO.getAllPayrolls();
            }

            // Récupérer tous les employés pour le menu déroulant de recherche
            List<Employee> allEmployees = employeeDAO.getAllEmployees();

            req.setAttribute("listePayrolls", listePayrolls);
            req.setAttribute("allEmployees", allEmployees);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur BDD : " + e.getMessage());
        }

        req.getRequestDispatcher("/GestionPayroll.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null || !user.hasRole(Role.ADMINISTRATOR)) {
            // Seuls les administrateurs peuvent effectuer des actions POST (création/suppression)
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");

        try{
            if ("create".equals(action)){
                // --- 1. Récupération des données ---
                int idEmployee = Integer.parseInt(req.getParameter("id_employee"));
                String dateStr = req.getParameter("date");
                int salary = Integer.parseInt(req.getParameter("salary"));

                // NOUVEAU : Récupération du NetPay envoyé par le formulaire JSP
                double netPay = Double.parseDouble(req.getParameter("netPay"));

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = formatter.parse(dateStr);
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                // --- 2. Création de la fiche de paie avec le NetPay ---
                // Appel de la méthode DAO modifiée qui insère directement le netPay
                int idPay = payrollDAO.createPayroll(idEmployee, sqlDate, salary, netPay);

                // --- 3. Création des lignes de primes/déductions (IntStringPayroll) ---
                String[] reqAmount = req.getParameterValues("Amount");
                String[] reqLabel = req.getParameterValues("Label");
                String[] reqType = req.getParameterValues("Type");

                int lineCount = (reqAmount != null) ? reqAmount.length : 0;

                for( int i = 0; i < lineCount; i++){
                    try {
                        int amount = Integer.parseInt(reqAmount[i]);
                        String label = (reqLabel != null && i < reqLabel.length) ? reqLabel[i] : "";
                        String type = (reqType != null && i < reqType.length) ? reqType[i] : "Prime";

                        payrollDAO.createPayrollLine(idPay, amount, label, type);
                    } catch (NumberFormatException e) {
                        // Ignorer les erreurs de parsing
                    }
                }

                // NOTE : On n'appelle PLUS payrollDAO.createPayrollNetPay(...) car le netPay est déjà inséré !

                session.setAttribute("successMessage", "Fiche de paie (ID: " + idPay + ") créée avec succès!");
            } else if ("update".equals(action)) {
                // 1. Récupérer les données de base
                int idPayroll = Integer.parseInt(req.getParameter("id_payroll"));
                int idEmployee = Integer.parseInt(req.getParameter("id_employee"));
                String dateStr = req.getParameter("date");
                int salary = Integer.parseInt(req.getParameter("salary"));
                double netPay = Double.parseDouble(req.getParameter("netPay"));

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                java.sql.Date sqlDate = new java.sql.Date(formatter.parse(dateStr).getTime());

                // 2. Mettre à jour l'objet Payroll principal
                Payroll payroll = new Payroll();
                payroll.setId(idPayroll); // ID de la fiche
                Employee emp = new Employee(); emp.setId(idEmployee); // Juste pour passer l'ID
                // Note: Le constructeur de Payroll prend un Employee complet, mais pour l'update DAO on a souvent juste besoin de l'ID employé.
                // Vérifiez votre méthode payrollDAO.updatePayroll. Si elle utilise payroll.getEmployeeId(), ça va marcher.
                // Sinon, créez un objet Payroll temporaire adapté.

                // Appel direct DAO update (à adapter selon votre DAO existant)
                // payrollDAO.updatePayroll(idPayroll, idEmployee, sqlDate, salary, netPay);
                // Si votre DAO utilise l'objet :
                Payroll pUpdate = new Payroll(idPayroll, emp, sqlDate.toLocalDate(), salary, netPay);
                payrollDAO.updatePayroll(pUpdate);


                // 3. Gérer les lignes (Stratégie simple : Tout supprimer et recréer)
                // D'abord, on supprime les anciennes lignes de cette payroll
                // (Nécessite une méthode deleteLinesByPayrollId dans le DAO, ou une boucle de suppression)
                List<IntStringPayroll> oldBonuses = payrollDAO.getAllPayrollsDetails(idPayroll, "Prime");
                List<IntStringPayroll> oldDeductions = payrollDAO.getAllPayrollsDetails(idPayroll, "Déduction");

                for(IntStringPayroll line : oldBonuses) payrollDAO.deletePayrollLine(line.getId_line());
                for(IntStringPayroll line : oldDeductions) payrollDAO.deletePayrollLine(line.getId_line());

                // Ensuite, on recrée les nouvelles
                String[] reqAmount = req.getParameterValues("Amount");
                String[] reqLabel = req.getParameterValues("Label");
                String[] reqType = req.getParameterValues("Type");
                int lineCount = (reqAmount != null) ? reqAmount.length : 0;

                for(int i = 0; i < lineCount; i++){
                    try {
                        int amount = Integer.parseInt(reqAmount[i]);
                        String label = (reqLabel != null) ? reqLabel[i] : "";
                        String type = (reqType != null) ? reqType[i] : "Prime";
                        payrollDAO.createPayrollLine(idPayroll, amount, label, type);
                    } catch (NumberFormatException e) {}
                }

                session.setAttribute("successMessage", "Fiche de paie mise à jour avec succès.");

            } else if ("delete".equals(action)) {

                String idPayrollStr = req.getParameter("IdPayroll");
                String idISPayrollStr = req.getParameter("IdISPayroll"); // Ligne individuelle

                if (idPayrollStr != null && !idPayrollStr.isEmpty()){
                    // Suppression d'une fiche de paie complète
                    int idPayroll = Integer.parseInt(idPayrollStr);
                    payrollDAO.deletePayroll(idPayroll);
                    // Grâce au ON DELETE CASCADE, toutes les lignes IntStringPayroll associées sont supprimées.
                    session.setAttribute("successMessage", "La fiche de paie (ID: " + idPayroll + ") supprimée avec succès!");

                } else if(idISPayrollStr != null && !idISPayrollStr.isEmpty()){
                    // Suppression d'une ligne de prime/déduction individuelle
                    int idISPayroll = Integer.parseInt(idISPayrollStr);
                    payrollDAO.deletePayrollLine(idISPayroll);
                    // NOTE: Idéalement, il faudrait recalculer le netPay ici après la suppression d'une ligne.
                    session.setAttribute("successMessage", "La ligne de paie (ID: " + idISPayroll + ") supprimée avec succès!");
                }

                // NOTE: La suppression d'une ligne individuelle devrait être suivie d'un recalcul du Net à Payer
                // Pour simplifier, nous n'appelons pas le recalcul ici, mais ce serait la prochaine étape d'amélioration.
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur lors de l'opération : " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/payroll");
    }
}