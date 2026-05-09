package com.ensa.servlet;

import com.ensa.model.Professeur;
import com.ensa.model.Etudiant;
import com.ensa.service.interfaces.PVGenerationService;
import com.ensa.service.impl.PVGenerationServiceImpl;
import com.ensa.util.PDFGeneratorUtil;
import com.ensa.util.ExcelReader;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet("/DownloadPvsServlet")
public class DownloadPvsServlet extends HttpServlet {
    
    private PVGenerationService pvService;
    
    @Override
    public void init() throws ServletException {
        pvService = new PVGenerationServiceImpl();
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
        
        String action = request.getParameter("action");
        String nomEncadrant = request.getParameter("nom");
        
        System.out.println("=== DownloadPvsServlet ===");
        System.out.println("Action: " + action);
        System.out.println("Nom encadrant: " + nomEncadrant);
        
        if (nomEncadrant != null) {
            nomEncadrant = URLDecoder.decode(nomEncadrant, "UTF-8");
            System.out.println("Nom décodé: " + nomEncadrant);
        }
        
        String uploadPath = getUploadPath();
        String annee = getAnneeUniversitaire();
        String planningPath = uploadPath + "/planning_" + annee + ".xlsx";
        
        System.out.println("Planning path: " + planningPath);
        System.out.println("Planning existe: " + new File(planningPath).exists());
        
        try {
            // Lire le planning avec ExcelReader
            Map<String, List<Map<String, String>>> encadrantsMap = ExcelReader.getEncadrantsAvecEtudiants(planningPath);
            
            System.out.println("Encadrants trouvés: " + encadrantsMap.size());
            for (String nom : encadrantsMap.keySet()) {
                System.out.println("  - " + nom);
            }
            
            if ("tous".equals(action)) {
                // Générer tous les PVs
                generateAllPVs(response, encadrantsMap, uploadPath);
                
            } else if ("encadrant".equals(action) && nomEncadrant != null) {
                // Générer les PVs pour un encadrant spécifique
                generateEncadrantPVs(response, encadrantsMap, nomEncadrant, uploadPath);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action ou paramètre invalide");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur: " + e.getMessage());
        }
    }
    
    private void generateAllPVs(HttpServletResponse response, 
                                 Map<String, List<Map<String, String>>> encadrantsMap,
                                 String uploadPath) throws Exception {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            for (Map.Entry<String, List<Map<String, String>>> entry : encadrantsMap.entrySet()) {
                String nomEncadrant = entry.getKey();
                List<Map<String, String>> etudiants = entry.getValue();
                
                String nomDossier = nomEncadrant.replaceAll("[^a-zA-Z0-9]", "_");
                File encadrantDir = new File(uploadPath + "/pvs", nomDossier);
                if (!encadrantDir.exists()) {
                    encadrantDir.mkdirs();
                }
                
                for (Map<String, String> etudiantInfo : etudiants) {
                    String nomEtudiant = etudiantInfo.get("nom");
                    String prenomEtudiant = etudiantInfo.get("prenom");
                    String filiere = etudiantInfo.get("filiere");
                    
                    // Créer l'objet Professeur
                    Professeur encadrant = new Professeur();
                    String[] parts = nomEncadrant.split(" ");
                    if (parts.length >= 2) {
                        encadrant.setPrenom(parts[0]);
                        encadrant.setNom(parts[1]);
                    } else {
                        encadrant.setNom(nomEncadrant);
                        encadrant.setPrenom("");
                    }
                    
                    // Créer l'étudiant
                    Etudiant etudiant = new Etudiant();
                    etudiant.setNom(nomEtudiant);
                    etudiant.setPrenom(prenomEtudiant);
                    etudiant.setFiliere(filiere);
                    
                    List<Etudiant> etudiantsList = new ArrayList<>();
                    etudiantsList.add(etudiant);
                    
                    List<PVGenerationService.Note> notes = new ArrayList<>();
                    PVGenerationService.Note note = new PVGenerationService.Note();
                    note.setNomComplet(prenomEtudiant + " " + nomEtudiant);
                    note.setNoteContenu(0);
                    note.setNoteMemoire(0);
                    note.setNoteSoutenance(0);
                    notes.add(note);
                    
                    // Générer le PDF
                    byte[] pdfBytes = PDFGeneratorUtil.generateEvaluationPDF(
                        encadrant, etudiantsList, notes, "", "", "",
                        new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date())
                    );
                    
                    // Sauvegarder
                    String fileName = prenomEtudiant + "_" + nomEtudiant + ".pdf";
                    File pdfFile = new File(encadrantDir, fileName);
                    try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                        fos.write(pdfBytes);
                    }
                    
                    // Ajouter au ZIP
                    ZipEntry zipEntry = new ZipEntry(nomDossier + "/" + fileName);
                    zos.putNextEntry(zipEntry);
                    zos.write(pdfBytes);
                    zos.closeEntry();
                }
            }
        }
        
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"Tous_les_PVs.zip\"");
        response.getOutputStream().write(baos.toByteArray());
        System.out.println("✅ ZIP généré avec succès");
    }
    
    private void generateEncadrantPVs(HttpServletResponse response,
                                       Map<String, List<Map<String, String>>> encadrantsMap,
                                       String nomEncadrant,
                                       String uploadPath) throws Exception {
        
        // Chercher l'encadrant (correspondance exacte ou partielle)
        Map<String, List<Map<String, String>>> encadrantData = null;
        String foundName = null;
        
        for (Map.Entry<String, List<Map<String, String>>> entry : encadrantsMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(nomEncadrant) || 
                entry.getKey().toLowerCase().contains(nomEncadrant.toLowerCase())) {
                encadrantData = new HashMap<>();
                encadrantData.put(entry.getKey(), entry.getValue());
                foundName = entry.getKey();
                break;
            }
        }
        
        if (encadrantData == null || foundName == null) {
            throw new Exception("Encadrant non trouvé: " + nomEncadrant);
        }
        
        System.out.println("Encadrant trouvé: " + foundName);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            for (Map.Entry<String, List<Map<String, String>>> entry : encadrantData.entrySet()) {
                String nomEnc = entry.getKey();
                List<Map<String, String>> etudiants = entry.getValue();
                
                String nomDossier = nomEnc.replaceAll("[^a-zA-Z0-9]", "_");
                File encadrantDir = new File(uploadPath + "/pvs", nomDossier);
                if (!encadrantDir.exists()) {
                    encadrantDir.mkdirs();
                }
                
                // Créer l'objet Professeur
                Professeur encadrant = new Professeur();
                String[] parts = nomEnc.split(" ");
                if (parts.length >= 2) {
                    encadrant.setPrenom(parts[0]);
                    encadrant.setNom(parts[1]);
                } else {
                    encadrant.setNom(nomEnc);
                    encadrant.setPrenom("");
                }
                
                for (Map<String, String> etudiantInfo : etudiants) {
                    String nomEtudiant = etudiantInfo.get("nom");
                    String prenomEtudiant = etudiantInfo.get("prenom");
                    String filiere = etudiantInfo.get("filiere");
                    
                    Etudiant etudiant = new Etudiant();
                    etudiant.setNom(nomEtudiant);
                    etudiant.setPrenom(prenomEtudiant);
                    etudiant.setFiliere(filiere);
                    
                    List<Etudiant> etudiantsList = new ArrayList<>();
                    etudiantsList.add(etudiant);
                    
                    List<PVGenerationService.Note> notes = new ArrayList<>();
                    PVGenerationService.Note note = new PVGenerationService.Note();
                    note.setNomComplet(prenomEtudiant + " " + nomEtudiant);
                    note.setNoteContenu(0);
                    note.setNoteMemoire(0);
                    note.setNoteSoutenance(0);
                    notes.add(note);
                    
                    byte[] pdfBytes = PDFGeneratorUtil.generateEvaluationPDF(
                        encadrant, etudiantsList, notes, "", "", "",
                        new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date())
                    );
                    
                    String fileName = prenomEtudiant + "_" + nomEtudiant + ".pdf";
                    File pdfFile = new File(encadrantDir, fileName);
                    try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                        fos.write(pdfBytes);
                    }
                    
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    zos.write(pdfBytes);
                    zos.closeEntry();
                }
            }
        }
        
        String zipName = foundName.replaceAll("[^a-zA-Z0-9]", "_") + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");
        response.getOutputStream().write(baos.toByteArray());
        System.out.println("✅ ZIP généré pour " + foundName);
    }
}