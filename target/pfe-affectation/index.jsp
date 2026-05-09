<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Accueil - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f7fa;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        
        .main-content {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 60px 0;
        }
        
        .welcome-section {
            text-align: center;
            margin-bottom: 50px;
        }
        
        .welcome-section h2 {
            color: #0033a0;
            font-size: 2rem;
            font-weight: 600;
            margin-bottom: 10px;
        }
        
        .welcome-section p {
            color: #6c757d;
            font-size: 1rem;
        }
        
        .card-home {
            background: white;
            border-radius: 16px;
            padding: 35px 25px;
            text-align: center;
            height: 100%;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
            border: 1px solid #e9ecef;
        }
        
        .card-home:hover {
            transform: translateY(-8px);
            box-shadow: 0 15px 35px rgba(0, 51, 160, 0.15);
            border-color: #0033a0;
        }
        
        .card-icon {
            width: 80px;
            height: 80px;
            background: #e8f0fe;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
        }
        
        .card-icon i {
            font-size: 38px;
            color: #0033a0;
        }
        
        .card-home h3 {
            font-size: 1.4rem;
            font-weight: 600;
            color: #0033a0;
            margin-bottom: 12px;
        }
        
        .card-home p {
            font-size: 0.9rem;
            color: #6c757d;
            margin: 0;
        }
        
        @media (max-width: 768px) {
            .main-content {
                padding: 40px 0;
            }
            .card-home {
                padding: 25px 20px;
            }
            .card-icon {
                width: 60px;
                height: 60px;
            }
            .card-icon i {
                font-size: 28px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="header.jsp" %>
    
    <div class="main-content">
        <div class="container">
            <div class="welcome-section">
                <h2>Plateforme PFE</h2>
                <p>Gérez l'affectation des encadrants et le planning des soutenances</p>
            </div>
            
            <div class="row g-4">
                <div class="col-md-4">
                    <div class="card-home" onclick="window.location.href='${pageContext.request.contextPath}/affectation'">
                        <div class="card-icon">
                            <i class="bi bi-people-fill"></i>
                        </div>
                        <h3>Affectation</h3>
                        <p>Générer l'affectation équilibrée des encadrants de PFE</p>
                    </div>
                </div>
                <div class="col-md-4">
    <div class="card-home" onclick="window.location.href='${pageContext.request.contextPath}/planning'">
        <div class="card-icon">
            <i class="bi bi-calendar-week-fill"></i>
        </div>
        <h3>Planning</h3>
        <p>Générer le planning des soutenances</p>
    </div>
</div>
                <div class="col-md-4">
                    <div class="card-home" onclick="window.location.href='#'">
                        <div class="card-icon">
                            <i class="bi bi-file-text-fill"></i>
                        </div>
                        <h3>PVs</h3>
                        <p>Générer les procès-verbaux des soutenances</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <%@ include file="footer.jsp" %>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>