package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import model.utils.Role;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Récupérer les données du formulaire
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        // 2. Simulation de la vérification BDD (À remplacer par ton appel JDBC)
        Employee user = authenticate(email, password);

        if (user != null) {
            // 3. CONNEXION RÉUSSIE : On stocke l'objet Employee en SESSION
            HttpSession session = req.getSession();
            session.setAttribute("currentUser", user);

            // Redirection vers l'accueil
            resp.sendRedirect("accueil.jsp");
        } else {
            // 4. ÉCHEC : On retourne au login avec une erreur
            req.setAttribute("errorMessage", "Email ou mot de passe incorrect");
            req.getRequestDispatcher("connexion.jsp").forward(req, resp);
        }
    }

    // Méthode temporaire pour simuler une base de données
    private Employee authenticate(String email, String password) {
        if ("admin@cytech.fr".equals(email) && "admin123".equals(password)) {
            Employee admin = new Employee("Jean", "Dupont", "M", email, password, "Directeur");
            admin.addRole(Role.ADMINISTRATOR);
            return admin;
        }

        if ("user@cytech.fr".equals(email) && "user123".equals(password)) {
            return new Employee("Alice", "Martin", "F", email, password, "Dev");
        }
        return null;
    }
}