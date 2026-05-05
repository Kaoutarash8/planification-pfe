<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Guide d'utilisation - ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: system-ui, 'Segoe UI', 'Inter', sans-serif;
            background: linear-gradient(145deg, #f4f7fc 0%, #e9eef3 100%);
        }
        .main-header {
            background: rgba(255, 255, 255, 0.96);
            backdrop-filter: blur(2px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
            border-bottom: 1px solid rgba(0, 0, 0, 0.05);
            position: sticky;
            top: 0;
            z-index: 1000;
        }
        .header-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 1rem 2rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
        }
        .brand-name {
            font-size: 1.8rem;
            font-weight: 700;
            color: #0033a0;
            text-decoration: none;
        }
        .brand-tag {
            display: block;
            font-size: 0.7rem;
            color: #0033a0;
        }
        .nav-links {
            display: flex;
            gap: 2rem;
            list-style: none;
        }
        .nav-link {
            text-decoration: none;
            color: #0033a0;
            font-weight: 500;
            transition: all 0.25s ease;
        }
        .nav-link:hover { color: #002080; }
        .main-content { padding: 2rem 0 3rem; }
        .page-title {
            color: #0033a0;
            margin-bottom: 2rem;
            font-weight: 600;
            border-left: 4px solid #0033a0;
            padding-left: 1rem;
        }
        .guide-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
            margin-bottom: 2rem;
            overflow: hidden;
        }
        .card-header-custom {
            background: #0033a0;
            color: white;
            padding: 1rem 1.5rem;
            font-weight: 600;
        }
        .card-header-custom i { margin-right: 10px; }
        .card-body-custom { padding: 1.5rem; }
        .table-structure {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.85rem;
        }
        .table-structure th {
            background: #f0f4f8;
            padding: 10px;
            text-align: left;
            border: 1px solid #ddd;
        }
        .table-structure td {
            padding: 8px 10px;
            border: 1px solid #ddd;
        }
        .required-star { color: red; }
        .example-box {
            background: #f8f9fa;
            border-left: 3px solid #0033a0;
            padding: 1rem;
            margin-top: 1rem;
            font-family: monospace;
            font-size: 0.8rem;
        }
        .btn-back {
            background: #6c757d;
            color: white;
            border: none;
            padding: 0.5rem 1.5rem;
            border-radius: 6px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        .ensa-footer {
            background: #0033a0;
            color: white;
            padding: 2rem 0 1rem;
            margin-top: auto;
        }
        .footer-content { text-align: center; }
        .footer-text { margin: 0.3rem 0; font-size: 0.85rem; opacity: 0.9; }
        .copyright { margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid rgba(255,255,255,0.2); font-size: 0.75rem; }
    </style>
</head>
<body>

<header class="main-header">
    <div class="header-container">
        <div class="brand">
            <a href="${pageContext.request.contextPath}/" class="brand-name">NomDeLaPP</a>
            <span class="brand-tag">planification & soutenances</span>
        </div>
        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/" class="nav-link">Accueil</a></li>
            <li><a href="${pageContext.request.contextPath}/histoire.jsp" class="nav-link">Histoire</a></li>
            <li><a href="#" class="nav-link" id="navGuide">Guide</a></li>
            <li><a href="${pageContext.request.contextPath}/statistiques.jsp" class="nav-link">Statistiques</a></li>
        </ul>
    </div>
</header>

<div class="main-content">
    <div class="container">
        <h1 class="page-title"><i class="fas fa-book-open me-2"></i>Guide d'utilisation</h1>
        
        <!-- Fichier Étudiants -->
        <div class="guide-card">
            <div class="card-header-custom">
                <i class="fas fa-user-graduate"></i> Structure du fichier EXCEL - Étudiants
            </div>
            <div class="card-body-custom">
                <p>Le fichier doit contenir une feuille avec les colonnes suivantes :</p>
                <table class="table-structure">
                    <thead>
                        <tr><th>Colonne</th><th>Nom exact</th><th>Description</th><th>Obligatoire</th></tr>
                    </thead>
                    <tbody>
                        <tr><td>A</td><td><strong>CNE</strong></td><td>Code National de l'Étudiant</td><td class="required-star">✓</td></tr>
                        <tr><td>B</td><td><strong>NOM</strong></td><td>Nom de famille (en majuscules)</td><td class="required-star">✓</td></tr>
                        <tr><td>C</td><td><strong>PRENOM</strong></td><td>Prénom</td><td class="required-star">✓</td></tr>
                        <tr><td>D</td><td><strong>EMAIL PERSONNEL</strong></td><td>Email personnel</td><td></td></tr>
                        <tr><td>E</td><td><strong>EMAIL ACADEMIQUE</strong></td><td>Email universitaire</td><td></td></tr>
                        <tr><td>F</td><td><strong>FILIERE</strong></td><td>Filière (INFO, ISI, GC, etc.)</td><td class="required-star">✓</td></tr>
                        <tr><td>G</td><td><strong>ENCADRANT NOM</strong></td><td>Peut être vide (rempli automatiquement)</td><td></td></tr>
                        <tr><td>H</td><td><strong>ENCADRANT PRENOM</strong></td><td>Peut être vide (rempli automatiquement)</td><td></td></tr>
                    </tbody>
                </table>
                <div class="example-box">
                    <strong>📌 Exemple de ligne :</strong><br>
                    CNE: H123456789 | NOM: BENALI | PRENOM: Youssef | FILIERE: INFO
                </div>
            </div>
        </div>

        <!-- Fichier Professeurs -->
        <div class="guide-card">
            <div class="card-header-custom">
                <i class="fas fa-chalkboard-user"></i> Structure du fichier EXCEL - Professeurs
            </div>
            <div class="card-body-custom">
                <p>Colonnes attendues (dans l'ordre) :</p>
                <table class="table-structure">
                    <thead><tr><th>Colonne</th><th>Contenu</th><th>Exemple</th><th>Obligatoire</th></tr></thead>
                    <tbody>
                        <tr><td>A</td><td><strong>NOM</strong></td><td>EL FALLAH</td><td class="required-star">✓</td></tr>
                        <tr><td>B</td><td><strong>PRENOM</strong></td><td>Mohammed</td><td class="required-star">✓</td></tr>
                        <tr><td>C</td><td><strong>SPECIALITE</strong></td><td>Informatique / Mathématiques / etc.</td><td class="required-star">✓</td></tr>
                    </tbody>
                </table>
                <div class="example-box">
                    <strong>📌 Ligne exemple :</strong> EL FALLAH | Mohammed | Informatique
                </div>
                <p class="mt-2 text-muted small">⚠️ Les professeurs avec "Info" ou "Informatique" dans la spécialité sont considérés comme INFO.</p>
            </div>
        </div>

        <!-- Fichier Configuration -->
        <div class="guide-card">
            <div class="card-header-custom">
                <i class="fas fa-sliders-h"></i> Structure du fichier de Configuration
            </div>
            <div class="card-body-custom">
                <p>Deux colonnes : <strong>Clé</strong> et <strong>Valeur</strong></p>
                <table class="table-structure">
                    <thead><tr><th>Clé</th><th>Valeur (exemple)</th><th>Description</th></tr></thead>
                    <tbody>
                        <tr><td>duree_soutenance_minutes</td><td>60 min</td><td>Durée par soutenance</td></tr>
                        <tr><td>heure_debut_matin</td><td>09:00</td><td>Début matinée</td></tr>
                        <tr><td>heure_fin_matin</td><td>12:00</td><td>Fin matinée</td></tr>
                        <tr><td>heure_debut_apres_midi</td><td>14:00</td><td>Début après-midi</td></tr>
                        <tr><td>heure_fin_apres_midi</td><td>18:00</td><td>Fin après-midi</td></tr>
                        <tr><td>salles</td><td>Salle 16, Salle 17, Amphi A</td><td>Liste des salles (séparées par des virgules)</td></tr>
                        <tr><td>date debut soutnance</td><td>2026-06-22</td><td>Date de début</td></tr>
                        <tr><td>jours</td><td>3</td><td>Nombre de jours</td></tr>
                        <tr><td>pause_max_sans_soutenance_heures</td><td>3h</td><td>Pause max autorisée</td></tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="text-center mt-4">
            <a href="${pageContext.request.contextPath}/" class="btn-back">
                <i class="fas fa-arrow-left"></i> Retour à l'accueil
            </a>
        </div>
    </div>
</div>

<footer class="ensa-footer">
    <div class="container">
        <div class="footer-content">
            <p class="footer-text"><strong>École Nationale des Sciences Appliquées - Al Hoceima</strong></p>
            <p class="footer-text"><i class="fas fa-map-marker-alt"></i> BP 03 - Ajdir, Al Hoceima, Maroc</p>
            <div class="copyright"><p>&copy; 2025 - Tous droits réservés</p></div>
        </div>
    </div>
</footer>

<script>
    document.getElementById('navGuide')?.addEventListener('click', (e) => { e.preventDefault(); location.reload(); });
</script>
</body>
</html>