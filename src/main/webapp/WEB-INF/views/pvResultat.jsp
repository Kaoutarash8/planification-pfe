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
    <title>PVs - ENSA Al Hoceima</title>
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
        
        .btn-download {
            background: #0033a0;
            color: white;
            border: none;
            padding: 0.8rem 2rem;
            border-radius: 8px;
            transition: all 0.3s;
            font-size: 1.1rem;
            cursor: pointer;
        }
        
        .btn-download:hover {
            background: #002a86;
            transform: translateY(-2px);
        }
        
        .btn-outline-custom {
            background: white;
            color: #0033a0;
            border: 1px solid #0033a0;
            padding: 0.6rem 1.5rem;
            border-radius: 8px;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-outline-custom:hover {
            background: #f0f4ff;
            transform: translateY(-2px);
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
            max-height: 400px;
            overflow-y: auto;
        }
        
        .modal-footer {
            padding: 1rem;
            border-top: 1px solid #e0e0e0;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
        
        .option-choix {
            display: flex;
            align-items: center;
            padding: 15px;
            margin: 10px 0;
            border: 1px solid #e0e0e0;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s;
        }
        
        .option-choix:hover {
            border-color: #0033a0;
            background: #f0f4ff;
            transform: translateX(5px);
        }
        
        .option-choix.selected {
            border-color: #0033a0;
            background: #e8f0fe;
            border-width: 2px;
        }
        
        .option-icon {
            font-size: 2rem;
            width: 60px;
            text-align: center;
        }
        
        .option-info {
            flex: 1;
        }
        
        .option-info h6 {
            margin: 0;
            font-size: 1.1rem;
            font-weight: 600;
        }
        
        .option-info p {
            margin: 5px 0 0;
            font-size: 0.8rem;
            color: #666;
        }
        
        .option-check {
            width: 30px;
            text-align: center;
        }
        
        .option-check i {
            font-size: 1.3rem;
            color: #1d6f42;
            display: none;
        }
        
        .btn-cancel {
            background: #6c757d;
            color: white;
            border: none;
            padding: 0.5rem 1.2rem;
            border-radius: 6px;
            cursor: pointer;
        }
        
        .btn-confirm {
            background: #0033a0;
            color: white;
            border: none;
            padding: 0.5rem 1.5rem;
            border-radius: 6px;
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
        
        .info-text {
            text-align: center;
            color: #666;
            margin-top: 1rem;
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
                <strong>PVs générés avec succès !</strong> Les procès-verbaux sont prêts à être téléchargés.
            </div>
            
            <div class="row">
                <div class="col-md-6 mx-auto text-center">
                    <div class="card border-0 shadow-sm" style="border-radius: 12px;">
                        <div class="card-body py-5">
                            <i class="fas fa-file-alt" style="font-size: 4rem; color: #0033a0;"></i>
                            <h4 class="mt-3">Procès-Verbaux</h4>
                            <p class="text-muted">
                                <c:out value="${sessionScope.fichiersPV != null ? sessionScope.fichiersPV.size() : 0}"/> fichiers générés
                                <br>organisés par encadrant
                            </p>
                            <button onclick="ouvrirModal()" class="btn-download">
                                <i class="fas fa-download me-2"></i>Télécharger les PVs
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="text-center mt-4">
                <a href="${pageContext.request.contextPath}/affectation?page=accueil" class="btn-outline-custom">
                    <i class="fas fa-home me-2"></i>Retour à l'accueil
                </a>
            </div>
            
            <div class="info-text small mt-4">
                <i class="fas fa-folder-tree me-1"></i> Les fichiers sont organisés par dossier : uploads/pvs/[Nom Encadrant]/
            </div>
        </div>
    </div>

    <footer class="ensa-footer">
        <div class="container">
            <p class="mb-0">© ${currentYear} - ENSA Al Hoceima | Planification des Soutenances PFE</p>
        </div>
    </footer>
    
    <!-- Modal de choix pour les PVs -->
    <div id="modalChoix" class="modal-choix">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="mb-0"><i class="fas fa-download me-2"></i>Choisir le type de téléchargement</h5>
            </div>
            <div class="modal-body">
                <p>Sélectionnez comment vous souhaitez télécharger les PVs :</p>
                
                <!-- Option 1: Tous les PVs -->
                <div class="option-choix" data-type="tous" onclick="selectionnerOption(this)">
                    <div class="option-icon">
                        <i class="fas fa-globe" style="color: #0033a0;"></i>
                    </div>
                    <div class="option-info">
                        <h6>Tous les PVs</h6>
                        <p>Télécharger l'ensemble des procès-verbaux (ZIP unique)</p>
                    </div>
                    <div class="option-check">
                        <i class="fas fa-check-circle"></i>
                    </div>
                </div>
                
                <!-- Option 2: Par encadrant -->
                <div class="option-choix" data-type="encadrant" onclick="selectionnerOption(this)">
                    <div class="option-icon">
                        <i class="fas fa-chalkboard-user" style="color: #1d6f42;"></i>
                    </div>
                    <div class="option-info">
                        <h6>Par encadrant</h6>
                        <p>Choisir un encadrant spécifique pour télécharger ses PVs</p>
                    </div>
                    <div class="option-check">
                        <i class="fas fa-check-circle"></i>
                    </div>
                </div>
                
                <!-- Liste des encadrants (cachée par défaut) -->
                <div id="encadrantList" style="display: none; margin-top: 15px;">
                    <p class="small text-muted mb-2"><i class="fas fa-users"></i> Sélectionnez un encadrant :</p>
                    <div id="encadrantContainer" style="max-height: 200px; overflow-y: auto;"></div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn-cancel" onclick="fermerModal()">Annuler</button>
                <button class="btn-confirm" onclick="confirmerTelechargement()">Télécharger</button>
            </div>
        </div>
    </div>
    
    <!-- Loading Overlay -->
    <div class="loading-overlay" id="loadingOverlay">
        <div class="loading-spinner">
            <i class="fas fa-spinner"></i>
            <p class="mt-2 mb-0">Préparation du téléchargement...</p>
        </div>
    </div>

    <script>
        var typeSelectionne = null;
        var encadrantSelectionne = null;
        var encadrants = ${encadrantsJson != null ? encadrantsJson : '[]'};
        
        function ouvrirModal() {
            document.getElementById('modalChoix').style.display = 'flex';
            typeSelectionne = null;
            encadrantSelectionne = null;
            
            // Réinitialiser la sélection visuelle
            document.querySelectorAll('.option-choix').forEach(function(opt) {
                opt.classList.remove('selected');
                opt.querySelector('.fa-check-circle').style.display = 'none';
            });
            
            // Cacher la liste des encadrants
            document.getElementById('encadrantList').style.display = 'none';
        }
        
        function fermerModal() {
            document.getElementById('modalChoix').style.display = 'none';
        }
        
        function selectionnerOption(element) {
            var type = element.getAttribute('data-type');
            
            // Retirer la sélection des autres options
            document.querySelectorAll('.option-choix').forEach(function(opt) {
                opt.classList.remove('selected');
                opt.querySelector('.fa-check-circle').style.display = 'none';
            });
            
            // Sélectionner celle-ci
            element.classList.add('selected');
            element.querySelector('.fa-check-circle').style.display = 'block';
            typeSelectionne = type;
            
            // Afficher/cacher la liste des encadrants
            if (type === 'encadrant') {
                afficherListeEncadrants();
                document.getElementById('encadrantList').style.display = 'block';
            } else {
                document.getElementById('encadrantList').style.display = 'none';
                encadrantSelectionne = null;
            }
        }
        
        function afficherListeEncadrants() {
            var container = document.getElementById('encadrantContainer');
            if (!container) return;
            
            if (encadrants.length === 0) {
                container.innerHTML = '<div class="text-center p-2 text-muted small">Aucun encadrant trouvé</div>';
                return;
            }
            
            var html = '';
            for (var i = 0; i < encadrants.length; i++) {
                var selectedClass = (encadrantSelectionne === encadrants[i].nom) ? 'style="background:#e8f0fe; border-left:3px solid #0033a0;"' : '';
                html += '<div class="encadrant-option" data-nom="' + encodeURIComponent(encadrants[i].nom) + '" ' + selectedClass + 
                        ' onclick="selectionnerEncadrant(\'' + encodeURIComponent(encadrants[i].nom) + '\', this)" style="padding: 8px 12px; margin: 4px 0; border-radius: 8px; cursor: pointer; border-left: 3px solid transparent;">';
                html += '<i class="fas fa-user me-2" style="color:#0033a0;"></i>';
                html += '<strong>' + escapeHtml(encadrants[i].nom) + '</strong>';
                html += '<span class="badge bg-secondary float-end" style="background:#0033a0;">' + encadrants[i].nbPVs + ' PVs</span>';
                html += '</div>';
            }
            container.innerHTML = html;
        }
        
        function selectionnerEncadrant(nom, element) {
            encadrantSelectionne = decodeURIComponent(nom);
            // Mettre à jour le style de tous les éléments
            document.querySelectorAll('.encadrant-option').forEach(function(opt) {
                opt.style.background = '';
                opt.style.borderLeftColor = 'transparent';
            });
            element.style.background = '#e8f0fe';
            element.style.borderLeftColor = '#0033a0';
        }
        
        function escapeHtml(str) {
            if (!str) return '';
            return str.replace(/[&<>]/g, function(m) {
                if (m === '&') return '&amp;';
                if (m === '<') return '&lt;';
                if (m === '>') return '&gt;';
                return m;
            });
        }
        
        function confirmerTelechargement() {
            if (!typeSelectionne) {
                alert('Veuillez sélectionner un type de téléchargement');
                return;
            }
            
            if (typeSelectionne === 'encadrant' && !encadrantSelectionne) {
                alert('Veuillez sélectionner un encadrant');
                return;
            }
            
            fermerModal();
            showLoading();
            
            var url = '';
            if (typeSelectionne === 'tous') {
                url = '${pageContext.request.contextPath}/DownloadPvsServlet?action=tous';
            } else {
                url = '${pageContext.request.contextPath}/DownloadPvsServlet?action=encadrant&nom=' + encodeURIComponent(encadrantSelectionne);
            }
            
            window.open(url, '_blank');
            
            setTimeout(function() {
                hideLoading();
            }, 2000);
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
</html>s