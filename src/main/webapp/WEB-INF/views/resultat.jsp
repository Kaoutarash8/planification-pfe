<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Résultat d'Affectation - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        
        /* Header - Bleu foncé */
        .ensa-header {
            background: #0033a0;
            color: white;
            padding: 1rem 0;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        
        .logo-container {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 15px;
            flex-wrap: wrap;
        }
        
        .logo-icon {
            font-size: 2rem;
        }
        
        .title-container h1 {
            font-size: 1.3rem;
            font-weight: 600;
            margin: 0;
        }
        
        .title-container p {
            margin: 0;
            font-size: 0.8rem;
            opacity: 0.85;
        }
        
        /* Main Content */
        .main-content {
            flex: 1;
            padding: 2rem 0;
        }
        
        /* Alert Success */
        .alert-success-custom {
            background: #e8f0fe;
            border-left: 4px solid #0033a0;
            color: #0033a0;
            padding: 1rem;
            margin-bottom: 2rem;
        }
        
        /* Statistics Cards */
        .stat-card {
            background: white;
            border: 1px solid #e0e0e0;
            border-radius: 4px;
            transition: all 0.3s ease;
            padding: 1.2rem;
        }
        
        .stat-card:hover {
            border-color: #0033a0;
            transform: translateY(-2px);
        }
        
        .stat-label {
            color: #666;
            font-size: 0.8rem;
            margin-bottom: 0.5rem;
        }
        
        .stat-value {
            color: #0033a0;
            font-size: 2rem;
            font-weight: 600;
            margin: 0;
        }
        
        .stat-icon {
            font-size: 2rem;
            color: #0033a0;
            opacity: 0.3;
        }
        
        /* Download Buttons */
        .btn-download-blue {
            background: #0033a0;
            color: white;
            border: none;
            padding: 0.6rem 1.5rem;
            border-radius: 4px;
            transition: all 0.3s ease;
        }
        
        .btn-download-blue:hover {
            background: #002a86;
            transform: translateY(-2px);
        }
        
        .btn-download-white {
            background: white;
            color: #0033a0;
            border: 1px solid #0033a0;
            padding: 0.6rem 1.5rem;
            border-radius: 4px;
            transition: all 0.3s ease;
        }
        
        .btn-download-white:hover {
            background: #f0f4ff;
            transform: translateY(-2px);
        }
        
        /* Table */
        .table-custom {
            background: white;
            border: 1px solid #e0e0e0;
        }
        
        .table-custom thead th {
            background: #0033a0;
            color: white;
            border: none;
            padding: 12px;
            font-weight: 500;
            font-size: 0.85rem;
        }
        
        .table-custom tbody td {
            padding: 10px;
            vertical-align: middle;
            font-size: 0.85rem;
            border-bottom: 1px solid #f0f0f0;
        }
        
        .table-custom tbody tr:hover {
            background: #f8f9fa;
        }
        
        .badge-specialite {
            background: #e8f0fe;
            color: #0033a0;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.75rem;
        }
        
        .badge-count {
            background: #0033a0;
            color: white;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 0.75rem;
        }
        
        /* Button Retour */
        .btn-retour {
            background: white;
            color: #0033a0;
            border: 1px solid #0033a0;
            padding: 0.6rem 2rem;
            border-radius: 4px;
            transition: all 0.3s ease;
            text-decoration: none;
        }
        
        .btn-retour:hover {
            background: #f0f4ff;
        }
        
        /* Footer */
        .ensa-footer {
            background: #0033a0;
            color: white;
            padding: 1.5rem 0;
            margin-top: auto;
            text-align: center;
            font-size: 0.8rem;
        }
        
        .fade-in {
            animation: fadeIn 0.5s ease-out;
        }
        
        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        @media (max-width: 768px) {
            .stat-value {
                font-size: 1.5rem;
            }
            
            .table-custom thead th,
            .table-custom tbody td {
                font-size: 0.75rem;
            }
        }
    </style>
</head>
<body>
    <!-- Header -->
    <header class="ensa-header">
        <div class="container">
            <div class="logo-container">
                <div class="logo-icon">
                    <i class="fas fa-graduation-cap"></i>
                </div>
                <div class="title-container text-center">
                    <h1>ENSA AL HOCEIMA</h1>
                    <p>Département Mathématiques et Informatique</p>
                </div>
            </div>
        </div>
    </header>

    <!-- Main Content -->
    <div class="main-content">
        <div class="container fade-in">
            <!-- Alert Success -->
            <div class="alert-success-custom">
                <i class="fas fa-check-circle me-2"></i>
                <strong>Affectation générée avec succès !</strong> L'affectation est équilibrée et les rapports ont été créés.
            </div>
            
            <!-- Statistics Cards -->
            <div class="row g-4 mb-4">
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="stat-label">Total Étudiants</div>
                                <div class="stat-value">${statistiques.total}</div>
                            </div>
                            <i class="fas fa-users stat-icon"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="stat-label">Total Professeurs</div>
                                <div class="stat-value">${statistiques.nbProfesseurs}</div>
                            </div>
                            <i class="fas fa-chalkboard-user stat-icon"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="stat-label">Min / Professeur</div>
                                <div class="stat-value">${statistiques.min}</div>
                            </div>
                            <i class="fas fa-arrow-down stat-icon"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="stat-label">Max / Professeur</div>
                                <div class="stat-value">${statistiques.max}</div>
                            </div>
                            <i class="fas fa-arrow-up stat-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Download Buttons -->
            <div class="row g-4 mb-4">
                <div class="col-md-6">
                    <div class="stat-card" style="text-align: center;">
                        <i class="fas fa-file-excel" style="font-size: 2.5rem; color: #0033a0; margin-bottom: 0.5rem;"></i>
                        <h5 style="color: #0033a0; margin-bottom: 0.3rem;">Fichier d'Affectation</h5>
                        <p class="text-muted" style="font-size: 0.8rem;">Détail complet des affectations par étudiant</p>
                        <a href="uploads/affectation_${timestamp}.xlsx" class="btn-download-blue" style="text-decoration: none; display: inline-block;">
                            <i class="fas fa-download me-2"></i>Télécharger
                        </a>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="stat-card" style="text-align: center;">
                        <i class="fas fa-chart-simple" style="font-size: 2.5rem; color: #0033a0; margin-bottom: 0.5rem;"></i>
                        <h5 style="color: #0033a0; margin-bottom: 0.3rem;">Statistiques</h5>
                        <p class="text-muted" style="font-size: 0.8rem;">Rapport détaillé avec analyses</p>
                        <a href="uploads/statistiques_affectation_${timestamp}.xlsx" class="btn-download-white" style="text-decoration: none; display: inline-block;">
                            <i class="fas fa-download me-2"></i>Télécharger
                        </a>
                    </div>
                </div>
            </div>
            
            <!-- Detailed Table -->
            <div class="stat-card" style="padding: 0; overflow: hidden;">
                <div style="padding: 1rem; border-bottom: 1px solid #e0e0e0; background: #fafafa;">
                    <i class="fas fa-list-alt me-2" style="color: #0033a0;"></i>
                    <strong style="color: #0033a0;">Détail des Affectations par Professeur</strong>
                </div>
                <div style="padding: 0;">
                    <div class="table-responsive">
                        <table class="table table-custom table-hover" style="margin: 0;">
                            <thead>
                                <tr>
                                    <th>Professeur</th>
                                    <th>Spécialité</th>
                                    <th>Nombre</th>
                                    <th>Liste des étudiants</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${affectation}" var="entry">
                                    <tr>
                                        <td style="font-weight: 500;">${entry.key.prenom} ${entry.key.nom}</td>
                                        <td><span class="badge-specialite">${entry.key.specialite}</span></td>
                                        <td><span class="badge-count">${entry.value.size()}</span></td>
                                        <td>
                                            <c:forEach items="${entry.value}" var="etudiant" varStatus="status">
                                                ${etudiant.prenom} ${etudiant.nom}<c:if test="${not status.last}">, </c:if>
                                            </c:forEach>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            
            <!-- Retour Button -->
            <div class="text-center mt-4">
                <a href="${pageContext.request.contextPath}/affectation?page=formulaire" class="btn-retour">
                    <i class="fas fa-plus me-2"></i>Nouvelle Affectation
                </a>
                <a href="${pageContext.request.contextPath}/affectation?page=accueil" class="btn-retour" style="margin-left: 1rem;">
                    <i class="fas fa-home me-2"></i>Accueil
                </a>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="ensa-footer">
        <div class="container">
            <p class="mb-0">© 2024 - ENSA Al Hoceima | Planification des Soutenances PFE</p>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>