<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historique - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        body { background: #f5f7fa; font-family: 'Segoe UI', sans-serif; min-height: 100vh; display: flex; flex-direction: column; }
        .main-content { flex: 1; padding: 50px 0; }
        .page-title { text-align: center; margin-bottom: 40px; }
        .page-title h2 { color: #0033a0; font-size: 1.8rem; font-weight: 600; }
        .table-custom { background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }
        .table-custom th { background: #0033a0; color: white; font-weight: 600; border: none; }
        .table-custom td { vertical-align: middle; }
        .btn-download { background: #28a745; color: white; padding: 5px 12px; border-radius: 6px; text-decoration: none; font-size: 0.8rem; }
        .btn-download:hover { background: #218838; color: white; }
        .badge-affectation { background: #0033a0; }
        .badge-planning { background: #28a745; }
        .back-link { color: #0033a0; text-decoration: none; }
        .back-link:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="page-title">
                <h2><i class="bi bi-clock-history me-2"></i> Historique des fichiers</h2>
                <p>Tous les fichiers d'affectation et planning generes</p>
            </div>
            
            <div class="table-responsive">
                <table class="table table-custom">
                    <thead>
                        <tr>
                            <th>Type</th>
                            <th>Annee universitaire</th>
                            <th>Nom du fichier</th>
                            <th>Taille</th>
                            <th>Date de modification</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${historique}" var="f">
                            <tr>
                                <td>
                                    <span class="badge ${f.type == 'Affectation' ? 'badge-affectation' : 'badge-planning'}">
                                        ${f.type}
                                    </span>
                                </td>
                                <td><strong>${f.annee}</strong></td>
                                <td>${f.nomFichier}</td>
                                <td>${f.taille}</td>
                                <td>${f.dateModif}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/download?file=${f.nomFichier}" class="btn-download" target="_blank">
                                        <i class="bi bi-download"></i> Telecharger
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty historique}">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-4">
                                    <i class="bi bi-inbox" style="font-size: 2rem;"></i>
                                    <p class="mt-2">Aucun fichier genere</p>
                                    <a href="${pageContext.request.contextPath}/affectation" class="btn btn-primary btn-sm">Generer une affectation</a>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            
            <div class="text-center mt-4">
                <a href="${pageContext.request.contextPath}/" class="back-link">
                    <i class="bi bi-arrow-left"></i> Retour a l'accueil
                </a>
            </div>
        </div>
    </div>
    
    <%@ include file="/footer.jsp" %>
</body>
</html>