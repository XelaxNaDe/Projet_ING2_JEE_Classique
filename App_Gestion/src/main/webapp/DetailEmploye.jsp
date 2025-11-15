<%@ page import="model.Employee" %>
<%@ page import="model.utils.Role" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Departement" %>
<%@ page contentType="text-html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null || !user.hasRole(Role.ADMINISTRATOR)) {
        response.sendRedirect(request.getContextPath() + "/accueil");
        return;
    }

    Employee employe = (Employee) request.getAttribute("employe");
    List<Departement> departements = (List<Departement>) request.getAttribute("listeDepartements");
    Map<Integer, String> allRoles = (Map<Integer, String>) request.getAttribute("allRoles");

    boolean isEditMode = (employe != null);
    // ... (gestion des messages) ...
%>

<html>
    <head>
        <title><%= isEditMode ? "Modifier l'Employé" : "Ajouter un Employé" %></title>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
        <style>
            .form-card { background: #fff; padding: 20px; border-radius: 8px; /* ... */ }
            .form-card h3 { border-bottom: 1px solid #eee; /* ... */ }
            .form-card label { font-weight: bold; display: block; margin-bottom: 5px; margin-top: 10px; }
            .form-card input[type="text"], .form-card input[type="email"], .form-card input[type="password"], .form-card select {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;
            }
            .form-card input:read-only { background-color: #eee; cursor: not-allowed; }
            .msg-error { color: red; background: #ffe0e0; /* ... */ }

            .roles-container {
            border: 1px solid #ccc;
            border-radius: 4px;
            padding: 10px;
            background: #f9f9f9;
            }
            .roles-container div {
            display: inline-block;
            margin-right: 15px;
            }
            .roles-container input[type="checkbox"] {
            width: auto;
            margin-right: 5px;
            }
            /* Style pour la note d'aide */
            .form-card label + small {
            font-style: italic;
            color: #555;
            margin-top: -5px;
            display: block;
            margin-bottom: 10px;
            }
        </style>
    </head>
    <body class="main-page">

        <div class="header">
            <h2><%= isEditMode ? "Modifier l'Employé" : "Ajouter un Employé" %></h2>
            <a href="${pageContext.request.contextPath}/employes" class="nav-button" style="margin-left: 20px;">Retour à la liste</a>
        </div>

        <div class="container">
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
                    <% if (isEditMode) { %> <input type="email" id="email" name="email_display" value="<%= employe.getEmail() %>" readonly> <% } else { %> <input type="email" id="email" name="email" value="" required> <% } %>
                    <label for="password">Mot de passe:</label>
                    <% if (isEditMode) { %> <input type="password" id="password" name="password_display" value="********" readonly> <% } else { %> <input type="password" id="password" name="password" required> <% } %>
                    <label for="position">Poste:</label>
                    <input type="text" id="position" name="position" value="<%= isEditMode ? employe.getPosition() : "" %>">

                        <%-- ----- BLOC RÔLES MODIFIÉ ----- --%>
                    <label>Rôles:</label>
                    <small>Ne cochez rien pour un "Employé" standard.</small>
                    <div class="roles-container">
                        <% if (allRoles != null) {
                            for (Map.Entry<Integer, String> entry : allRoles.entrySet()) {
                                int roleId = entry.getKey();
                                String roleName = entry.getValue();

                                String roleLabel = roleName;
                                if ("ADMINISTRATOR".equals(roleName)) {
                                    roleLabel = "Administrateur";
                                } else if ("HEADDEPARTEMENT".equals(roleName)) {
                                    roleLabel = "Chef de département";
                                } else if ("PROJECTMANAGER".equals(roleName)) {
                                    roleLabel = "Chef de projet";
                                }

                                boolean hasRole = false;
                                if (isEditMode) {
                                    hasRole = employe.hasRole(Role.valueOf(roleName));
                                }
                        %>
                        <div>
                            <input type="checkbox" id="role_<%= roleId %>" name="roles" value="<%= roleId %>" <%= hasRole ? "checked" : "" %>>
                            <label for="role_<%= roleId %>"><%= roleLabel %></label>
                        </div>
                        <%
                                } // Fin for
                            } // Fin if
                        %>
                    </div>
                        <%-- ----- FIN DU BLOC ----- --%>


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
                                boolean selected = isEditMode && dept.getId() == employe.getIdDepartement();
                        %>
                        <option value="<%= dept.getId() %>" <%= selected ? "selected" : "" %>><%= dept.getNomDepartement() %></option>
                        <% } } %>
                    </select>

                    <button type="submit" class="nav-button" style="margin-top: 20px;">
                            <%= isEditMode ? "Sauvegarder les Modifications" : "Créer l'Employé" %>
                    </button>
                </div>
            </form>
        </div>
    </body>
</html>