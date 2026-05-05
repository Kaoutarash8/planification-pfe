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
    <title>Planning - ENSA Al Hoceima</title>
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
        
        /* Styles pour les cartes de statistiques */
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
        
        .btn-download {
            background: #0033a0;
            color: white;
            border: none;
            padding: 0.6rem 1.5rem;
            border-radius: 4px;
            transition: all 0.3s ease;
            text-decoration: none;
            cursor: pointer;
        }
        
        .btn-download:hover {
            background: #002a86;
            transform: translateY(-2px);
        }
        
        .btn-retour {
            background: white;
            color: #0033a0;
            border: 1px solid #0033a0;
            padding: 0.6rem 1.5rem;
            border-radius: 4px;
            transition: all 0.3s ease;
            text-decoration: none;
        }
        
        .btn-retour:hover {
            background: #f0f4ff;
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
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        /* Modal styles */
        .modal-choix {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            z-index: 1000;
            justify-content: center;
            align-items: center;
        }
        
        .modal-content {
            background: white;
            border-radius: 12px;
            width: 90%;
            max-width: 450px;
            animation: modalFadeIn 0.3s ease-out;
        }
        
        @keyframes modalFadeIn {
            from { opacity: 0; transform: scale(0.9); }
            to { opacity: 1; transform: scale(1); }
        }
        
        .modal-header {
            padding: 1rem;
            border-bottom: 1px solid #e0e0e0;
            background: #0033a0;
            color: white;
            border-radius: 12px 12px 0 0;
        }
        
        .modal-body {
            padding: 1.5rem;
        }
        
        .modal-footer {
            padding: 1rem;
            border-top: 1px solid #e0e0e0;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
        
        .format-option {
            display: flex;
            align-items: center;
            padding: 12px;
            margin: 8px 0;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s;
        }
        
        .format-option:hover {
            border-color: #0033a0;
            background: #f0f4ff;
        }
        
        .format-option.selected {
            border-color: #0033a0;
            background: #e8f0fe;
            border-width: 2px;
        }
        
        .format-icon {
            font-size: 1.8rem;
            width: 50px;
            text-align: center;
        }
        
        .format-info {
            flex: 1;
        }
        
        .format-info h6 {
            margin: 0;
            font-size: 1rem;
        }
        
        .format-info p {
            margin: 0;
            font-size: 0.75rem;
            color: #666;
        }
        
        .format-check {
            width: 30px;
            text-align: center;
        }
        
        .btn-cancel {
            background: #6c757d;
            color: white;
            border: none;
            padding: 0.5rem 1.2rem;
            border-radius: 4px;
            cursor: pointer;
        }
        
        .btn-confirm {
            background: #0033a0;
            color: white;
            border: none;
            padding: 0.5rem 1.2rem;
            border-radius: 4px;
            cursor: pointer;
        }
        
        .btn-confirm:hover {
            background: #002a86;
        }
        
        .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            display: none;
            justify-content: center;
            align-items: center;
            z-index: 1100;
        }
        
        .loading-spinner {
            background: white;
            padding: 2rem;
            border-radius: 12px;
            text-align: center;
        }
        
        .loading-spinner i {
            font-size: 2rem;
            color: #0033a0;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }
        
        @media (max-width: 768px) {
            .stat-value {
                font-size: 1.5rem;
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
                <strong>Planning généré avec succès !</strong> Le planning des soutenances a été créé pour l'année ${currentYear}.
            </div>
            
            <!-- Statistics Cards -->
            <div class="row g-4 mb-5">
                <div class="col-md-3 col-sm-6">
                    <div class="stat-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="stat-label">Total Soutenances</div>
                                <div class="stat-value">${totalSoutenances != null ? totalSoutenances : planning.size()}</div>
                            </div>
                            <i class="fas fa-calendar-check stat-icon"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 col-sm-6">
                    <div class="stat-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="stat-label">Total Professeurs</div>
                                <div class="stat-value">${totalProfesseurs != null ? totalProfesseurs : 0}</div>
                            </div>
                            <i class="fas fa-chalkboard-user stat-icon"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 col-sm-6">
                    <div class="stat-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="stat-label">Jours de soutenance</div>
                                <div class="stat-value">${nbJours != null ? nbJours : 3}</div>
                            </div>
                            <i class="fas fa-calendar-week stat-icon"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 col-sm-6">
                    <div class="stat-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="stat-label">Moyenne / Professeur</div>
                                <div class="stat-value">${moyenneParticipations != null ? moyenneParticipations : '-'}</div>
                            </div>
                            <i class="fas fa-chart-line stat-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row">
                <div class="col-md-6 mx-auto text-center">
                    <div class="card border-0 shadow-sm" style="border-radius: 4px;">
                        <div class="card-body">
                            <i class="fas fa-calendar-alt" style="font-size: 3rem; color: #0033a0;"></i>
                            <h4 class="mt-3">Planning des Soutenances</h4>
                            <p class="text-muted">Cliquez sur le bouton pour choisir le(s) format(s) de téléchargement</p>
                            <button onclick="ouvrirModal()" class="btn-download" style="display: inline-block;">
                                <i class="fas fa-download me-2"></i>Télécharger le Planning
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="text-center mt-4">
                <a href="${pageContext.request.contextPath}/affectation?page=accueil" class="btn-retour">
                    <i class="fas fa-home me-2"></i>Retour à l'accueil
                </a>
            </div>
        </div>
    </div>

    <footer class="ensa-footer">
        <div class="container">
            <p class="mb-0">© ${currentYear} - ENSA Al Hoceima | Planification des Soutenances PFE</p>
            <p class="mb-0 small mt-1">Planning de l'année universitaire ${currentYear}/${currentYear+1}</p>
        </div>
    </footer>
    
    <!-- Modal de choix des formats (SELECTION MULTIPLE) -->
    <div id="modalChoix" class="modal-choix">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="mb-0"><i class="fas fa-download me-2"></i>Choisir le(s) format(s)</h5>
            </div>
            <div class="modal-body">
                <p>Sélectionnez le(s) format(s) dans lequel vous souhaitez télécharger le planning :</p>
                
                <div class="format-option" data-format="excel" onclick="toggleFormat(this)">
                    <div class="format-icon">
                        <i class="fas fa-file-excel" style="color: #1d6f42;"></i>
                    </div>
                    <div class="format-info">
                        <h6>Excel (.xlsx)</h6>
                      
                    </div>
                    <div class="format-check">
                        <i class="fas fa-check-circle" style="color: #1d6f42; display: none;"></i>
                    </div>
                </div>
                
                <div class="format-option" data-format="pdf" onclick="toggleFormat(this)">
                    <div class="format-icon">
                        <i class="fas fa-file-pdf" style="color: #dc3545;"></i>
                    </div>
                    <div class="format-info">
                        <h6>PDF (.pdf)</h6>
                   
                    </div>
                    <div class="format-check">
                        <i class="fas fa-check-circle" style="color: #1d6f42; display: none;"></i>
                    </div>
                </div>
                
                <div class="format-option" data-format="word" onclick="toggleFormat(this)">
                    <div class="format-icon">
                        <i class="fas fa-file-word" style="color: #2b5797;"></i>
                    </div>
                    <div class="format-info">
                        <h6>Word (.docx)</h6>
           
                    </div>
                    <div class="format-check">
                        <i class="fas fa-check-circle" style="color: #1d6f42; display: none;"></i>
                    </div>
                </div>
                
                <div class="mt-3 p-2 bg-light rounded">
                    <small class="text-muted">
                        <i class="fas fa-info-circle"></i> Vous pouvez sélectionner plusieurs formats
                    </small>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn-cancel" onclick="fermerModal()">Annuler</button>
                <button class="btn-confirm" onclick="telechargerFormats()">Télécharger</button>
            </div>
        </div>
    </div>
    
    <!-- Loading Overlay -->
    <div class="loading-overlay" id="loadingOverlay">
        <div class="loading-spinner">
            <i class="fas fa-spinner"></i>
            <p class="mt-2 mb-0">Génération en cours...</p>
        </div>
    </div>

    <script>
        var formatsSelectionnes = [];
        
        function ouvrirModal() {
            document.getElementById('modalChoix').style.display = 'flex';
            formatsSelectionnes = [];
            // Réinitialiser la sélection visuelle
            document.querySelectorAll('.format-option').forEach(function(opt) {
                opt.classList.remove('selected');
                opt.querySelector('.fa-check-circle').style.display = 'none';
            });
        }
        
        function fermerModal() {
            document.getElementById('modalChoix').style.display = 'none';
        }
        
        function toggleFormat(element) {
            var format = element.getAttribute('data-format');
            var index = formatsSelectionnes.indexOf(format);
            
            if (index === -1) {
                // Ajouter le format
                formatsSelectionnes.push(format);
                element.classList.add('selected');
                element.querySelector('.fa-check-circle').style.display = 'block';
            } else {
                // Retirer le format
                formatsSelectionnes.splice(index, 1);
                element.classList.remove('selected');
                element.querySelector('.fa-check-circle').style.display = 'none';
            }
        }
        
        function telechargerFormats() {
            if (formatsSelectionnes.length === 0) {
                alert('Veuillez sélectionner au moins un format');
                return;
            }
            
            fermerModal();
            showLoading();
            
            // Télécharger chaque format sélectionné
            var count = 0;
            formatsSelectionnes.forEach(function(format) {
                setTimeout(function() {
                    window.open('${pageContext.request.contextPath}/DownloadPlanningServlet?format=' + format, '_blank');
                    count++;
                    if (count === formatsSelectionnes.length) {
                        setTimeout(function() {
                            hideLoading();
                        }, 500);
                    }
                }, count * 200);
            });
        }
        
        function showLoading() {
            document.getElementById('loadingOverlay').style.display = 'flex';
        }
        
        function hideLoading() {
            document.getElementById('loadingOverlay').style.display = 'none';
        }
        
        // Fermer le modal en cliquant en dehors
        window.onclick = function(event) {
            var modal = document.getElementById('modalChoix');
            if (event.target === modal) {
                fermerModal();
            }
        }
    </script>
</body>
</html>