<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
                <a href="${pageContext.request.contextPath}/affectation" class="${param.page == 'affectation' ? 'active' : ''}">
                    <i class="bi bi-people"></i> Affectation
                </a>
                <a href="#" class="${param.page == 'planning' ? 'active' : ''}">
                    <i class="bi bi-calendar-week"></i> Planning
                </a>
                <a href="#" class="${param.page == 'pvs' ? 'active' : ''}">
                    <i class="bi bi-file-text"></i> PVs
                </a>
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
    }
    
    .nav-menu a {
        color: #555;
        text-decoration: none;
        font-size: 0.95rem;
        font-weight: 500;
        padding: 8px 0;
        transition: all 0.3s;
        border-bottom: 2px solid transparent;
    }
    
    .nav-menu a:hover, .nav-menu a.active {
        color: #0033a0;
        border-bottom-color: #0033a0;
    }
    
    @media (max-width: 768px) {
        .header-content {
            flex-direction: column;
            text-align: center;
        }
        .nav-menu {
            gap: 20px;
        }
    }
</style>