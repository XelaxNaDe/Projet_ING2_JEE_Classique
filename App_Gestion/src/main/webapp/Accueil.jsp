<%@ page import="model.Employee" %>
<%@ page import="model.utils.RoleEnum" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect("Connexion.jsp");
        return;
    }
    // Vérification du rôle Admin pour afficher le bouton Stats
    boolean isAdmin = user.hasRole(RoleEnum.ADMINISTRATOR);
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
        <a href="${pageContext.request.contextPath}/profil" class="nav-button">Mon Profil</a>
        <a href="${pageContext.request.contextPath}/departements" class="nav-button admin-btn">Départements</a>
        <a href="${pageContext.request.contextPath}/projects" class="nav-button admin-btn">Projets</a>
        <a href="${pageContext.request.contextPath}/employes" class="nav-button admin-btn">Employés</a>
        <a href="${pageContext.request.contextPath}/payroll" class="nav-button">Mes fiches de paye</a>

        <% if (isAdmin) { %>
        <a href="${pageContext.request.contextPath}/stats" class="nav-button admin-btn" target="_blank" style="background-color: #6f42c1;">
            Générer le Rapport Statistiques
        </a>
        <% } %>

        <a href="${pageContext.request.contextPath}/logout" class="nav-button logout">Déconnexion</a>
    </div>
</div>

</body>
</html>