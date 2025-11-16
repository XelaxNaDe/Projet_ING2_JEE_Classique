<%@ page import="model.Employee" %>
<%@ page import="model.utils.Role" %>
<%@ page import="model.Departement" %>
<%@ page import="java.util.List" %>
<%-- L'import de Projet.java a été supprimé --%>
<%@ page contentType="text-html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/connexion.jsp");
        return;
    }

    Departement dept = (Departement) request.getAttribute("departement");
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");

    // On récupère les membres
    List<Employee> assignedEmployees = (List<Employee>) request.getAttribute("assignedEmployees");

    // On ne récupère plus la listeProjets
    if (dept == null || allEmployees == null || assignedEmployees == null) {
        response.sendRedirect(request.getContextPath() + "/departements");
        return;
    }

    boolean isAdmin = user.hasRole(Role.ADMINISTRATOR);
    boolean isHead = user.hasRole(Role.HEADDEPARTEMENT);
    boolean isThisDeptsHead = (isHead && user.getId() == dept.getIdChefDepartement());
    boolean canModify = isAdmin || isThisDeptsHead;
    boolean canDelete = isAdmin;

    // ... (Gestion des messages) ...
%>

<html>
    <head>
        <title>Détail - <%= dept.getNomDepartement() %></title>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
        <style>
            /* (Tes styles) */
            .detail-card { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
            .detail-card h3 { border-bottom: 1px solid #eee; padding-bottom: 10px; }
            .detail-card p, .detail-card div { font-size: 1.1em; line-height: 1.6; margin-bottom: 10px; }
            .detail-card label { font-weight: bold; display: block; margin-bottom: 5px; }
            .detail-card input[type="text"], .detail-card select {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;
            }
            .detail-actions { margin-top: 20px; }
            .btn-delete {
            background-color: #dc3545; color: white; padding: 8px 15px;
            border: none; border-radius: 5px; cursor: pointer; font-size: 1em;
            }
            /* Style pour la table des membres */
            .team-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
            .team-table th, .team-table td { border: 1px solid #ddd; padding: 8px; }
            .team-table th { background-color: #f9f9f9; }
            .team-table form { display: inline; }
            .team-table .btn-remove {
            color: red; border: none; background: none; cursor: pointer;
            padding: 0; font-family: inherit; font-size: 0.9em; text-decoration: underline;
            }
        </style>
    </head>
    <body class="main-page">

        <div class="header">
            <h2>Détail : <%= dept.getNomDepartement() %></h2>
            <a href="${pageContext.request.contextPath}/departements" class="nav-button" style="margin-left: 20px;">Retour à la liste</a>
        </div>

        <div class="container">

            <form action="${pageContext.request.contextPath}/detail-departement" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<%= dept.getId() %>">
                <div class="detail-card">
                    <h3>Informations</h3>
                    <p><strong>ID:</strong> <%= dept.getId() %></p>
                    <div>
                        <label for="nomDepartement">Nom:</label>
                        <% if (canModify) { %>
                        <input type="text" id="nomDepartement" name="nomDepartement" value="<%= dept.getNomDepartement() %>" required>
                        <% } else { %>
                        <p><%= dept.getNomDepartement() %></p>
                        <% } %>
                    </div>
                    <div>
                        <label for="idChefDepartement">Chef de Département:</label>
                        <% if (isAdmin) { %>
                        <select id="idChefDepartement" name="idChefDepartement">
                            <option value="0" <%= (dept.getIdChefDepartement() == 0) ? "selected" : "" %>>-- Non Assigné --</option>
                            <% for (Employee emp : allEmployees) { %>
                            <option value="<%= emp.getId() %>" <%= (dept.getIdChefDepartement() == emp.getId()) ? "selected" : "" %>>
                                    <%= emp.getFname() %> <%= emp.getSname() %> (ID: <%= emp.getId() %>)
                            </option>
                            <% } %>
                        </select>
                        <% } else { %>
                        <p><%= (dept.getIdChefDepartement() == 0) ? "Non assigné" : "ID " + dept.getIdChefDepartement() %></p>
                        <% } %>
                    </div>
                    <% if (canModify) { %>
                    <div class="detail-actions">
                        <button type="submit" class="nav-button admin-btn">Sauvegarder les Modifications</button>
                    </div>
                    <% } %>
                </div>
            </form>


            <div class="detail-card">
                <h3>Membres du Département</h3>

                <table class="team-table">
                    <thead>
                        <tr>
                            <th>Nom</th>
                            <th>Poste</th>
                            <% if (canModify) { %><th>Action</th><% } %>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (assignedEmployees.isEmpty()) { %>
                        <tr><td colspan="<%= canModify ? "3" : "2" %>" style="text-align: center;">Aucun employé dans ce département.</td></tr>
                        <% } else {
                            for (Employee emp : assignedEmployees) {
                        %>
                        <tr>
                            <td><%= emp.getFname() %> <%= emp.getSname() %></td>
                            <td><%= emp.getPosition() %></td>
                            <% if (canModify) { %>
                            <td>
                                <form action="${pageContext.request.contextPath}/detail-departement" method="post">
                                    <input type="hidden" name="action" value="removeEmployee">
                                    <input type="hidden" name="id" value="<%= dept.getId() %>">
                                    <input type="hidden" name="idEmploye" value="<%= emp.getId() %>">
                                    <button type="submit" class="btn-remove" onclick="return confirm('Retirer cet employé du département ?');">Retirer</button>
                                </form>
                            </td>
                            <% } %>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>

                <% if (canModify) { %>
                <div class="detail-actions">
                    <h4>Affecter un employé</h4>
                    <form action="${pageContext.request.contextPath}/detail-departement" method="post">
                        <input type="hidden" name="action" value="assignEmployee">
                        <input type="hidden" name="id" value="<%= dept.getId() %>">

                        <label for="idEmploye">Employé:</label>
                        <select id="idEmploye" name="idEmploye" required>
                            <option value="">-- Choisir un employé --</option>
                            <% for (Employee emp : allEmployees) {
                                if (emp.getIdDepartement() != dept.getId()) {
                            %>
                            <option value="<%= emp.getId() %>">
                                    <%= emp.getFname() %> <%= emp.getSname() %>
                            </option>
                            <% } } %>
                        </select>

                        <button type="submit" class="nav-button admin-btn" style="margin-top: 10px;">Affecter</button>
                    </form>
                </div>
                <% } %>
            </div>


            <% if (canDelete) { %>
            <div class="detail-card detail-actions">
                <h3>Zone de Danger</h3>
                <form action="${pageContext.request.contextPath}/departements" method="post" style="display:inline; margin-top: 10px;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="deptId" value="<%= dept.getId() %>">
                    <button type="submit" class="btn-delete" onclick="return confirm('Supprimer ce département ?');">
                        Supprimer le Département
                    </button>
                </form>
            </div>
            <% } %>

            <% if (!canModify && !canDelete) { %>
            <div class="detail-card">
                <p>Vous avez un accès en lecture seule à ce département.</p>
            </div>
            <% } %>

        </div>
    </body>
</html>