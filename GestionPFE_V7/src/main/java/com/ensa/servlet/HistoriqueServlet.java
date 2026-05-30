package com.ensa.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

@WebServlet("/historique")
public class HistoriqueServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Récupérer le chemin d'upload depuis le contexte
        String uploadPath = (String) getServletContext().getAttribute("uploadPath");
        
        if (uploadPath == null) {
            uploadPath = getTomcatUploadPath();
            getServletContext().setAttribute("uploadPath", uploadPath);
        }
        
        // Lire TOUS les fichiers (affectation ET planning)
        List<Map<String, String>> historique = new ArrayList<>();
        File uploadDir = new File(uploadPath);
        
        if (uploadDir.exists()) {
            File[] fichiers = uploadDir.listFiles((dir, name) -> 
                (name.startsWith("affectation_") || name.startsWith("planning_")) && 
                name.endsWith(".xlsx"));
            
            if (fichiers != null) {
                for (File f : fichiers) {
                    Map<String, String> info = new LinkedHashMap<>();
                    
                    String nom = f.getName();
                    String type = "";
                    String annee = "";
                    
                    if (nom.startsWith("affectation_")) {
                        type = "Affectation";
                        annee = nom.replace("affectation_", "").replace(".xlsx", "");
                    } else if (nom.startsWith("planning_")) {
                        type = "Planning";
                        annee = nom.replace("planning_", "").replace(".xlsx", "");
                    }
                    
                    info.put("type", type);
                    info.put("annee", annee);
                    info.put("nomFichier", nom);
                    info.put("taille", formatTaille(f.length()));
                    info.put("dateModif", formatDate(f.lastModified()));
                    info.put("chemin", f.getAbsolutePath());
                    
                    historique.add(info);
                }
            }
        }
        
        // Trier par année (plus récente en premier)
        historique.sort((a, b) -> b.get("annee").compareTo(a.get("annee")));
        
        request.setAttribute("historique", historique);
        request.setAttribute("uploadPath", uploadPath);
        
        request.getRequestDispatcher("/WEB-INF/views/historique.jsp").forward(request, response);
    }
    
    private String getTomcatUploadPath() {
        String realPath = getServletContext().getRealPath("/uploads");
        if (realPath != null && !realPath.isEmpty()) {
            return realPath;
        }
        
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null) {
            String contextPath = getServletContext().getContextPath().replace("/", "");
            return catalinaBase + File.separator + "webapps" + 
                   File.separator + contextPath + File.separator + "uploads";
        }
        
        return System.getProperty("java.io.tmpdir") + File.separator + "pfe_uploads";
    }
    
    private String formatTaille(long bytes) {
        if (bytes < 1024) return bytes + " o";
        if (bytes < 1024 * 1024) return String.format("%.1f Ko", bytes / 1024.0);
        return String.format("%.1f Mo", bytes / (1024.0 * 1024));
    }
    
    private String formatDate(long timestamp) {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            .format(new java.util.Date(timestamp));
    }
}