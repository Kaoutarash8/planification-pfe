package com.ensa.servlet;

import com.ensa.service.interfaces.StatistiqueService;
import com.ensa.service.impl.StatistiqueServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@WebServlet("/statistiques")
public class StatistiqueServlet extends HttpServlet {
    
    private StatistiqueService statService;
    
    @Override
    public void init() throws ServletException {
        statService = new StatistiqueServiceImpl();
        System.out.println("=== STATISTIQUE SERVLET INITIALISEE ===");
    }
    
    private String getUploadPath() {
        String uploadPath = (String) getServletContext().getAttribute("uploadPath");
        if (uploadPath == null) {
            String projectPath = System.getProperty("user.dir").replace('\\', '/');
            uploadPath = projectPath + "/src/main/webapp/uploads";
            getServletContext().setAttribute("uploadPath", uploadPath);
        }
        return uploadPath;
    }
    
    private String getAnneeUniversitaire() {
        Calendar cal = Calendar.getInstance();
        int annee = cal.get(Calendar.YEAR);
        int anneePrecedente = annee - 1;
        return anneePrecedente + "_" + annee;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String uploadPath = getUploadPath();
            String annee = getAnneeUniversitaire();
            String planningPath = uploadPath + "/planning_" + annee + ".xlsx";
            
            System.out.println("=== STATISTIQUES ===");
            System.out.println("Planning path: " + planningPath);
            
            File planningFile = new File(planningPath);
            if (!planningFile.exists()) {
                request.setAttribute("error", "Planning non trouvé. Veuillez d'abord générer le planning.");
                request.getRequestDispatcher("/WEB-INF/views/statistiques.jsp").forward(request, response);
                return;
            }
            
            // Récupérer toutes les statistiques
            request.setAttribute("totalEtudiants", statService.getTotalEtudiants(planningPath));
            request.setAttribute("totalSoutenances", statService.getTotalSoutenances(planningPath));
            request.setAttribute("totalEncadrants", statService.getTotalEncadrants(planningPath));
            request.setAttribute("totalSalles", statService.getTotalSalles(planningPath));
            request.setAttribute("totalMembresJury", statService.getTotalMembresJury(planningPath));
            request.setAttribute("repartitionFiliere", statService.getRepartitionParFiliere(planningPath));
            request.setAttribute("soutenancesParJour", statService.getSoutenancesParJour(planningPath));
            request.setAttribute("soutenancesParSalle", statService.getSoutenancesParSalle(planningPath));
            request.setAttribute("etudiantsParEncadrant", statService.getEtudiantsParEncadrant(planningPath, 10));
           
            request.setAttribute("participationsSpecialite", statService.getParticipationsParSpecialite(planningPath));

            request.setAttribute("chargeMoyenne", statService.getChargeMoyenneParProfesseur(planningPath));
            
            request.getRequestDispatcher("/WEB-INF/views/statistiques.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur lors du chargement des statistiques: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/statistiques.jsp").forward(request, response);
        }
    }
}