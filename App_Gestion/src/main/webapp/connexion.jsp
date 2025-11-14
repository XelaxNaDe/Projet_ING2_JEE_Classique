<%--
  Created by IntelliJ IDEA.
  User: Cytech
  Date: 10/11/2025
  Time: 01:02
  To change this template use File | Settings | File Templates.
--%>
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

    <form action="${pageContext.request.contextPath}/login" method="post">
        <div class="form-group">
            <label for="email">Adresse Email</label>
            <input type="email" id="email" name="email" required placeholder="ex: admin@cytech.fr">

            <label for="password">Mot de passe</label>
            <input type="password" id="password" name="password" required placeholder="ex: admin@cytech.fr">
        </div>

        <button type="submit" class="login-button">Se connecter</button>
    </form>
</div>

</body>
</html>