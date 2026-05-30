<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Statistiques – ENSA Al Hoceima</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <style>
        :root {
            --blue:    #0033a0;
            --blue-lt: #e8f0fe;
            --orange:  #e87722;
            --green:   #198754;
            --gray-bg: #f5f7fa;
            --card-sh: 0 2px 12px rgba(0,51,160,.07);
        }
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            background: var(--gray-bg);
            font-family: 'Segoe UI', system-ui, sans-serif;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .main-content { flex: 1; padding: 30px 0 20px; }

        /* KPI cards */
        .kpi-card {
            background: #fff;
            border-radius: 16px;
            padding: 18px 20px;
            border: 1px solid #e4e8ef;
            box-shadow: var(--card-sh);
            display: flex;
            align-items: center;
            gap: 14px;
            transition: transform .2s;
        }
        .kpi-card:hover { transform: translateY(-3px); }
        .kpi-icon {
            width: 48px; height: 48px; border-radius: 12px;
            display: flex; align-items: center; justify-content: center;
        }
        .kpi-icon i { font-size: 22px; }
        .kpi-val  { font-size: 1.9rem; font-weight: 800; color: var(--blue); line-height: 1; }
        .kpi-lbl  { font-size: .72rem; color: #6c757d; text-transform: uppercase; letter-spacing: .5px; margin-top: 3px; }

        .icon-blue   { background: var(--blue-lt); } .icon-blue i   { color: var(--blue); }
        .icon-orange { background: #fff3e8; } .icon-orange i { color: var(--orange); }
        .icon-green  { background: #d1f0e2; } .icon-green i  { color: var(--green); }

        /* Section cards */
        .section-card {
            background: #fff;
            border-radius: 18px;
            padding: 22px 24px;
            border: 1px solid #e4e8ef;
            box-shadow: var(--card-sh);
            margin-bottom: 24px;
        }
        .section-head {
            display: flex; align-items: center; justify-content: space-between;
            padding-bottom: 14px; margin-bottom: 18px;
            border-bottom: 2px solid #f0f2f7;
        }
        .section-title {
            font-size: 1rem; font-weight: 700; color: var(--blue);
            display: flex; align-items: center; gap: 8px;
        }

        /* Badges */
        .badge-ok  { background: #d1f0e2; color: #0a5c35; border-radius: 20px; padding: 3px 11px; font-size: .7rem; font-weight: 700; }
        .badge-nd  { background: #fff3cd; color: #664d00; border-radius: 20px; padding: 3px 11px; font-size: .7rem; font-weight: 700; }

        /* Mini boxes */
        .mini-box {
            background: var(--gray-bg); border-radius: 12px;
            padding: 14px; text-align: center; border: 1px solid #e4e8ef;
        }
        .mini-val { font-size: 1.4rem; font-weight: 800; color: var(--blue); }
        .mini-lbl { font-size: .7rem; color: #6c757d; text-transform: uppercase; }

        /* Tables */
        .table-custom thead th {
            background: var(--blue); color: #fff; font-size: .78rem;
            font-weight: 600; border: none; padding: 9px 12px;
        }
        .table-custom td { padding: 7px 12px; font-size: .8rem; vertical-align: middle; }
        .table-custom tbody tr:hover { background: var(--blue-lt); }

        /* Progress bars */
        .bar-wrap { background: #e4e8ef; border-radius: 8px; height: 6px; }
        .bar-fill { height: 6px; border-radius: 8px; background: var(--blue); }
        .bar-fill.orange { background: var(--orange); }

        /* Empty state */
        .empty-state { text-align: center; padding: 36px 20px; color: #adb5bd; }
        .empty-state i { font-size: 2.5rem; display: block; margin-bottom: 10px; }

        @media(max-width:768px){ .kpi-val{ font-size:1.4rem; } }
    </style>
</head>
<body>
    <%@ include file="/header.jsp" %>

    <div class="main-content">
        <div class="container">

            <!-- ==================== KPI CARDS ==================== -->
            <c:if test="${affExiste or planExiste}">
                <div class="row g-3 mb-4">
                    <div class="col-6 col-md-3">
                        <div class="kpi-card">
                            <div class="kpi-icon icon-blue"><i class="bi bi-people-fill"></i></div>
                            <div>
                                <div class="kpi-val">${statsAff.totalEtudiants}</div>
                                <div class="kpi-lbl">Étudiants</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="kpi-card">
                            <div class="kpi-icon icon-orange"><i class="bi bi-person-badge-fill"></i></div>
                            <div>
                                <div class="kpi-val">${statsAff.totalProfesseurs}</div>
                                <div class="kpi-lbl">Encadrants</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="kpi-card">
                            <div class="kpi-icon icon-green"><i class="bi bi-calendar-check-fill"></i></div>
                            <div>
                                <div class="kpi-val">${statsPlan.totalSoutenances}</div>
                                <div class="kpi-lbl">Soutenances</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="kpi-card">
                            <div class="kpi-icon icon-blue"><i class="bi bi-building-fill"></i></div>
                            <div>
                                <div class="kpi-val">${statsPlan.totalSalles}</div>
                                <div class="kpi-lbl">Salles</div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- ==================== SECTION AFFECTATION ==================== -->
            <div class="section-card">
                <div class="section-head">
                    <div class="section-title"><i class="bi bi-people-fill"></i> Affectation des encadrants</div>
                    <c:choose>
                        <c:when test="${affExiste}"><span class="badge-ok"><i class="bi bi-check-circle-fill me-1"></i>Générée</span></c:when>
                        <c:otherwise><span class="badge-nd"><i class="bi bi-exclamation-triangle-fill me-1"></i>Non générée</span></c:otherwise>
                    </c:choose>
                </div>

                <c:choose>
                    <c:when test="${affExiste}">
                        <div class="row g-3 mb-4">
                            <div class="col-4">
                                <div class="mini-box"><div class="mini-val">${statsAff.minParEncadrant}</div><div class="mini-lbl">Min / encadrant</div></div>
                            </div>
                            <div class="col-4">
                                <div class="mini-box"><div class="mini-val">${statsAff.maxParEncadrant}</div><div class="mini-lbl">Max / encadrant</div></div>
                            </div>
                            <div class="col-4">
                                <div class="mini-box"><div class="mini-val">${statsAff.moyenneParEncadrant}</div><div class="mini-lbl">Moyenne</div></div>
                            </div>
                        </div>

                        <div class="row g-4">
                            <div class="col-lg-7">
                                <div style="max-height:300px; overflow-y:auto; border-radius:12px; border:1px solid #e4e8ef;">
                                    <table class="table table-sm table-custom mb-0">
                                        <thead><tr><th>Encadrant</th><th class="text-center">Étudiants</th><th>Répartition</th></tr></thead>
                                        <tbody>
                                            <c:forEach items="${statsAff.encadrants}" var="enc">
                                                <tr>
                                                    <td><i class="bi bi-person-circle me-1 text-primary"></i><strong>${enc.nom}</strong></td>
                                                    <td class="text-center"><span class="badge rounded-pill" style="background:var(--blue);">${enc.nbEtudiants}</span></td>
                                                    <td style="width:38%;">
                                                        <div class="bar-wrap"><div class="bar-fill" style="width:${(enc.nbEtudiants / statsAff.maxParEncadrant) * 100}%"></div></div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="col-lg-5">
                                <div class="fw-bold mb-2" style="font-size:.85rem; color:#6c757d;"><i class="bi bi-pie-chart me-1"></i>Répartition par filière</div>
                                <canvas id="chartFiliereAff" style="max-height:220px;"></canvas>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <i class="bi bi-file-excel text-muted"></i>
                            <div>Aucune affectation disponible</div>
                            <a href="${pageContext.request.contextPath}/affectation" class="btn btn-primary btn-sm mt-3">Générer l'affectation</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- ==================== SECTION PLANNING ==================== -->
            <div class="section-card">
                <div class="section-head">
                    <div class="section-title"><i class="bi bi-calendar-week-fill"></i> Planning des soutenances</div>
                    <c:choose>
                        <c:when test="${planExiste}"><span class="badge-ok"><i class="bi bi-check-circle-fill me-1"></i>Généré</span></c:when>
                        <c:otherwise><span class="badge-nd"><i class="bi bi-exclamation-triangle-fill me-1"></i>Non généré</span></c:otherwise>
                    </c:choose>
                </div>

                <c:choose>
                    <c:when test="${planExiste}">
                        <div class="row g-3 mb-4">
                            <div class="col-6 col-md-3">
                                <div class="mini-box"><div class="mini-val">${statsPlan.totalSoutenances}</div><div class="mini-lbl">Total</div></div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="mini-box"><div class="mini-val">${statsPlan.totalJours}</div><div class="mini-lbl">Jours</div></div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="mini-box"><div class="mini-val">${statsPlan.totalSalles}</div><div class="mini-lbl">Salles</div></div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="mini-box"><div class="mini-val">${statsPlan.totalProfesseurs}</div><div class="mini-lbl">Professeurs</div></div>
                            </div>
                        </div>

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <div class="p-3 rounded-3 border bg-light">
                                    <div class="fw-bold mb-2"><i class="bi bi-calendar-day me-1 text-primary"></i>Jour le plus chargé</div>
                                    <div style="color:var(--blue); font-weight:800;">${statsPlan.jourPlusCharge} <span class="text-muted fw-normal">– ${statsPlan.maxParJour} soutenances</span></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="p-3 rounded-3 border bg-light">
                                    <div class="fw-bold mb-2"><i class="bi bi-building me-1 text-warning"></i>Salle la plus utilisée</div>
                                    <div style="color:var(--orange); font-weight:800;">${statsPlan.sallePlusUtilisee}</div>
                                </div>
                            </div>
                        </div>

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <div class="mini-box p-3">
                                    <div class="fw-bold mb-3"><i class="bi bi-calendar-day me-1 text-primary"></i>Soutenances par jour</div>
                                    <canvas id="chartJour" style="max-height:200px;"></canvas>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mini-box p-3">
                                    <div class="fw-bold mb-3"><i class="bi bi-building me-1 text-warning"></i>Soutenances par salle</div>
                                    <canvas id="chartSalle" style="max-height:200px;"></canvas>
                                </div>
                            </div>
                        </div>

                        <c:if test="${not empty statsPlan.topProfs}">
                            <div class="fw-bold mb-2" style="font-size:.9rem; color:var(--blue);"><i class="bi bi-bar-chart-fill me-1"></i>Charge des professeurs</div>
                            <div style="border-radius:12px; border:1px solid #e4e8ef; max-height:260px; overflow-y:auto;">
                                <table class="table table-sm table-custom mb-0">
                                    <thead><tr><th>Professeur</th><th class="text-center">Participations</th><th>Charge</th></tr></thead>
                                    <tbody>
                                        <c:set var="maxProf" value="${statsPlan.topProfs[0].total}"/>
                                        <c:forEach items="${statsPlan.topProfs}" var="prof">
                                            <tr>
                                                <td><i class="bi bi-person-circle me-1 text-primary"></i>${prof.nom}</td>
                                                <td class="text-center"><span class="badge rounded-pill" style="background:var(--orange);">${prof.total}</span></td>
                                                <td style="width:40%;"><div class="bar-wrap"><div class="bar-fill orange" style="width:${(prof.total / maxProf) * 100}%"></div></div></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <i class="bi bi-calendar-x text-muted"></i>
                            <div>Aucun planning disponible</div>
                            <a href="${pageContext.request.contextPath}/planning" class="btn btn-primary btn-sm mt-3">Générer le planning</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- ==================== VALIDATION ==================== -->
            <c:if test="${planExiste}">
                <div class="section-card">
                    <div class="section-head">
                        <div class="section-title"><i class="bi bi-shield-check-fill"></i> Validation des contraintes</div>
                        <c:choose>
                            <c:when test="${statsPlan.planningValide}"><span class="badge-ok"><i class="bi bi-check-circle-fill me-1"></i>Valide</span></c:when>
                            <c:otherwise><span class="badge-nd"><i class="bi bi-exclamation-triangle-fill me-1"></i>Conflits détectés</span></c:otherwise>
                        </c:choose>
                    </div>
                    <div class="row g-3">
                        <div class="col-md-4">
                            <div class="p-3 rounded-3 border bg-light text-center">
                                <i class="bi bi-building ${statsPlan.conflitsSalles == 0 ? 'text-success' : 'text-danger'} fs-3"></i>
                                <div class="fw-bold mt-1">${statsPlan.conflitsSalles == 0 ? 'Aucun conflit' : statsPlan.conflitsSalles + ' conflit(s)'}</div>
                                <small class="text-muted">Conflits salles</small>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="p-3 rounded-3 border bg-light text-center">
                                <i class="bi bi-people ${statsPlan.conflitsProfs == 0 ? 'text-success' : 'text-danger'} fs-3"></i>
                                <div class="fw-bold mt-1">${statsPlan.conflitsProfs == 0 ? 'Aucun conflit' : statsPlan.conflitsProfs + ' conflit(s)'}</div>
                                <small class="text-muted">Conflits professeurs</small>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="p-3 rounded-3 border bg-light text-center">
                                <i class="bi bi-${statsPlan.planningValide ? 'check-circle-fill text-success' : 'x-circle-fill text-danger'} fs-3"></i>
                                <div class="fw-bold mt-1">${statsPlan.planningValide ? 'VALIDE' : 'NON VALIDE'}</div>
                                <small class="text-muted">Statut global</small>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

        </div>
    </div>

    <%@ include file="/footer.jsp" %>

    <script>
    const COLORS = ['#0033a0','#e87722','#198754','#17a2b8','#6f42c1','#fd7e14','#20c997','#dc3545'];

    <c:if test="${affExiste and not empty statsAff.parFiliere}">
    new Chart(document.getElementById('chartFiliereAff'), {
        type: 'doughnut',
        data: {
            labels: [<c:forEach items="${statsAff.parFiliere}" var="e" varStatus="s">'${e.key}'<c:if test="${not s.last}">,</c:if></c:forEach>],
            datasets: [{ data: [<c:forEach items="${statsAff.parFiliere}" var="e" varStatus="s">${e.value}<c:if test="${not s.last}">,</c:if></c:forEach>], backgroundColor: COLORS, borderWidth: 0 }]
        },
        options: { responsive: true, maintainAspectRatio: true, cutout: '60%', plugins: { legend: { position: 'bottom', labels: { font: { size: 11 } } } } }
    });
    </c:if>

    <c:if test="${planExiste and not empty statsPlan.parJour}">
    new Chart(document.getElementById('chartJour'), {
        type: 'bar',
        data: { labels: [<c:forEach items="${statsPlan.parJour}" var="e" varStatus="s">'${e.key}'<c:if test="${not s.last}">,</c:if></c:forEach>], datasets: [{ label: 'Soutenances', data: [<c:forEach items="${statsPlan.parJour}" var="e" varStatus="s">${e.value}<c:if test="${not s.last}">,</c:if></c:forEach>], backgroundColor: '#0033a0', borderRadius: 7 }] },
        options: { responsive: true, maintainAspectRatio: true, plugins: { legend: { display: false } }, scales: { x: { grid: { display: false } }, y: { beginAtZero: true, ticks: { stepSize: 1 } } } }
    });
    new Chart(document.getElementById('chartSalle'), {
        type: 'bar',
        data: { labels: [<c:forEach items="${statsPlan.parSalle}" var="e" varStatus="s">'${e.key}'<c:if test="${not s.last}">,</c:if></c:forEach>], datasets: [{ label: 'Soutenances', data: [<c:forEach items="${statsPlan.parSalle}" var="e" varStatus="s">${e.value}<c:if test="${not s.last}">,</c:if></c:forEach>], backgroundColor: '#e87722', borderRadius: 7 }] },
        options: { responsive: true, maintainAspectRatio: true, plugins: { legend: { display: false } }, scales: { x: { grid: { display: false } }, y: { beginAtZero: true, ticks: { stepSize: 1 } } } }
    });
    </c:if>
    </script>
</body>
</html>