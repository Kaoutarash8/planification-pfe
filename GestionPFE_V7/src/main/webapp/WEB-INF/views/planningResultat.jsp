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
        .stat-value-error { font-size: 2rem; font-weight: 700; color: #dc3545; }
        .btn-download { background: #28a745; color: white; padding: 12px 35px; border-radius: 50px; text-decoration: none; font-weight: 600; margin: 10px; }
        .btn-download:hover { background: #218838; transform: translateY(-2px); color: white; }
        .btn-retour { background: #0033a0; color: white; padding: 12px 35px; border-radius: 50px; text-decoration: none; font-weight: 600; margin: 10px; }
        .btn-retour:hover { background: #002a86; transform: translateY(-2px); color: white; }
        .alert-success-custom { background: #eaf7ee; border-left: 4px solid #28a745; padding: 12px 16px; border-radius: 10px; margin-bottom: 30px; }
        .alert-error-custom { background: #fef2e8; border-left: 4px solid #dc3545; padding: 12px 16px; border-radius: 10px; margin-bottom: 30px; color: #721c24; }
        .download-box { background: white; border-radius: 16px; padding: 30px; text-align: center; margin-top: 20px; }
        .error-list { text-align: left; margin-top: 15px; }
        .error-list li { margin: 8px 0; }
        .solution-box { background: #e8f0fe; border-radius: 12px; padding: 15px; margin-top: 20px; }
        .solution-box h5 { color: #0033a0; margin-bottom: 10px; }
        .solution-box ul { margin-left: 20px; }
        .solution-box li { margin: 5px 0; }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="page-title">
                <h2><i class="bi bi-calendar-check-fill me-2"></i> Planning des soutenances</h2>
            </div>
            
            <c:choose>
                <c:when test="${afficherErreur}">
                    <!-- Affichage en cas d'erreur -->
                    <div class="alert-error-custom text-center">
                        <strong><i class="bi bi-exclamation-triangle-fill me-2"></i> Generation impossible</strong>
                    </div>
                    
                    <div class="row g-4 mb-4">
                        <div class="col-md-6">
                            <div class="stat-card">
                                <div class="stat-value">${totalEtudiants}</div>
                                <small>Etudiants a planifier</small>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="stat-card">
                                <div class="stat-value-error">${soutenancesPlacees}</div>
                                <small>Soutenances placees</small>
                            </div>
                        </div>
                    </div>
                    
                    <div class="download-box">
                        <i class="bi bi-exclamation-octagon" style="font-size: 3rem; color: #dc3545;"></i>
                        <h4 class="mt-3 text-danger">Planning non genere</h4>
                        <p class="text-muted">Le planning n'a pas pu etre genere a cause des contraintes non satisfaites.</p>
                        
                        <div class="error-list">
                            <h5><i class="bi bi-list-check me-2"></i>Erreurs detectees:</h5>
                            <ul>
                                <c:forEach items="${erreurs}" var="err">
                                    <li><i class="bi bi-x-circle-fill text-danger me-2"></i>${err}</li>
                                </c:forEach>
                            </ul>
                        </div>
                        
                        <div class="solution-box">
                            <h5><i class="bi bi-lightbulb-fill me-2"></i>Solutions possibles:</h5>
                            <ul>
                                <li>Augmenter le nombre de jours de soutenance dans la configuration</li>
                                <li>Ajouter plus de salles disponibles</li>
                                <li>Reduire la pause entre soutenances (pause_max_sans_soutenance_heures)</li>
                                <li>Augmenter le nombre max de soutenances par jour par professeur</li>
                                <li>Reduire l'exigence de professeurs informaticiens par jury (info_prof_min)</li>
                                <li>Verifier que tous les professeurs sont disponibles</li>
                            </ul>
                        </div>
                        
                        <div class="mt-4">
                            <a href="${pageContext.request.contextPath}/planning" class="btn-retour">
                                <i class="bi bi-arrow-repeat me-2"></i> Modifier la configuration
                            </a>
                            <a href="${pageContext.request.contextPath}/" class="btn-retour">
                                <i class="bi bi-house me-2"></i> Accueil
                            </a>
                        </div>
                    </div>
                </c:when>
                
                <c:otherwise>
                    <!-- Affichage normal en cas de succes -->
                    <div class="alert-success-custom text-center">
                        <strong>Planning des soutenances genere avec succes !</strong>
                    </div>
                    
                    <div class="row g-4 mb-4">
                        <div class="col-md-4">
                            <div class="stat-card">
                                <div class="stat-value">${totalSoutenances}</div>
                                <small><i class="bi bi-calendar-check-fill me-1"></i> Soutenances</small>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="stat-card">
                                <div class="stat-value">${nbJours}</div>
                                <small><i class="bi bi-calendar-week-fill me-1"></i> Jours de soutenance</small>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="stat-card">
                                <div class="stat-value">${nbSalles}</div>
                                <small><i class="bi bi-building me-1"></i> Salles utilisees</small>
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
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    
    <%@ include file="/footer.jsp" %>
</body>
</html>