<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.ensa.model.*" %>
<%@ page import="com.ensa.dao.*" %>
<%@ page import="com.ensa.service.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.poi.ss.usermodel.*" %>
<%@ page import="org.apache.poi.xssf.usermodel.XSSFWorkbook" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Statistiques — ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500&family=DM+Mono:wght@400;500&display=swap');

        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'DM Sans', system-ui, sans-serif;
            background: #f0f2f7;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            color: #1a1a2e;
        }

        /* ===== HEADER ===== */
        .main-header {
            background: rgba(255,255,255,0.97);
            backdrop-filter: blur(4px);
            border-bottom: 0.5px solid rgba(0,0,0,0.08);
            position: sticky;
            top: 0;
            z-index: 1000;
        }
        .header-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 0.9rem 2rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 0.8rem;
        }
        .brand-name {
            font-size: 1.5rem;
            font-weight: 500;
            color: #0033a0;
            text-decoration: none;
            letter-spacing: -0.3px;
        }
        .brand-tag {
            display: block;
            font-size: 0.68rem;
            color: #6b7399;
            letter-spacing: 0.4px;
            margin-top: 1px;
        }
        .nav-links { display: flex; gap: 2rem; list-style: none; padding: 0; }
        .nav-link {
            text-decoration: none;
            color: #0033a0;
            font-weight: 500;
            font-size: 0.9rem;
            display: flex;
            align-items: center;
            gap: 6px;
            transition: opacity 0.2s;
        }
        .nav-link:hover { opacity: 0.7; }

        /* ===== MAIN ===== */
        .main-content { flex: 1; padding: 2rem 0 3rem; }

        /* ===== PAGE HEADER ===== */
        .page-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 1.5rem;
            flex-wrap: wrap;
            gap: 1rem;
        }
        .page-title {
            font-size: 1.4rem;
            font-weight: 500;
            color: #1a1a2e;
            letter-spacing: -0.3px;
        }
        .page-badge {
            font-family: 'DM Mono', monospace;
            font-size: 0.7rem;
            color: #6b7399;
            background: white;
            padding: 4px 12px;
            border-radius: 20px;
            border: 0.5px solid rgba(0,0,0,0.1);
        }

        /* ===== KPI CARDS ===== */
        .kpi-grid {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 12px;
            margin-bottom: 1.5rem;
        }
        .kpi-card {
            background: white;
            border-radius: 14px;
            border: 0.5px solid rgba(0,0,0,0.07);
            padding: 1.1rem 1.1rem 0.9rem;
            position: relative;
            overflow: hidden;
            transition: transform 0.2s;
        }
        .kpi-card:hover { transform: translateY(-2px); }
        .kpi-icon {
            width: 34px;
            height: 34px;
            border-radius: 9px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            margin-bottom: 0.7rem;
        }
        .kpi-value {
            font-family: 'DM Mono', monospace;
            font-size: 28px;
            font-weight: 500;
            line-height: 1;
            color: #1a1a2e;
        }
        .kpi-label {
            font-size: 11px;
            font-weight: 500;
            color: #6b7399;
            margin-top: 5px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .kpi-sub {
            font-size: 10px;
            color: #adb3cc;
            margin-top: 2px;
        }
        .kpi-accent {
            position: absolute;
            bottom: 0; left: 0; right: 0;
            height: 2.5px;
        }

        /* KPI color variants */
        .kpi-blue  .kpi-icon { background: #E6F1FB; color: #185FA5; }
        .kpi-teal  .kpi-icon { background: #E1F5EE; color: #0F6E56; }
        .kpi-amber .kpi-icon { background: #FAEEDA; color: #BA7517; }
        .kpi-coral .kpi-icon { background: #FAECE7; color: #993C1D; }
        .kpi-purple .kpi-icon { background: #EEEDFE; color: #534AB7; }
        .kpi-blue  .kpi-accent  { background: #185FA5; }
        .kpi-teal  .kpi-accent  { background: #0F6E56; }
        .kpi-amber .kpi-accent  { background: #BA7517; }
        .kpi-coral .kpi-accent  { background: #993C1D; }
        .kpi-purple .kpi-accent { background: #534AB7; }

        /* ===== CHART CARDS ===== */
        .chart-card {
            background: white;
            border-radius: 14px;
            border: 0.5px solid rgba(0,0,0,0.07);
            padding: 1.25rem;
            margin-bottom: 14px;
        }
        .chart-title {
            font-size: 11px;
            font-weight: 500;
            color: #6b7399;
            text-transform: uppercase;
            letter-spacing: 0.6px;
            margin-bottom: 0.9rem;
            display: flex;
            align-items: center;
            gap: 7px;
        }
        .chart-title-dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
            display: inline-block;
        }
        .chart-legend {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 10px;
            font-size: 11px;
            color: #6b7399;
        }
        .legend-item {
            display: flex;
            align-items: center;
            gap: 5px;
        }
        .legend-sq {
            width: 9px;
            height: 9px;
            border-radius: 2px;
            display: inline-block;
        }

        /* ===== GRID LAYOUTS ===== */
        .grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 14px; }
        .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

        /* ===== JURY SECTION ===== */
        .jury-grid {
            display: grid;
            grid-template-columns: 180px 1fr;
            gap: 1.5rem;
            align-items: center;
        }
        .jury-stat {
            text-align: center;
            padding: 0.8rem;
            background: #f7f8fc;
            border-radius: 10px;
        }
        .jury-stat-val {
            font-family: 'DM Mono', monospace;
            font-size: 22px;
            font-weight: 500;
        }
        .jury-stat-lbl {
            font-size: 10px;
            color: #6b7399;
            margin-top: 2px;
        }
        .jury-avg {
            margin-top: 10px;
            padding: 0.75rem 1rem;
            background: #f7f8fc;
            border-radius: 10px;
            border-left: 2.5px solid #534AB7;
        }
        .jury-avg-lbl { font-size: 10px; color: #6b7399; }
        .jury-avg-val {
            font-family: 'DM Mono', monospace;
            font-size: 16px;
            font-weight: 500;
            color: #1a1a2e;
            margin-top: 2px;
        }

        /* ===== TABLE ===== */
        .data-table-wrap {
            background: white;
            border-radius: 14px;
            border: 0.5px solid rgba(0,0,0,0.07);
            overflow: hidden;
            margin-bottom: 14px;
        }
        .data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
        .data-table th {
            background: #f7f8fc;
            color: #6b7399;
            font-weight: 500;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            padding: 10px 14px;
            border-bottom: 0.5px solid rgba(0,0,0,0.07);
            text-align: left;
        }
        .data-table td {
            padding: 10px 14px;
            border-bottom: 0.5px solid rgba(0,0,0,0.04);
            color: #1a1a2e;
        }
        .data-table tr:last-child td { border-bottom: none; }
        .data-table tr:hover td { background: #f9fafd; }
        .pill {
            display: inline-block;
            padding: 2px 9px;
            border-radius: 20px;
            font-size: 11px;
            font-weight: 500;
        }
        .pill-blue   { background: #E6F1FB; color: #0C447C; }
        .pill-green  { background: #EAF3DE; color: #27500A; }
        .pill-amber  { background: #FAEEDA; color: #633806; }
        .pill-gray   { background: #F1EFE8; color: #444441; }

        /* ===== SECTION TITLE ===== */
        .section-title {
            font-size: 1rem;
            font-weight: 500;
            color: #1a1a2e;
            margin: 1.5rem 0 0.75rem;
            padding-left: 10px;
            border-left: 2.5px solid #0033a0;
        }

        /* ===== FOOTER ===== */
        .ensa-footer {
            background: #0033a0;
            color: white;
            padding: 1.8rem 0 1rem;
        }
        .footer-content { text-align: center; }
        .footer-text { font-size: 0.83rem; opacity: 0.9; margin: 0.25rem 0; }
        .copyright {
            margin-top: 1.2rem;
            padding-top: 0.9rem;
            border-top: 1px solid rgba(255,255,255,0.18);
            font-size: 0.72rem;
            opacity: 0.7;
        }
        .btn-back {
            background: white;
            color: #0033a0;
            border: 0.5px solid rgba(0,51,160,0.3);
            padding: 0.5rem 1.4rem;
            border-radius: 8px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-size: 0.85rem;
            font-weight: 500;
            transition: all 0.2s;
        }
        .btn-back:hover { background: #f0f4ff; color: #0033a0; }

        @media (max-width: 900px) {
            .kpi-grid { grid-template-columns: repeat(3, 1fr); }
            .grid-3   { grid-template-columns: 1fr 1fr; }
        }
        @media (max-width: 600px) {
            .kpi-grid { grid-template-columns: 1fr 1fr; }
            .grid-3, .grid-2 { grid-template-columns: 1fr; }
            .jury-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>

<%
    /* ============================================================
       LECTURE DES DONNÉES DEPUIS LES FICHIERS XLSX
       ============================================================ */
    String basePath = "C:/Users/e/workspace-pfe/pfe-affectation/uploads/Affectation";
    String dataPath = "C:/Users/e/workspace-pfe/pfe-affectation/uploads/data";

    List<Professeur>          professeurs          = new ArrayList<>();
    List<Etudiant>            etudiants            = new ArrayList<>();
    Map<String, Integer>      chargeProfesseurs    = new LinkedHashMap<>();
    Map<String, String>       specialiteProfesseurs= new HashMap<>();
    Map<String, List<String>> etudiantsParEncadrant= new LinkedHashMap<>();
    Map<String, Integer>      etudiantsParFiliere  = new LinkedHashMap<>();
    Map<String, Integer>      soutenancesParJour   = new LinkedHashMap<>();
    Map<String, Integer>      soutenancesParJury   = new HashMap<>();
    Map<String, Integer>      soutenancesParSalle  = new LinkedHashMap<>();
    Map<String, Map<String,Integer>> filiereParEncadrant = new LinkedHashMap<>();

    int totalEtudiants      = 0;
    int totalSoutenances    = 0;
    int totalParticipations = 0;
    int totalSalles         = 0;
    int presidentCount      = 0;
    int rapporteur1Count    = 0;
    int rapporteur2Count    = 0;

    try {
        /* ---------- 1. ÉTUDIANTS / ENCADRANTS (affectation_latest.xlsx) ---------- */
        File affectationFile = new File(basePath + "/affectation_latest.xlsx");
        if (affectationFile.exists()) {
            try (FileInputStream fis = new FileInputStream(affectationFile);
                 Workbook wb = new XSSFWorkbook(fis)) {
                Sheet sheet = wb.getSheetAt(0);
                boolean first = true;
                for (Row row : sheet) {
                    if (first) { first = false; continue; }
                    String cne          = getCellValue(row.getCell(0));
                    String nom          = getCellValue(row.getCell(1));
                    String prenom       = getCellValue(row.getCell(2));
                    String filiere      = getCellValue(row.getCell(5));
                    String encNom       = getCellValue(row.getCell(6));
                    String encPrenom    = getCellValue(row.getCell(7));
                    String encadrant    = encPrenom + " " + encNom;
                    if (cne == null || cne.isEmpty()) continue;

                    Etudiant e = new Etudiant();
                    e.setCne(cne); e.setNom(nom); e.setPrenom(prenom);
                    e.setFiliere(filiere);
                    e.setEncadrantNom(encNom); e.setEncadrantPrenom(encPrenom);
                    etudiants.add(e);

                    etudiantsParEncadrant.computeIfAbsent(encadrant, k -> new ArrayList<>()).add(cne);
                    String fk = (filiere == null || filiere.isEmpty()) ? "INFO" : filiere;
                    etudiantsParFiliere.put(fk, etudiantsParFiliere.getOrDefault(fk, 0) + 1);
                    totalEtudiants++;
                }
            }
        }

        /* ---------- 2. PROFESSEURS (professeurs_latest.xlsx) ---------- */
        File profFile = new File(dataPath + "/professeurs_latest.xlsx");
        if (profFile.exists()) {
            try (FileInputStream fis = new FileInputStream(profFile);
                 Workbook wb = new XSSFWorkbook(fis)) {
                Sheet sheet = wb.getSheetAt(0);
                boolean first = true;
                for (Row row : sheet) {
                    if (first) { first = false; continue; }
                    String nom       = getCellValue(row.getCell(0));
                    String prenom    = getCellValue(row.getCell(1));
                    String specialite= getCellValue(row.getCell(2));
                    String nomComplet= prenom + " " + nom;
                    professeurs.add(new Professeur(nom, prenom, specialite));
                    specialiteProfesseurs.put(nomComplet, specialite);
                    chargeProfesseurs.put(nomComplet, 0);
                }
            }
        }

        /* ---------- 3. PLANNING (planning_latest.xlsx) ---------- */
        File planningFile = new File(basePath + "/planning_latest.xlsx");
        if (planningFile.exists()) {
            try (FileInputStream fis = new FileInputStream(planningFile);
                 Workbook wb = new XSSFWorkbook(fis)) {
                Sheet sheet = wb.getSheetAt(0);
                int startRow = 7;
                for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    String num        = getCellValue(row.getCell(0));
                    if (num == null || num.isEmpty() || num.equals("N°")) continue;

                    String date       = getCellValue(row.getCell(2));
                    String salle      = getCellValue(row.getCell(3));
                    String encadrant  = getCellValue(row.getCell(5));
                    String jury1      = getCellValue(row.getCell(6));
                    String jury2      = getCellValue(row.getCell(7));
                    String filiere    = getCellValue(row.getCell(10));
                    totalSoutenances++;

                    /* participations encadrant = président */
                    if (encadrant != null && !encadrant.isEmpty()) {
                        chargeProfesseurs.put(encadrant, chargeProfesseurs.getOrDefault(encadrant,0)+1);
                        soutenancesParJury.put(encadrant, soutenancesParJury.getOrDefault(encadrant,0)+1);
                        totalParticipations++; presidentCount++;
                    }
                    if (jury1 != null && !jury1.isEmpty()) {
                        chargeProfesseurs.put(jury1, chargeProfesseurs.getOrDefault(jury1,0)+1);
                        totalParticipations++; rapporteur1Count++;
                    }
                    if (jury2 != null && !jury2.isEmpty()) {
                        chargeProfesseurs.put(jury2, chargeProfesseurs.getOrDefault(jury2,0)+1);
                        totalParticipations++; rapporteur2Count++;
                    }
                    if (date  != null && !date.isEmpty())
                        soutenancesParJour.put(date, soutenancesParJour.getOrDefault(date,0)+1);
                    if (salle != null && !salle.isEmpty())
                        soutenancesParSalle.put(salle, soutenancesParSalle.getOrDefault(salle,0)+1);
                    if (encadrant != null && filiere != null && !filiere.isEmpty()) {
                        filiereParEncadrant.computeIfAbsent(encadrant, k -> new LinkedHashMap<>())
                            .put(filiere, filiereParEncadrant.get(encadrant).getOrDefault(filiere,0)+1);
                    }
                }
            }
        }
    } catch (Exception ex) { ex.printStackTrace(); }

    totalSalles = soutenancesParSalle.size();

    /* ------ Calculs statistiques ------ */
    int nbProfs = chargeProfesseurs.isEmpty() ? 1 : chargeProfesseurs.size();
    double moyenneParticipations = (double) totalParticipations / nbProfs;
    int maxCharge = chargeProfesseurs.values().stream().mapToInt(Integer::intValue).max().orElse(0);

    int infoParticipations    = 0;
    int nonInfoParticipations = 0;
    for (Map.Entry<String,Integer> e : chargeProfesseurs.entrySet()) {
        String spec = specialiteProfesseurs.getOrDefault(e.getKey(),"");
        if (spec.toLowerCase().contains("info")) infoParticipations    += e.getValue();
        else                                      nonInfoParticipations += e.getValue();
    }

    /* Top 10 professeurs par charge */
    List<Map.Entry<String,Integer>> sortedProfs = new ArrayList<>(chargeProfesseurs.entrySet());
    sortedProfs.sort((a,b) -> b.getValue().compareTo(a.getValue()));

    List<String>  topProfNames   = new ArrayList<>();
    List<Integer> topProfCharges = new ArrayList<>();
    for (int i = 0; i < Math.min(10, sortedProfs.size()); i++) {
        topProfNames.add(sortedProfs.get(i).getKey());
        topProfCharges.add(sortedProfs.get(i).getValue());
    }

    /* Top 7 encadrants par étudiants */
    List<Map.Entry<String,List<String>>> sortedEnc = new ArrayList<>(etudiantsParEncadrant.entrySet());
    sortedEnc.sort((a,b) -> b.getValue().size() - a.getValue().size());

    List<String>  encNames  = new ArrayList<>();
    List<Integer> encCounts = new ArrayList<>();
    for (int i = 0; i < Math.min(7, sortedEnc.size()); i++) {
        encNames.add(sortedEnc.get(i).getKey());
        encCounts.add(sortedEnc.get(i).getValue().size());
    }

    /* Données filières */
    List<String>  filiereNames  = new ArrayList<>(etudiantsParFiliere.keySet());
    List<Integer> filiereCounts = new ArrayList<>();
    for (String f : filiereNames) filiereCounts.add(etudiantsParFiliere.get(f));

    /* Données jours */
    List<String>  jourNames  = new ArrayList<>(soutenancesParJour.keySet());
    List<Integer> jourCounts = new ArrayList<>();
    for (String j : jourNames) jourCounts.add(soutenancesParJour.get(j));

    /* Données salles */
    List<String>  salleNames  = new ArrayList<>(soutenancesParSalle.keySet());
    List<Integer> salleCounts = new ArrayList<>();
    for (String s : salleNames) salleCounts.add(soutenancesParSalle.get(s));

    /* Présidents par soutenance (soutenancesParJury) — top 7 */
    List<Map.Entry<String,Integer>> sortedPresidents = new ArrayList<>(soutenancesParJury.entrySet());
    sortedPresidents.sort((a,b) -> b.getValue().compareTo(a.getValue()));
    List<String>  presNames  = new ArrayList<>();
    List<Integer> presCounts = new ArrayList<>();
    for (int i = 0; i < Math.min(7, sortedPresidents.size()); i++) {
        presNames.add(sortedPresidents.get(i).getKey());
        presCounts.add(sortedPresidents.get(i).getValue());
    }

    /* Helper JS : convertit une List<String> en tableau JS */
%>

<!-- ===================== HEADER ===================== -->
<header class="main-header">
    <div class="header-container">
        <div class="brand">
            <a href="${pageContext.request.contextPath}/" class="brand-name">ENSA Al Hoceima</a>
            <span class="brand-tag">planification &amp; soutenances PFE</span>
        </div>
        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/affectation?page=accueil" class="nav-link">
                <i class="fas fa-home"></i> Accueil</a></li>
            <li><a href="${pageContext.request.contextPath}/statistiques.jsp" class="nav-link">
                <i class="fas fa-chart-line"></i> Statistiques</a></li>
        </ul>
    </div>
</header>

<!-- ===================== MAIN ===================== -->
<div class="main-content">
    <div class="container-xl">

        <!-- En-tête de page -->
        <div class="page-header">
            <h1 class="page-title"><i class="fas fa-chart-line me-2" style="color:#0033a0;font-size:1rem;"></i>Tableau de bord statistique</h1>
            <span class="page-badge">ENSA Al Hoceima — PFE 2025-2026</span>
        </div>

        <!-- ===== KPI CARDS ===== -->
        <div class="kpi-grid">
            <div class="kpi-card kpi-blue">
                <div class="kpi-icon"><i class="fas fa-users"></i></div>
                <div class="kpi-value"><%= totalEtudiants %></div>
                <div class="kpi-label">Total étudiants</div>
                <div class="kpi-sub">100% affectés</div>
                <div class="kpi-accent"></div>
            </div>
            <div class="kpi-card kpi-teal">
                <div class="kpi-icon"><i class="fas fa-chalkboard-user"></i></div>
                <div class="kpi-value"><%= etudiantsParEncadrant.size() %></div>
                <div class="kpi-label">Encadrants</div>
                <div class="kpi-sub">Actifs</div>
                <div class="kpi-accent"></div>
            </div>
            <div class="kpi-card kpi-amber">
                <div class="kpi-icon"><i class="fas fa-calendar-check"></i></div>
                <div class="kpi-value"><%= totalSoutenances %></div>
                <div class="kpi-label">Soutenances</div>
                <div class="kpi-sub">Sur la période</div>
                <div class="kpi-accent"></div>
            </div>
            <div class="kpi-card kpi-coral">
                <div class="kpi-icon"><i class="fas fa-building"></i></div>
                <div class="kpi-value"><%= totalSalles > 0 ? totalSalles : 4 %></div>
                <div class="kpi-label">Salles</div>
                <div class="kpi-sub">Utilisées</div>
                <div class="kpi-accent"></div>
            </div>
            <div class="kpi-card kpi-purple">
                <div class="kpi-icon"><i class="fas fa-user-tie"></i></div>
                <div class="kpi-value"><%= chargeProfesseurs.size() %></div>
                <div class="kpi-label">Membres jury</div>
                <div class="kpi-sub">Impliqués</div>
                <div class="kpi-accent"></div>
            </div>
        </div>

        <!-- ===== RANGÉE 1 : Filière | Jour | Salle ===== -->
        <div class="grid-3">

            <!-- Répartition par filière -->
            <div class="chart-card">
                <div class="chart-title">
                    <span class="chart-title-dot" style="background:#185FA5;"></span>
                    Répartition par filière
                </div>
                <div class="chart-legend" id="filiereLegend"></div>
                <div style="position:relative;width:100%;height:200px;">
                    <canvas id="filiereChart"
                        role="img"
                        aria-label="Graphique de répartition des étudiants par filière">
                    </canvas>
                </div>
            </div>

            <!-- Soutenances par jour -->
            <div class="chart-card">
                <div class="chart-title">
                    <span class="chart-title-dot" style="background:#BA7517;"></span>
                    Soutenances par jour
                </div>
                <div class="chart-legend">
                    <span class="legend-item">
                        <span class="legend-sq" style="background:#BA7517;"></span>
                        Nombre de soutenances
                    </span>
                </div>
                <div style="position:relative;width:100%;height:200px;">
                    <canvas id="jourChart"
                        role="img"
                        aria-label="Nombre de soutenances par jour">
                    </canvas>
                </div>
            </div>

            <!-- Soutenances par salle -->
            <div class="chart-card">
                <div class="chart-title">
                    <span class="chart-title-dot" style="background:#993C1D;"></span>
                    Soutenances par salle
                </div>
                <div class="chart-legend" id="salleLegend"></div>
                <div style="position:relative;width:100%;height:200px;">
                    <canvas id="salleChart"
                        role="img"
                        aria-label="Répartition des soutenances par salle">
                    </canvas>
                </div>
            </div>

        </div><!-- /grid-3 -->

        <!-- ===== RANGÉE 2 : Encadrant | Président ===== -->
        <div class="grid-2">

            <div class="chart-card">
                <div class="chart-title">
                    <span class="chart-title-dot" style="background:#185FA5;"></span>
                    Étudiants par encadrant
                </div>
                <div style="position:relative;width:100%;height:<%=Math.max(200, encNames.size()*40+60)%>px;">
                    <canvas id="encadrantChart"
                        role="img"
                        aria-label="Nombre d'étudiants par encadrant">
                    </canvas>
                </div>
            </div>

            <div class="chart-card">
                <div class="chart-title">
                    <span class="chart-title-dot" style="background:#0F6E56;"></span>
                    Soutenances par encadrant (président)
                </div>
                <div style="position:relative;width:100%;height:<%=Math.max(200, presNames.size()*40+60)%>px;">
                    <canvas id="presidentChart"
                        role="img"
                        aria-label="Nombre de soutenances par encadrant en tant que président">
                    </canvas>
                </div>
            </div>

        </div><!-- /grid-2 -->

        <!-- ===== JURY ===== -->
        <div class="chart-card">
            <div class="chart-title">
                <span class="chart-title-dot" style="background:#534AB7;"></span>
                Répartition des rôles jury
            </div>
            <div class="jury-grid">
                <div style="position:relative;width:100%;height:170px;">
                    <canvas id="juryChart"
                        role="img"
                        aria-label="Répartition des rôles: Président, Rapporteur 1, Rapporteur 2">
                    </canvas>
                </div>
                <div>
                    <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:10px;">
                        <div class="jury-stat">
                            <div class="jury-stat-val" style="color:#534AB7;"><%= presidentCount %></div>
                            <div class="jury-stat-lbl">Présidents</div>
                        </div>
                        <div class="jury-stat">
                            <div class="jury-stat-val" style="color:#0F6E56;"><%= rapporteur1Count %></div>
                            <div class="jury-stat-lbl">Rapporteurs 1</div>
                        </div>
                        <div class="jury-stat">
                            <div class="jury-stat-val" style="color:#185FA5;"><%= rapporteur2Count %></div>
                            <div class="jury-stat-lbl">Rapporteurs 2</div>
                        </div>
                    </div>
                    <div class="jury-avg">
                        <div class="jury-avg-lbl">Charge moyenne par professeur</div>
                        <div class="jury-avg-val">
                            <%= String.format("%.1f", moyenneParticipations) %>
                            <span style="font-size:11px;font-family:'DM Sans',sans-serif;color:#6b7399;">
                                participations / prof
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ===== INFO vs NON-INFO ===== -->
        <div class="grid-2">
            <div class="chart-card">
                <div class="chart-title">
                    <span class="chart-title-dot" style="background:#185FA5;"></span>
                    Participations INFO vs NON-INFO
                </div>
                <div class="chart-legend">
                    <span class="legend-item"><span class="legend-sq" style="background:#185FA5;"></span>INFO</span>
                    <span class="legend-item"><span class="legend-sq" style="background:#73726c;"></span>NON-INFO</span>
                </div>
                <div style="position:relative;width:100%;height:200px;">
                    <canvas id="infoChart"
                        role="img"
                        aria-label="Participations des professeurs INFO versus non-INFO">
                    </canvas>
                </div>
            </div>

            <div class="chart-card">
                <div class="chart-title">
                    <span class="chart-title-dot" style="background:#BA7517;"></span>
                    Top 10 — participations par professeur
                </div>
                <div style="position:relative;width:100%;height:<%=Math.max(200, Math.min(10,topProfNames.size())*32+60)%>px;">
                    <canvas id="topProfChart"
                        role="img"
                        aria-label="Top 10 professeurs par nombre de participations">
                    </canvas>
                </div>
            </div>
        </div>

      
        <!-- Bouton retour -->
        <div class="text-center mt-4">
            <a href="${pageContext.request.contextPath}/affectation?page=accueil" class="btn-back">
                <i class="fas fa-arrow-left"></i> Retour à l'accueil
            </a>
        </div>

    </div><!-- /container-xl -->
</div><!-- /main-content -->

<!-- ===================== FOOTER ===================== -->
<footer class="ensa-footer">
    <div class="container">
        <div class="footer-content">
            <p class="footer-text"><strong>École Nationale des Sciences Appliquées — Al Hoceima</strong></p>
            <p class="footer-text"><i class="fas fa-map-marker-alt"></i> BP 03 - Ajdir, Al Hoceima, Maroc</p>
            <div class="copyright"><p>&copy; 2025 — Tous droits réservés</p></div>
        </div>
    </div>
</footer>

<!-- ===================== SCRIPTS CHART.JS ===================== -->
<script>
/* === Données injectées depuis JSP === */
const filiereLabels = <%= buildJsArray(filiereNames) %>;
const filiereCounts = <%= filiereCounts.toString() %>;

const jourLabels  = <%= buildJsArray(jourNames) %>;
const jourCounts  = <%= jourCounts.toString() %>;

const salleLabels  = <%= buildJsArray(salleNames) %>;
const salleCounts  = <%= salleCounts.toString() %>;

const encLabels  = <%= buildJsArray(encNames) %>;
const encCounts  = <%= encCounts.toString() %>;

const presLabels = <%= buildJsArray(presNames) %>;
const presCounts = <%= presCounts.toString() %>;

const topProfLabels  = <%= buildJsArray(topProfNames) %>;
const topProfCounts  = <%= topProfCharges.toString() %>;

const infoCount    = <%= infoParticipations %>;
const nonInfoCount = <%= nonInfoParticipations %>;

const presidentCount    = <%= presidentCount %>;
const rapporteur1Count  = <%= rapporteur1Count %>;
const rapporteur2Count  = <%= rapporteur2Count %>;

/* === Palette === */
const COLORS = {
  blue:   '#185FA5',
  teal:   '#0F6E56',
  amber:  '#BA7517',
  coral:  '#993C1D',
  purple: '#534AB7',
  gray:   '#73726c',
  blueLt: '#E6F1FB',
  tealLt: '#E1F5EE',
};
const PALETTE_DONUT = [COLORS.blue, COLORS.teal, COLORS.purple, COLORS.coral, COLORS.amber, COLORS.gray];

const TICK_COLOR = 'rgba(107,115,153,0.8)';
const GRID_COLOR = 'rgba(0,0,0,0.06)';

/* === Helper légende HTML === */
function buildLegend(containerId, labels, colors, counts, total) {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = labels.map((lbl, i) => {
        const pct = total ? Math.round(counts[i]/total*100) + '%' : '';
        return `<span class="legend-item">
            <span class="legend-sq" style="background:${colors[i]};"></span>
            ${lbl} ${pct}
        </span>`;
    }).join('');
}

/* === Options communes === */
function baseOpts(extra) {
    return Object.assign({
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } }
    }, extra);
}

/* ---- 1. FILIÈRE (donut) ---- */
const filTotal = filiereCounts.reduce((a,b)=>a+b, 0);
buildLegend('filiereLegend', filiereLabels, PALETTE_DONUT, filiereCounts, filTotal);
new Chart(document.getElementById('filiereChart'), {
    type: 'doughnut',
    data: {
        labels: filiereLabels,
        datasets: [{
            data: filiereCounts,
            backgroundColor: PALETTE_DONUT.slice(0, filiereLabels.length),
            borderWidth: 0,
            hoverOffset: 6
        }]
    },
    options: baseOpts({
        cutout: '62%',
        plugins: {
            legend: { display: false },
            tooltip: { callbacks: { label: c => c.label + ': ' + c.raw + ' étudiants' } }
        }
    })
});

/* ---- 2. SOUTENANCES PAR JOUR (barres) ---- */
new Chart(document.getElementById('jourChart'), {
    type: 'bar',
    data: {
        labels: jourLabels,
        datasets: [{
            data: jourCounts,
            backgroundColor: COLORS.amber,
            borderRadius: 6,
            borderSkipped: false
        }]
    },
    options: baseOpts({
        scales: {
            x: { ticks: { color: TICK_COLOR, font: { size: 10 } }, grid: { display: false } },
            y: { ticks: { color: TICK_COLOR, font: { size: 10 } }, grid: { color: GRID_COLOR }, beginAtZero: true }
        },
        plugins: {
            legend: { display: false },
            tooltip: { callbacks: { label: c => c.raw + ' soutenances' } }
        }
    })
});

/* ---- 3. SALLES (donut) ---- */
const salTotal = salleCounts.reduce((a,b)=>a+b, 0);
buildLegend('salleLegend', salleLabels, PALETTE_DONUT, salleCounts, salTotal);
new Chart(document.getElementById('salleChart'), {
    type: 'doughnut',
    data: {
        labels: salleLabels,
        datasets: [{
            data: salleCounts,
            backgroundColor: PALETTE_DONUT.slice(0, salleLabels.length),
            borderWidth: 0,
            hoverOffset: 6
        }]
    },
    options: baseOpts({
        cutout: '60%',
        plugins: {
            legend: { display: false },
            tooltip: { callbacks: { label: c => c.label + ': ' + c.raw + ' soutenances' } }
        }
    })
});

/* ---- 4. ÉTUDIANTS PAR ENCADRANT (barres horizontales) ---- */
new Chart(document.getElementById('encadrantChart'), {
    type: 'bar',
    data: {
        labels: encLabels,
        datasets: [{
            data: encCounts,
            backgroundColor: COLORS.blue,
            borderRadius: 4,
            borderSkipped: false,
            barThickness: 14
        }]
    },
    options: baseOpts({
        indexAxis: 'y',
        scales: {
            x: { ticks: { color: TICK_COLOR, font: { size: 10 } }, grid: { color: GRID_COLOR }, beginAtZero: true },
            y: { ticks: { color: TICK_COLOR, font: { size: 10 } }, grid: { display: false } }
        }
    })
});

/* ---- 5. PRÉSIDENTS (barres horizontales) ---- */
new Chart(document.getElementById('presidentChart'), {
    type: 'bar',
    data: {
        labels: presLabels,
        datasets: [{
            data: presCounts,
            backgroundColor: COLORS.teal,
            borderRadius: 4,
            borderSkipped: false,
            barThickness: 14
        }]
    },
    options: baseOpts({
        indexAxis: 'y',
        scales: {
            x: { ticks: { color: TICK_COLOR, font: { size: 10 } }, grid: { color: GRID_COLOR }, beginAtZero: true },
            y: { ticks: { color: TICK_COLOR, font: { size: 10 } }, grid: { display: false } }
        }
    })
});

/* ---- 6. JURY (donut) ---- */
new Chart(document.getElementById('juryChart'), {
    type: 'doughnut',
    data: {
        labels: ['Président', 'Rapporteur 1', 'Rapporteur 2'],
        datasets: [{
            data: [presidentCount, rapporteur1Count, rapporteur2Count],
            backgroundColor: [COLORS.purple, COLORS.teal, COLORS.blue],
            borderWidth: 0,
            hoverOffset: 6
        }]
    },
    options: baseOpts({
        cutout: '62%',
        plugins: {
            legend: { display: false },
            tooltip: { callbacks: { label: c => c.label + ': ' + c.raw } }
        }
    })
});

/* ---- 7. INFO vs NON-INFO (donut) ---- */
new Chart(document.getElementById('infoChart'), {
    type: 'doughnut',
    data: {
        labels: ['Professeurs INFO', 'Professeurs NON-INFO'],
        datasets: [{
            data: [infoCount, nonInfoCount],
            backgroundColor: [COLORS.blue, COLORS.gray],
            borderWidth: 0,
            hoverOffset: 6
        }]
    },
    options: baseOpts({
        cutout: '60%',
        plugins: {
            legend: { display: false },
            tooltip: { callbacks: { label: c => c.label + ': ' + c.raw + ' participations' } }
        }
    })
});

/* ---- 8. TOP 10 PROFS (barres horizontales) ---- */
new Chart(document.getElementById('topProfChart'), {
    type: 'bar',
    data: {
        labels: topProfLabels,
        datasets: [{
            data: topProfCounts,
            backgroundColor: COLORS.amber,
            borderRadius: 4,
            borderSkipped: false,
            barThickness: 12
        }]
    },
    options: baseOpts({
        indexAxis: 'y',
        scales: {
            x: { ticks: { color: TICK_COLOR, font: { size: 10 } }, grid: { color: GRID_COLOR }, beginAtZero: true },
            y: { ticks: { color: TICK_COLOR, font: { size: 10 } }, grid: { display: false } }
        }
    })
});
</script>

<%!
/* ===================== MÉTHODES UTILITAIRES ===================== */

/** Lit la valeur d'une cellule Excel quelque soit son type. */
private String getCellValue(Cell cell) {
    if (cell == null) return "";
    switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue().trim();
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return new java.text.SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
            }
            return String.valueOf((long) cell.getNumericCellValue());
        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
        case FORMULA:
            try { return cell.getStringCellValue().trim(); } catch (Exception ex) {
                return String.valueOf((long) cell.getNumericCellValue());
            }
        default:
            return "";
    }
}

/** Convertit une List<String> Java en tableau JavaScript : ['a','b','c'] */
private String buildJsArray(List<String> list) {
    if (list == null || list.isEmpty()) return "[]";
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < list.size(); i++) {
        if (i > 0) sb.append(",");
        sb.append("'").append(list.get(i).replace("'", "\\'")).append("'");
    }
    sb.append("]");
    return sb.toString();
}
%>

</body>
</html>
