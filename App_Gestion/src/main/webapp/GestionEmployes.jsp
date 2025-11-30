<%@ page import="model.Employee" %>
<%@ page import="model.utils.RoleEnum" %>
<%@ page import="model.RoleEmp" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Departement" %>
<%@ page import="model.Project" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/accueil");
        return;
    }

    boolean isAdmin = user.hasRole(RoleEnum.ADMINISTRATOR);
    List<Employee> listeEmployes = (List<Employee>) request.getAttribute("listeEmployes");

    List<String> postes = (List<String>) request.getAttribute("postes");
    List<String> roles = (List<String>) request.getAttribute("roles");
    List<Departement> listeDepartements = (List<Departement>) request.getAttribute("listeDepartements");
    List<Project> listeProjets = (List<Project>) request.getAttribute("listeProjets");

    String filterPoste = (String) request.getAttribute("filterPoste");
    String filterRole = (String) request.getAttribute("filterRole");
    String searchPrenom = (String) request.getAttribute("searchPrenom");
    String searchNom = (String) request.getAttribute("searchNom");
    String searchMatricule = (String) request.getAttribute("searchMatricule");

    Integer searchDepartementId = (Integer) request.getAttribute("searchDepartementId");
    Integer searchProjetId = (Integer) request.getAttribute("searchProjetId");

    int selectedDeptId = (searchDepartementId != null) ? searchDepartementId : 0;
    int selectedProjId = (searchProjetId != null) ? searchProjetId : 0;

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
        .data-table, .data-form, .filter-form { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .data-table table { width: 100%; border-collapse: collapse; }
        .data-table th, .data-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        .filter-form form { display: flex; flex-wrap: wrap; gap: 15px; }
        .filter-form .form-row { width: 100%; display: flex; gap: 15px; align-items: flex-end; }
        .filter-form .form-group { flex: 1; min-width: 150px; }
        .filter-form label { font-weight: bold; display: block; margin-bottom: 5px; }
        .filter-form input[type="text"], .filter-form select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
        .filter-form button { padding: 8px 15px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; height: 38px; }
        .admin-controls { display: flex; flex-direction: column; align-items: flex-start; gap: 5px; }
        .admin-controls form { display: inline; margin: 0; }
        .admin-controls button { color: red; border: none; background: none; cursor: pointer; padding: 0; font-family: inherit; font-size: 1em; text-decoration: underline; }
        .msg-error { color: red; background: #ffe0e0; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
        .msg-success { color: green; background: #e0ffe0; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
        .detail-button { padding: 5px 10px; font-size: 14px; text-decoration: none; background-color: #007bff; color: white; border-radius: 4px; display: inline-block; }
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
        <h3>Filtres de recherche</h3>
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
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="search_departement">Département:</label>
                    <select id="search_departement" name="search_departement">
                        <option value="0">Tous les départements</option>
                        <% if (listeDepartements != null) {
                            for (Departement d : listeDepartements) {
                                boolean selected = (d.getId() == selectedDeptId);
                        %>
                        <option value="<%= d.getId() %>" <%= selected ? "selected" : "" %>><%= d.getNomDepartement() %></option>
                        <% } } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="search_projet">Projet:</label>
                    <select id="search_projet" name="search_projet">
                        <option value="0">Tous les projets</option>
                        <% if (listeProjets != null) {
                            for (Project p : listeProjets) {
                                boolean selected = (p.getIdProjet() == selectedProjId);
                        %>
                        <option value="<%= p.getIdProjet() %>" <%= selected ? "selected" : "" %>><%= p.getNomProjet() %></option>
                        <% } } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="poste">Poste:</label>
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
                    <label for="role">Rôle:</label>
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
            </div>

            <div class="form-row" style="justify-content: flex-end;">
                <button type="submit">Appliquer les filtres</button>
            </div>
        </form>
    </div>

    <div class="data-table">
        <h3>Résultats (<%= (listeEmployes != null) ? listeEmployes.size() : 0 %>)</h3>
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
            <tbody>
            <% if (listeEmployes != null && !listeEmployes.isEmpty()) {
                for (Employee emp : listeEmployes) {
            %>
            <tr>
                <td><%= emp.getId() %></td>
                <td><%= emp.getFname() %> <%= emp.getSname() %></td>
                <td><%= emp.getEmail() %></td>
                <td><%= emp.getPosition() %></td>
                <td>
                    <%= (emp.getDepartement() != null && emp.getDepartement().getNomDepartement() != null)
                            ? emp.getDepartement().getNomDepartement()
                            : "N/A"
                    %>
                </td>

                <td>
                    <%
                        if (emp.getRoles().isEmpty()) {
                            out.print("Employé standard");
                        } else {
                            StringBuilder rolesStr = new StringBuilder();
                            for (RoleEmp rEntity : emp.getRoles()) {
                                String rName = rEntity.getNomRole();
                                if ("ADMINISTRATOR".equals(rName)) rolesStr.append("Admin, ");
                                else if ("HEADDEPARTEMENT".equals(rName)) rolesStr.append("Chef Dept, ");
                                else if ("PROJECTMANAGER".equals(rName)) rolesStr.append("Chef Projet, ");
                                else rolesStr.append(rName).append(", ");
                            }
                            if (rolesStr.length() > 2) out.print(rolesStr.substring(0, rolesStr.length() - 2));
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
                }
            } else {
            %>
            <tr>
                <td colspan="7" style="text-align: center;">Aucun employé trouvé avec ces critères.</td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>