<%@ page import="model.Employee" %>
<%@ page import="model.utils.Role" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Projet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    // 1. Récupération de l'utilisateur (géré par la Servlet, mais vérifié ici)
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/connexion.jsp");
        return;
    }

    // 2. Récupération des données fournies par la Servlet (doGet)
    List<Projet> projets = (List<Projet>) request.getAttribute("listeProjets");

    // 3. Gestion des messages (après une action POST)
    String errorMessage = (String) session.getAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("errorMessage"); // Nettoyer après lecture
    session.removeAttribute("successMessage"); // Nettoyer après lecture
%>

<html>
<head>
    <title>Gestion des Projets</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
    <style>
        /* Styles spécifiques pour le tableau et le formulaire */
        .project-table, .project-form {
            background: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            margin-bottom: 20px;
        }
        .project-table table {
            width: 100%;
            border-collapse: collapse;
        }
        .project-table th, .project-table td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }
        .admin-controls a {
            margin-right: 10px;
            text-decoration: none;
            color: #007bff;
        }
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
    <h2>Gestion des Projets</h2>
    <%-- Lien vers la servlet d'accueil --%>
    <a href="${pageContext.request.contextPath}/accueil" class="nav-button" style="margin-left: 20px;">Retour au Tableau de bord</a>
</div>

<div class="container">
    <h1>Liste des Projets de l'Entreprise</h1>

    <%-- Affichage des messages d'erreur ou de succès --%>
    <% if (errorMessage != null) { %>
    <div class="msg-error"><%= errorMessage %></div>
    <% } %>
    <% if (successMessage != null) { %>
    <div class="msg-success"><%= successMessage %></div>
    <% } %>

    <%-- Formulaire (Admin seulement) --%>
    <% if (user.hasRole(Role.ADMINISTRATOR)) { %>
    <div class="project-form">
        <h3>Ajouter un Nouveau Projet</h3>

        <%-- Formulaire qui POST vers la servlet /projets --%>
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

            <%-- TODO: Ajouter un <select> pour choisir le chef de projet --%>
            <%-- Pour l'instant, la servlet utilise l'admin connecté comme chef --%>

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
                <th>Chef (ID)</th> <%-- Amélioré si tu utilises la jointure --%>
                <% if (user.hasRole(Role.ADMINISTRATOR)) { %>
                <th>Actions</th>
                <% } %>
            </tr>
            </thead>
            <tbody>

            <%-- Boucle dynamique sur la liste des projets --%>
            <% if (projets != null && !projets.isEmpty()) {
                for (Projet p : projets) {
            %>
            <tr>
                <td><%= p.getIdProjet() %></td>
                <td><%= p.getNomProjet() %></td>
                <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getDateDebut()) %></td>
                <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getDateFin()) %></td>
                <td><%= p.getEtat() %></td>
                <td><%= p.getIdChefProjet() %></td> <%-- Remplace par p.getNomChefProjet() si tu as fait la jointure --%>

                <% if (user.hasRole(Role.ADMINISTRATOR)) { %>
                <td class="admin-controls">
                    <a href="#">Modifier</a> |
                    <form action="${pageContext.request.contextPath}/projets" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="projectId" value="<%= p.getIdProjet() %>">

                        <button type="submit"
                                class="admin-controls"
                                style="color: red; border: none; background: none; cursor: pointer; padding: 0;"
                                onclick="return confirm('Es-tu sûr de vouloir supprimer ce projet ?');">
                            Supprimer
                        </button>
                    </form>                </td>
                <% } %>
            </tr>
            <%
                } // Fin de la boucle for
            } else {
            %>
            <tr>
                <td colspan="7" style="text-align: center;">Aucun projet trouvé.</td>
            </tr>
            <% } // Fin du else %>

            </tbody>
        </table>
    </div>
</div>
</body>
</html>