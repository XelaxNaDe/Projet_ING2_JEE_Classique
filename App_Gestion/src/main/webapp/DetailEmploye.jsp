<%@ page import="model.Employee" %>
<%@ page import="model.utils.RoleEnum" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Departement" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null || !user.hasRole(RoleEnum.ADMINISTRATOR)) {
        response.sendRedirect(request.getContextPath() + "/Connexion.jsp");
        return;
    }

    Employee employe = (Employee) request.getAttribute("employe");
    List<Departement> departements = (List<Departement>) request.getAttribute("listeDepartements");

    // On n'a plus besoin de la Map allRoles ici

    boolean isEditMode = (employe != null);

    // Vérification si l'employé est admin
    boolean isTargetAdmin = false;
    if (isEditMode) {
        isTargetAdmin = employe.hasRole(RoleEnum.ADMINISTRATOR);
    }

    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("errorMessage");
%>

<html>
<head>
    <title><%= isEditMode ? "Modifier l'Employé" : "Ajouter un Employé" %></title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
    <style>
        .form-card { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .form-card label { font-weight: bold; display: block; margin-bottom: 5px; margin-top: 10px; }
        .form-card input[type="text"], .form-card input[type="email"], .form-card input[type="password"], .form-card select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
        .form-card input:read-only { background-color: #eee; cursor: not-allowed; }
        .msg-error { color: red; background: #ffe0e0; padding: 10px; border-radius: 4px; margin-bottom: 15px; border: 1px solid red; }

        /* Style checkbox simple */
        .checkbox-container { display: flex; align-items: center; margin-top: 10px; background: #f9f9f9; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .checkbox-container input { width: auto; margin-right: 10px; transform: scale(1.2); }
        .checkbox-container label { margin: 0; cursor: pointer; }
    </style>
</head>
<body class="main-page">

<div class="header">
    <h2><%= isEditMode ? "Modifier l'Employé" : "Ajouter un Employé" %></h2>
    <a href="${pageContext.request.contextPath}/employes" class="nav-button" style="margin-left: 20px;">Retour à la liste</a>
</div>

<div class="container">

    <% if (errorMessage != null) { %>
    <div class="msg-error"><%= errorMessage %></div>
    <% } %>

    <form action="${pageContext.request.contextPath}/detail-employe" method="post">
        <input type="hidden" name="action" value="<%= isEditMode ? "update" : "create" %>">
        <% if (isEditMode) { %>
        <input type="hidden" name="id" value="<%= employe.getId() %>">
        <% } %>

        <div class="form-card">
            <h3>Informations de l'Employé</h3>

            <label for="fname">Prénom:</label>
            <input type="text" id="fname" name="fname" value="<%= isEditMode ? employe.getFname() : "" %>" required>

            <label for="sname">Nom:</label>
            <input type="text" id="sname" name="sname" value="<%= isEditMode ? employe.getSname() : "" %>" required>

            <label for="email">Email:</label>
            <% if (isEditMode) { %>
            <input type="email" id="email" name="email_display" value="<%= employe.getEmail() %>" readonly title="L'email ne peut être modifié que par l'utilisateur lui-même">
            <% } else { %>
            <input type="email" id="email" name="email" value="" required>
            <% } %>

            <label for="password">Mot de passe:</label>
            <% if (isEditMode) { %>
            <input type="password" id="password" name="password_display" value="********" readonly title="Le mot de passe ne peut être modifié que par l'utilisateur lui-même">
            <% } else { %>
            <input type="password" id="password" name="password" required>
            <% } %>

            <label for="position">Poste:</label>
            <input type="text" id="position" name="position" value="<%= isEditMode ? employe.getPosition() : "" %>">

            <label for="gender">Sexe:</label>
            <select id="gender" name="gender">
                <option value="M" <%= (isEditMode && "M".equals(employe.getGender())) ? "selected" : "" %>>M</option>
                <option value="F" <%= (isEditMode && "F".equals(employe.getGender())) ? "selected" : "" %>>F</option>
                <option value="Autre" <%= (isEditMode && "Autre".equals(employe.getGender())) ? "selected" : "" %>>Autre</option>
            </select>

            <label for="idDepartement">Département:</label>
            <select id="idDepartement" name="idDepartement">
                <option value="0">-- Non Assigné --</option>
                <% if (departements != null) {
                    for (Departement dept : departements) {
                        boolean selected = false;
                        if (isEditMode && employe.getDepartement() != null) {
                            selected = (dept.getId() == employe.getDepartement().getId());
                        }
                %>
                <option value="<%= dept.getId() %>" <%= selected ? "selected" : "" %>><%= dept.getNomDepartement() %></option>
                <% } } %>
            </select>

            <label>Permissions :</label>
            <div class="checkbox-container">
                <input type="checkbox" id="isAdmin" name="isAdmin" value="true" <%= isTargetAdmin ? "checked" : "" %>>
                <label for="isAdmin">Accorder les droits Administrateur</label>
            </div>

            <button type="submit" class="nav-button admin-btn" style="margin-top: 20px; width:100%;">
                <%= isEditMode ? "Sauvegarder les Modifications" : "Créer l'Employé" %>
            </button>
        </div>
    </form>
</div>

</body>
</html>