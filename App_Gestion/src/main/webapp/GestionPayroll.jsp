<%@ page import="model.Employee" %>
<%@ page import="model.Payroll" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="model.utils.RoleEnum" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/Connexion.jsp");
        return;
    }

    boolean isAdmin = user.hasRole(RoleEnum.ADMINISTRATOR);

    List<Payroll> listePayrolls = (List<Payroll>) request.getAttribute("listePayrolls");
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");

    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("successMessage");
    String reqError = (String) request.getAttribute("errorMessage");


    String searchEmployeeId = request.getParameter("search_employee");
    String searchMonth = request.getParameter("search_month");

    DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE);

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    currencyFormatter.setMaximumFractionDigits(2);
    currencyFormatter.setMinimumFractionDigits(2);
%>

<html>
<head>
    <title>Gestion des Fiches de Paie</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
    <style>
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
        .filter-form input[type="month"],
        .filter-form select {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;
        }
        .filter-form button {
            padding: 8px 15px; background-color: #007bff; color: white;
            border: none; border-radius: 4px; cursor: pointer; height: 38px;
        }

        .admin-controls { display: flex; flex-direction: column; align-items: flex-start; gap: 5px; }
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
        .delete-button {
            padding: 5px 10px; font-size: 14px; text-decoration: none;
            background-color: red; color: white; border-radius: 4px;
            display: inline-block;
        }
        .add-button {
            padding: 10px 15px; text-decoration: none; background-color: #28a745;
            color: white; border-radius: 5px; display: inline-block; margin-bottom: 20px;
        }
        .print-button {
            padding: 5px 10px; font-size: 14px; text-decoration: none;
            background-color: #6c757d; color: white; border-radius: 4px;
            display: inline-block; margin-right: 5px;
        }
    </style>
</head>
<body class="main-page">

<div class="header">
    <h2>Gestion des Fiches de Paie</h2>
    <a href="${pageContext.request.contextPath}/accueil" class="nav-button" style="margin-left: 20px;">Retour à l'accueil</a>
</div>

<div class="container">
    <h1>Gérer les Fiches de Paie</h1>

    <% if (reqError != null) { %> <div class="msg-error"><%= reqError %></div> <% } %>
    <% if (errorMessage != null) { %> <div class="msg-error"><%= errorMessage %></div> <% } %>
    <% if (successMessage != null) { %> <div class="msg-success"><%= successMessage %></div> <% } %>

    <% if (isAdmin) { %>
    <a href="${pageContext.request.contextPath}/detail-payroll" class="add-button">Créer une nouvelle fiche de paie</a>
    <% } %>

    <div class="filter-form">
        <h3>Rechercher</h3>
        <form action="${pageContext.request.contextPath}/payroll" method="get">
            <div class="form-row">
                <div class="form-group">
                    <label for="search_employee">Employé:</label>
                    <% if (isAdmin) { %>
                    <select id="search_employee" name="search_employee">
                        <option value="">Tous les employés</option>
                        <% if (allEmployees != null) { for (Employee emp : allEmployees) {
                            String selected = (searchEmployeeId != null && searchEmployeeId.equals(String.valueOf(emp.getId()))) ? "selected" : "";
                        %>
                        <option value="<%= emp.getId() %>" <%= selected %>><%= emp.getFname() %> <%= emp.getSname() %></option>
                        <% } } %>
                    </select>
                    <% } else { %>
                    <input type="hidden" name="search_employee" value="<%= user.getId() %>">
                    <span style="display: block; padding: 8px; border: 1px solid #ccc; background: #eee;">
                        <%= user.getFname() %> <%= user.getSname() %> (Vous)
                    </span>
                    <% } %>
                </div>

                <div class="form-group">
                    <label for="search_month">Mois / Année :</label>
                    <input type="month" id="search_month" name="search_month" value="<%= (searchMonth != null) ? searchMonth : "" %>">
                </div>

                <button type="submit">Filtrer</button>
            </div>
        </form>
    </div>

    <div class="data-table">
        <h3>Historique des Paies</h3>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <% if (isAdmin) { %><th>Employé</th><% } %>
                <th>Période</th>
                <th>Salaire Brut</th>
                <th>Net à Payer</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                int colspan = isAdmin ? 6 : 5;
                if (listePayrolls != null && !listePayrolls.isEmpty()) {
                    for (Payroll payroll : listePayrolls) {
                        String rawDate = payroll.getDate().format(monthYearFormatter);
                        String formattedPeriod = rawDate.substring(0, 1).toUpperCase() + rawDate.substring(1);

                        String formattedSalary = currencyFormatter.format(payroll.getSalary());
                        String formattedNetPay = currencyFormatter.format(payroll.getNetPay());
            %>
            <tr>
                <td><%= payroll.getId() %></td>
                <% if (isAdmin) { %>
                <td><%= payroll.getEmployee().getFname() %> <%= payroll.getEmployee().getSname() %></td>
                <% } %>
                <td><%= formattedPeriod %></td>
                <td><%= formattedSalary %></td>
                <td><strong><%= formattedNetPay %></strong></td>
                <td class="admin-controls">
                    <div style="display: flex; gap: 5px; align-items: center;">
                        <a href="${pageContext.request.contextPath}/detail-payroll?id=<%= payroll.getId() %>" class="detail-button">Voir</a>
                        <a href="${pageContext.request.contextPath}/print-payroll?id=<%= payroll.getId() %>" class="print-button" target="_blank">PDF</a>
                    </div>
                    <% if (isAdmin) { %>
                    <form action="${pageContext.request.contextPath}/payroll" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="IdPayroll" value="<%= payroll.getId() %>">
                        <button type="submit" class="delete-button" onclick="return confirm('Supprimer ?');">X</button>
                    </form>
                    <% } %>
                </td>
            </tr>
            <% } } else { %>
            <tr><td colspan="<%= colspan %>" style="text-align: center;">Aucune fiche trouvée.</td></tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>