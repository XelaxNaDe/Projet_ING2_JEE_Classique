package controller;

import dao.EmployeeDAO; // 1. Importer le DAO
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
// model.utils.Role n'est plus nécessaire ici

import java.io.IOException;
import java.sql.SQLException; // 2. Importer SQLException

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    // 3. Créer une instance du DAO
    private EmployeeDAO employeeDAO;

    @Override
    public void init() {
        this.employeeDAO = new EmployeeDAO(); // Initialiser le DAO
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Récupérer les données du formulaire
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        // 2. VRAIE VÉRIFICATION BDD (remplace la simulation)
        Employee user = null;
        try {
            // On appelle le DAO
            user = employeeDAO.findByEmailAndPassword(email, password);

        } catch (SQLException e) {
            e.printStackTrace();
            // Si la BDD plante, on gère ça comme un échec
            req.setAttribute("errorMessage", "Erreur de base de données. Veuillez contacter un admin.");
            req.getRequestDispatcher("Connexion.jsp").forward(req, resp);
            return;
        }

        if (user != null) {
            // 3. CONNEXION RÉUSSIE : On stocke l'objet Employee en SESSION
            HttpSession session = req.getSession();
            session.setAttribute("currentUser", user);

            // Redirection vers la SERVLET d'accueil (meilleur MVC)
            resp.sendRedirect(req.getContextPath() + "/accueil");
        } else {
            // 4. ÉCHEC : (user == null) -> Email ou MDP incorrect
            req.setAttribute("errorMessage", "Email ou mot de passe incorrect");
            req.getRequestDispatcher("Connexion.jsp").forward(req, resp);
        }
    }

    // 5. L'ancienne méthode authenticate() est supprimée
}