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
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f7fa;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .main-content { flex: 1; padding: 50px 0; }
        .page-title { text-align: center; margin-bottom: 40px; }
        .page-title h2 { color: #0033a0; font-size: 1.8rem; font-weight: 600; }
        .card-upload {
            background: white;
            border-radius: 16px;
            padding: 25px;
            height: 100%;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
            border: 1px solid #e9ecef;
        }
        .card-upload h4 {
            font-size: 1.2rem;
            font-weight: 600;
            color: #0033a0;
            border-bottom: 2px solid #e8f0fe;
            padding-bottom: 12px;
            margin-bottom: 20px;
        }
        .upload-area {
            border: 2px dashed #cbd5e1;
            border-radius: 12px;
            padding: 40px 25px;
            text-align: center;
            cursor: pointer;
            background: #fafcff;
            transition: all 0.3s;
        }
        .upload-area:hover { border-color: #0033a0; background: #f0f4ff; }
        .upload-area i { font-size: 3rem; color: #0033a0; margin-bottom: 15px; }
        .upload-area h5 { font-size: 1rem; font-weight: 600; margin-bottom: 8px; }
        .upload-area p { font-size: 0.85rem; color: #6c757d; margin: 0; }
        .format-hint { font-size: 0.7rem; color: #adb5bd; margin-top: 10px; }
        .file-info {
            margin-top: 15px;
            padding: 10px 15px;
            background: #e8f0fe;
            border-radius: 8px;
            font-size: 0.85rem;
            color: #0033a0;
            display: inline-block;
        }
        .btn-generer {
            background: #0033a0;
            color: white;
            padding: 14px 40px;
            font-size: 1rem;
            font-weight: 600;
            border: none;
            border-radius: 50px;
            cursor: pointer;
            margin-top: 30px;
        }
        .btn-generer:hover { background: #002a86; transform: translateY(-2px); }
        .btn-generer:disabled { opacity: 0.7; }
        .alert-custom { border-radius: 12px; background: #fef2e8; color: #e67e22; margin-bottom: 30px; }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="page-title">
                <h2>Affectation des encadrants</h2>
                <p>Téléchargez les fichiers Excel des étudiants et des professeurs</p>
            </div>
            
            <c:if test="${not empty error}">
                <div class="alert alert-custom alert-dismissible fade show">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            
            <form id="affectationForm" action="${pageContext.request.contextPath}/affectation" method="post" enctype="multipart/form-data">
                <div class="row g-4">
                    <div class="col-md-6">
                        <div class="card-upload">
                            <h4><i class="bi bi-people-fill me-2"></i> Fichier des étudiants</h4>
                            <div class="upload-area" id="uploadAreaEtudiants">
                                <i class="bi bi-file-earmark-excel-fill"></i>
                                <h5>Excel des étudiants</h5>
                                <p>Cliquez ou glissez-déposez</p>
                                <div class="format-hint">Format: .xlsx | Colonnes: NOM, PRENOM, FILIERE</div>
                                <input type="file" id="fichierEtudiants" name="fichierEtudiants" class="d-none" accept=".xlsx,.xls" required>
                                <div id="fileInfoEtudiants" class="file-info" style="display: none;"></div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-6">
                        <div class="card-upload">
                            <h4><i class="bi bi-person-badge-fill me-2"></i> Fichier des professeurs</h4>
                            <div class="upload-area" id="uploadAreaProfesseurs">
                                <i class="bi bi-file-earmark-excel-fill"></i>
                                <h5>Excel des professeurs</h5>
                                <p>Cliquez ou glissez-déposez</p>
                                <div class="format-hint">Format: .xlsx | Colonnes: NOM, PRENOM, SPECIALITE</div>
                                <input type="file" id="fichierProfesseurs" name="fichierProfesseurs" class="d-none" accept=".xlsx,.xls" required>
                                <div id="fileInfoProfesseurs" class="file-info" style="display: none;"></div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="text-center">
                    <button type="submit" class="btn-generer" id="btnGenerer">
                        <i class="bi bi-magic me-2"></i> Générer l'affectation
                    </button>
                </div>
            </form>
        </div>
    </div>
    
    <%@ include file="/footer.jsp" %>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function setupUpload(areaId, inputId, infoId) {
            const area = document.getElementById(areaId);
            const input = document.getElementById(inputId);
            const info = document.getElementById(infoId);
            
            area.onclick = () => input.click();
            area.ondragover = (e) => { e.preventDefault(); area.style.borderColor = "#0033a0"; area.style.background = "#f0f4ff"; };
            area.ondragleave = () => { area.style.borderColor = "#cbd5e1"; area.style.background = "#fafcff"; };
            area.ondrop = (e) => {
                e.preventDefault();
                area.style.borderColor = "#cbd5e1";
                area.style.background = "#fafcff";
                const files = e.dataTransfer.files;
                if (files.length > 0) {
                    input.files = files;
                    updateFileInfo(input, info);
                }
            };
            input.onchange = () => updateFileInfo(input, info);
        }
        
        function updateFileInfo(input, info) {
            if (input.files && input.files[0]) {
                const file = input.files[0];
                const fileSize = (file.size / 1024).toFixed(2);
                info.innerHTML = `<i class="bi bi-check-circle-fill me-1"></i> ${file.name} (${fileSize} KB)`;
                info.style.display = "inline-block";
            } else {
                info.style.display = "none";
            }
        }
        
        setupUpload("uploadAreaEtudiants", "fichierEtudiants", "fileInfoEtudiants");
        setupUpload("uploadAreaProfesseurs", "fichierProfesseurs", "fileInfoProfesseurs");
        
        document.getElementById('affectationForm').addEventListener('submit', function(e) {
            const etudiants = document.getElementById('fichierEtudiants').files[0];
            const professeurs = document.getElementById('fichierProfesseurs').files[0];
            const btn = document.getElementById('btnGenerer');
            
            if (!etudiants) {
                e.preventDefault();
                alert('Veuillez sélectionner le fichier des étudiants');
                return;
            }
            if (!professeurs) {
                e.preventDefault();
                alert('Veuillez sélectionner le fichier des professeurs');
                return;
            }
            
            btn.disabled = true;
            btn.innerHTML = '<i class="bi bi-hourglass-split me-2"></i> Génération en cours...';
        });
    </script>
</body>
</html>