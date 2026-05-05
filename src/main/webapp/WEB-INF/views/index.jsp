<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes">
    <title>ENSA Al Hoceima - Planification des Soutenances PFE</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* RESET & STYLES DE BASE */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: system-ui, 'Segoe UI', 'Inter', 'Helvetica Neue', sans-serif;
            background: linear-gradient(145deg, #f4f7fc 0%, #e9eef3 100%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        /* ========== HEADER PRINCIPAL ========== */
        .main-header {
            background: rgba(255, 255, 255, 0.96);
            backdrop-filter: blur(2px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05), 0 2px 4px rgba(0, 0, 0, 0.02);
            border-bottom: 1px solid rgba(0, 0, 0, 0.05);
            position: sticky;
            top: 0;
            z-index: 1000;
            width: 100%;
        }

        .header-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 1rem 2rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 1rem;
        }

        /* --- SECTION GAUCHE : NOM DE LA PP (BLEU UNIFORME) --- */
        .brand {
            flex-shrink: 0;
        }

        .brand-name {
            font-size: 1.8rem;
            font-weight: 700;
            letter-spacing: -0.01em;
            color: #0033a0;
            text-decoration: none;
            transition: opacity 0.2s ease;
            display: inline-block;
        }

        .brand-name:hover {
            opacity: 0.85;
            color: #002a86;
        }

        .brand-tag {
            display: block;
            font-size: 0.7rem;
            font-weight: 400;
            color: #0033a0;
            letter-spacing: 0.3px;
            margin-top: 2px;
        }

        /* --- SECTION DROITE : NAVIGATION (BLEU UNIFORME) --- */
        .nav-links {
            display: flex;
            align-items: center;
            gap: 2rem;
            flex-wrap: wrap;
            list-style: none;
            margin: 0;
            padding: 0;
        }

        .nav-item {
            font-size: 1.05rem;
            font-weight: 500;
        }

        .nav-link {
            text-decoration: none;
            color: #0033a0;
            padding: 0.5rem 0.2rem;
            transition: all 0.25s ease;
            position: relative;
            font-weight: 500;
            letter-spacing: 0.2px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            cursor: pointer;
        }

        .nav-link i {
            font-style: normal;
            font-weight: 400;
            font-size: 1.1rem;
            transition: transform 0.2s;
        }

        .nav-link:hover i {
            transform: translateX(2px);
        }

        .nav-link::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            width: 0%;
            height: 2.5px;
            background: #0033a0;
            border-radius: 4px;
            transition: width 0.3s cubic-bezier(0.2, 0.9, 0.4, 1.1);
        }

        .nav-link:hover {
            color: #002080;
        }

        .nav-link:hover::after {
            width: 100%;
        }

        .nav-link:active {
            transform: scale(0.97);
        }

        /* Responsive header */
        @media (max-width: 680px) {
            .header-container {
                flex-direction: column;
                align-items: center;
                text-align: center;
                padding: 1rem 1.5rem;
                gap: 0.8rem;
            }
            .brand {
                text-align: center;
            }
            .brand-name {
                font-size: 1.6rem;
            }
            .nav-links {
                gap: 1.5rem;
                justify-content: center;
            }
            .nav-item {
                font-size: 0.95rem;
            }
        }

        @media (max-width: 480px) {
            .nav-links {
                gap: 1.2rem;
            }
            .brand-name {
                font-size: 1.4rem;
            }
        }

        /* Main Content */
        .main-content {
            flex: 1;
            padding: 3rem 0;
        }
        
        /* Message animé - lettre par lettre */
        .animated-message {
            background: white;
            border-left: 4px solid #0033a0;
            padding: 2rem;
            margin-bottom: 3rem;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        
        .message-title {
            font-size: 2rem;
            font-weight: 300;
            color: #0033a0;
            margin-bottom: 1rem;
            letter-spacing: -0.5px;
        }
        
        #animatedText {
            font-size: 1.2rem;
            color: #333;
            line-height: 1.8;
            min-height: 100px;
            font-weight: 400;
        }
        
        .cursor {
            display: inline-block;
            width: 2px;
            height: 1.2rem;
            background-color: #0033a0;
            margin-left: 2px;
            animation: blink 1s infinite;
            vertical-align: middle;
        }
        
        @keyframes blink {
            0%, 50% { opacity: 1; }
            51%, 100% { opacity: 0; }
        }
        
        /* Boutons - BLEU UNIFORME (#0033a0) */
        .action-buttons {
            display: flex;
            justify-content: center;
            gap: 1.5rem;
            flex-wrap: wrap;
            margin-top: 2rem;
        }
        
        .btn-pfe {
            padding: 0.8rem 2rem;
            font-size: 1rem;
            font-weight: 600;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s ease;
            font-family: inherit;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            border: none;
            background: #0033a0;
            color: white;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        
        .btn-pfe:hover {
            background: #002a86;
            transform: translateY(-3px);
            box-shadow: 0 6px 14px rgba(0, 51, 160, 0.3);
        }
        
        .btn-pfe:active {
            transform: translateY(0);
        }
        
        .btn-icon {
            font-size: 1rem;
        }
        
        /* Cards informations */
        .info-section {
            margin-top: 3rem;
        }
        
        .info-card {
            background: white;
            padding: 1.5rem;
            text-align: center;
            height: 100%;
            border: 1px solid #e0e0e0;
            transition: all 0.3s ease;
        }
        
        .info-card:hover {
            border-color: #0033a0;
            transform: translateY(-3px);
        }
        
        .info-icon {
            font-size: 2rem;
            color: #0033a0;
            margin-bottom: 1rem;
        }
        
        .info-card h4 {
            color: #0033a0;
            margin-bottom: 0.8rem;
            font-size: 1.1rem;
            font-weight: 600;
        }
        
        .info-card p {
            color: #555;
            font-size: 0.85rem;
            line-height: 1.5;
            margin: 0;
        }
        
        /* Footer - BLEU UNIFORME */
        .ensa-footer {
            background: #0033a0;
            color: white;
            padding: 2rem 0 1rem;
            margin-top: auto;
        }
        
        .footer-content {
            text-align: center;
        }
        
        .footer-text {
            margin: 0.3rem 0;
            font-size: 0.85rem;
            opacity: 0.9;
        }
        
        .footer-links {
            margin-top: 1rem;
        }
        
        .footer-links a {
            color: white;
            text-decoration: none;
            margin: 0 0.8rem;
            font-size: 0.8rem;
            opacity: 0.8;
            transition: opacity 0.3s;
        }
        
        .footer-links a:hover {
            opacity: 1;
        }
        
        .copyright {
            margin-top: 1.5rem;
            padding-top: 1rem;
            border-top: 1px solid rgba(255,255,255,0.2);
            font-size: 0.75rem;
        }
        
        /* Loading */
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
            border-radius: 8px;
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
        
        /* Toast message pour la navigation */
        .demo-toast-msg {
            transition: opacity 0.2s ease;
            opacity: 1;
        }
        
        /* Responsif général */
        @media (max-width: 768px) {
            .action-buttons {
                flex-direction: column;
                align-items: center;
            }
            .btn-pfe {
                width: 100%;
                justify-content: center;
            }
            .message-title {
                font-size: 1.5rem;
            }
            #animatedText {
                font-size: 1rem;
            }
        }
    </style>
</head>
<body>

    <!-- Loading Overlay -->
    <div class="loading-overlay" id="loadingOverlay">
        <div class="loading-spinner">
            <i class="fas fa-spinner"></i>
            <p class="mt-2">Traitement en cours...</p>
        </div>
    </div>

    <!-- HEADER : NomDeLaPP + tag + Histoire, Guide, Statistiques TOUT EN BLEU #0033a0 -->
    <header class="main-header">
        <div class="header-container">
            <!-- Partie GAUCHE : Nom de la PP en BLEU -->
            <div class="brand">
                <a href="${pageContext.request.contextPath}/" class="brand-name" id="brandLink" aria-label="Accueil - Nom du projet">
                    NomDeLaPP
                </a>
                <span class="brand-tag">planification & soutenances</span>
            </div>

            <!-- Partie DROITE : Liens de navigation en BLEU #0033a0 - MAINTENANT FONCTIONNELS -->
            <ul class="nav-links" role="list">
                <li class="nav-item" role="listitem">
                    <a href="${pageContext.request.contextPath}/histoire.jsp" class="nav-link nav-histoire" aria-label="Histoire">
                        <i class="fas fa-history"></i> Histoire
                    </a>
                </li>
                <li class="nav-item" role="listitem">
                    <a href="${pageContext.request.contextPath}/guide.jsp" class="nav-link nav-guide" aria-label="Guide d'utilisation">
                        <i class="fas fa-book-open"></i> Guide d'utilisation
                    </a>
                </li>
                <li class="nav-item" role="listitem">
                    <a href="${pageContext.request.contextPath}/statistiques.jsp" class="nav-link nav-statistiques" aria-label="Statistiques">
                        <i class="fas fa-chart-line"></i> Statistiques
                    </a>
                </li>
            </ul>
        </div>
    </header>

    <!-- Main Content -->
    <div class="main-content">
        <div class="container">
            <!-- Animated Message -->
            <div class="animated-message">
                <div class="message-title">
                    <i class="fas fa-greeting"></i> Bienvenue
                </div>
                <div id="animatedText">
                    <span id="typewriter"></span><span class="cursor"></span>
                </div>
            </div>
            
            <!-- 3 Dynamic Buttons - BLEU UNIFORME #0033a0 -->
            <div class="action-buttons">
                <button class="btn-pfe" onclick="genererAffectation()">
                    <i class="fas fa-users btn-icon"></i>
                    Générer l'Affectation
                </button>
                <button class="btn-pfe" onclick="genererPlanning()">
                    <i class="fas fa-calendar-alt btn-icon"></i>
                    Générer le Planning
                </button>
                <button class="btn-pfe" onclick="genererPV()">
                    <i class="fas fa-file-alt btn-icon"></i>
                    Générer les PVs
                </button>
            </div>
            
            <!-- Info Cards -->
            <div class="info-section">
                <div class="row g-4">
                    <div class="col-md-4">
                        <div class="info-card">
                            <i class="fas fa-user-friends info-icon"></i>
                            <h4>Affectation Équilibrée</h4>
                            <p>Répartition automatique des étudiants entre les encadrants</p>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="info-card">
                            <i class="fas fa-calendar-week info-icon"></i>
                            <h4>Planning Optimisé</h4>
                            <p>Génération intelligente des créneaux de soutenance</p>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="info-card">
                            <i class="fas fa-file-signature info-icon"></i>
                            <h4>PVs Automatisés</h4>
                            <p>Procès-verbaux prêts à être signés</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="ensa-footer">
        <div class="container">
            <div class="footer-content">
                <p class="footer-text">
                    <strong>École Nationale des Sciences Appliquées - Al Hoceima</strong>
                </p>
                <p class="footer-text">
                    <i class="fas fa-map-marker-alt"></i> BP 03 - Ajdir, Al Hoceima, Maroc
                </p>
                <div class="footer-links">
                    <a href="#">Contact</a>
                    <a href="#">Mentions légales</a>
                    <a href="#">Support</a>
                </div>
                <div class="copyright">
                    <p>&copy; 2025 - Tous droits réservés</p>
                </div>
            </div>
        </div>
    </footer>

    <script>
        // Message animé lettre par lettre
        const message = "Bienvenue sur la plateforme de planification des soutenances PFE. Vous pouvez générer l'affectation des encadrants, planifier les soutenances et créer les procès-verbaux automatiquement.";
        
        let i = 0;
        const typewriterElement = document.getElementById('typewriter');
        
        function typeWriter() {
            if (i < message.length) {
                typewriterElement.innerHTML += message.charAt(i);
                i++;
                setTimeout(typeWriter, 50);
            }
        }
        
        // Démarrer l'animation au chargement de la page
        document.addEventListener('DOMContentLoaded', function() {
            typeWriter();
            
            // Animation des cards
            const cards = document.querySelectorAll('.info-card');
            cards.forEach((card, index) => {
                card.style.opacity = '0';
                card.style.transform = 'translateY(15px)';
                setTimeout(() => {
                    card.style.transition = 'all 0.4s ease';
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, 200 + (index * 100));
            });
        });
        
        // Fonctions des boutons
        function genererAffectation() {
            showLoading();
            window.location.href = '${pageContext.request.contextPath}/affectation?page=formulaire';
        }

        function genererPlanning() {
            showLoading();
            window.location.href = '${pageContext.request.contextPath}/affectation?page=planning';
        }
        
        function genererPV() {
            showLoading();
            window.location.href = '${pageContext.request.contextPath}/affectation?action=genererPVs';
        }
        
        function showLoading() {
            document.getElementById('loadingOverlay').style.display = 'flex';
        }
        
        function hideLoading() {
            document.getElementById('loadingOverlay').style.display = 'none';
        }
        
        // Le brand (NomDeLaPP) redirige vers l'accueil
        const brandClick = document.querySelector('.brand-name');
        if(brandClick) {
            brandClick.addEventListener('click', (e) => {
                // Déjà un lien vers l'accueil, pas besoin de toast
                // Le comportement par défaut du href fonctionne
            });
        }
    </script>
</body>
</html>