<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Connexion</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
</head>
<body class="login-page">

<div class="login-container">
    <h1>Connexion</h1>

    <%-- ZONE D'ERREUR : S'affiche seulement si "errorMessage" existe --%>
    <%
        String error = (String) request.getAttribute("errorMessage");
        if (error != null) {
    %>
    <div class="msg-error" style="color: white; background-color: #dc3545; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
        <%= error %>
    </div>
    <%
        }
    %>

    <form action="${pageContext.request.contextPath}/login" method="post">
        <div class="form-group">
            <label for="email">Adresse Email</label>
            <input type="email" id="email" name="email" required placeholder="ex: admin@cytech.fr">

            <label for="password">Mot de passe</label>
            <input type="password" id="password" name="password" required placeholder="ex: admin123">
        </div>

        <button type="submit" class="login-button">Se connecter</button>
    </form>
</div>

</body>
</html>