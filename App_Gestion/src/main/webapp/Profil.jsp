<%@ page import="model.Employee" %>
<%@ page import="model.Departement" %>
<%@ page import="model.utils.Role" %>
<%@ page contentType="text-html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/Connexion.jsp");
        return;
    }

    Departement departement = (Departement) request.getAttribute("departement");
    String nomDepartement = (departement != null) ? departement.getNomDepartement() : "Non assigné";

    String errorMessage = (String) session.getAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("errorMessage");
    session.removeAttribute("successMessage");
%>

<html>
    <head>
        <title>Mon Profil</title>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
        <style>
            /* (Tes styles sont ici) */
            .profile-container {
            background: #fff; padding: 30px; border-radius: 8px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
            max-width: 600px; margin: 20px auto;
            }
            .profile-container h1 {
            border-bottom: 2px solid #f0f0f0; padding-bottom: 10px;
            }
            .profile-info { line-height: 2; }
            .profile-info strong {
            display: inline-block; width: 120px; color: #555;
            }
            .update-form {
            border-top: 1px solid #eee; margin-top: 20px; padding-top: 20px;
            }
            .update-form label {
            display: block; font-weight: bold; margin-top: 10px; margin-bottom: 5px;
            }
            .update-form input[type="email"],
            .update-form input[type="password"] {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;
            }
            .update-form .nav-button { margin-top: 15px; }
            .msg-error {
            color: red; background: #ffe0e0; padding: 10px; border-radius: 5px; margin-bottom: 15px;
            }
            .msg-success {
            color: green; background: #e0ffe0; padding: 10px; border-radius: 5px; margin-bottom: 15px;
            }
        </style>
    </head>
    <body class="main-page">

        <div class="header">
            <h2>Mon Profil</h2>
            <a href="${pageContext.request.contextPath}/accueil" class="nav-button" style="margin-left: 20px;">Retour à l'accueil</a>
        </div>

        <div class="container">
            <div class="profile-container">

                <% if (errorMessage != null) { %>
                <div class="msg-error"><%= errorMessage %></div>
                <% } %>
                <% if (successMessage != null) { %>
                <div class="msg-success"><%= successMessage %></div>
                <% } %>

                <h1><%= user.getFname() %> <%= user.getSname() %></h1>

                <div class="profile-info">
                    <p><strong>Matricule:</strong> <%= user.getId() %></p>
                    <p><strong>Email:</strong> <%= user.getEmail() %></p>
                    <p><strong>Poste:</strong> <%= user.getPosition() %></p>
                    <p><strong>Rôle(s):</strong>
                        <%
                            if (user.getRoles().isEmpty()) {
                                out.print("Employé");
                            } else {
                                StringBuilder rolesStr = new StringBuilder();
                                for (model.utils.Role r : user.getRoles()) {
                                    if (r == model.utils.Role.ADMINISTRATOR) {
                                        rolesStr.append("Administrateur, ");
                                    } else if (r == model.utils.Role.HEADDEPARTEMENT) {
                                        rolesStr.append("Chef de département, ");
                                    } else if (r == model.utils.Role.PROJECTMANAGER) {
                                        rolesStr.append("Chef de projet, ");
                                    } else {
                                        rolesStr.append(r.name() + ", ");
                                    }
                                }
                                out.print(rolesStr.substring(0, rolesStr.length() - 2));
                            }
                        %>
                    </p>
                    <p><strong>Département:</strong> <%= nomDepartement %></p>
                    <p><strong>Sexe:</strong> <%= user.getGender() %></p>
                </div>

                <div class="update-form">
                    <h3>Changer mon email</h3>
                    <form action="${pageContext.request.contextPath}/profil" method="post">
                        <input type="hidden" name="action" value="updateEmail">

                        <label for="newEmail1">Nouvel Email:</label>
                        <input type="email" id="newEmail1" name="newEmail1">

                        <label for="newEmail2">Confirmer le nouvel email:</label>
                        <input type="email" id="newEmail2" name="newEmail2">

                        <label for="currentPassword">Mot de passe actuel:</label>
                        <input type="password" id="currentPassword" name="currentPassword">

                        <button type="submit" class="nav-button">Mettre à jour l'email</button>
                    </form>
                </div>

                <div class="update-form">
                    <h3>Changer mon mot de passe</h3>
                    <form action="${pageContext.request.contextPath}/profil" method="post">
                        <input type="hidden" name="action" value="updatePassword">

                        <label for="newPassword1">Nouveau mot de passe:</label>
                        <input type="password" id="newPassword1" name="newPassword1">

                        <label for="newPassword2">Confirmer le nouveau mot de passe:</label>
                        <input type="password" id="newPassword2" name="newPassword2">

                        <label for="oldPassword">Mot de passe actuel:</label>
                        <input type="password" id="oldPassword" name="oldPassword">

                        <button type="submit" class="nav-button">Mettre à jour le mot de passe</button>
                    </form>
                </div>

            </div>
        </div>

    </body>
</html>