<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.time.Year" %>

<!-- Footer commun -->
<footer class="ensa-footer">
    <div class="container">
        <div class="footer-content">
            <p>
                &copy; <%= Year.now().getValue() %> ENSA Al Hoceima - Département Mathématiques et Informatique
            </p>
            <p class="small">
                Plateforme d'affectation des encadrants de PFE
            </p>
        </div>
    </div>
</footer>

<style>
    .ensa-footer {
        background: #f8f9fa;
        border-top: 1px solid #e5e7eb;
        padding: 20px 0;
        margin-top: 50px;
    }

    .footer-content {
        text-align: center;
    }

    .footer-content p {
        margin: 0;
        color: #6c757d;
        font-size: 0.85rem;
    }

    .footer-content .small {
        font-size: 0.75rem;
        margin-top: 5px;
        color: #9aa0a6;
    }
</style>