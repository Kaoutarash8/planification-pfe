<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Générer l'Affectation - ENSA Al Hoceima</title>
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
        
        .form-container {
            background: white;
            border-radius: 4px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
            padding: 2rem;
            max-width: 800px;
            margin: 0 auto;
        }
        
        .form-title {
            color: #0033a0;
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 0.5rem;
            text-align: center;
        }
        
        .form-subtitle {
            color: #666;
            text-align: center;
            margin-bottom: 2rem;
            font-size: 0.9rem;
        }
        
        .upload-area {
            border: 2px dashed #ccc;
            border-radius: 8px;
            padding: 2rem;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s ease;
            background: #fafafa;
        }
        
        .upload-area:hover {
            border-color: #0033a0;
            background: #f0f4ff;
        }
        
        .upload-area.dragover {
            border-color: #0033a0;
            background: #e8eef8;
        }
        
        .upload-icon {
            font-size: 3rem;
            color: #0033a0;
            margin-bottom: 1rem;
        }
        
        .upload-text {
            color: #666;
            font-size: 0.9rem;
        }
        
        .upload-text strong {
            color: #0033a0;
        }
        
        .file-info {
            margin-top: 1rem;
            padding: 0.5rem;
            background: #e8f0fe;
            border-radius: 4px;
            font-size: 0.85rem;
            color: #0033a0;
        }
        
        .btn-generer {
            background: #0033a0;
            color: white;
            border: none;
            padding: 0.8rem 2rem;
            font-size: 1rem;
            font-weight: 500;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s ease;
            width: 100%;
            margin-top: 1rem;
        }
        
        .btn-generer:hover {
            background: #002a86;
            transform: translateY(-1px);
        }
        
        .btn-retour {
            background: white;
            color: #0033a0;
            border: 1px solid #0033a0;
            padding: 0.6rem 1.5rem;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            margin-top: 1rem;
        }
        
        .btn-retour:hover {
            background: #f0f4ff;
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
            z-index: 9999;
        }
        
        .loading-spinner {
            text-align: center;
            background: white;
            padding: 2rem;
            border-radius: 4px;
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
        
        .ensa-footer {
            background: #0033a0;
            color: white;
            padding: 1.5rem 0;
            margin-top: auto;
            text-align: center;
            font-size: 0.8rem;
        }
        
        @media (max-width: 768px) {
            .form-container {
                margin: 0 1rem;
                padding: 1.5rem;
            }
        }
    </style>
</head>
<body>
    <div class="loading-overlay" id="loadingOverlay">
        <div class="loading-spinner">
            <i class="fas fa-spinner"></i>
            <p class="mt-2">Génération de l'affectation en cours...</p>
        </div>
    </div>

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
        <div class="container">
            <div class="form-container">
                <div class="form-title">
                    <i class="fas fa-users me-2"></i>Générer l'Affectation
                </div>
                <div class="form-subtitle">
                    Téléchargez les fichiers Excel des étudiants et des professeurs
                </div>
                
                <form id="affectationForm" action="${pageContext.request.contextPath}/affectation" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="generer">
                    
                    <!-- Upload Étudiants -->
                    <div class="mb-4">
                        <label class="form-label fw-bold mb-2">
                            <i class="fas fa-user-graduate me-1" style="color:#0033a0;"></i> 
                            Fichier des Étudiants
                        </label>
                        <div class="upload-area" id="uploadAreaEtudiants">
                            <i class="fas fa-file-excel upload-icon"></i>
                            <p class="upload-text">
                                <strong>Cliquez ou glissez-déposez</strong> le fichier Excel<br>
                                Format: .xlsx | Colonnes: CNE, NOM, PRENOM, EMAIL PERSONNEL, EMAIL ACADEMIQUE, FILIERE
                            </p>
                            <input type="file" id="fichierEtudiants" name="fichierEtudiants" class="d-none" accept=".xlsx">
                            <div id="fileInfoEtudiants" class="file-info" style="display: none;"></div>
                        </div>
                    </div>
                    
                    <!-- Upload Professeurs -->
                    <div class="mb-4">
                        <label class="form-label fw-bold mb-2">
                            <i class="fas fa-chalkboard-user me-1" style="color:#0033a0;"></i> 
                            Fichier des Professeurs
                        </label>
                        <div class="upload-area" id="uploadAreaProfesseurs">
                            <i class="fas fa-chalkboard upload-icon"></i>
                            <p class="upload-text">
                                <strong>Cliquez ou glissez-déposez</strong> le fichier Excel<br>
                                Format: .xlsx | Colonnes: NOM, PRENOM, SPECIALITE
                            </p>
                            <input type="file" id="fichierProfesseurs" name="fichierProfesseurs" class="d-none" accept=".xlsx">
                            <div id="fileInfoProfesseurs" class="file-info" style="display: none;"></div>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn-generer" id="btnGenerer">
                        <i class="fas fa-magic me-2"></i> Générer l'Affectation
                    </button>
                </form>
                
                <div class="text-center mt-3">
                    <a href="${pageContext.request.contextPath}/affectation" class="btn-retour">
                        <i class="fas fa-arrow-left"></i> Retour à l'accueil
                    </a>
                </div>
            </div>
        </div>
    </div>

    <footer class="ensa-footer">
        <div class="container">
            <p class="mb-0">© 2024 - ENSA Al Hoceima | Planification des Soutenances PFE</p>
        </div>
    </footer>

    <script>
        // Setup upload areas
        const uploadAreaEtudiants = document.getElementById('uploadAreaEtudiants');
        const inputEtudiants = document.getElementById('fichierEtudiants');
        const fileInfoEtudiants = document.getElementById('fileInfoEtudiants');
        
        const uploadAreaProfesseurs = document.getElementById('uploadAreaProfesseurs');
        const inputProfesseurs = document.getElementById('fichierProfesseurs');
        const fileInfoProfesseurs = document.getElementById('fileInfoProfesseurs');
        
        function setupUploadArea(area, input, infoDiv) {
            area.addEventListener('click', () => input.click());
            
            area.addEventListener('dragover', (e) => {
                e.preventDefault();
                area.classList.add('dragover');
            });
            
            area.addEventListener('dragleave', () => {
                area.classList.remove('dragover');
            });
            
            area.addEventListener('drop', (e) => {
                e.preventDefault();
                area.classList.remove('dragover');
                const files = e.dataTransfer.files;
                if (files.length > 0) {
                    input.files = files;
                    updateFileInfo(input, infoDiv);
                }
            });
            
            input.addEventListener('change', () => {
                updateFileInfo(input, infoDiv);
            });
        }
        
        function updateFileInfo(input, infoDiv) {
            if (input.files && input.files[0]) {
                const file = input.files[0];
                const fileSize = (file.size / 1024).toFixed(2);
                infoDiv.innerHTML = `<i class="fas fa-check-circle me-1"></i> ${file.name} (${fileSize} KB)`;
                infoDiv.style.display = 'block';
            } else {
                infoDiv.style.display = 'none';
            }
        }
        
        setupUploadArea(uploadAreaEtudiants, inputEtudiants, fileInfoEtudiants);
        setupUploadArea(uploadAreaProfesseurs, inputProfesseurs, fileInfoProfesseurs);
        
        // Form submission
        document.getElementById('affectationForm').addEventListener('submit', function(e) {
            const btn = document.getElementById('btnGenerer');
            const etudiantsFile = document.getElementById('fichierEtudiants').files[0];
            const professeursFile = document.getElementById('fichierProfesseurs').files[0];
            
            if (!etudiantsFile) {
                e.preventDefault();
                alert('Veuillez sélectionner le fichier des étudiants');
                return;
            }
            
            if (!professeursFile) {
                e.preventDefault();
                alert('Veuillez sélectionner le fichier des professeurs');
                return;
            }
            
            document.getElementById('loadingOverlay').style.display = 'flex';
            btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Génération...';
            btn.disabled = true;
        });
    </script>
</body>
</html>