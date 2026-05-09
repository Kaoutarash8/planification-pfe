<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    // Récupérer les fichiers récents dans le dossier uploads
    String uploadPath = application.getRealPath("/uploads");
    if (uploadPath == null) {
        uploadPath = "C:/Users/e/workspace-pfe/GestionPFE_V08/src/main/webapp/uploads";
    }
    File uploadDir = new File(uploadPath);
    
    List<Map<String, String>> fichiersHistorique = new ArrayList<>();
    
    if (uploadDir.exists()) {
        File[] fichiers = uploadDir.listFiles();
        if (fichiers != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (File f : fichiers) {
                String nom = f.getName();
                if ((nom.startsWith("affectation_") || nom.startsWith("planning_")) && nom.endsWith(".xlsx")) {
                    Map<String, String> info = new HashMap<>();
                    info.put("nom", nom);
                    info.put("date", sdf.format(new Date(f.lastModified())));
                    info.put("type", nom.startsWith("affectation_") ? "Affectation" : "Planning");
                    info.put("annee", nom.replaceAll("[^0-9_]", "").replace("__", "_"));
                    fichiersHistorique.add(info);
                }
            }
            
            fichiersHistorique.sort((a, b) -> b.get("date").compareTo(a.get("date")));
        }
    }
    
    List<Map<String, String>> cinqRecents = fichiersHistorique.size() > 5 ? 
        fichiersHistorique.subList(0, 5) : fichiersHistorique;
%>

<!-- Header commun -->
<header class="ensa-header">
    <div class="container">
        <div class="header-content">
            <div class="logo">
                <div class="logo-icon">
                    <i class="bi bi-mortarboard"></i>
                </div>
                <div class="logo-text">
                    <h1>ENSA Al Hoceima</h1>
                    <p>École Nationale des Sciences Appliquées</p>
                </div>
            </div>
            <nav class="nav-menu">
                <a href="${pageContext.request.contextPath}/" class="${param.page == 'home' ? 'active' : ''}">
                    <i class="bi bi-house"></i> Accueil
                </a>
                
                <a href="${pageContext.request.contextPath}/statistiques" class="${param.page == 'statistiques' ? 'active' : ''}">
                    <i class="bi bi-graph-up"></i> Dashboard
                </a>
                <a href="#" class="${param.page == 'guide' ? 'active' : ''}" onclick="ouvrirGuide()">
                    <i class="bi bi-question-circle"></i> Guide
                </a>
                
                <!-- 3 tirets pour l'historique -->
                <div class="historique-wrapper">
                    <div class="historique-trigger" onclick="toggleHistorique(event)">
                        <i class="bi bi-list"></i>
                        <% if (!cinqRecents.isEmpty()) { %>
                            <span class="historique-badge"><%= cinqRecents.size() %></span>
                        <% } %>
                    </div>
                    <div class="historique-dropdown" id="historiqueDropdown">
                        <div class="dropdown-arrow"></div>
                        <div class="dropdown-header">
                            <i class="bi bi-clock-history"></i> Dernières générations
                        </div>
                        <% if (cinqRecents.isEmpty()) { %>
                            <div class="dropdown-empty">
                                <i class="bi bi-inbox"></i> Aucun fichier
                            </div>
                        <% } else { %>
                            <% for (Map<String, String> f : cinqRecents) { 
                                String type = f.get("type");
                                String nom = f.get("nom");
                                String annee = f.get("annee");
                                String date = f.get("date");
                                String icon = type.equals("Affectation") ? "bi-people" : "bi-calendar-week";
                            %>
                            <a href="${pageContext.request.contextPath}/download?file=<%= nom %>" class="dropdown-item" target="_blank">
                                <i class="bi <%= icon %>"></i>
                                <div class="dropdown-item-info">
                                    <span><%= type %> <%= annee %></span>
                                    <small><%= date %></small>
                                </div>
                                <i class="bi bi-download"></i>
                            </a>
                            <% } %>
                        <% } %>
                    </div>
                </div>
            </nav>
        </div>
    </div>
</header>

<style>
    .ensa-header {
        background: white;
        box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
        position: sticky;
        top: 0;
        z-index: 1000;
    }
    
    .header-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        flex-wrap: wrap;
        gap: 15px;
    }
    
    .logo {
        display: flex;
        align-items: center;
        gap: 15px;
    }
    
    .logo-icon {
        width: 50px;
        height: 50px;
        background: #0033a0;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 24px;
    }
    
    .logo-text h1 {
        font-size: 1.3rem;
        font-weight: 700;
        color: #0033a0;
        margin: 0;
    }
    
    .logo-text p {
        font-size: 0.75rem;
        color: #6c757d;
        margin: 0;
    }
    
    .nav-menu {
        display: flex;
        gap: 25px;
        align-items: center;
    }
    
    .nav-menu > a {
        color: #555;
        text-decoration: none;
        font-size: 0.95rem;
        font-weight: 500;
        padding: 8px 0;
        transition: all 0.3s;
        border-bottom: 2px solid transparent;
        display: flex;
        align-items: center;
        gap: 5px;
    }
    
    .nav-menu > a:hover, .nav-menu > a.active {
        color: #0033a0;
        border-bottom-color: #0033a0;
    }
    
    /* 3 tirets */
    .historique-wrapper {
        position: relative;
    }
    
    .historique-trigger {
        width: 38px;
        height: 38px;
        background: #f0f2f5;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;
        position: relative;
        border: 1px solid #e0e0e0;
    }
    
    .historique-trigger:hover {
        background: #e8f0fe;
        border-color: #0033a0;
    }
    
    .historique-trigger i {
        font-size: 1.3rem;
        color: #0033a0;
    }
    
    .historique-badge {
        position: absolute;
        top: -5px;
        right: -5px;
        background: #dc3545;
        color: white;
        font-size: 0.6rem;
        font-weight: bold;
        width: 18px;
        height: 18px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    .historique-dropdown {
        position: absolute;
        top: 48px;
        right: 0;
        width: 300px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
        border: 1px solid #e0e0e0;
        display: none;
        z-index: 1001;
        overflow: hidden;
    }
    
    .historique-dropdown.show {
        display: block;
        animation: fadeIn 0.2s ease;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    .dropdown-arrow {
        position: absolute;
        top: -8px;
        right: 15px;
        width: 0;
        height: 0;
        border-left: 8px solid transparent;
        border-right: 8px solid transparent;
        border-bottom: 8px solid white;
        filter: drop-shadow(0 -1px 0 #e0e0e0);
    }
    
    .dropdown-header {
        padding: 12px 15px;
        background: #0033a0;
        color: white;
        font-size: 0.8rem;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    
    .dropdown-empty {
        padding: 30px;
        text-align: center;
        color: #999;
        font-size: 0.8rem;
    }
    
    .dropdown-empty i {
        font-size: 2rem;
        display: block;
        margin-bottom: 10px;
        color: #ccc;
    }
    
    .dropdown-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 10px 15px;
        text-decoration: none;
        color: #333;
        border-bottom: 1px solid #f0f0f0;
        transition: background 0.2s;
    }
    
    .dropdown-item:hover {
        background: #e8f0fe;
    }
    
    .dropdown-item i:first-child {
        font-size: 1.1rem;
        color: #0033a0;
        width: 24px;
    }
    
    .dropdown-item-info {
        flex: 1;
    }
    
    .dropdown-item-info span {
        font-size: 0.8rem;
        font-weight: 600;
        display: block;
        color: #0033a0;
    }
    
    .dropdown-item-info small {
        font-size: 0.65rem;
        color: #888;
    }
    
    .dropdown-item i:last-child {
        font-size: 0.85rem;
        color: #bbb;
        opacity: 0;
        transition: opacity 0.2s;
    }
    
    .dropdown-item:hover i:last-child {
        opacity: 1;
        color: #0033a0;
    }
    
    @media (max-width: 768px) {
        .header-content {
            flex-direction: column;
            text-align: center;
        }
        .nav-menu {
            gap: 15px;
            flex-wrap: wrap;
            justify-content: center;
        }
        .historique-dropdown {
            position: fixed;
            top: 60px;
            right: 10px;
            left: 10px;
            width: auto;
        }
        .dropdown-arrow {
            right: 20px;
        }
    }
</style>

<script>
    function toggleHistorique(event) {
        event.stopPropagation();
        var dropdown = document.getElementById('historiqueDropdown');
        dropdown.classList.toggle('show');
    }
    

    
    // Fermer le menu en cliquant ailleurs
    document.addEventListener('click', function() {
        var dropdown = document.getElementById('historiqueDropdown');
        if (dropdown) {
            dropdown.classList.remove('show');
        }
    });
</script>