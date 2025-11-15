<%@ page import="model.Employee" %>
<%@ page import="model.utils.Role" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect("connexion.jsp");
        return;
    }
%>

<html>
<head>
    <title>Accueil - Tableau de bord</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
</head>
<body class="main-page">

<div class="header">
    <h2>Bienvenue, <%= user.getFname() %> <%= user.getSname() %></h2>
</div>

<div class="container">
    <h1>Bienvenue sur votre tableau de bord</h1>

    <div class="button-nav">

        <h3 style="width: 100%; border-bottom: 1px solid #ccc;">Mon espace</h3>
        <a href="#" class="nav-button">Mon Profil</a>
        <a href="#" class="nav-button">Mon Département</a>
        <a href="#" class="nav-button">Mes Projets</a>
        <a href="#" class="nav-button">Mes fiches de paye</a>

        <% if (user.hasRole(Role.ADMINISTRATOR)) { %>
        <h3 style="width: 100%; border-bottom: 1px solid #ccc;">Administration</h3>
        <a href="#" class="nav-button admin-btn">Gérer les Employés</a>
        <a href="#" class="nav-button admin-btn">Gérer les Départements</a>

        <form action="${pageContext.request.contextPath}/projets" method="get" style="display: inline;">
            <button type="submit" class="nav-button admin-btn">
                Gérer les Projets
            </button>
        </form>

        <% } %> <%-- CORRECTION : La balise de fermeture est <% } %> --%>

        <% if (!user.hasRole(Role.ADMINISTRATOR)) { %>
        <a href="#" class="nav-button">Rechercher un Employé</a>
        <% } %>

        <a href="connexion.jsp" class="nav-button logout">Déconnexion</a>
    </div>
</div>

</body>
</html>