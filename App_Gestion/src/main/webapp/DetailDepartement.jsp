<%@ page import="model.Employee" %>
<%@ page import="model.utils.Role" %>
<%@ page import="model.Departement" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text-html;charset=UTF-8" language="java" %>

<%
    // 1. Vérification de la session
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/connexion.jsp");
        return;
    }

    // 2. Récupération des données (placées par la Servlet)
    Departement dept = (Departement) request.getAttribute("departement");
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");

    if (dept == null || allEmployees == null) {
        response.sendRedirect(request.getContextPath() + "/departements");
        return;
    }

    // 3. Logique des rôles (simplifiée)
    boolean isAdmin = user.hasRole(Role.ADMINISTRATOR);
    boolean canModify = isAdmin;
    boolean canDelete = isAdmin;

    // 4. Gestion des messages
    String errorMessage = (String) session.getAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("errorMessage");
    session.removeAttribute("successMessage");
%>

<html>
    <head>
        <title>Détail - <%= dept.getNomDepartement() %></title>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
        <style>
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
            text-decoration: none; font-family: Arial, sans-serif;
            }
            .btn-delete:hover { background-color: #c82333; }
        </style>
    </head>
    <body class="main-page">

        <div class="header">
            <h2>Détail : <%= dept.getNomDepartement() %></h2>
            <a href="${pageContext.request.contextPath}/departements" class="nav-button" style="margin-left: 20px;">Retour à la liste</a>
        </div>

        <div class="container">

                <%-- Affichage des messages --%>
            <% if (errorMessage != null) { %> <div class="msg-error"><%= errorMessage %></div> <% } %>
            <% if (successMessage != null) { %> <div class="msg-success"><%= successMessage %></div> <% } %>

                <%-- =================================================== --%>
                <%-- SECTION DES INFORMATIONS (FORMULAIRE DE MISE À JOUR) --%>
                <%-- =================================================== --%>

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
                        <% if (canModify) { %>
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

                        <%-- Bouton MODIFIER (si Admin) --%>
                    <% if (canModify) { %>
                    <div class="detail-actions">
                        <button type="submit" class="nav-button admin-btn">Sauvegarder les Modifications</button>
                    </div>
                    <% } %>
                </div>
            </form>

                <%-- =================================================== --%>
                <%-- SECTION SUPPRESSION (Admin seulement) --%>
                <%-- =================================================== --%>

            <% if (canDelete) { %>
            <div class="detail-card detail-actions">
                <h3>Zone de Danger</h3>
                    <%-- Ce formulaire poste vers la servlet de la LISTE, comme ton modèle --%>
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