package com.ensa.servlet;

import com.ensa.util.ExcelReader;
import com.ensa.util.ExcelReaderPlanning;
import com.ensa.model.Affectation;
import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.model.PlanningSoutenance;
import com.ensa.dao.impl.EtudiantExcelDao;
import com.ensa.dao.impl.ProfesseurExcelDao;
import com.ensa.service.impl.AffectationServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/statistiques")
public class StatistiquesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── Chemin uploads ──────────────────────────────────────────────
        String uploadPath = (String) getServletContext().getAttribute("uploadPath");
        if (uploadPath == null) {
            uploadPath = getServletContext().getRealPath("/uploads");
            if (uploadPath == null) {
                uploadPath = System.getProperty("user.dir").replace('\\', '/') + "/src/main/webapp/uploads";
            }
            getServletContext().setAttribute("uploadPath", uploadPath);
        }

        File uploadDir = new File(uploadPath);
        File affectationFile = null;
        File planningFile    = null;
        long latestAff = 0, latestPlan = 0;

        if (uploadDir.exists() && uploadDir.listFiles() != null) {
            for (File f : uploadDir.listFiles()) {
                String name = f.getName();
                if (name.startsWith("affectation_") && name.endsWith(".xlsx") && f.lastModified() > latestAff) {
                    latestAff = f.lastModified();
                    affectationFile = f;
                }
                if (name.startsWith("planning_") && name.endsWith(".xlsx") && f.lastModified() > latestPlan) {
                    latestPlan = f.lastModified();
                    planningFile = f;
                }
            }
        }

        // ── Statistiques AFFECTATION ────────────────────────────────────
        Map<String, Object> statsAff = new LinkedHashMap<>();
        boolean affExiste = false;

        if (affectationFile != null) {
            try {
                // Relire les fichiers sources pour reconstruire l'affectation
                File etudiantsFile    = new File(uploadPath, "etudiants.xlsx");
                File professeursFile  = new File(uploadPath, "professeurs.xlsx");

                if (etudiantsFile.exists() && professeursFile.exists()) {
                    EtudiantExcelDao   etudiantDao   = new EtudiantExcelDao();
                    ProfesseurExcelDao professeurDao = new ProfesseurExcelDao();

                    List<Etudiant>   etudiants   = etudiantDao.lireEtudiants(etudiantsFile.getAbsolutePath());
                    List<Professeur> professeurs = professeurDao.lireProfesseurs(professeursFile.getAbsolutePath());

                    AffectationServiceImpl service = new AffectationServiceImpl();
                    Affectation affectation = service.genererAffectation(etudiants, professeurs);

                    // Nombre d'étudiants / professeurs
                    int totalEtudiants  = etudiants.size();
                    int totalProfesseurs = professeurs.size();

                    // Min / Max / Moyenne par encadrant
                    int min = affectation.getMinParProfesseur();
                    int max = affectation.getMaxParProfesseur();
                    double moy = totalProfesseurs > 0 ? (double) totalEtudiants / totalProfesseurs : 0;

                    // Répartition par encadrant
                    List<Map<String, Object>> listeEncadrants = new ArrayList<>();
                    for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.getAffectations().entrySet()) {
                        Map<String, Object> enc = new LinkedHashMap<>();
                        enc.put("nom", entry.getKey().getNomComplet());
                        enc.put("specialite", entry.getKey().getSpecialite());
                        enc.put("nbEtudiants", entry.getValue().size());
                        // Filières encadrées
                        Map<String, Long> filieres = entry.getValue().stream()
                            .collect(Collectors.groupingBy(
                                e -> (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "N/A" : e.getFiliere(),
                                Collectors.counting()));
                        enc.put("filieres", filieres);
                        listeEncadrants.add(enc);
                    }
                    // Trier par nb étudiants décroissant
                    listeEncadrants.sort((a, b) -> Integer.compare((int) b.get("nbEtudiants"), (int) a.get("nbEtudiants")));

                    // Répartition étudiants par filière
                    Map<String, Long> parFiliere = etudiants.stream()
                        .collect(Collectors.groupingBy(
                            e -> (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "N/A" : e.getFiliere(),
                            Collectors.counting()));

                    // Affectation équilibrée ?
                    boolean equilibree = affectation.estEquilibree();

                    statsAff.put("totalEtudiants",   totalEtudiants);
                    statsAff.put("totalProfesseurs",  totalProfesseurs);
                    statsAff.put("minParEncadrant",   min);
                    statsAff.put("maxParEncadrant",   max);
                    statsAff.put("moyenneParEncadrant", String.format("%.1f", moy));
                    statsAff.put("encadrants",        listeEncadrants);
                    statsAff.put("parFiliere",        parFiliere);
                    statsAff.put("equilibree",        equilibree);
                    statsAff.put("nomFichier",        affectationFile.getName());
                    affExiste = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                statsAff.put("erreur", "Erreur lecture affectation : " + e.getMessage());
            }
        }

        // ── Statistiques PLANNING ────────────────────────────────────────
        Map<String, Object> statsPlan = new LinkedHashMap<>();
        boolean planExiste = false;

        if (planningFile != null) {
            try {
                List<PlanningSoutenance> planning = ExcelReaderPlanning.lirePlanning(planningFile.getAbsolutePath());

                int totalSoutenances = planning.size();

                // Soutenances par jour (trié)
                Map<String, Integer> parJour = new LinkedHashMap<>();
                for (PlanningSoutenance p : planning) {
                    if (p.getDate() != null && !p.getDate().isEmpty())
                        parJour.merge(p.getDate(), 1, Integer::sum);
                }

                // Soutenances par salle
                Map<String, Integer> parSalle = new LinkedHashMap<>();
                for (PlanningSoutenance p : planning) {
                    if (p.getSalle() != null && !p.getSalle().isEmpty())
                        parSalle.merge(p.getSalle(), 1, Integer::sum);
                }

                // Répartition par filière dans le planning
                Map<String, Integer> parFilierePlan = new LinkedHashMap<>();
                for (PlanningSoutenance p : planning) {
                    String f = (p.getFiliere() == null || p.getFiliere().isEmpty()) ? "N/A" : p.getFiliere();
                    parFilierePlan.merge(f, 1, Integer::sum);
                }

                // Charge des professeurs (toutes rôles : encadrant + jury)
                Map<String, Integer> chargeProf = new LinkedHashMap<>();
                for (PlanningSoutenance p : planning) {
                    if (p.getEncadrant() != null) chargeProf.merge(p.getEncadrant(), 1, Integer::sum);
                    if (p.getJury1()     != null) chargeProf.merge(p.getJury1(),     1, Integer::sum);
                    if (p.getJury2()     != null) chargeProf.merge(p.getJury2(),     1, Integer::sum);
                }
                // Trier par charge décroissante
                List<Map<String, Object>> topProfs = chargeProf.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(10)
                    .map(e -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("nom", e.getKey());
                        m.put("total", e.getValue());
                        return m;
                    })
                    .collect(Collectors.toList());

                // Nombre total encadrants distincts
                Set<String> distinctProfs = new HashSet<>();
                for (PlanningSoutenance p : planning) {
                    if (p.getEncadrant() != null) distinctProfs.add(p.getEncadrant());
                    if (p.getJury1()     != null) distinctProfs.add(p.getJury1());
                    if (p.getJury2()     != null) distinctProfs.add(p.getJury2());
                }

                // Conflits salles : même salle + même date + même heure
                Map<String, Integer> creneauSalle = new HashMap<>();
                int conflitsSalles = 0;
                for (PlanningSoutenance p : planning) {
                    if (p.getSalle() != null && p.getDate() != null && p.getHeure() != null) {
                        String key = p.getSalle() + "_" + p.getDate() + "_" + p.getHeure();
                        creneauSalle.merge(key, 1, Integer::sum);
                    }
                }
                for (int v : creneauSalle.values()) if (v > 1) conflitsSalles += (v - 1);

                // Conflits professeurs : même prof + même date + même heure
                Map<String, Integer> creneauProf = new HashMap<>();
                for (PlanningSoutenance p : planning) {
                    if (p.getDate() != null && p.getHeure() != null) {
                        for (String prof : new String[]{p.getEncadrant(), p.getJury1(), p.getJury2()}) {
                            if (prof != null && !prof.isEmpty()) {
                                String key = prof + "_" + p.getDate() + "_" + p.getHeure();
                                creneauProf.merge(key, 1, Integer::sum);
                            }
                        }
                    }
                }
                int conflitsProfs = 0;
                for (int v : creneauProf.values()) if (v > 1) conflitsProfs += (v - 1);

                // Jour le plus chargé
                String jourPlusCharge = parJour.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("–");
                int maxJour = parJour.values().stream().mapToInt(Integer::intValue).max().orElse(0);

                // Salle la plus utilisée
                String sallePlusUtilisee = parSalle.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("–");

                statsPlan.put("totalSoutenances",   totalSoutenances);
                statsPlan.put("totalJours",          parJour.size());
                statsPlan.put("totalSalles",         parSalle.size());
                statsPlan.put("totalProfesseurs",    distinctProfs.size());
                statsPlan.put("parJour",             parJour);
                statsPlan.put("parSalle",            parSalle);
                statsPlan.put("parFiliere",          parFilierePlan);
                statsPlan.put("topProfs",            topProfs);
                statsPlan.put("conflitsSalles",      conflitsSalles);
                statsPlan.put("conflitsProfs",       conflitsProfs);
                statsPlan.put("planningValide",      (conflitsSalles == 0 && conflitsProfs == 0));
                statsPlan.put("jourPlusCharge",      jourPlusCharge);
                statsPlan.put("maxParJour",          maxJour);
                statsPlan.put("sallePlusUtilisee",   sallePlusUtilisee);
                statsPlan.put("nomFichier",          planningFile.getName());
                planExiste = true;

            } catch (Exception e) {
                e.printStackTrace();
                statsPlan.put("erreur", "Erreur lecture planning : " + e.getMessage());
            }
        }

        // ── Envoi au JSP ────────────────────────────────────────────────
        request.setAttribute("statsAff",   statsAff);
        request.setAttribute("statsPlan",  statsPlan);
        request.setAttribute("affExiste",  affExiste);
        request.setAttribute("planExiste", planExiste);
        request.getRequestDispatcher("/WEB-INF/views/statistiques.jsp").forward(request, response);
    }
}