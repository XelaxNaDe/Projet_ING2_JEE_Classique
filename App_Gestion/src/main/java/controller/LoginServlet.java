package controller;

import dao.EmployeeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private EmployeeDAO employeeDAO;

    @Override
    public void init() {
        this.employeeDAO = new EmployeeDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        Employee user = null;
        try {
            // Le DAO fait maintenant la vérification BCrypt
            user = employeeDAO.findByEmailAndPassword(email, password);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("errorMessage", "Erreur technique : " + e.getMessage());
            req.getRequestDispatcher("Connexion.jsp").forward(req, resp);
            return;
        }

        if (user != null) {
            // SUCCÈS
            HttpSession session = req.getSession();
            session.setAttribute("currentUser", user);
            resp.sendRedirect(req.getContextPath() + "/accueil");
        } else {
            // ÉCHEC : On renvoie vers la JSP avec le message
            req.setAttribute("errorMessage", "Email ou mot de passe incorrect.");
            req.getRequestDispatcher("Connexion.jsp").forward(req, resp);
        }
    }
}