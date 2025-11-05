package controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "TestServlet", value = "/index")
public class TestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String message = "Si vous voyez ca, le projet est configure !";
        request.setAttribute("messageTest", message);

        // Transférer la requête à la vue (JSP)
        request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
    }
}