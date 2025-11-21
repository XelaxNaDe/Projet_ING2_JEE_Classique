<%@ page import="model.Employee" %>
<%@ page import="model.Payroll" %>
<%@ page import="model.utils.IntStringPayroll" %>
<%@ page import="model.utils.Role" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    // --- Sécurité ---
    Employee user = (Employee) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/accueil");
        return;
    }

    // --- Récupération des données ---
    List<Employee> allEmployees = (List<Employee>) request.getAttribute("allEmployees");
    Payroll payroll = (Payroll) request.getAttribute("payroll"); // Récupéré depuis le Servlet

    // --- Détection du mode (Création ou Édition) ---
    boolean isEditMode = (payroll != null);

    // Si on est en edit mode, on prépare la liste combinée des lignes (Primes + Déductions)
    List<IntStringPayroll> allLines = new ArrayList<>();
    if (isEditMode) {
        allLines.addAll(payroll.getBonusesList());
        allLines.addAll(payroll.getDeductionsList());
    }
%>

<html>
<head>
    <title><%= isEditMode ? "Modifier Fiche de Paie" : "Créer Fiche de Paie" %></title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/style.css">
    <style>
        /* Styles identiques au précédent */
        .payroll-grid { width: 100%; border-collapse: collapse; margin-top: 15px; }
        .payroll-grid th, .payroll-grid td { border: 1px solid #ccc; padding: 5px; text-align: center; }
        .payroll-grid input, .payroll-grid select { width: 100%; border: none; padding: 8px; box-sizing: border-box; }
        .payroll-grid input:focus, .payroll-grid select:focus { outline: 2px solid #007bff; background-color: #f0f8ff; }
        .total-row { background-color: #f8f9fa; font-weight: bold; font-size: 1.1em; }
        .btn-remove { background-color: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer; padding: 5px 10px; }
        .btn-add { background-color: #28a745; color: white; border: none; border-radius: 4px; padding: 10px; cursor: pointer; margin-top: 10px; }
    </style>
</head>
<body class="main-page">

<div class="header">
    <h2><%= isEditMode ? "Modification Fiche de Paie" : "Création Fiche de Paie" %></h2>
    <a href="${pageContext.request.contextPath}/payroll" class="nav-button" style="margin-left: 20px;">Retour à la liste</a>
</div>

<div class="container">
    <h1><%= isEditMode ? "Modifier la fiche de Paie" + payroll.getId() : "Nouvelle Fiche de Paie" %></h1>

    <div class="data-form" style="background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">

        <form action="${pageContext.request.contextPath}/payroll" method="post" id="payrollForm">

            <%-- Gestion de l'action (create ou update) --%>
            <input type="hidden" name="action" value="<%= isEditMode ? "update" : "create" %>">

            <%-- Si mode édition, on doit passer l'ID de la fiche de paie --%>
            <% if (isEditMode) { %>
            <input type="hidden" name="id_payroll" value="<%= payroll.getId() %>">
            <% } %>

            <%-- Champ caché pour le NetPay calculé --%>
            <input type="hidden" id="netPayInput" name="netPay" value="0">

            <h3>Informations Générales</h3>
            <div class="form-row" style="display: flex; gap: 20px; margin-bottom: 20px;">
                <div class="form-group" style="flex: 1;">
                    <label for="id_employee">Employé :</label>
                    <%if (!isEditMode && user.hasRole(Role.ADMINISTRATOR)) { %>
                        <select id="id_employee" name="id_employee" required style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px;">
                            <option value="">-- Sélectionner un employé --</option>
                            <% if (allEmployees != null) {
                                for (Employee emp : allEmployees) {
                            %>
                            <option value="<%= emp.getId() %>">
                                <%= emp.getFname() %> <%= emp.getSname() %> (Mat: <%= emp.getId() %>)
                            </option>
                            <%  } } %>
                        </select>
                    <% } else if(isEditMode) {%>
                        <% Employee selected = new Employee();
                            if(allEmployees != null) {
                                for (Employee emp : allEmployees) {
                                    // Pré-sélection si mode édition
                                    if (emp.getId() == payroll.getEmployeeId()) {
                                        selected = emp;
                                    }
                                }
                            }
                        %>
                            <input id="id_employee" name="id_employee" type="hidden"  style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px;" value="<%= selected.getId()%>">
                                <%= selected.getFname() %> <%= selected.getSname() %>
                    <% } %>
                </div>

                <div class="form-group" style="flex: 1;">
                    <label for="date">Date de la fiche :</label>
                    <input type="date" id="date" name="date" required
                           value="<%= isEditMode ? payroll.getDate().toString() : "" %>"
                           style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
                </div>

                <div class="form-group" style="flex: 1;">
                    <label for="salary">Salaire de base (€) :</label>
                    <input type="number" id="salary" name="salary" min="0" required
                           value="<%= isEditMode ? payroll.getSalary() : "0" %>"
                           oninput="calculateTotal()"
                           style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; font-weight: bold;">
                </div>
            </div>

            <hr>

            <h3>Détails (Primes & Déductions)</h3>
            <table class="payroll-grid" id="linesTable">
                <thead>
                <tr style="background-color: #f1f1f1;">
                    <th style="width: 40%;">Libellé (Label)</th>
                    <th style="width: 25%;">Type</th>
                    <th style="width: 25%;">Montant (€)</th>
                    <th style="width: 10%;">Action</th>
                </tr>
                </thead>
                <tbody id="tableBody">
                <%-- BOUCLE JSP POUR AFFICHER LES LIGNES EXISTANTES --%>
                <% if (isEditMode && !allLines.isEmpty()) {
                    for (IntStringPayroll line : allLines) {
                %>
                <tr>
                    <td>
                        <%-- Champ caché ID ligne (pour suppression ou update éventuel) --%>
                        <input type="hidden" name="id_line_existing" value="<%= line.getId_line() %>">
                        <input type="text" name="Label" value="<%= line.getLabel() %>" required>
                    </td>
                    <td>
                        <select name="Type" onchange="calculateTotal()">
                            <option value="Prime" <%= "Prime".equals(line.getType()) ? "selected" : "" %>>Prime (+)</option>
                            <option value="Déduction" <%= "Déduction".equals(line.getType()) ? "selected" : "" %>>Déduction (-)</option>
                        </select>
                    </td>
                    <td>
                        <input type="number" name="Amount" value="<%= line.getAmount() %>" min="0" oninput="calculateTotal()" required>
                    </td>
                    <td>
                        <button type="button" class="btn-remove" onclick="removeLine(this)">X</button>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>

            <button type="button" class="btn-add" onclick="addLine()">+ Ajouter une ligne</button>

            <div style="margin-top: 30px; text-align: right; border-top: 2px solid #ddd; padding-top: 15px;">
                <div style="margin-bottom: 10px;">Salaire Base : <span id="displayBase">0.00</span> €</div>
                <div style="margin-bottom: 10px; color: green;">Total Primes (+) : <span id="displayPrimes">0.00</span> €</div>
                <div style="margin-bottom: 10px; color: red;">Total Déductions (-) : <span id="displayDeductions">0.00</span> €</div>
                <div class="total-row" style="font-size: 1.5em; color: #007bff;">
                    NET À PAYER : <span id="displayNet">0.00</span> €
                </div>
            </div>

            <div style="margin-top: 20px; text-align: center;">
                <button type="submit" class="add-button" style="cursor: pointer; font-size: 1.1em;">
                    <%= isEditMode ? "Mettre à jour la Fiche" : "Enregistrer la Fiche" %>
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    // Ajout d'une ligne vide (Identique avant)
    function addLine() {
        const tbody = document.getElementById('tableBody');
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>
                <input type="text" name="Label" placeholder="Ex: Prime..." required>
            </td>
            <td>
                <select name="Type" onchange="calculateTotal()">
                    <option value="Prime">Prime (+)</option>
                    <option value="Déduction">Déduction (-)</option>
                </select>
            </td>
            <td><input type="number" name="Amount" value="0" min="0" oninput="calculateTotal()" required></td>
            <td><button type="button" class="btn-remove" onclick="removeLine(this)">X</button></td>
        `;
        tbody.appendChild(row);
    }

    function removeLine(button) {
        button.closest('tr').remove();
        calculateTotal();
    }

    function calculateTotal() {
        const baseSalary = parseFloat(document.getElementById('salary').value) || 0;
        let totalPrimes = 0;
        let totalDeductions = 0;

        document.querySelectorAll('#tableBody tr').forEach(row => {
            const amount = parseFloat(row.querySelector('input[name="Amount"]').value) || 0;
            const type = row.querySelector('select[name="Type"]').value;
            if (type === 'Prime' || type === 'prime') totalPrimes += amount; // Gestion casse au cas où
            else totalDeductions += amount;
        });

        const netPay = baseSalary + totalPrimes - totalDeductions;

        document.getElementById('displayBase').textContent = baseSalary.toFixed(2);
        document.getElementById('displayPrimes').textContent = totalPrimes.toFixed(2);
        document.getElementById('displayDeductions').textContent = totalDeductions.toFixed(2);
        document.getElementById('displayNet').textContent = netPay.toFixed(2);
        document.getElementById('netPayInput').value = netPay.toFixed(2);
    }

    // Initialisation
    window.onload = function() {
        // En mode création, on ajoute une ligne vide.
        // En mode édition, s'il n'y a pas de lignes, on en ajoute une vide aussi.
        const tbody = document.getElementById('tableBody');
        if (tbody.children.length === 0) {
            addLine();
        }
        // IMPORTANT : Recalculer les totaux immédiatement pour le mode édition
        calculateTotal();
    };
</script>

</body>
</html>