<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>PVs Générés - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body { background: #f5f5f5; font-family: 'Segoe UI', sans-serif; }
        .header { background: #0033a0; color: white; padding: 1rem 0; }
        .card { border-radius: 4px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .btn-download { background: #0033a0; color: white; border-radius: 4px; }
        .btn-download:hover { background: #002a86; }
        .pv-list { max-height: 400px; overflow-y: auto; }
        .pv-item { border-bottom: 1px solid #eee; padding: 8px; }
        .pv-item:hover { background: #f0f4ff; }
    </style>
</head>
<body>
    <div class="header">
        <div class="container text-center">
            <i class="fas fa-file-alt fa-2x"></i>
            <h3 class="mt-2">Génération des Procès-Verbaux</h3>
        </div>
    </div>
    
    <div class="container mt-4">
        <div class="card p-4">
            <div class="text-center">
                <i class="fas fa-check-circle fa-4x text-success mb-3"></i>
                <h4>PVs générés avec succès !</h4>
                <p><c:out value="${sessionScope.fichiersPV.size()}"/> fichiers ont été créés.</p>
            </div>
            
            <div class="row mt-4">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header bg-white">
                            <strong><i class="fas fa-file-pdf me-2"></i>Fichiers PDF</strong>
                        </div>
                        <div class="pv-list">
                            <c:forEach items="${sessionScope.fichiersPV}" var="fichier">
                                <c:if test="${fichier.endsWith('.pdf')}">
                                    <div class="pv-item">
                                        <i class="fas fa-file-pdf text-danger me-2"></i>
                                        <a href="${pageContext.request.contextPath}/uploads/pvs/${fichier}" download class="text-decoration-none">
                                            ${fichier}
                                        </a>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header bg-white">
                            <strong><i class="fas fa-file-word me-2"></i>Fichiers DOCX</strong>
                        </div>
                        <div class="pv-list">
                            <c:forEach items="${sessionScope.fichiersPV}" var="fichier">
                                <c:if test="${fichier.endsWith('.docx')}">
                                    <div class="pv-item">
                                        <i class="fas fa-file-word text-primary me-2"></i>
                                        <a href="${pageContext.request.contextPath}/uploads/pvs/${fichier}" download class="text-decoration-none">
                                            ${fichier}
                                        </a>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="text-center mt-4">
                <a href="${pageContext.request.contextPath}/affectation?page=accueil" class="btn btn-download">
                    <i class="fas fa-home me-2"></i>Retour à l'accueil
                </a>
            </div>
            
            <div class="alert alert-info mt-4 small">
                <i class="fas fa-info-circle me-2"></i>
                Les fichiers sont sauvegardés dans : <strong>C:\Users\e\workspace-pfe\pfe-affectation\uploads\pvs\</strong>
            </div>
        </div>
    </div>
</body>
</html>