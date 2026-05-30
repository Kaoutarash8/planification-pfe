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
        .main-content { flex: 1; padding: 50px 0; }
        .page-title { text-align: center; margin-bottom: 40px; }
        .page-title h2 { color: #0033a0; font-size: 1.8rem; font-weight: 600; }
        .card-upload { background: white; border-radius: 16px; padding: 25px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); border: 1px solid #e9ecef; }
        .upload-area { border: 2px dashed #cbd5e1; border-radius: 12px; padding: 40px 25px; text-align: center; cursor: pointer; transition: all 0.3s; }
        .upload-area:hover { border-color: #0033a0; background: #f0f4ff; }
        .upload-area i { font-size: 3rem; color: #0033a0; margin-bottom: 15px; }
        .btn-generer { background: #0033a0; color: white; padding: 14px 40px; border: none; border-radius: 50px; font-weight: 600; margin-top: 30px; }
        .btn-generer:hover { background: #002a86; transform: translateY(-2px); }
        .format-hint { font-size: 0.7rem; color: #adb5bd; margin-top: 10px; }
        .file-info { margin-top: 15px; padding: 10px 15px; background: #e8f0fe; border-radius: 8px; color: #0033a0; display: inline-block; }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8">
                    <div class="page-title">
                        <h2><i class="bi bi-calendar-week-fill me-2"></i> Planning des soutenances</h2>
                        <p>Téléchargez le fichier de configuration Excel</p>
                    </div>
                    
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">${error}</div>
                    </c:if>
                    
                    <div class="card-upload">
                        <form action="${pageContext.request.contextPath}/planning" method="post" enctype="multipart/form-data">
                            <div class="upload-area" id="uploadAreaConfig">
                                <i class="bi bi-file-earmark-excel-fill"></i>
                                <h5>Fichier de configuration</h5>
                                <p>Cliquez ou glissez-déposez</p>
                                <div class="format-hint">Format: .xlsx | Colonnes: Paramètre, Valeur</div>
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
    
    <%@ include file="/footer.jsp" %>
    
    <script>
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