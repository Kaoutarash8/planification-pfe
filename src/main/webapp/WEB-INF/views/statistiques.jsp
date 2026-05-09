<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%
    // Récupération des données depuis la requête
    Integer totalEtudiants = (Integer) request.getAttribute("totalEtudiants");
    Integer totalSoutenances = (Integer) request.getAttribute("totalSoutenances");
    Integer totalEncadrants = (Integer) request.getAttribute("totalEncadrants");
    Integer totalSalles = (Integer) request.getAttribute("totalSalles");
    Integer totalMembresJury = (Integer) request.getAttribute("totalMembresJury");
    Double chargeMoyenne = (Double) request.getAttribute("chargeMoyenne");
    Integer totalJours = (Integer) request.getAttribute("totalJours");
    
    Map<String, Integer> repartitionFiliere = (Map<String, Integer>) request.getAttribute("repartitionFiliere");
    Map<String, Integer> soutenancesParJour = (Map<String, Integer>) request.getAttribute("soutenancesParJour");
    Map<String, Integer> soutenancesParSalle = (Map<String, Integer>) request.getAttribute("soutenancesParSalle");
    Map<String, Integer> etudiantsParEncadrant = (Map<String, Integer>) request.getAttribute("etudiantsParEncadrant");
    Map<String, Integer> participationsSpecialite = (Map<String, Integer>) request.getAttribute("participationsSpecialite");
    
    // Valeurs par défaut
    if (repartitionFiliere == null) repartitionFiliere = new HashMap<>();
    if (soutenancesParJour == null) soutenancesParJour = new HashMap<>();
    if (soutenancesParSalle == null) soutenancesParSalle = new HashMap<>();
    if (etudiantsParEncadrant == null) etudiantsParEncadrant = new HashMap<>();
    if (participationsSpecialite == null) participationsSpecialite = new HashMap<>();
    if (chargeMoyenne == null) chargeMoyenne = 0.0;
    if (totalJours == null) totalJours = 0;
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Statistiques — ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .kpi-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 15px; margin-bottom: 25px; }
        .kpi-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); text-align: center; }
        .kpi-value { font-size: 28px; font-weight: bold; color: #0033a0; }
        .kpi-label { font-size: 12px; color: #666; margin-top: 5px; }
        .chart-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .chart-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 15px; border-left: 3px solid #0033a0; padding-left: 10px; }
        .grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
        .grid-2 { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
        @media (max-width: 768px) { .grid-3, .grid-2 { grid-template-columns: 1fr; } .kpi-grid { grid-template-columns: repeat(2, 1fr); } }
        .alert { padding: 15px; margin-bottom: 20px; border-radius: 8px; }
        .alert-danger { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
    </style>
</head>
<body>

<jsp:include page="/header.jsp">
    <jsp:param name="page" value="statistiques" />
</jsp:include>

<div class="container mt-4">
    <h2 class="mb-4"><i class="bi bi-graph-up"></i> Tableau de bord statistique</h2>
    
    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
    <% } %>
    
    <!-- KPI CARDS -->
    <div class="kpi-grid">
        <div class="kpi-card"><div class="kpi-value"><%= totalEtudiants != null ? totalEtudiants : 0 %></div><div class="kpi-label">Total étudiants</div></div>
        <div class="kpi-card"><div class="kpi-value"><%= totalEncadrants != null ? totalEncadrants : 0 %></div><div class="kpi-label">Total encadrants</div></div>
        <div class="kpi-card"><div class="kpi-value"><%= totalSoutenances != null ? totalSoutenances : 0 %></div><div class="kpi-label">Total soutenances</div></div>
        <div class="kpi-card"><div class="kpi-value"><%= totalSalles != null ? totalSalles : 0 %></div><div class="kpi-label">Salles utilisées</div></div>
        <div class="kpi-card"><div class="kpi-value"><%= totalJours %></div><div class="kpi-label">Jours concernés</div></div>
        <div class="kpi-card"><div class="kpi-value"><%= String.format("%.1f", chargeMoyenne) %></div><div class="kpi-label">Charge moyenne/prof</div></div>
    </div>
    
    <!-- LIGNE 1 : Filière | Jour | Salle -->
    <div class="grid-3">
        <div class="chart-card">
            <div class="chart-title">Nombre d'étudiants par filière</div>
            <canvas id="filiereChart" height="200"></canvas>
        </div>
        <div class="chart-card">
            <div class="chart-title">Soutenances par jour</div>
            <canvas id="jourChart" height="200"></canvas>
        </div>
        <div class="chart-card">
            <div class="chart-title">Soutenances par salle</div>
            <canvas id="salleChart" height="200"></canvas>
        </div>
    </div>
    
    <!-- LIGNE 2 : Étudiants par encadrant | Participations encadrant INFO vs NON-INFO -->
    <div class="grid-2">
        <div class="chart-card">
            <div class="chart-title">Nombre d'étudiants par encadrant</div>
            <canvas id="encadrantChart" height="250"></canvas>
        </div>
        <div class="chart-card">
            <div class="chart-title">Participations encadrant INFO vs NON-INFO</div>
            <canvas id="infoChart" height="200"></canvas>
        </div>
    </div>
    
    <div class="text-center mt-3 mb-5">
        <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-outline-primary"><i class="bi bi-arrow-left"></i> Accueil</a>
    </div>
</div>

<jsp:include page="/footer.jsp" />

<script>
    // Données pour le nombre d'étudiants par filière
    const filiereLabels = [<% 
        int i = 0;
        for(String key : repartitionFiliere.keySet()) { 
            if(i++ > 0) out.print(",");
            out.print("'" + key.replace("'", "\\'") + "'");
        } 
    %>];
    const filiereCounts = [<% 
        i = 0;
        for(Integer val : repartitionFiliere.values()) { 
            if(i++ > 0) out.print(",");
            out.print(val);
        } 
    %>];
    
    // Données pour les soutenances par jour
    const jourLabels = [<% 
        i = 0;
        for(String key : soutenancesParJour.keySet()) { 
            if(i++ > 0) out.print(",");
            out.print("'" + key.replace("'", "\\'") + "'");
        } 
    %>];
    const jourCounts = [<% 
        i = 0;
        for(Integer val : soutenancesParJour.values()) { 
            if(i++ > 0) out.print(",");
            out.print(val);
        } 
    %>];
    
    // Données pour les soutenances par salle
    const salleLabels = [<% 
        i = 0;
        for(String key : soutenancesParSalle.keySet()) { 
            if(i++ > 0) out.print(",");
            out.print("'" + key.replace("'", "\\'") + "'");
        } 
    %>];
    const salleCounts = [<% 
        i = 0;
        for(Integer val : soutenancesParSalle.values()) { 
            if(i++ > 0) out.print(",");
            out.print(val);
        } 
    %>];
    
    // Données pour le nombre d'étudiants par encadrant
    const encadrantLabels = [<% 
        i = 0;
        for(String key : etudiantsParEncadrant.keySet()) { 
            if(i++ > 0) out.print(",");
            out.print("'" + key.replace("'", "\\'") + "'");
        } 
    %>];
    const encadrantCounts = [<% 
        i = 0;
        for(Integer val : etudiantsParEncadrant.values()) { 
            if(i++ > 0) out.print(",");
            out.print(val);
        } 
    %>];
    
    // Données pour les participations INFO vs NON-INFO
    const infoCount = <%= participationsSpecialite.getOrDefault("INFO", 0) %>;
    const nonInfoCount = <%= participationsSpecialite.getOrDefault("NON-INFO", 0) %>;
    
    const COLORS = { blue: '#185FA5', teal: '#0F6E56', amber: '#BA7517', purple: '#534AB7', gray: '#73726c' };
    
    // Graphique 1 : Nombre d'étudiants par filière (diagramme en barres)
    if(filiereLabels.length > 0) {
        new Chart(document.getElementById('filiereChart'), {
            type: 'bar',
            data: { 
                labels: filiereLabels, 
                datasets: [{ 
                    data: filiereCounts, 
                    backgroundColor: COLORS.blue,
                    label: "Nombre d'étudiants"
                }] 
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                scales: { 
                    y: { 
                        beginAtZero: true, 
                        title: { display: true, text: "Nombre d'étudiants" },
                        ticks: { stepSize: 10 }
                    } 
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.raw + " étudiants";
                            }
                        }
                    }
                }
            }
        });
    }
    
    // Graphique 2 : Soutenances par jour
    if(jourLabels.length > 0) {
        new Chart(document.getElementById('jourChart'), {
            type: 'bar',
            data: { 
                labels: jourLabels, 
                datasets: [{ 
                    data: jourCounts, 
                    backgroundColor: COLORS.amber,
                    label: "Nombre de soutenances"
                }] 
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                scales: { 
                    y: { 
                        beginAtZero: true, 
                        title: { display: true, text: "Nombre de soutenances" }
                    } 
                }
            }
        });
    }
    
    // Graphique 3 : Soutenances par salle
    if(salleLabels.length > 0) {
        new Chart(document.getElementById('salleChart'), {
            type: 'doughnut',
            data: { 
                labels: salleLabels, 
                datasets: [{ 
                    data: salleCounts, 
                    backgroundColor: [COLORS.blue, COLORS.teal, COLORS.purple, COLORS.amber, COLORS.gray]
                }] 
            }
        });
    }
    
    // Graphique 4 : Nombre d'étudiants par encadrant
    if(encadrantLabels.length > 0) {
        new Chart(document.getElementById('encadrantChart'), {
            type: 'bar',
            data: { 
                labels: encadrantLabels, 
                datasets: [{ 
                    data: encadrantCounts, 
                    backgroundColor: COLORS.teal,
                    label: "Nombre d'étudiants"
                }] 
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: true,
                scales: { 
                    x: { 
                        beginAtZero: true, 
                        title: { display: true, text: "Nombre d'étudiants" }
                    } 
                }
            }
        });
    }
    
    // Graphique 5 : Participations encadrant INFO vs NON-INFO
    if(infoCount > 0 || nonInfoCount > 0) {
        new Chart(document.getElementById('infoChart'), {
            type: 'doughnut',
            data: { 
                labels: ['Professeurs INFO', 'Professeurs NON-INFO'], 
                datasets: [{ 
                    data: [infoCount, nonInfoCount], 
                    backgroundColor: [COLORS.blue, COLORS.gray]
                }] 
            }
        });
    }
</script>

</body>
</html>