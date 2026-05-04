<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
            padding: 0.6rem 1.5rem;
            border-radius: 4px;
            transition: all 0.3s ease;
            text-decoration: none;
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
                <strong>Planning généré avec succès !</strong> Le planning des soutenances a été créé.
            </div>
            
            <div class="row">
                <div class="col-md-6 mx-auto text-center">
                    <div class="card border-0 shadow-sm" style="border-radius: 4px;">
                        <div class="card-body">
                            <i class="fas fa-calendar-alt" style="font-size: 3rem; color: #0033a0;"></i>
                            <h4 class="mt-3">Planning des Soutenances</h4>
                            <p class="text-muted">Fichier Excel avec toutes les soutenances planifiées</p>
                            <a href="uploads/planning_${timestampPlanning}.xlsx" class="btn-download" style="display: inline-block;">
                                <i class="fas fa-download me-2"></i>Télécharger le Planning
                            </a>
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
            <p class="mb-0">© 2024 - ENSA Al Hoceima | Planification des Soutenances PFE</p>
        </div>
    </footer>
</body>
</html>