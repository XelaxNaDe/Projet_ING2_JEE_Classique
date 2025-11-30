<%@ page import="model.Employee" %>
<%@ page import="model.utils.RoleEnum" %>
<%@ page import="model.Departement" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    // 1. Vérification de la session
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/Connexion.jsp");
        return;
    }

    // 2. Récupération des données
    Departement dept = (Departement) request.getAttribute("departement");
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");
    List<Employee> assignedEmployees = (List<Employee>) request.getAttribute("assignedEmployees");

    if (dept == null || allEmployees == null) {
        response.sendRedirect(request.getContextPath() + "/departements");
        return;
    }

    // 3. Permissions
    boolean isAdmin = user.hasRole(RoleEnum.ADMINISTRATOR);
    boolean isHead = user.hasRole(RoleEnum.HEADDEPARTEMENT);
    boolean isThisDeptsHead = (isHead && dept.getChefDepartement() != null && user.getId() == dept.getChefDepartement().getId());

    boolean canModify = isAdmin || isThisDeptsHead;
    boolean canDelete = isAdmin;

    // 4. Gestion des messages
    String successMessage = (String) session.getAttribute("successMessage");
    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("successMessage");
    session.removeAttribute("errorMessage");
%>

<html>
<head>
    <title>Détail - <%= dept.getNomDepartement() %></title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
    <style>
        /* Styles identiques à DetailProjet.jsp */
        .detail-card { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .detail-card h3 { border-bottom: 1px solid #eee; padding-bottom: 10px; }
        .detail-card p, .detail-card div { font-size: 1.1em; line-height: 1.6; margin-bottom: 10px; }
        .detail-card label { font-weight: bold; display: block; margin-bottom: 5px; }
        .detail-card input[type="text"], .detail-card select {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;
        }
        .detail-actions { margin-top: 20px; }

        /* Style pour le gros bouton supprimer en bas */
        .btn-delete {
            background-color: #dc3545; color: white; padding: 8px 15px;
            border: none; border-radius: 5px; cursor: pointer; font-size: 1em;
        }

        /* Style du tableau d'équipe */
        .team-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .team-table th, .team-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        .team-table th { background-color: #f9f9f9; }
        .team-table form { display: inline; }
        /* Petit bouton retirer dans le tableau */
        .team-table .btn-remove {
            color: red; border: none; background: none; cursor: pointer;
            padding: 0; font-family: inherit; font-size: 0.9em; text-decoration: underline;
        }

        .msg-error { color: red; background: #ffe0e0; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
        .msg-success { color: green; background: #e0ffe0; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    </style>
</head>
<body class="main-page">

<div class="header">
    <h2>Détail Département : <%= dept.getNomDepartement() %></h2>
    <a href="${pageContext.request.contextPath}/departements" class="nav-button" style="margin-left: 20px;">Retour liste</a>
</div>

<div class="container">

    <% if (errorMessage != null) { %> <div class="msg-error"><%= errorMessage %></div> <% } %>
    <% if (successMessage != null) { %> <div class="msg-success"><%= successMessage %></div> <% } %>

    <%-- =================================================== --%>
    <%-- SECTION 1: INFORMATIONS DU DÉPARTEMENT --%>
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
                <% if (isAdmin) { %>
                <select id="idChefDepartement" name="idChefDepartement">
                    <option value="0">-- Non Assigné --</option>
                    <% for (Employee emp : allEmployees) {
                        boolean isChef = (dept.getChefDepartement() != null && dept.getChefDepartement().getId() == emp.getId());
                    %>
                    <option value="<%= emp.getId() %>" <%= isChef ? "selected" : "" %>>
                        <%= emp.getFname() %> <%= emp.getSname() %> (ID: <%= emp.getId() %>)
                    </option>
                    <% } %>
                </select>
                <% } else { %>
                <p>
                    <%= (dept.getChefDepartement() != null) ? dept.getChefDepartement().getFname() + " " + dept.getChefDepartement().getSname() : "Non assigné" %>
                </p>
                <% } %>
            </div>

            <% if (canModify) { %>
            <div class="detail-actions">
                <button type="submit" class="nav-button admin-btn">Sauvegarder les Modifications</button>
            </div>
            <% } %>
        </div>
    </form>


    <%-- =================================================== --%>
    <%-- SECTION 2: MEMBRES DE L'ÉQUIPE --%>
    <%-- =================================================== --%>
    <div class="detail-card">
        <h3>Membres de l'équipe (<%= assignedEmployees != null ? assignedEmployees.size() : 0 %>)</h3>

        <table class="team-table">
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
                        <button type="submit" class="btn-remove">Retirer</button>
                    </form>
                </td>
                <% } %>
            </tr>
            <% } } else { %>
            <tr><td colspan="<%= canModify ? "4" : "3" %>" style="text-align:center;">Aucun membre dans ce département.</td></tr>
            <% } %>
            </tbody>
        </table>

        <% if (canModify) { %>
        <div class="detail-actions">
            <h4>Ajouter un membre</h4>
            <form action="${pageContext.request.contextPath}/detail-departement" method="post" style="display:flex; gap:10px; align-items: flex-end;">
                <input type="hidden" name="action" value="assignEmployee">
                <input type="hidden" name="id" value="<%= dept.getId() %>">

                <div style="flex-grow:1;">
                    <label for="idEmploye">Choisir un employé :</label>
                    <select id="idEmploye" name="idEmploye" required>
                        <option value="">-- Sélectionner --</option>
                        <% for (Employee emp : allEmployees) {
                            // On n'affiche que ceux qui ne sont PAS déjà dans ce département
                            boolean isNotInDept = (emp.getDepartement() == null || emp.getDepartement().getId() != dept.getId());
                            if (isNotInDept) {
                        %>
                        <option value="<%= emp.getId() %>">
                            <%= emp.getFname() %> <%= emp.getSname() %>
                        </option>
                        <% } } %>
                    </select>
                </div>
                <button type="submit" class="nav-button admin-btn" style="margin-bottom: 2px;">Ajouter</button>
            </form>
        </div>
        <% } %>
    </div>

    <%-- =================================================== --%>
    <%-- SECTION 3: ZONE DE DANGER --%>
    <%-- =================================================== --%>
    <% if (canDelete) { %>
    <div class="detail-card detail-actions">
        <h3>Zone de Danger</h3>
        <form action="${pageContext.request.contextPath}/departements" method="post" style="display:inline;">
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