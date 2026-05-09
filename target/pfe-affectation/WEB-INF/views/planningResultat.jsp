<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Planning généré - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        body { background: #f5f7fa; font-family: 'Segoe UI', sans-serif; min-height: 100vh; display: flex; flex-direction: column; }
        .main-content { flex: 1; padding: 50px 0; }
        .page-title { text-align: center; margin-bottom: 40px; }
        .page-title h2 { color: #0033a0; font-size: 1.8rem; font-weight: 600; }
        .stat-card { background: white; border-radius: 16px; padding: 20px; text-align: center; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }
        .stat-value { font-size: 2rem; font-weight: 700; color: #0033a0; }
        .btn-download { background: #28a745; color: white; padding: 12px 35px; border-radius: 50px; text-decoration: none; font-weight: 600; margin: 10px; }
        .btn-download:hover { background: #218838; transform: translateY(-2px); color: white; }
        .btn-retour { background: #0033a0; color: white; padding: 12px 35px; border-radius: 50px; text-decoration: none; font-weight: 600; margin: 10px; }
        .btn-retour:hover { background: #002a86; transform: translateY(-2px); color: white; }
        .alert-success-custom { background: #eaf7ee; border-left: 4px solid #28a745; padding: 12px 16px; border-radius: 10px; margin-bottom: 30px; }
        .download-box { background: white; border-radius: 16px; padding: 30px; text-align: center; margin-top: 20px; }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="page-title">
                <h2><i class="bi bi-calendar-check-fill me-2"></i> Planning généré avec succès</h2>
            </div>
            
            <div class="alert-success-custom text-center">
                <strong>Planning des soutenances généré !</strong>
            </div>
            
            <div class="row g-4 mb-4">
                <div class="col-md-4">
                    <div class="stat-card">
                        <div class="stat-value">${totalSoutenances}</div>
                        <small>Soutenances</small>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="stat-card">
                        <div class="stat-value">${nbJours}</div>
                        <small>Jours</small>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="stat-card">
                        <div class="stat-value">${nbSalles}</div>
                        <small>Salles utilisées</small>
                    </div>
                </div>
            </div>
            
            <div class="download-box">
                <a href="${pageContext.request.contextPath}/download?file=${fileName}" class="btn-download" target="_blank">
                    <i class="bi bi-file-earmark-excel-fill me-2"></i> Télécharger le planning
                </a>
                <a href="${pageContext.request.contextPath}/planning" class="btn-retour">
                    <i class="bi bi-arrow-repeat me-2"></i> Nouveau planning
                </a>
            </div>
        </div>
    </div>
    
    <%@ include file="/footer.jsp" %>
</body>
</html>