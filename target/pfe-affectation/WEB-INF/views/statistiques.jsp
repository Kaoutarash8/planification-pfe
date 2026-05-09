<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Statistiques - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { background: #f5f7fa; font-family: 'Segoe UI', sans-serif; min-height: 100vh; display: flex; flex-direction: column; }
        .main-content { flex: 1; padding: 50px 0; }
        .page-title { text-align: center; margin-bottom: 40px; }
        .page-title h2 { color: #0033a0; font-size: 1.8rem; font-weight: 600; }
        .stat-card {
            background: white;
            border-radius: 16px;
            padding: 20px;
            text-align: center;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
            height: 100%;
        }
        .stat-value {
            font-size: 2.5rem;
            font-weight: 700;
            color: #0033a0;
        }
        .chart-card {
            background: white;
            border-radius: 16px;
            padding: 20px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
            margin-bottom: 20px;
            height: 100%;
        }
        .chart-title {
            font-size: 1rem;
            font-weight: 600;
            color: #0033a0;
            margin-bottom: 15px;
            border-bottom: 2px solid #e8f0fe;
            padding-bottom: 10px;
        }
        canvas { max-height: 300px; }
        .alert-custom {
            border-radius: 12px;
            background: #fef2e8;
            color: #e67e22;
            margin-bottom: 30px;
        }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="page-title">
                <h2><i class="bi bi-graph-up me-2"></i> Statistiques des soutenances</h2>
                <p>Analyse des données du planning PFE</p>
            </div>
            
            <c:if test="${not empty error}">
                <div class="alert alert-custom alert-dismissible fade show">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            
            <!-- KPI Cards -->
            <div class="row g-4 mb-4">
                <div class="col-md-2 col-6">
                    <div class="stat-card">
                        <div class="stat-value">${totalEtudiants}</div>
                        <small>Étudiants</small>
                    </div>
                </div>
                <div class="col-md-2 col-6">
                    <div class="stat-card">
                        <div class="stat-value">${totalSoutenances}</div>
                        <small>Soutenances</small>
                    </div>
                </div>
                <div class="col-md-2 col-6">
                    <div class="stat-card">
                        <div class="stat-value">${totalEncadrants}</div>
                        <small>Encadrants</small>
                    </div>
                </div>
                <div class="col-md-2 col-6">
                    <div class="stat-card">
                        <div class="stat-value">${totalSalles}</div>
                        <small>Salles</small>
                    </div>
                </div>
                <div class="col-md-2 col-6">
                    <div class="stat-card">
                        <div class="stat-value">${totalMembresJury}</div>
                        <small>Membres jury</small>
                    </div>
                </div>
                <div class="col-md-2 col-6">
                    <div class="stat-card">
                        <div class="stat-value"><%= String.format("%.1f", (Double) request.getAttribute("chargeMoyenne")) %></div>
                        <small>Charge moyenne/prof</small>
                    </div>
                </div>
            </div>
            
            <!-- Charts Row 1 -->
            <div class="row g-4">
                <div class="col-md-6">
                    <div class="chart-card">
                        <div class="chart-title"><i class="bi bi-pie-chart me-2"></i> Répartition par filière</div>
                        <canvas id="filiereChart"></canvas>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="chart-card">
                        <div class="chart-title"><i class="bi bi-calendar me-2"></i> Soutenances par jour</div>
                        <canvas id="jourChart"></canvas>
                    </div>
                </div>
            </div>
            
            <!-- Charts Row 2 -->
            <div class="row g-4 mt-2">
                <div class="col-md-6">
                    <div class="chart-card">
                        <div class="chart-title"><i class="bi bi-building me-2"></i> Soutenances par salle</div>
                        <canvas id="salleChart"></canvas>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="chart-card">
                        <div class="chart-title"><i class="bi bi-person-workspace me-2"></i> Top encadrants (étudiants)</div>
                        <canvas id="encadrantChart"></canvas>
                    </div>
                </div>
            </div>
            
            <!-- Charts Row 3 -->
            <div class="row g-4 mt-2">
                <div class="col-md-6">
                    <div class="chart-card">
                        <div class="chart-title"><i class="bi bi-bar-chart me-2"></i> Top professeurs (participations)</div>
                        <canvas id="topProfChart"></canvas>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="chart-card">
                        <div class="chart-title"><i class="bi bi-people me-2"></i> Répartition des rôles jury</div>
                        <canvas id="rolesChart"></canvas>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <%@ include file="/footer.jsp" %>
    
    <script>
        // Données pour les graphiques
        const filiereLabels = [<c:forEach var="entry" items="${repartitionFiliere}" varStatus="status">'${entry.key}'<c:if test="${not status.last}">,</c:if></c:forEach>];
        const filiereData = [<c:forEach var="entry" items="${repartitionFiliere}" varStatus="status">${entry.value}<c:if test="${not status.last}">,</c:if></c:forEach>];
        
        const jourLabels = [<c:forEach var="entry" items="${soutenancesParJour}" varStatus="status">'${entry.key}'<c:if test="${not status.last}">,</c:if></c:forEach>];
        const jourData = [<c:forEach var="entry" items="${soutenancesParJour}" varStatus="status">${entry.value}<c:if test="${not status.last}">,</c:if></c:forEach>];
        
        const salleLabels = [<c:forEach var="entry" items="${soutenancesParSalle}" varStatus="status">'${entry.key}'<c:if test="${not status.last}">,</c:if></c:forEach>];
        const salleData = [<c:forEach var="entry" items="${soutenancesParSalle}" varStatus="status">${entry.value}<c:if test="${not status.last}">,</c:if></c:forEach>];
        
        const encadrantLabels = [<c:forEach var="entry" items="${etudiantsParEncadrant}" varStatus="status">'${entry.key}'<c:if test="${not status.last}">,</c:if></c:forEach>];
        const encadrantData = [<c:forEach var="entry" items="${etudiantsParEncadrant}" varStatus="status">${entry.value}<c:if test="${not status.last}">,</c:if></c:forEach>];
        
        const topProfLabels = [<c:forEach var="entry" items="${topProfesseurs}" varStatus="status">'${entry.key}'<c:if test="${not status.last}">,</c:if></c:forEach>];
        const topProfData = [<c:forEach var="entry" items="${topProfesseurs}" varStatus="status">${entry.value}<c:if test="${not status.last}">,</c:if></c:forEach>];
        
        const rolesLabels = [<c:forEach var="entry" items="${repartitionRoles}" varStatus="status">'${entry.key}'<c:if test="${not status.last}">,</c:if></c:forEach>];
        const rolesData = [<c:forEach var="entry" items="${repartitionRoles}" varStatus="status">${entry.value}<c:if test="${not status.last}">,</c:if></c:forEach>];
        
        // Graphique Filière (Doughnut)
        new Chart(document.getElementById('filiereChart'), {
            type: 'doughnut',
            data: { labels: filiereLabels, datasets: [{ data: filiereData, backgroundColor: ['#0033a0', '#28a745', '#ffc107', '#dc3545', '#17a2b8'] }] },
            options: { responsive: true, maintainAspectRatio: true }
        });
        
        // Graphique Jour (Bar)
        new Chart(document.getElementById('jourChart'), {
            type: 'bar',
            data: { labels: jourLabels, datasets: [{ data: jourData, backgroundColor: '#0033a0', borderRadius: 8 }] },
            options: { responsive: true, maintainAspectRatio: true, scales: { y: { beginAtZero: true } } }
        });
        
        // Graphique Salle (Bar)
        new Chart(document.getElementById('salleChart'), {
            type: 'bar',
            data: { labels: salleLabels, datasets: [{ data: salleData, backgroundColor: '#28a745', borderRadius: 8 }] },
            options: { responsive: true, maintainAspectRatio: true, scales: { y: { beginAtZero: true } } }
        });
        
        // Graphique Encadrant (Bar horizontale)
        new Chart(document.getElementById('encadrantChart'), {
            type: 'bar',
            data: { labels: encadrantLabels, datasets: [{ data: encadrantData, backgroundColor: '#ffc107', borderRadius: 8 }] },
            options: { responsive: true, maintainAspectRatio: true, indexAxis: 'y', scales: { x: { beginAtZero: true } } }
        });
        
        // Graphique Top Professeurs (Bar horizontale)
        new Chart(document.getElementById('topProfChart'), {
            type: 'bar',
            data: { labels: topProfLabels, datasets: [{ data: topProfData, backgroundColor: '#17a2b8', borderRadius: 8 }] },
            options: { responsive: true, maintainAspectRatio: true, indexAxis: 'y', scales: { x: { beginAtZero: true } } }
        });
        
        // Graphique Rôles (Doughnut)
        new Chart(document.getElementById('rolesChart'), {
            type: 'doughnut',
            data: { labels: rolesLabels, datasets: [{ data: rolesData, backgroundColor: ['#0033a0', '#28a745', '#ffc107'] }] },
            options: { responsive: true, maintainAspectRatio: true }
        });
    </script>
</body>
</html>