<%@ page import="model.Employee" %>
<%@ page import="model.utils.Role" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text-html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/accueil");
        return;
    }

    boolean isAdmin = user.hasRole(Role.ADMINISTRATOR);
    List<Employee> listeEmployes = (List<Employee>) request.getAttribute("listeEmployes");

    // Récupérer les listes pour les <select>
    List<String> postes = (List<String>) request.getAttribute("postes");
    List<String> roles = (List<String>) request.getAttribute("roles"); // C'est bon

    // Récupérer les filtres actifs
    String filterPoste = (String) request.getAttribute("filterPoste");
    String filterRole = (String) request.getAttribute("filterRole"); // C'est bon
    String searchPrenom = (String) request.getAttribute("searchPrenom");
    String searchNom = (String) request.getAttribute("searchNom");
    String searchMatricule = (String) request.getAttribute("searchMatricule");
    String searchDepartement = (String) request.getAttribute("searchDepartement");

    // Gestion des messages
    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("successMessage");
    String reqError = (String) request.getAttribute("errorMessage");
%>

<html>
    <head>
        <title>Gestion des Employés</title>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
        <style>
            /* (Tes styles sont ici) */
            .data-table, .data-form, .filter-form {
            background: #fff; padding: 20px; border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px;
            }
            .data-table table { width: 100%; border-collapse: collapse; }
            .data-table th, .data-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }

            .filter-form form {
            display: flex; flex-wrap: wrap; gap: 15px;
            }
            .filter-form .form-row {
            width: 100%; display: flex; gap: 15px; align-items: flex-end;
            }
            .filter-form .form-group { flex: 1; min-width: 150px; }
            .filter-form label { font-weight: bold; display: block; margin-bottom: 5px; }
            .filter-form input[type="text"],
            .filter-form select {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;
            }
            .filter-form button {
            padding: 8px 15px; background-color: #007bff; color: white;
            border: none; border-radius: 4px; cursor: pointer; height: 38px;
            }

            .admin-controls {
            display: flex; flex-direction: column;
            align-items: flex-start; gap: 5px;
            }
            .admin-controls form { display: inline; margin: 0; }
            .admin-controls button {
            color: red; border: none; background: none; cursor: pointer; padding: 0;
            font-family: inherit; font-size: 1em; text-decoration: underline;
            }
            .admin-controls button:hover { color: #c82333; }

            .msg-error { color: red; background: #ffe0e0; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
            .msg-success { color: green; background: #e0ffe0; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
            .detail-button {
            padding: 5px 10px; font-size: 14px; text-decoration: none;
            background-color: #007bff; color: white; border-radius: 4px;
            display: inline-block;
            }
            .add-button { padding: 10px 15px; text-decoration: none; background-color: #28a745; color: white; border-radius: 5px; display: inline-block; margin-bottom: 20px; }
        </style>
    </head>
    <body class="main-page">

        <div class="header">
            <h2>Gestion des Employés</h2>
            <a href="${pageContext.request.contextPath}/accueil" class="nav-button" style="margin-left: 20px;">Retour à l'accueil</a>
        </div>

        <div class="container">
            <h1>Gérer les Employés</h1>

            <% if (reqError != null) { %> <div class="msg-error"><%= reqError %></div> <% } %>
            <% if (errorMessage != null) { %> <div class="msg-error"><%= errorMessage %></div> <% } %>
            <% if (successMessage != null) { %> <div class="msg-success"><%= successMessage %></div> <% } %>

            <% if (isAdmin) { %>
            <a href="${pageContext.request.contextPath}/detail-employe" class="add-button">Ajouter un nouvel employé</a>
            <% } %>

            <div class="filter-form">
                <h3>Lister les employés par</h3>
                <form action="${pageContext.request.contextPath}/employes" method="get">

                    <div class="form-row">
                        <div class="form-group">
                            <label for="search_prenom">Prénom:</label>
                            <input type="text" id="search_prenom" name="search_prenom" value="<%= searchPrenom != null ? searchPrenom : "" %>">
                        </div>
                        <div class="form-group">
                            <label for="search_nom">Nom:</label>
                            <input type="text" id="search_nom" name="search_nom" value="<%= searchNom != null ? searchNom : "" %>">
                        </div>
                        <div class="form-group">
                            <label for="search_matricule">Matricule (ID):</label>
                            <input type="text" id="search_matricule" name="search_matricule" value="<%= searchMatricule != null ? searchMatricule : "" %>">
                        </div>
                        <div class="form-group">
                            <label for="search_departement">Département:</label>
                            <input type="text" id="search_departement" name="search_departement" value="<%= searchDepartement != null ? searchDepartement : "" %>">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="poste">Lister par Poste:</label>
                            <select id="poste" name="poste">
                                <option value="">Tous les postes</option>
                                <% if (postes != null) {
                                    for (String poste : postes) {
                                        boolean selected = poste.equals(filterPoste);
                                %>
                                <option value="<%= poste %>" <%= selected ? "selected" : "" %>><%= poste %></option>
                                <% } } %>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="role">Lister par Rôle:</label>
                            <select id="role" name="role">
                                <option value="">Tous les rôles</option>
                                <% if (roles != null) {
                                    for (String role : roles) {
                                        boolean selected = role.equals(filterRole);
                                %>
                                <option value="<%= role %>" <%= selected ? "selected" : "" %>><%= role %></option>
                                <% } } %>
                            </select>
                        </div>
                        <button type="submit">Rechercher / Filtrer</button>
                    </div>
                </form>
            </div>

            <div class="data-table">
                <h3>Employés Actuels</h3>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nom</th>
                            <th>Email</th>
                            <th>Poste</th>
                            <th>Département</th>
                            <th>Rôles</th>
                            <th>Actions</th>
                        </tr>
                    </thead>

                        <%-- ***** BLOC TBODY AJOUTÉ ***** --%>
                    <tbody>
                        <% if (listeEmployes != null && !listeEmployes.isEmpty()) {
                            for (Employee emp : listeEmployes) {
                        %>
                        <tr>
                            <td><%= emp.getId() %></td>
                            <td><%= emp.getFname() %> <%= emp.getSname() %></td>
                            <td><%= emp.getEmail() %></td>
                            <td><%= emp.getPosition() %></td>
                            <td><%= emp.getNomDepartement() == null ? "N/A" : emp.getNomDepartement() %></td>
                            <td>
                                <%
                                    // Affichage propre des rôles
                                    if (emp.getRoles().isEmpty()) {
                                        out.print("Employé");
                                    } else {
                                        StringBuilder rolesStr = new StringBuilder();
                                        for (model.utils.Role r : emp.getRoles()) {
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
                            </td>
                            <td class="admin-controls">
                                <% if (isAdmin) { %>
                                <a href="${pageContext.request.contextPath}/detail-employe?id=<%= emp.getId() %>" class="detail-button">Modifier</a>
                                <form action="${pageContext.request.contextPath}/employes" method="post">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="<%= emp.getId() %>">
                                    <button type="submit" onclick="return confirm('Supprimer cet employé ?');">Supprimer</button>
                                </form>
                                <% } else { %>
                                Lecture seule
                                <% } %>
                            </td>
                        </tr>
                        <%
                            } // Fin for
                        } else {
                        %>
                        <tr>
                            <td colspan="7" style="text-align: center;">Aucun employé trouvé.</td>
                        </tr>
                        <% } // Fin else %>
                    </tbody>
                        <%-- ***** FIN DU BLOC TBODY ***** --%>

                </table>
            </div>
        </div>
    </body>
</html>