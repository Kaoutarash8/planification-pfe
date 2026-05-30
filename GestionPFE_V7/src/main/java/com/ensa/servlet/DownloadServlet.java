package com.ensa.servlet;

import com.ensa.util.FileUtil;
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
        
        // Utiliser FileUtil pour le chemin (portable)
        String uploadPath = FileUtil.getUploadPath();
        
        File file = new File(uploadPath, fileName);
        
        System.out.println("Download - uploadPath: " + uploadPath);
        System.out.println("Download - fichier: " + file.getAbsolutePath());
        System.out.println("Download - existe: " + file.exists());
        
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier non trouve: " + fileName);
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
            System.out.println("Download - fichier envoye avec succes: " + fileName);
        } catch (Exception e) {
            System.err.println("Download - erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}