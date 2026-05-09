package com.ensa.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String fileName = request.getParameter("file");
        
        if (fileName == null || fileName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nom de fichier manquant");
            return;
        }
        
        // Récupérer le chemin depuis le contexte
        String uploadPath = (String) getServletContext().getAttribute("uploadPath");
        
        if (uploadPath == null) {
            // Fallback: utiliser le chemin du projet
            String projectPath = System.getProperty("user.dir").replace('\\', '/');
            uploadPath = projectPath + "/src/main/webapp/uploads";
            getServletContext().setAttribute("uploadPath", uploadPath);
        }
        
        File file = new File(uploadPath, fileName);
        
        System.out.println("=== DOWNLOAD SERVLET ===");
        System.out.println("Upload path: " + uploadPath);
        System.out.println("Fichier demandé: " + fileName);
        System.out.println("Chemin complet: " + file.getAbsolutePath());
        System.out.println("Fichier existe: " + file.exists());
        
        if (!file.exists()) {
            // Lister les fichiers disponibles
            File dir = new File(uploadPath);
            if (dir.exists()) {
                System.out.println("Fichiers disponibles dans le dossier:");
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        System.out.println("  - " + f.getName());
                    }
                }
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier non trouvé: " + fileName);
            return;
        }
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", 
            "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
        response.setContentLengthLong(file.length());
        
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            fis.transferTo(os);
            os.flush();
            System.out.println("✅ Fichier envoyé avec succès: " + fileName);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}