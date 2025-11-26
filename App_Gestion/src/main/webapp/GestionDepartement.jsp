<%@ page import="model.Employee" %>
<%@ page import="model.utils.RoleEnum" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Departement" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/Connexion.jsp");
        return;
    }

    List<Departement> departements = (List<Departement>) request.getAttribute("listeDepartements");
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");

    String errorMessage = (String) session.getAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("errorMessage");
    session.removeAttribute("successMessage");
    String reqError = (String) request.getAttribute("errorMessage");
%>

<html>
<head>
    <title>Gestion des Départements</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
    <style>
        .data-table, .data-form { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); margin-bottom: 20px; }
        .data-table table { width: 100%; border-collapse: collapse; }
        .data-table th, .data-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        .data-form select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
        .msg-error { color: red; font-weight: bold; margin-bottom: 10px; }
        .msg-success { color: green; font-weight: bold; margin-bottom: 10px; }
    </style>
</head>
<body class="main-page">

<div class="header">
    <h2>Gestion des Départements</h2>
    <a href="${pageContext.request.contextPath}/accueil" class="nav-button" style="margin-left: 20px;">Retour à l'accueil</a>
</div>

<div class="container">
    <h1>Liste des Départements</h1>

    <% if (reqError != null) { %> <div class="msg-error"><%= reqError %></div> <% } %>
    <% if (errorMessage != null) { %> <div class="msg-error"><%= errorMessage %></div> <% } %>
    <% if (successMessage != null) { %> <div class="msg-success"><%= successMessage %></div> <% } %>

    <%-- FORMULAIRE DE CRÉATION (ADMIN SEULEMENT) --%>
    <% if (user.hasRole(RoleEnum.ADMINISTRATOR)) { %>
    <div class="data-form">
        <h3>Ajouter un Département</h3>
        <form action="${pageContext.request.contextPath}/departements" method="post">
            <input type="hidden" name="action" value="create">

            <div style="margin-bottom: 10px;">
                <label for="nomDepartement">Nom du Département:</label>
                <input type="text" id="nomDepartement" name="nomDepartement" required style="width: 100%; padding: 8px;">
            </div>

            <div style="margin-bottom: 10px;">
                <label for="idChefDepartement">Chef de Département:</label>
                <select id="idChefDepartement" name="idChefDepartement">
                    <option value="0">-- Non Assigné --</option>
                    <% if (allEmployees != null) {
                        for (Employee emp : allEmployees) {
                    %>
                    <option value="<%= emp.getId() %>">
                        <%= emp.getFname() %> <%= emp.getSname() %> (ID: <%= emp.getId() %>)
                    </option>
                    <%
                            }
                        }
                    %>
                </select>
            </div>

            <button type="submit" class="nav-button" style="margin-top: 10px;">Créer</button>
        </form>
    </div>
    <% } %>

    <%-- TABLE DES DÉPARTEMENTS --%>
    <div class="data-table">
        <h3>Départements Actuels</h3>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Nom</th>
                <th>Chef de Département</th> <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <% if (departements != null && !departements.isEmpty()) {
                for (Departement dept : departements) {
            %>
            <tr>
                <td><%= dept.getId() %></td>
                <td><%= dept.getNomDepartement() %></td>

                <td>
                    <% if (dept.getChefDepartement() != null) { %>
                    <%= dept.getChefDepartement().getFname() %> <%= dept.getChefDepartement().getSname() %>
                    <% } else { %>
                    Non assigné
                    <% } %>
                </td>

                <td>
                    <a href="${pageContext.request.contextPath}/detail-departement?id=<%= dept.getId() %>" class="detail-button">Détail</a>
                </td>
            </tr>
            <%
                }
            } else {
            %>
            <tr>
                <td colspan="4" style="text-align: center;">Aucun département trouvé.</td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>