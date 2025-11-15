<%@ page import="model.Employee" %>
<%@ page import="model.utils.Role" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Projet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    // 1. Récupération de l'utilisateur
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/connexion.jsp");
        return;
    }

    // 2. RÉCUPÉRATION DES DEUX LISTES (fournies par la servlet)
    List<Projet> projets = (List<Projet>) request.getAttribute("listeProjets");
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");

    // 3. Gestion des messages
    String errorMessage = (String) session.getAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("errorMessage");
    session.removeAttribute("successMessage");
%>

<html>
<head>
    <title>Gestion des Projets</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
    <style>
        /* (Styles omis pour la clarté) */
        .project-table, .project-form { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); margin-bottom: 20px; }
        .project-table table { width: 100%; border-collapse: collapse; }
        .project-table th, .project-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        /* Style pour le <select> */
        .project-form select {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;
        }
    </style>
</head>
<body class="main-page">

<div class="header">
    <h2>Gestion des Projets</h2>
    <a href="${pageContext.request.contextPath}/accueil" class="nav-button" style="margin-left: 20px;">Retour au Tableau de bord</a>
</div>

<div class="container">
    <h1>Liste des Projets de l'Entreprise</h1>

    <%-- Affichage des messages --%>
    <% if (errorMessage != null) { %> <div class="msg-error"><%= errorMessage %></div> <% } %>
    <% if (successMessage != null) { %> <div class="msg-success"><%= successMessage %></div> <% } %>

    <%-- Formulaire (Admin seulement) --%>
    <% if (user.hasRole(Role.ADMINISTRATOR)) { %>
    <div class="project-form">
        <h3>Ajouter un Nouveau Projet</h3>

        <form action="${pageContext.request.contextPath}/projets" method="post">
            <input type="hidden" name="action" value="create">

            <div style="margin-bottom: 10px;">
                <label for="projectName">Nom du Projet:</label>
                <input type="text" id="projectName" name="projectName" required style="width: 100%; padding: 8px;">
            </div>

            <div style="margin-bottom: 10px;">
                <label for="dateDebut">Date de Début:</label>
                <input type="date" id="dateDebut" name="dateDebut" required style="padding: 8px;">
            </div>

            <div style="margin-bottom: 10px;">
                <label for="dateFin">Date de Fin (prévue):</label>
                <input type="date" id="dateFin" name="dateFin" required style="padding: 8px;">
            </div>

            <%-- ************************************************* --%>
            <%-- ***** BLOC REMPLAÇANT LE PLACEHOLDER ***** --%>
            <%-- ************************************************* --%>
            <div style="margin-bottom: 10px;">
                <label for="idChefProjet">Chef de Projet:</label>
                <select id="idChefProjet" name="idChefProjet" required>
                    <option value="">-- Choisir un employé --</option>
                    <% if (allEmployees != null) {
                        for (Employee emp : allEmployees) {
                    %>
                    <option value="<%= emp.getId() %>">
                        <%= emp.getFname() %> <%= emp.getSname() %> (ID: <%= emp.getId() %>)
                    </option>
                    <%
                            } // Fin for
                        } // Fin if
                    %>
                </select>
            </div>
            <%-- ************************************************* --%>
            <%-- ************************************************* --%>

            <button type="submit" class="nav-button admin-btn">Créer le Projet</button>
        </form>
    </div>
    <% } %>

    <%-- Tableau dynamique des projets --%>
    <div class="project-table">
        <h3>Projets Actuels</h3>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Nom</th>
                <th>Début</th>
                <th>Fin</th>
                <th>Statut</th>
                <th>Chef (ID)</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <% if (projets != null && !projets.isEmpty()) {
                for (Projet p : projets) {
            %>
            <tr>
                <td><%= p.getIdProjet() %></td>
                <td><%= p.getNomProjet() %></td>
                <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getDateDebut()) %></td>
                <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getDateFin()) %></td>
                <td><%= p.getEtat() %></td>
                <td><%= p.getIdChefProjet() %></td>
                <td class="admin-controls">
                    <a href="${pageContext.request.contextPath}/detail-projet?id=<%= p.getIdProjet() %>" class="detail-button">Détail</a>
                </td>
            </tr>
            <%
                } // Fin for
            } else {
            %>
            <tr>
                <td colspan="7" style="text-align: center;">Aucun projet trouvé.</td>
            </tr>
            <% } // Fin else %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>