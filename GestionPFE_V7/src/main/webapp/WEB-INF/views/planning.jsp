<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Planning - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        body { background: #f5f7fa; font-family: 'Segoe UI', sans-serif; min-height: 100vh; display: flex; flex-direction: column; }
        .main-content { flex: 1; padding: 30px 0; }
        .page-title { text-align: center; margin-bottom: 25px; }
        .page-title h2 { color: #0033a0; font-size: 1.5rem; font-weight: 600; display: inline-flex; align-items: center; gap: 8px; }
        .help-icon { cursor: pointer; color: #6c757d; font-size: 1rem; transition: color 0.2s; }
        .help-icon:hover { color: #0033a0; }
        
        .upload-card {
            background: white;
            border-radius: 16px;
            padding: 25px 30px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.05);
            border: 1px solid #e9ecef;
            max-width: 600px;
            margin: 0 auto;
        }
        
        .upload-area {
            border: 2px dashed #cbd5e1;
            border-radius: 14px;
            padding: 25px 20px;
            text-align: center;
            cursor: pointer;
            background: #fafcff;
            transition: all 0.3s;
        }
        .upload-area:hover { border-color: #0033a0; background: #f0f4ff; }
        .upload-area i { font-size: 2.5rem; color: #0033a0; margin-bottom: 10px; display: block; }
        .upload-area h5 { font-size: 1rem; font-weight: 600; margin-bottom: 5px; }
        .upload-area p { font-size: 0.8rem; color: #6c757d; margin-bottom: 0; }
        
        .btn-generer {
            background: #0033a0; color: white; padding: 10px 30px;
            font-size: 0.9rem; font-weight: 600; border: none; border-radius: 50px;
            cursor: pointer; margin-top: 20px; width: 100%;
        }
        .btn-generer:hover { background: #002a86; transform: translateY(-1px); }
        
        .btn-exemple {
            background: #28a745; color: white; padding: 6px 15px;
            font-size: 0.75rem; border: none; border-radius: 50px;
            cursor: pointer; margin-top: 12px; display: inline-flex; align-items: center; gap: 6px;
        }
        .btn-exemple:hover { background: #218838; }
        
        .file-info {
            margin-top: 12px; padding: 8px 12px; background: #e8f0fe;
            border-radius: 8px; font-size: 0.8rem; color: #0033a0; display: inline-block;
        }
        
        .modal-guide .modal-content { border-radius: 16px; }
        .modal-guide .modal-header { background: #0033a0; color: white; border-radius: 16px 16px 0 0; }
        .param-badge { background: #e8f0fe; color: #0033a0; padding: 4px 8px; border-radius: 6px; font-size: 0.7rem; margin: 3px; display: inline-block; font-family: monospace; }
        
        @media (max-width: 768px) {
            .upload-card { padding: 20px; margin: 0 15px; }
            .upload-area { padding: 20px 15px; }
        }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8">
                    <div class="page-title">
                        <h2>
                            <i class="bi bi-calendar-week-fill"></i>Planning des soutenances
                            <i class="bi bi-question-circle-fill help-icon" data-bs-toggle="modal" data-bs-target="#helpModal"></i>
                        </h2>
                        <p class="small">Téléchargez le fichier de configuration Excel</p>
                        <button class="btn-exemple" onclick="telechargerExemple()">
                            <i class="bi bi-download"></i> Télécharger un exemple
                        </button>
                    </div>
                    
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger text-center small">${error}</div>
                    </c:if>
                    
                    <div class="upload-card">
                        <form action="${pageContext.request.contextPath}/planning" method="post" enctype="multipart/form-data">
                            <div class="upload-area" id="uploadAreaConfig">
                                <i class="bi bi-file-earmark-excel-fill"></i>
                                <h5>Fichier de configuration</h5>
                                <p>Cliquez ou glissez-déposez</p>
                                <p class="small text-muted mt-1">Format .xlsx | Colonnes: Paramètre, Valeur</p>
                                <input type="file" id="fichierConfiguration" name="fichierConfiguration" class="d-none" accept=".xlsx,.xls" required>
                                <div id="fileInfoConfig" class="file-info" style="display: none;"></div>
                            </div>
                            
                            <div class="text-center">
                                <button type="submit" class="btn-generer">
                                    <i class="bi bi-magic me-2"></i> Générer le planning
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal aide -->
    <div class="modal fade modal-guide" id="helpModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-sm">
            <div class="modal-content">
                <div class="modal-header py-2">
                    <h6 class="modal-title"><i class="bi bi-file-earmark-excel-fill me-1"></i>Structure du fichier</h6>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body p-3">
                    <div class="mb-2">
                        <strong class="small">Colonnes :</strong>
                        <div class="d-flex gap-2 mt-1">
                            <span class="badge bg-primary">Paramètre</span>
                            <span class="badge bg-secondary">Valeur</span>
                        </div>
                    </div>
                    <div>
                        <strong class="small">Paramètres disponibles :</strong>
                        <div class="mt-1">
                            <span class="param-badge">duree_soutenance_minutes</span>
                            <span class="param-badge">heure_debut_matin</span>
                            <span class="param-badge">heure_fin_matin</span>
                            <span class="param-badge">heure_debut_apres_midi</span>
                            <span class="param-badge">heure_fin_apres_midi</span>
                            <span class="param-badge">pause_max_sans_soutenance_heures</span>
                            <span class="param-badge">salles</span>
                            <span class="param-badge">date_debut_soutenance</span>
                            <span class="param-badge">jours</span>
                            <span class="param-badge">InfoProf_min</span>
                        </div>
                    </div>
                    <hr class="my-2">
                    <div class="text-center">
                        <button class="btn btn-sm btn-success" onclick="telechargerExemple()">
                            <i class="bi bi-download"></i> Télécharger exemple
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <%@ include file="/footer.jsp" %>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function telechargerExemple() {
            window.location.href = '${pageContext.request.contextPath}/telecharger-exemple?type=planning';
        }

        const area = document.getElementById('uploadAreaConfig');
        const input = document.getElementById('fichierConfiguration');
        const info = document.getElementById('fileInfoConfig');
        
        area.onclick = () => input.click();
        area.ondragover = (e) => { e.preventDefault(); area.style.borderColor = "#0033a0"; area.style.background = "#f0f4ff"; };
        area.ondragleave = () => { area.style.borderColor = "#cbd5e1"; area.style.background = "#fafcff"; };
        area.ondrop = (e) => {
            e.preventDefault();
            area.style.borderColor = "#cbd5e1";
            area.style.background = "#fafcff";
            if (e.dataTransfer.files.length > 0) {
                input.files = e.dataTransfer.files;
                if (input.files[0]) {
                    info.innerHTML = `<i class="bi bi-check-circle-fill me-1"></i> ${input.files[0].name}`;
                    info.style.display = "inline-block";
                }
            }
        };
        input.onchange = () => {
            if (input.files[0]) {
                info.innerHTML = `<i class="bi bi-check-circle-fill me-1"></i> ${input.files[0].name}`;
                info.style.display = "inline-block";
            }
        };
    </script>
</body>
</html>