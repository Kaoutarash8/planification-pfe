package com.ensa.servlet;

import com.ensa.util.PDFGeneratorUtil;
import com.ensa.util.ExcelReader;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet("/DownloadPvsServlet")
public class DownloadPvsServlet extends HttpServlet {

    private static final String LOGO_ENSA = "/images/logo_ensa.png";
    private static final String LOGO_UAE  = "/images/logo_uae.png";

    private String getUploadPath() {
        String uploadPath = (String) getServletContext().getAttribute("uploadPath");
        if (uploadPath == null) {
            uploadPath = getServletContext().getRealPath("/uploads");
            if (uploadPath == null) {
                uploadPath = System.getProperty("user.dir").replace('\\', '/') + "/src/main/webapp/uploads";
            }
            getServletContext().setAttribute("uploadPath", uploadPath);
        }
        return uploadPath;
    }

    private String getAnneeUniversitaire() {
        Calendar cal  = Calendar.getInstance();
        int mois  = cal.get(Calendar.MONTH);
        int annee = cal.get(Calendar.YEAR);
        return (mois >= 8) ? annee + "_" + (annee + 1) : (annee - 1) + "_" + annee;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action       = request.getParameter("action");
        String nomEncadrant = request.getParameter("nom");

        if (nomEncadrant != null) {
            nomEncadrant = URLDecoder.decode(nomEncadrant, "UTF-8");
        }

        String uploadPath    = getUploadPath();
        String planningPath  = uploadPath + "/planning_" + getAnneeUniversitaire() + ".xlsx";
        String logoEnsaPath  = getServletContext().getRealPath(LOGO_ENSA);
        String logoUaePath   = getServletContext().getRealPath(LOGO_UAE);

        System.out.println("=== DownloadPvsServlet ===");
        System.out.println("Planning : " + planningPath + " | existe: " + new File(planningPath).exists());

        try {
            Map<String, List<Map<String, String>>> encadrantsMap =
                    ExcelReader.getEncadrantsAvecEtudiants(planningPath);

            if ("tous".equals(action)) {
                generateAllPVs(response, encadrantsMap, uploadPath, logoEnsaPath, logoUaePath);
            } else if ("encadrant".equals(action) && nomEncadrant != null) {
                generateEncadrantPVs(response, encadrantsMap, nomEncadrant, uploadPath, logoEnsaPath, logoUaePath);
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
                                  String uploadPath,
                                  String logoEnsaPath, String logoUaePath) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, List<Map<String, String>>> entry : encadrantsMap.entrySet()) {

                String nomEncadrant = entry.getKey();
                String nomDossier   = toSafeName(nomEncadrant);
                new File(uploadPath + "/pvs", nomDossier).mkdirs();

                for (Map<String, String> info : entry.getValue()) {

                    String etudiantComplet = getEtudiantComplet(info);
                    String filiere = nvl(info.get("filiere"));
                    String jury1   = nvl(info.get("jury1"));   // Premier rapporteur
                    String jury2   = nvl(info.get("jury2"));   // Deuxième rapporteur
                    String date    = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

                    // CORRECTION: 
                    // - President = encadrant (nomEncadrant)
                    // - Rapporteur 1 = jury1
                    // - Rapporteur 2 = jury2
                    byte[] pdf = PDFGeneratorUtil.generateSimpleEvaluationPDF(
                            logoEnsaPath, logoUaePath,
                            nomEncadrant,           // Encadrant
                            etudiantComplet,        // Etudiant
                            filiere,                // Filiere
                            nomEncadrant,           // President = ENCADRANT
                            jury1,                  // Rapporteur 1 = Jury 1 du planning
                            jury2,                  // Rapporteur 2 = Jury 2 du planning
                            date);

                    String fileName = toSafeName(etudiantComplet) + ".pdf";

                    try (FileOutputStream fos = new FileOutputStream(
                            new File(uploadPath + "/pvs/" + nomDossier, fileName))) {
                        fos.write(pdf);
                    }

                    zos.putNextEntry(new ZipEntry(nomDossier + "/" + fileName));
                    zos.write(pdf);
                    zos.closeEntry();
                    System.out.println("  ✅ " + nomEncadrant + " / " + etudiantComplet);
                    System.out.println("     President: " + nomEncadrant);
                    System.out.println("     Rapporteur1: " + jury1);
                    System.out.println("     Rapporteur2: " + jury2);
                }
            }
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"Tous_les_PVs.zip\"");
        response.getOutputStream().write(baos.toByteArray());
    }

    private void generateEncadrantPVs(HttpServletResponse response,
                                       Map<String, List<Map<String, String>>> encadrantsMap,
                                       String nomEncadrant,
                                       String uploadPath,
                                       String logoEnsaPath, String logoUaePath) throws Exception {

        String foundName = null;
        List<Map<String, String>> etudiants = null;

        for (Map.Entry<String, List<Map<String, String>>> entry : encadrantsMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(nomEncadrant) ||
                entry.getKey().toLowerCase().contains(nomEncadrant.toLowerCase())) {
                foundName = entry.getKey();
                etudiants = entry.getValue();
                break;
            }
        }

        if (foundName == null) throw new Exception("Encadrant non trouvé : " + nomEncadrant);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map<String, String> info : etudiants) {

                String etudiantComplet = getEtudiantComplet(info);
                String filiere = nvl(info.get("filiere"));
                String jury1   = nvl(info.get("jury1"));   // Premier rapporteur
                String jury2   = nvl(info.get("jury2"));   // Deuxième rapporteur
                String date    = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

                // CORRECTION: 
                // - President = encadrant (foundName)
                // - Rapporteur 1 = jury1
                // - Rapporteur 2 = jury2
                byte[] pdf = PDFGeneratorUtil.generateSimpleEvaluationPDF(
                        logoEnsaPath, logoUaePath,
                        foundName,              // Encadrant
                        etudiantComplet,        // Etudiant
                        filiere,                // Filiere
                        foundName,              // President = ENCADRANT
                        jury1,                  // Rapporteur 1 = Jury 1 du planning
                        jury2,                  // Rapporteur 2 = Jury 2 du planning
                        date);

                String fileName = toSafeName(etudiantComplet) + ".pdf";
                zos.putNextEntry(new ZipEntry(fileName));
                zos.write(pdf);
                zos.closeEntry();
                System.out.println("  ✅ " + etudiantComplet);
                System.out.println("     President: " + foundName);
                System.out.println("     Rapporteur1: " + jury1);
                System.out.println("     Rapporteur2: " + jury2);
            }
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + toSafeName(foundName) + ".zip\"");
        response.getOutputStream().write(baos.toByteArray());
    }

    private static String getEtudiantComplet(Map<String, String> info) {
        String nc = info.get("nomComplet");
        if (nc != null && !nc.trim().isEmpty()) return nc.trim();
        String prenom = nvl(info.get("prenom"));
        String nom    = nvl(info.get("nom"));
        return (prenom + " " + nom).trim();
    }

    private static String nvl(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static String toSafeName(String s) {
        return (s == null) ? "_" : s.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}