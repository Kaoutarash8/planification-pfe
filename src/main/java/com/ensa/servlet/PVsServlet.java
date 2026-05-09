package com.ensa.servlet;

import com.ensa.util.ExcelReader;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@WebServlet("/pvs")
public class PVsServlet extends HttpServlet {
    
    private ObjectMapper objectMapper;
    
    @Override
    public void init() throws ServletException {
        objectMapper = new ObjectMapper();
        
        String uploadPath = getUploadPath();
        File pvsDir = new File(uploadPath, "pvs");
        if (!pvsDir.exists()) {
            pvsDir.mkdirs();
        }
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
        
        String uploadPath = getUploadPath();
        String annee = getAnneeUniversitaire();
        String planningPath = uploadPath + "/planning_" + annee + ".xlsx";
        
        try {
            // Lire le planning avec ExcelReader
            Map<String, List<Map<String, String>>> encadrantsMap = ExcelReader.getEncadrantsAvecEtudiants(planningPath);
            
            File pvsDir = new File(uploadPath, "pvs");
            List<Map<String, Object>> encadrantsList = new ArrayList<>();
            
            // Pour chaque encadrant unique
            for (Map.Entry<String, List<Map<String, String>>> entry : encadrantsMap.entrySet()) {
                String nomEncadrant = entry.getKey();
                List<Map<String, String>> etudiants = entry.getValue();
                
                Map<String, Object> enc = new LinkedHashMap<>();
                enc.put("nom", nomEncadrant);
                enc.put("nbEtudiants", etudiants.size());
                
                // Compter les PVs existants
                String nomDossier = nomEncadrant.replaceAll("[^a-zA-Z0-9]", "_");
                File encadrantDir = new File(pvsDir, nomDossier);
                int nbPVs = 0;
                if (encadrantDir.exists()) {
                    File[] fichiers = encadrantDir.listFiles();
                    if (fichiers != null) {
                        nbPVs = fichiers.length;
                    }
                }
                enc.put("nbPVs", nbPVs);
                
                encadrantsList.add(enc);
            }
            
            // Trier par nom
            encadrantsList.sort((a, b) -> ((String)a.get("nom")).compareTo((String)b.get("nom")));
            
            // Afficher le nombre dans la console pour debug
            System.out.println("=== PVs Servlet ===");
            System.out.println("Nombre d'encadrants uniques: " + encadrantsList.size());
            for (Map<String, Object> e : encadrantsList) {
                System.out.println("  - " + e.get("nom") + " (" + e.get("nbEtudiants") + " étudiants)");
            }
            
            // Convertir en JSON
            String encadrantsJson = objectMapper.writeValueAsString(encadrantsList);
            request.setAttribute("encadrantsJson", encadrantsJson);
            request.getSession().setAttribute("fichiersPV", encadrantsList);
            
            request.getRequestDispatcher("/WEB-INF/views/pvs.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/pvs.jsp").forward(request, response);
        }
    }
}