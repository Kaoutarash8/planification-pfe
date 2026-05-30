<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Affectation - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        body { background: #f5f7fa; font-family: 'Segoe UI', sans-serif; min-height: 100vh; display: flex; flex-direction: column; }
        .main-content { flex: 1; padding: 30px 0; }
        .page-title { text-align: center; margin-bottom: 25px; }
        .page-title h2 { color: #0033a0; font-size: 1.5rem; font-weight: 600; display: inline-flex; align-items: center; gap: 8px; }
        .help-icon { cursor: pointer; color: #6c757d; font-size: 1rem; transition: color 0.2s; }
        .help-icon:hover { color: #0033a0; }
        
        /* Card horizontale compacte */
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
        .upload-area:hover, .upload-area.drag-over { border-color: #0033a0; background: #f0f4ff; }
        .upload-area i.main-icon { font-size: 2.5rem; color: #0033a0; display: block; margin-bottom: 10px; }
        .upload-area h5 { font-size: 1rem; font-weight: 600; margin-bottom: 5px; }
        .upload-area p { font-size: 0.8rem; color: #6c757d; margin-bottom: 0; }
        
        .file-selected {
            display: none; margin-top: 15px; padding: 10px 15px;
            background: #e8f0fe; border-radius: 10px; font-size: 0.85rem;
            color: #0033a0; font-weight: 500; align-items: center; gap: 10px;
        }
        .file-selected.show { display: flex; }
        .file-selected .btn-remove { margin-left: auto; background: none; border: none; color: #dc3545; cursor: pointer; }
        
        .btn-generer {
            background: #0033a0; color: white; padding: 10px 30px;
            font-size: 0.9rem; font-weight: 600; border: none; border-radius: 50px;
            cursor: pointer; margin-top: 20px; width: 100%;
        }
        .btn-generer:hover:not(:disabled) { background: #002a86; transform: translateY(-1px); }
        .btn-generer:disabled { opacity: 0.65; cursor: not-allowed; }
        
        .btn-exemple {
            background: #28a745; color: white; padding: 6px 15px;
            font-size: 0.75rem; border: none; border-radius: 50px;
            cursor: pointer; margin-top: 12px; display: inline-flex; align-items: center; gap: 6px;
        }
        .btn-exemple:hover { background: #218838; }
        
        /* Modal */
        .modal-guide .modal-content { border-radius: 16px; }
        .modal-guide .modal-header { background: #0033a0; color: white; border-radius: 16px 16px 0 0; }
        .badge-colonne { background: #e8f0fe; color: #0033a0; padding: 4px 8px; border-radius: 6px; font-size: 0.7rem; margin: 3px; display: inline-block; }
        
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
            <div class="page-title">
                <h2>
                    <i class="bi bi-diagram-3-fill"></i>Affectation des encadrants
                    <i class="bi bi-question-circle-fill help-icon" data-bs-toggle="modal" data-bs-target="#helpModal"></i>
                </h2>
                <p class="small">Importez votre fichier Excel (étudiants + professeurs)</p>
                <button class="btn-exemple" onclick="telechargerExemple()">
                    <i class="bi bi-download"></i> Télécharger un exemple
                </button>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-warning alert-dismissible fade show text-center small" role="alert">
                    ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <form id="affectationForm" action="${pageContext.request.contextPath}/affectation" method="post" enctype="multipart/form-data">
                <div class="upload-card">
                    <div class="upload-area" id="uploadArea">
                        <input type="file" id="fichierCombine" name="fichierCombine" class="d-none" accept=".xlsx,.xls" required>
                        <i class="bi bi-file-earmark-excel-fill main-icon"></i>
                        <h5>Déposez votre fichier Excel ici</h5>
                        <p>ou <strong style="color:#0033a0;">cliquez pour parcourir</strong></p>
                        <p class="small text-muted mt-1">Format .xlsx — max 10 Mo</p>
                    </div>

                    <div class="file-selected" id="fileSelected">
                        <i class="bi bi-file-earmark-check-fill"></i>
                        <span id="fileName">—</span>
                        <span id="fileSize" class="text-muted small"></span>
                        <button type="button" class="btn-remove" onclick="retirerFichier()"><i class="bi bi-x-circle-fill"></i></button>
                    </div>

                    <button type="submit" class="btn-generer" id="btnGenerer" disabled>
                        <i class="bi bi-magic me-2"></i> Générer l'affectation
                    </button>
                </div>
            </form>
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
                        <strong class="small">Feuille 1 — Étudiants</strong>
                        <div class="mt-1">
                            <span class="badge-colonne">CNE</span>
                            <span class="badge-colonne">NOM</span>
                            <span class="badge-colonne">PRENOM</span>
                            <span class="badge-colonne">EMAIL PERSONNEL</span>
                            <span class="badge-colonne">EMAIL ACADEMIQUE</span>
                            <span class="badge-colonne">FILIERE</span>
                        </div>
                    </div>
                    <div>
                        <strong class="small">Feuille 2 — Professeurs</strong>
                        <div class="mt-1">
                            <span class="badge-colonne">NOM</span>
                            <span class="badge-colonne">PRENOM</span>
                            <span class="badge-colonne">SPECIALITE</span>
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
            window.location.href = '${pageContext.request.contextPath}/telecharger-exemple?type=affectation';
        }

        const uploadArea = document.getElementById('uploadArea');
        const fileInput = document.getElementById('fichierCombine');
        const fileSelected = document.getElementById('fileSelected');
        const fileNameEl = document.getElementById('fileName');
        const fileSizeEl = document.getElementById('fileSize');
        const btnGenerer = document.getElementById('btnGenerer');

        uploadArea.onclick = () => fileInput.click();
        uploadArea.ondragover = (e) => { e.preventDefault(); uploadArea.classList.add('drag-over'); };
        uploadArea.ondragleave = () => { uploadArea.classList.remove('drag-over'); };
        uploadArea.ondrop = (e) => {
            e.preventDefault();
            uploadArea.classList.remove('drag-over');
            if (e.dataTransfer.files.length > 0) {
                const dt = new DataTransfer();
                dt.items.add(e.dataTransfer.files[0]);
                fileInput.files = dt.files;
                afficherFichier(e.dataTransfer.files[0]);
            }
        };
        fileInput.onchange = () => { if (fileInput.files[0]) afficherFichier(fileInput.files[0]); };

        function afficherFichier(file) {
            const kb = (file.size / 1024).toFixed(0);
            fileNameEl.textContent = file.name;
            fileSizeEl.textContent = kb > 0 ? `(${kb} Ko)` : '';
            fileSelected.classList.add('show');
            btnGenerer.disabled = false;
        }

        function retirerFichier() {
            fileInput.value = '';
            fileSelected.classList.remove('show');
            btnGenerer.disabled = true;
        }

        document.getElementById('affectationForm').onsubmit = function(e) {
            if (!fileInput.files || fileInput.files.length === 0) {
                e.preventDefault();
                alert('Veuillez sélectionner un fichier Excel.');
            } else {
                btnGenerer.disabled = true;
                btnGenerer.innerHTML = '<i class="bi bi-hourglass-split me-2"></i> Génération...';
            }
        };
    </script>
</body>
</html>