<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Résultat - Affectation PFE</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        body {
            background: #f5f7fa;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .page-title h2 {
            color: #2c3e50;
            font-weight: 600;
        }

        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 18px;
            text-align: center;
            border: 1px solid #eee;
            transition: 0.2s;
        }

        .stat-card:hover {
            transform: translateY(-3px);
            border-color: #2c3e50;
        }

        .stat-value {
            font-size: 2rem;
            font-weight: 700;
            color: #2c3e50;
        }

        .alert-success-custom {
            background: #eaf7ee;
            border-left: 4px solid #28a745;
            padding: 12px 16px;
            border-radius: 10px;
        }

        .download-box {
            background: white;
            border-radius: 12px;
            padding: 25px;
            border: 1px solid #eee;
            text-align: center;
        }

        .btn-container {
            display: flex;
            justify-content: center;
            gap: 20px;
            flex-wrap: wrap;
            margin-top: 5px;
        }

        /* Boutons professionnels de même taille */
        .btn-professional {
            padding: 12px 30px;
            font-size: 1rem;
            font-weight: 600;
            border-radius: 10px;
            transition: all 0.3s ease;
            min-width: 220px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
        }

        .btn-excel {
            background: #28a745;
            color: white;
            border: none;
        }

        .btn-excel:hover {
            background: #218838;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(40, 167, 69, 0.3);
            color: white;
        }

        .btn-new {
            background: #2c3e50;
            color: white;
            border: none;
        }

        .btn-new:hover {
            background: #1f2d3a;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(44, 62, 80, 0.3);
            color: white;
        }

        /* Réduction légère des espaces */
        .container.py-5 {
            padding-top: 2.5rem !important;
            padding-bottom: 1.5rem !important;
        }

        @media (max-width: 576px) {
            .btn-professional {
                min-width: 100%;
                padding: 10px 20px;
            }
            .btn-container {
                flex-direction: column;
                gap: 10px;
            }
        }
    </style>
</head>

<body>

<%@ include file="/header.jsp" %>

<div class="container py-5">

    <!-- TITRE -->
    <div class="text-center mb-4 page-title">
        <h2><i class="bi bi-check-circle-fill"></i> Résultat de l'affectation</h2>
        <p class="text-muted">
            Généré le <fmt:formatDate value="${affectation.dateCreation}" pattern="dd/MM/yyyy HH:mm"/>
        </p>
    </div>

    <!-- SUCCESS -->
    <div class="alert-success-custom mb-4 text-center">
        <strong>Affectation générée avec succès</strong>
    </div>

    <!-- STATS -->
    <div class="row g-3 mb-4">
        <div class="col-md-3 col-6">
            <div class="stat-card">
                <div class="stat-value">${affectation.totalEtudiants}</div>
                <small>Étudiants</small>
            </div>
        </div>

        <div class="col-md-3 col-6">
            <div class="stat-card">
                <div class="stat-value">${affectation.totalProfesseurs}</div>
                <small>Professeurs</small>
            </div>
        </div>

        <div class="col-md-3 col-6">
            <div class="stat-card">
                <div class="stat-value">${affectation.minParProfesseur}</div>
                <small>Min / Prof</small>
            </div>
        </div>

        <div class="col-md-3 col-6">
            <div class="stat-card">
                <div class="stat-value">${affectation.maxParProfesseur}</div>
                <small>Max / Prof</small>
            </div>
        </div>
    </div>

    <!-- DOWNLOAD -->
    <div class="download-box">
        <div class="btn-container">
           <a href="${pageContext.request.contextPath}/${excelPath}" 
   class="btn-professional btn-excel"
   download="${nomFichier}">
   <i class="bi bi-file-earmark-excel-fill"></i> Télécharger Excel
</a>

            <a href="${pageContext.request.contextPath}/" 
               class="btn-professional btn-new">
                <i class="bi bi-plus-circle"></i> Accueil
            </a>
        </div>
    </div>

</div>

<%@ include file="/footer.jsp" %>

</body>
</html>