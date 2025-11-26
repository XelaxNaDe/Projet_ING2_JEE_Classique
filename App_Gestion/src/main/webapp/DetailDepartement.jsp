<%@ page import="model.Employee" %>
<%@ page import="model.utils.RoleEnum" %>
<%@ page import="model.Departement" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/Connexion.jsp");
        return;
    }

    Departement dept = (Departement) request.getAttribute("departement");
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");
    List<Employee> assignedEmployees = (List<Employee>) request.getAttribute("assignedEmployees");

    if (dept == null) {
        response.sendRedirect(request.getContextPath() + "/departements");
        return;
    }

    // Gestion des messages
    String successMessage = (String) session.getAttribute("successMessage");
    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("successMessage");
    session.removeAttribute("errorMessage");

    boolean isAdmin = user.hasRole(RoleEnum.ADMINISTRATOR);
    boolean isHead = user.hasRole(RoleEnum.HEADDEPARTEMENT);
    boolean isThisDeptsHead = (isHead && dept.getChefDepartement() != null && user.getId() == dept.getChefDepartement().getId());

    // Le chef peut voir et affecter des gens, l'admin peut tout faire
    boolean canModify = isAdmin || isThisDeptsHead;
%>

<html>
<head>
    <title>Détail - <%= dept.getNomDepartement() %></title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
    <style>
        .card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .btn-danger { background-color: #dc3545; color: white; padding: 5px 10px; border-radius: 4px; border: none; cursor: pointer; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    </style>
</head>
<body class="main-page">

<div class="header">
    <h2>Détail Département : <%= dept.getNomDepartement() %></h2>
    <a href="${pageContext.request.contextPath}/departements" class="nav-button" style="margin-left: 20px;">Retour liste</a>
</div>

<div class="container">

    <% if (errorMessage != null) { %> <div class="msg-error" style="color:red; background:#fee; padding:10px;"><%= errorMessage %></div> <% } %>
    <% if (successMessage != null) { %> <div class="msg-success" style="color:green; background:#efe; padding:10px;"><%= successMessage %></div> <% } %>

    <% if (isAdmin) { %>
    <div class="card">
        <h3>Modifier les informations</h3>
        <form action="${pageContext.request.contextPath}/detail-departement" method="post">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<%= dept.getId() %>">

            <label>Nom :</label>
            <input type="text" name="nomDepartement" value="<%= dept.getNomDepartement() %>" required>

            <label>Chef :</label>
            <select name="idChefDepartement">
                <option value="0">-- Aucun --</option>
                <% for (Employee emp : allEmployees) {
                    boolean isChef = (dept.getChefDepartement() != null && dept.getChefDepartement().getId() == emp.getId());
                %>
                <option value="<%= emp.getId() %>" <%= isChef ? "selected" : "" %>>
                    <%= emp.getFname() %> <%= emp.getSname() %>
                </option>
                <% } %>
            </select>
            <button type="submit" class="nav-button">Mettre à jour</button>
        </form>
    </div>
    <% } else { %>
    <div class="card">
        <h3>Informations</h3>
        <p><strong>Nom :</strong> <%= dept.getNomDepartement() %></p>
        <p><strong>Chef :</strong> <%= (dept.getChefDepartement() != null) ? dept.getChefDepartement().getFname() + " " + dept.getChefDepartement().getSname() : "Non assigné" %></p>
    </div>
    <% } %>

    <% if (canModify) { %>
    <div class="card">
        <h3>Ajouter un membre à l'équipe</h3>
        <form action="${pageContext.request.contextPath}/detail-departement" method="post" style="display:flex; gap:10px; align-items: flex-end;">
            <input type="hidden" name="action" value="assignEmployee">
            <input type="hidden" name="id" value="<%= dept.getId() %>">

            <div style="flex-grow:1;">
                <label>Choisir un employé :</label>
                <select name="idEmploye" required style="width:100%; padding:8px;">
                    <option value="">-- Sélectionner --</option>
                    <% for (Employee emp : allEmployees) {
                        // On n'affiche que ceux qui ne sont PAS déjà dans ce département
                        boolean isNotInDept = (emp.getDepartement() == null || emp.getDepartement().getId() != dept.getId());
                        if (isNotInDept) {
                    %>
                    <option value="<%= emp.getId() %>"><%= emp.getFname() %> <%= emp.getSname() %></option>
                    <% } } %>
                </select>
            </div>
            <button type="submit" class="nav-button admin-btn">Ajouter</button>
        </form>
    </div>
    <% } %>

    <div class="card">
        <h3>Membres de l'équipe (<%= assignedEmployees != null ? assignedEmployees.size() : 0 %>)</h3>
        <table>
            <thead>
            <tr>
                <th>Nom</th>
                <th>Email</th>
                <th>Poste</th>
                <% if (canModify) { %> <th>Action</th> <% } %>
            </tr>
            </thead>
            <tbody>
            <% if (assignedEmployees != null && !assignedEmployees.isEmpty()) {
                for (Employee emp : assignedEmployees) {
            %>
            <tr>
                <td><%= emp.getFname() %> <%= emp.getSname() %></td>
                <td><%= emp.getEmail() %></td>
                <td><%= emp.getPosition() %></td>

                <% if (canModify) { %>
                <td>
                    <form action="${pageContext.request.contextPath}/detail-departement" method="post" onsubmit="return confirm('Retirer cet employé du département ?');">
                        <input type="hidden" name="action" value="removeEmployee">
                        <input type="hidden" name="id" value="<%= dept.getId() %>">
                        <input type="hidden" name="idEmploye" value="<%= emp.getId() %>">
                        <button type="submit" class="btn-danger">Retirer</button>
                    </form>
                </td>
                <% } %>
            </tr>
            <% } } else { %>
            <tr><td colspan="4" style="text-align:center;">Aucun membre dans ce département.</td></tr>
            <% } %>
            </tbody>
        </table>
    </div>

</div>
</body>
</html>