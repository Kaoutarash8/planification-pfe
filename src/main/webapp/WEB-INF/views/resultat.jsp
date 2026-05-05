<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page import="java.util.Calendar" %>
<%
    Calendar cal = Calendar.getInstance();
    int currentYear = cal.get(Calendar.YEAR);
    request.setAttribute("currentYear", currentYear);
%>
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
        
        .main-content {
            flex: 1;
            padding: 2rem 0;
        }
        
        .alert-success-custom {
            background: #e8f0fe;
            border-left: 4px solid #0033a0;
            color: #0033a0;
            padding: 1rem;
            margin-bottom: 2rem;
        }
        
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
        
        .btn-open {
            background: #0033a0;
            color: white;
            border: none;
            padding: 0.6rem 1.5rem;
            border-radius: 4px;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-open:hover {
            background: #002a86;
            transform: translateY(-2px);
            color: white;
        }
        
        .btn-retour {
            background: white;
            color: #0033a0;
            border: 1px solid #0033a0;
            padding: 0.6rem 2rem;
            border-radius: 4px;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-retour:hover {
            background: #f0f4ff;
            color: #0033a0;
        }
        
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
        
        .card-excel {
            border-top: 4px solid #1F724C;
        }
        
        .card-pdf {
            border-top: 4px solid #dc3545;
        }
        
        .card-word {
            border-top: 4px solid #2b5797;
        }
        
        .btn-excel {
            background: #1F724C;
        }
        
        .btn-excel:hover {
            background: #155a3d;
        }
        
        .btn-pdf {
            background: #dc3545;
        }
        
        .btn-pdf:hover {
            background: #b02a37;
        }
        
        .btn-word {
            background: #2b5797;
        }
        
        .btn-word:hover {
            background: #1e3f6e;
        }
        
        @media (max-width: 768px) {
            .stat-value {
                font-size: 1.5rem;
            }
            .btn-open, .btn-retour {
                width: 100%;
                margin-bottom: 10px;
            }
        }
    </style>
</head>
<body>
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

    <div class="main-content">
        <div class="container fade-in">
            <div class="alert-success-custom">
                <i class="fas fa-check-circle me-2"></i>
                <strong>Affectation générée avec succès !</strong> L'affectation est équilibrée et sauvegardée pour l'année ${currentYear}.
            </div>
            
            <!-- Statistics Cards -->
            <div class="row g-4 mb-5">
                <div class="col-md-3 col-sm-6">
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
                <div class="col-md-3 col-sm-6">
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
                <div class="col-md-3 col-sm-6">
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
                <div class="col-md-3 col-sm-6">
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
            
            <!-- 3 Boutons: PDF, Excel, Word -->
            <div class="row g-4 mb-4">
                <!-- PDF Card -->
                <div class="col-md-4">
                    <div class="stat-card card-pdf" style="text-align: center;">
                        <i class="fas fa-file-pdf" style="font-size: 3rem; color: #dc3545; margin-bottom: 0.5rem;"></i>
                        <h5 style="color: #0033a0; margin-bottom: 0.3rem;">Document PDF</h5>
                        <p class="text-muted" style="font-size: 0.8rem;">Affectation format PDF - Orientation paysage</p>
                        <p class="small text-muted mb-2">Année universitaire ${currentYear}/${currentYear+1}</p>
                        <a href="${pageContext.request.contextPath}/download/affectation_${currentYear}.pdf?t=${timestamp}" class="btn-open btn-pdf" target="_blank">
                            <i class="fas fa-external-link-alt me-2"></i>Ouvrir PDF
                        </a>
                    </div>
                </div>
                
                <!-- Excel Card -->
                <div class="col-md-4">
                    <div class="stat-card card-excel" style="text-align: center;">
                        <i class="fas fa-file-excel" style="font-size: 3rem; color: #1F724C; margin-bottom: 0.5rem;"></i>
                        <h5 style="color: #0033a0; margin-bottom: 0.3rem;">Document Excel</h5>
                        <p class="text-muted" style="font-size: 0.8rem;">Affectation format Excel - Liste des étudiants</p>
                        <p class="small text-muted mb-2">Année universitaire ${currentYear}/${currentYear+1}</p>
                        <a href="${pageContext.request.contextPath}/download/affectation_${timestamp}.xlsx" class="btn-open btn-excel" target="_blank">
                            <i class="fas fa-external-link-alt me-2"></i>Ouvrir Excel
                        </a>
                    </div>
                </div>
                
                <!-- Word Card -->
                <div class="col-md-4">
                    <div class="stat-card card-word" style="text-align: center;">
                        <i class="fas fa-file-word" style="font-size: 3rem; color: #2b5797; margin-bottom: 0.5rem;"></i>
                        <h5 style="color: #0033a0; margin-bottom: 0.3rem;">Document Word</h5>
                        <p class="text-muted" style="font-size: 0.8rem;">Affectation format DOCX - Orientation paysage</p>
                        <p class="small text-muted mb-2">Année universitaire ${currentYear}/${currentYear+1}</p>
                        <a href="${pageContext.request.contextPath}/download/affectation_${currentYear}.docx?t=${timestamp}" class="btn-open btn-word" target="_blank">
                            <i class="fas fa-external-link-alt me-2"></i>Ouvrir Word
                        </a>
                    </div>
                </div>
            </div>
            
            <!-- Message d'information sur le remplacement -->
            <div class="alert alert-info text-center mt-3" style="background: #e8f0fe; border: none; color: #0033a0;">
                <i class="fas fa-info-circle me-2"></i>
                <strong>Note :</strong> Les fichiers sont sauvegardés par année académique. 
                Si vous générez une nouvelle affectation pour l'année ${currentYear}, 
                les anciens fichiers seront automatiquement remplacés.
            </div>
            
            <!-- Return Buttons -->
            <div class="text-center mt-4">
                <a href="${pageContext.request.contextPath}/affectation?page=formulaire" class="btn-retour">
                    <i class="fas fa-plus me-2"></i>Nouvelle Affectation
                </a>
                <a href="${pageContext.request.contextPath}/affectation?page=planning" class="btn-retour" style="margin-left: 1rem;">
                    <i class="fas fa-calendar-alt me-2"></i>Générer Planning
                </a>
                <a href="${pageContext.request.contextPath}/affectation?page=accueil" class="btn-retour" style="margin-left: 1rem;">
                    <i class="fas fa-home me-2"></i>Accueil
                </a>
            </div>
        </div>
    </div>

    <footer class="ensa-footer">
        <div class="container">
            <p class="mb-0">© ${currentYear} - ENSA Al Hoceima | Planification des Soutenances PFE</p>
            <p class="mb-0 small mt-1">Affectation de l'année universitaire ${currentYear}/${currentYear+1}</p>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // Ajouter le timestamp pour éviter le cache
        document.querySelectorAll('.btn-open').forEach(btn => {
            btn.addEventListener('click', function(e) {
                const url = this.getAttribute('href');
                console.log('Ouverture du fichier: ' + url);
            });
        });
    </script>
</body>
</html>