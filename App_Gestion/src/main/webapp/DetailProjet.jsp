<%@ page import="model.Employee" %>
<%@ page import="model.utils.Role" %>
<%@ page import="model.Projet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Map.Entry" %>
<%@ page contentType="text-html;charset=UTF-8" language="java" %>

<%
    // 1. Vérification de la session
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/Connexion.jsp");
        return;
    }

    // 2. Récupération des données (placé par la Servlet)
    Projet projet = (Projet) request.getAttribute("projet");
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");
    Map<Employee, String> assignedTeam = (Map<Employee, String>) request.getAttribute("assignedTeam"); // AJOUTÉ

    if (projet == null || allEmployees == null || assignedTeam == null) {
        response.sendRedirect(request.getContextPath() + "/projets");
        return;
    }

    // 3. Logique des rôles pour les permissions
    boolean isAdmin = user.hasRole(Role.ADMINISTRATOR);
    boolean isProjectManager = user.hasRole(Role.PROJECTMANAGER);
    boolean canModify = isAdmin || (isProjectManager && user.getId() == projet.getIdChefProjet());
    boolean canDelete = isAdmin;

    // 4. Gestion des messages
    String errorMessage = (String) session.getAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("errorMessage");
    session.removeAttribute("successMessage");
%>

<html>
    <head>
        <title>Détail - <%= projet.getNomProjet() %></title>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
        <style>
            .detail-card { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
            .detail-card h3 { border-bottom: 1px solid #eee; padding-bottom: 10px; }
            .detail-card p, .detail-card div { font-size: 1.1em; line-height: 1.6; margin-bottom: 10px; }
            .detail-card label { font-weight: bold; display: block; margin-bottom: 5px; }
            .detail-card input[type="text"], .detail-card input[type="date"], .detail-card select {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;
            }
            .detail-actions { margin-top: 20px; }
            .btn-delete { background-color: #dc3545; color: white; padding: 8px 15px; border: none; border-radius: 5px; cursor: pointer; font-size: 1em; }

            /* NOUVEAU : Style pour la table de l'équipe */
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
            <h2>Détail du Projet : <%= projet.getNomProjet() %></h2>
            <a href="${pageContext.request.contextPath}/projets" class="nav-button" style="margin-left: 20px;">Retour à la liste</a>
        </div>

        <div class="container">

                <%-- Affichage des messages --%>
            <% if (errorMessage != null) { %> <div class="msg-error"><%= errorMessage %></div> <% } %>
            <% if (successMessage != null) { %> <div class="msg-success"><%= successMessage %></div> <% } %>

                <%-- =================================================== --%>
                <%-- SECTION DES INFORMATIONS (FORMULAIRE DE MISE À JOUR) --%>
                <%-- =================================================== --%>

            <form action="${pageContext.request.contextPath}/detail-projet" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<%= projet.getIdProjet() %>">

                <% if (!isAdmin) { %>
                <input type="hidden" name="originalIdChefProjet" value="<%= projet.getIdChefProjet() %>">
                <% } %>

                <div class="detail-card">
                    <h3>Informations</h3>
                    <p><strong>ID:</strong> <%= projet.getIdProjet() %></p>

                    <div>
                        <label for="nomProjet">Nom:</label>
                        <% if (canModify) { %>
                        <input type="text" id="nomProjet" name="nomProjet" value="<%= projet.getNomProjet() %>" required>
                        <% } else { %>
                        <p><%= projet.getNomProjet() %></p>
                        <% } %>
                    </div>

                    <div>
                        <label for="etat">Statut:</label>
                        <% if (canModify) { %>
                        <select id="etat" name="etat">
                            <option value="En cours" <%= "En cours".equals(projet.getEtat()) ? "selected" : "" %>>En cours</option>
                            <option value="Terminé" <%= "Terminé".equals(projet.getEtat()) ? "selected" : "" %>>Terminé</option>
                            <option value="Annulé" <%= "Annulé".equals(projet.getEtat()) ? "selected" : "" %>>Annulé</option>
                        </select>
                        <% } else { %>
                        <p><%= projet.getEtat() %></p>
                        <% } %>
                    </div>

                    <div>
                        <label for="dateDebut">Date de début:</label>
                        <% if (canModify) { %>
                        <input type="date" id="dateDebut" name="dateDebut" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(projet.getDateDebut()) %>" required>
                        <% } else { %>
                        <p><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(projet.getDateDebut()) %></p>
                        <% } %>
                    </div>

                    <div>
                        <label for="dateFin">Date de fin:</label>
                        <% if (canModify) { %>
                        <input type="date" id="dateFin" name="dateFin" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(projet.getDateFin()) %>" required>
                        <% } else { %>
                        <p><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(projet.getDateFin()) %></p>
                        <% } %>
                    </div>

                    <div>
                        <label for="idChefProjet">Chef de projet:</label>
                        <% if (isAdmin) { // Seul l'Admin peut changer le chef %>
                        <select id="idChefProjet" name="idChefProjet">
                            <option value="0">-- Non Assigné --</option>
                            <% for (Employee emp : allEmployees) { %>
                            <option value="<%= emp.getId() %>" <%= (projet.getIdChefProjet() == emp.getId()) ? "selected" : "" %>>
                                    <%= emp.getFname() %> <%= emp.getSname() %> (ID: <%= emp.getId() %>)
                            </option>
                            <% } %>
                        </select>
                        <% } else { // Les autres (ex: le PM) voient juste l'ID %>
                        <p>ID: <%= projet.getIdChefProjet() %></p>
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
                <%-- NOUVELLE SECTION : ÉQUIPE DU PROJET --%>
                <%-- =================================================== --%>

            <div class="detail-card">
                <h3>Équipe du Projet</h3>

                <table class="team-table">
                    <thead>
                        <tr>
                            <th>Employé</th>
                            <th>Rôle dans le projet</th>
                            <% if (canModify) { %><th>Action</th><% } %>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (assignedTeam.isEmpty()) { %>
                        <tr><td colspan="<%= canModify ? "3" : "2" %>" style="text-align: center;">Aucun employé affecté.</td></tr>
                        <% } else {
                            for (Entry<Employee, String> entry : assignedTeam.entrySet()) {
                                Employee emp = entry.getKey();
                                String roleInProject = entry.getValue();
                        %>
                        <tr>
                            <td><%= emp.getFname() %> <%= emp.getSname() %> (ID: <%= emp.getId() %>)</td>
                            <td><%= roleInProject %></td>
                            <% if (canModify) { %>
                            <td>
                                <form action="${pageContext.request.contextPath}/detail-projet" method="post">
                                    <input type="hidden" name="action" value="removeEmployee">
                                    <input type="hidden" name="id" value="<%= projet.getIdProjet() %>">
                                    <input type="hidden" name="idEmploye" value="<%= emp.getId() %>">
                                    <button type="submit" class="btn-remove" onclick="return confirm('Retirer cet employé ?');">Retirer</button>
                                </form>
                            </td>
                            <% } %>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>

                    <%-- Formulaire d'affectation (Admin ou PM seulement) --%>
                <% if (canModify) { %>
                <div class="detail-actions">
                    <h4>Affecter un employé</h4>
                    <form action="${pageContext.request.contextPath}/detail-projet" method="post">
                        <input type="hidden" name="action" value="assignEmployee">
                        <input type="hidden" name="id" value="<%= projet.getIdProjet() %>">

                        <label for="idEmploye">Employé:</label>
                        <select id="idEmploye" name="idEmploye" required>
                            <option value="">-- Choisir un employé --</option>
                            <% for (Employee emp : allEmployees) { %>
                            <option value="<%= emp.getId() %>">
                                    <%= emp.getFname() %> <%= emp.getSname() %>
                            </option>
                            <% } %>
                        </select>

                        <label for="role_dans_projet">Rôle dans le projet:</label>
                        <input type="text" id="role_dans_projet" name="role_dans_projet" required placeholder="Ex: Développeur Backend">

                        <button type="submit" class="nav-button admin-btn" style="margin-top: 10px;">Affecter</button>
                    </form>
                </div>
                <% } %>
            </div>

                <%-- =================================================== --%>
                <%-- SECTION ZONE DE DANGER (SUPPRESSION PROJET) --%>
                <%-- =================================================== --%>

            <% if (canDelete) { %>
            <div class="detail-card detail-actions">
                <h3>Zone de Danger</h3>
                <form action="${pageContext.request.contextPath}/projets" method="post" style="display:inline; margin-top: 10px;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="projectId" value="<%= projet.getIdProjet() %>">
                    <button type="submit" class="btn-delete" onclick="return confirm('Supprimer ce projet ?');">
                        Supprimer le Projet
                    </button>
                </form>
            </div>
            <% } %>

                <%-- Message si aucune action n'est possible --%>
            <% if (!canModify && !canDelete) { %>
            <div class="detail-card">
                <p>Vous avez un accès en lecture seule à ce projet.</p>
            </div>
            <% } %>
        </div>
    </body>
</html>