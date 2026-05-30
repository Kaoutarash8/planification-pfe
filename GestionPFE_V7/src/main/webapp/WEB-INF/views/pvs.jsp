<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PVs - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        body { background: #f5f7fa; font-family: 'Segoe UI', sans-serif; min-height: 100vh; display: flex; flex-direction: column; }
        .main-content { flex: 1; padding: 50px 0; }
        .page-title { text-align: center; margin-bottom: 40px; }
        .page-title h2 { color: #0033a0; font-size: 1.8rem; font-weight: 600; }
        .card-pv { background: white; border-radius: 16px; padding: 30px; text-align: center; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }
        .btn-download { background: #0033a0; color: white; border: none; padding: 12px 30px; border-radius: 8px; font-weight: 500; cursor: pointer; }
        .btn-download:hover { background: #002a86; transform: translateY(-2px); }
        .alert-success-custom { background: #e8f0fe; border-left: 4px solid #0033a0; padding: 15px; border-radius: 12px; margin-bottom: 30px; }
        .modal-choix { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 2000; justify-content: center; align-items: center; }
        .modal-content { background: white; border-radius: 16px; width: 90%; max-width: 450px; }
        .modal-header { padding: 20px; border-bottom: 1px solid #e9ecef; }
        .modal-body { padding: 20px; max-height: 400px; overflow-y: auto; }
        .modal-footer { padding: 15px 20px; border-top: 1px solid #e9ecef; display: flex; justify-content: flex-end; gap: 10px; }
        .option-choix { display: flex; align-items: center; padding: 15px; margin: 10px 0; border: 1px solid #e0e0e0; border-radius: 12px; cursor: pointer; }
        .option-choix:hover { border-color: #0033a0; background: #e8f0fe; }
        .option-choix.selected { border-color: #0033a0; background: #e8f0fe; border-width: 2px; }
        .option-icon { font-size: 1.8rem; width: 50px; text-align: center; }
        .option-info { flex: 1; }
        .option-info h6 { margin: 0; font-size: 1rem; font-weight: 600; }
        .option-info p { margin: 5px 0 0; font-size: 0.75rem; color: #666; }
        .btn-cancel { background: #6c757d; color: white; border: none; padding: 8px 20px; border-radius: 6px; }
        .btn-confirm { background: #0033a0; color: white; border: none; padding: 8px 20px; border-radius: 6px; }
        .loading-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: none; justify-content: center; align-items: center; z-index: 2100; }
        .loading-spinner { background: white; padding: 30px; border-radius: 16px; text-align: center; }
        .loading-spinner i { font-size: 2rem; color: #0033a0; animation: spin 1s linear infinite; }
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .encadrant-option { padding: 10px 12px; margin: 5px 0; border-radius: 8px; cursor: pointer; border-left: 3px solid transparent; }
        .encadrant-option:hover { background: #e8f0fe; }
        .badge-pv { background: #0033a0; color: white; padding: 2px 8px; border-radius: 20px; font-size: 0.7rem; float: right; }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="page-title">
                <h2><i class="bi bi-file-text-fill me-2"></i> Procès-Verbaux de soutenance</h2>
                <p>Téléchargez les PVs par encadrant ou globalement</p>
            </div>
            
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
            
            <c:if test="${empty error}">
                <div class="alert-success-custom">
                    <i class="bi bi-check-circle-fill me-2"></i>
                    <strong>PVs disponibles !</strong> Les procès-verbaux sont prêts à être téléchargés.
                </div>
            </c:if>
            
            <div class="row">
                <div class="col-md-6 mx-auto">
                    <div class="card-pv">
                        <i class="bi bi-file-pdf-fill" style="font-size: 4rem; color: #0033a0;"></i>
                        <h4 class="mt-3">Procès-Verbaux</h4>
                        <p class="text-muted">Tous les PVs des soutenances PFE</p>
                        <button onclick="ouvrirModal()" class="btn-download">
                            <i class="bi bi-download me-2"></i>Télécharger les PVs
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <%@ include file="/footer.jsp" %>
    
    <div id="modalChoix" class="modal-choix">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="mb-0"><i class="bi bi-download me-2"></i>Choisir le type de téléchargement</h5>
            </div>
            <div class="modal-body">
                <p>Sélectionnez comment vous souhaitez télécharger les PVs</p>
                
                <div class="option-choix" data-type="tous" onclick="selectionnerOption(this)">
                    <div class="option-icon"><i class="bi bi-globe"></i></div>
                    <div class="option-info">
                        <h6>Tous les PVs</h6>
                        <p>Télécharger l'ensemble des procès-verbaux (ZIP unique)</p>
                    </div>
                </div>
                
                <div class="option-choix" data-type="encadrant" onclick="selectionnerOption(this)">
                    <div class="option-icon"><i class="bi bi-person-badge"></i></div>
                    <div class="option-info">
                        <h6>Par encadrant</h6>
                        <p>Choisir un encadrant spécifique pour télécharger ses PVs</p>
                    </div>
                </div>
                
                <div id="encadrantList" style="display: none; margin-top: 15px;">
                    <p class="small text-muted mb-2"><i class="bi bi-people"></i> Sélectionnez un encadrant :</p>
                    <div id="encadrantContainer" style="max-height: 200px; overflow-y: auto;"></div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn-cancel" onclick="fermerModal()">Annuler</button>
                <button class="btn-confirm" onclick="confirmerTelechargement()">Télécharger</button>
            </div>
        </div>
    </div>
    
    <div class="loading-overlay" id="loadingOverlay">
        <div class="loading-spinner">
            <i class="bi bi-arrow-repeat"></i>
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
            document.querySelectorAll('.option-choix').forEach(opt => {
                opt.classList.remove('selected');
            });
            document.getElementById('encadrantList').style.display = 'none';
        }
        
        function fermerModal() {
            document.getElementById('modalChoix').style.display = 'none';
        }
        
        function selectionnerOption(element) {
            var type = element.getAttribute('data-type');
            document.querySelectorAll('.option-choix').forEach(opt => {
                opt.classList.remove('selected');
            });
            element.classList.add('selected');
            typeSelectionne = type;
            
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
                html += '<div class="encadrant-option" data-nom="' + encodeURIComponent(encadrants[i].nom) + '" onclick="selectionnerEncadrant(this)">';
                html += '<i class="bi bi-person-circle me-2"></i>';
                html += '<strong>' + encadrants[i].nom + '</strong>';
                html += '<span class="badge-pv">' + (encadrants[i].nbPVs || 0) + ' PVs</span>';
                html += '</div>';
            }
            container.innerHTML = html;
        }
        
        function selectionnerEncadrant(element) {
            encadrantSelectionne = decodeURIComponent(element.getAttribute('data-nom'));
            document.querySelectorAll('.encadrant-option').forEach(opt => {
                opt.style.background = '';
                opt.style.borderLeftColor = 'transparent';
            });
            element.style.background = '#e8f0fe';
            element.style.borderLeftColor = '#0033a0';
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
            document.getElementById('loadingOverlay').style.display = 'flex';
            
            var url = '${pageContext.request.contextPath}/DownloadPvsServlet?action=' + typeSelectionne;
            if (typeSelectionne === 'encadrant') {
                url += '&nom=' + encodeURIComponent(encadrantSelectionne);
            }
            
            window.open(url, '_blank');
            
            setTimeout(function() {
                document.getElementById('loadingOverlay').style.display = 'none';
            }, 2000);
        }
        
        window.onclick = function(event) {
            var modal = document.getElementById('modalChoix');
            if (event.target === modal) {
                fermerModal();
            }
        }
    </script>
</body>
</html>