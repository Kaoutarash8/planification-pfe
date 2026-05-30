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
    }

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

        String uploadPath   = getUploadPath();
        String planningPath = uploadPath + "/planning_" + getAnneeUniversitaire() + ".xlsx";

        // Vérifier que le planning existe
        if (!new File(planningPath).exists()) {
            request.setAttribute("error", "Planning non trouvé : " + planningPath
                    + ". Veuillez d'abord générer le planning.");
            request.getRequestDispatcher("/WEB-INF/views/pvs.jsp").forward(request, response);
            return;
        }

        try {
            Map<String, List<Map<String, String>>> encadrantsMap =
                    ExcelReader.getEncadrantsAvecEtudiants(planningPath);

            List<Map<String, Object>> encadrantsList = new ArrayList<>();
            File pvsDir = new File(uploadPath, "pvs");

            for (Map.Entry<String, List<Map<String, String>>> entry : encadrantsMap.entrySet()) {
                String nomEncadrant = entry.getKey();
                int nbEtudiants = entry.getValue().size();

                // Compter les PVs déjà générés
                String nomDossier = nomEncadrant.replaceAll("[^a-zA-Z0-9_\\-]", "_");
                File encadrantDir = new File(pvsDir, nomDossier);
                int nbPVs = 0;
                if (encadrantDir.exists()) {
                    File[] fichiers = encadrantDir.listFiles((d, n) -> n.endsWith(".pdf"));
                    if (fichiers != null) nbPVs = fichiers.length;
                }

                Map<String, Object> enc = new LinkedHashMap<>();
                enc.put("nom",         nomEncadrant);
                enc.put("nbEtudiants", nbEtudiants);
                enc.put("nbPVs",       nbPVs);
                encadrantsList.add(enc);
            }

            // Trier par nom
            encadrantsList.sort((a, b) -> ((String) a.get("nom")).compareTo((String) b.get("nom")));

            String encadrantsJson = objectMapper.writeValueAsString(encadrantsList);
            request.setAttribute("encadrantsJson", encadrantsJson);
            request.getSession().setAttribute("fichiersPV", encadrantsList);

            System.out.println("PVsServlet : " + encadrantsList.size() + " encadrants chargés depuis " + planningPath);

            request.getRequestDispatcher("/WEB-INF/views/pvs.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur lors de la lecture du planning : " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/pvs.jsp").forward(request, response);
        }
    }
}