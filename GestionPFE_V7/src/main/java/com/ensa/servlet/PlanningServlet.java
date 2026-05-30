package com.ensa.servlet;

import com.ensa.dao.impl.*;
import com.ensa.model.*;
import com.ensa.service.impl.PlanningServiceImpl;
import com.ensa.util.FileUtil;
import com.ensa.util.PlanningExcelWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/planning")
@MultipartConfig
public class PlanningServlet extends HttpServlet {
    
    private EtudiantExcelDao etudiantDao;
    private ProfesseurExcelDao professeurDao;
    private ConfigurationExcelDao configurationDao;
    private PlanningServiceImpl planningService;
    
    @Override
    public void init() throws ServletException {
        FileUtil.init(getServletContext());
        
        etudiantDao = new EtudiantExcelDao();
        professeurDao = new ProfesseurExcelDao();
        configurationDao = new ConfigurationExcelDao();
        planningService = new PlanningServiceImpl();
        
        System.out.println("=== PLANNING SERVLET INITIALISEE ===");
        System.out.println("Upload path: " + FileUtil.getUploadPath());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/planning.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<String> erreurs = new ArrayList<>();
        
        try {
            System.out.println("\n=== GENERATION PLANNING ===");
            
            String uploadPath = FileUtil.getUploadPath();
            String annee = FileUtil.getAnneeUniversitaire();
            String affectationPath = FileUtil.getCheminFichierAffectation();
            
            File affectationFile = new File(affectationPath);
            if (!affectationFile.exists()) {
                String errorMsg = "Impossible de generer le planning. Veuillez d'abord generer l'affectation pour l'annee " + annee.replace("_", "/");
                System.out.println("ERREUR: " + errorMsg);
                request.setAttribute("error", errorMsg);
                request.getRequestDispatcher("/WEB-INF/views/planning.jsp").forward(request, response);
                return;
            }
            
            System.out.println("Affectation trouvee: " + affectationPath);
            
            Part configPart = request.getPart("fichierConfiguration");
            if (configPart == null || configPart.getSize() == 0) {
                throw new Exception("Le fichier de configuration est requis");
            }
            
            String configPath = uploadPath + "/config_temp.xlsx";
            try (InputStream is = configPart.getInputStream();
                 FileOutputStream fos = new FileOutputStream(configPath)) {
                is.transferTo(fos);
            }
            
            ConfigurationSoutenance config = configurationDao.lireConfiguration(configPath);
            new File(configPath).delete();
            
            String professeursPath = uploadPath + "/professeurs.xlsx";
            List<Professeur> professeurs = new ArrayList<>();
            File profFile = new File(professeursPath);
            if (profFile.exists()) {
                professeurs = professeurDao.lireProfesseurs(professeursPath);
            }
            System.out.println("Professeurs: " + professeurs.size());
            
            String etudiantsCompletPath = uploadPath + "/etudiants.xlsx";
            Map<String, String> filiereParNomComplet = new HashMap<>();
            List<Etudiant> tousEtudiants = new ArrayList<>();
            File etuFile = new File(etudiantsCompletPath);
            if (etuFile.exists()) {
                tousEtudiants = etudiantDao.lireEtudiantsComplet(etudiantsCompletPath);
                for (Etudiant e : tousEtudiants) {
                    String nomComplet = e.getPrenom() + " " + e.getNom().toUpperCase();
                    filiereParNomComplet.put(nomComplet, e.getFiliere());
                }
            }
            System.out.println("Filieres chargees: " + filiereParNomComplet.size());
            
            // ============================================================
            // CALCUL AUTOMATIQUE DU NOMBRE DE JOURS SI NON DEFINI
            // ============================================================
            if (config.getNbJours() <= 0) {
                Map<String, List<String>> affectationMapTemp = etudiantDao.lireAffectation(affectationPath);
                int totalEtudiantsTemp = 0;
                for (List<String> etudiants : affectationMapTemp.values()) {
                    totalEtudiantsTemp += etudiants.size();
                }
                
                int capaciteParJour = config.calculerCapaciteParJour();
                int joursNecessaires = (int) Math.ceil((double) totalEtudiantsTemp / capaciteParJour);
                joursNecessaires = (int) Math.ceil(joursNecessaires * 1.1);
                joursNecessaires = Math.max(1, Math.min(10, joursNecessaires));
                config.setNbJours(joursNecessaires);
                
                System.out.println("\n=== CALCUL AUTOMATIQUE DU NOMBRE DE JOURS ===");
                System.out.println("  Total etudiants: " + totalEtudiantsTemp);
                System.out.println("  Capacite par jour: " + capaciteParJour);
                System.out.println("  Jours calcules: " + joursNecessaires);
            }
            
            Map<String, List<String>> affectationMap = etudiantDao.lireAffectation(affectationPath);
            System.out.println("Affectation lue: " + affectationMap.size() + " encadrants");
            System.out.println("Nombre de jours de soutenance: " + config.getNbJours());
            
            // ============================================================
            // VERIFICATION DE LA CAPACITE TOTALE
            // ============================================================
            int totalEtudiants = 0;
            for (List<String> etudiants : affectationMap.values()) {
                totalEtudiants += etudiants.size();
            }
            
            int nbHeuresParJour = config.getNbHeuresParJour();
            int nbSalles = config.getSalles().size();
            int capaciteTotale = config.getNbJours() * nbHeuresParJour * nbSalles;
            
            System.out.println("=== VERIFICATION CAPACITE ===");
            System.out.println("  Total etudiants: " + totalEtudiants);
            System.out.println("  Heures par jour: " + nbHeuresParJour);
            System.out.println("  Salles: " + nbSalles);
            System.out.println("  Jours: " + config.getNbJours());
            System.out.println("  Capacite totale: " + capaciteTotale);
            
            if (totalEtudiants > capaciteTotale) {
                erreurs.add("Capacite insuffisante: " + totalEtudiants + " etudiants pour " + capaciteTotale + " creneaux disponibles");
                erreurs.add("  - Heures par jour: " + nbHeuresParJour);
                erreurs.add("  - Salles disponibles: " + nbSalles);
                erreurs.add("  - Jours prevus: " + config.getNbJours());
                erreurs.add("Augmentez le nombre de jours ou de salles dans la configuration");
            }
            
            // ============================================================
            // CONSTRUCTION DE L'AFFECTATION
            // ============================================================
            Map<Professeur, List<Etudiant>> affectation = new LinkedHashMap<>();
            Map<String, Professeur> professeurParNom = new HashMap<>();
            
            for (Professeur p : professeurs) {
                String key = p.getNom().toUpperCase() + " " + p.getPrenom();
                professeurParNom.put(key, p);
            }
            
            for (Map.Entry<String, List<String>> entry : affectationMap.entrySet()) {
                String encadrantKey = entry.getKey();
                List<String> nomsEtudiants = entry.getValue();
                
                Professeur encadrant = professeurParNom.get(encadrantKey);
                if (encadrant == null) {
                    String[] parts = encadrantKey.split(" ");
                    String nom = parts[0];
                    StringBuilder prenom = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        if (i > 1) prenom.append(" ");
                        prenom.append(parts[i]);
                    }
                    encadrant = new Professeur();
                    encadrant.setNom(nom);
                    encadrant.setPrenom(prenom.toString());
                    encadrant.setSpecialite("");
                    professeurs.add(encadrant);
                }
                
                List<Etudiant> etudiants = new ArrayList<>();
                for (String nomEtudiant : nomsEtudiants) {
                    Etudiant e = new Etudiant();
                    String[] parts = nomEtudiant.split(" ");
                    if (parts.length >= 1) {
                        e.setPrenom(parts[0]);
                        if (parts.length > 1) {
                            StringBuilder nom = new StringBuilder();
                            for (int i = 1; i < parts.length; i++) {
                                if (i > 1) nom.append(" ");
                                nom.append(parts[i]);
                            }
                            e.setNom(nom.toString());
                        }
                    }
                    e.setFiliere(filiereParNomComplet.getOrDefault(nomEtudiant, ""));
                    etudiants.add(e);
                }
                
                affectation.put(encadrant, etudiants);
            }
            
            System.out.println("Affectation convertie: " + affectation.size() + " encadrants");
            
            // ============================================================
            // GENERATION DU PLANNING
            // ============================================================
            List<PlanningSoutenance> planning = planningService.genererPlanning(
                affectation, professeurs, new HashMap<>(), config
            );
            
            System.out.println("Planning genere: " + planning.size() + " soutenances");
            
            // ============================================================
            // VERIFICATION: Si toutes les soutenances n'ont pas ete placees
            // ============================================================
            if (planning.size() < totalEtudiants) {
                int nonPlaces = totalEtudiants - planning.size();
                erreurs.add("Impossible de placer " + nonPlaces + " etudiant(s) sur " + totalEtudiants);
                erreurs.add("Causes possibles: contraintes trop strictes, pause trop longue, ou manque de creneaux");
                
                if (config.getPauseMaxSansSoutenanceHeures() > 1) {
                    erreurs.add("La pause de " + config.getPauseMaxSansSoutenanceHeures() + "h entre soutenances est trop restrictive");
                }
                if (config.getMaxSoutenancesParJourParProf() < 4) {
                    erreurs.add("La limite de " + config.getMaxSoutenancesParJourParProf() + " soutenances par jour par professeur est trop basse");
                }
                if (config.getMinInfoParJury() > 1) {
                    erreurs.add("L'exigence de " + config.getMinInfoParJury() + " informaticiens par jury est difficile a satisfaire");
                }
                if (nbSalles < 3) {
                    erreurs.add("Nombre de salles insuffisant: " + nbSalles + " salle(s)");
                }
            }
            
            // ============================================================
            // AFFICHAGE DES RESULTATS OU DES ERREURS
            // ============================================================
            if (!erreurs.isEmpty()) {
                request.setAttribute("erreurs", erreurs);
                request.setAttribute("totalEtudiants", totalEtudiants);
                request.setAttribute("soutenancesPlacees", planning.size());
                request.setAttribute("afficherErreur", true);
                request.getRequestDispatcher("/WEB-INF/views/planningResultat.jsp").forward(request, response);
                return;
            }
            
            String fileName = PlanningExcelWriter.sauvegarderPlanning(planning, uploadPath, annee);
            
            Set<String> joursUtilises = new HashSet<>();
            Set<String> sallesUtilisees = new HashSet<>();
            for (PlanningSoutenance ps : planning) {
                joursUtilises.add(ps.getDate());
                sallesUtilisees.add(ps.getSalle());
            }
            
            request.setAttribute("totalSoutenances", planning.size());
            request.setAttribute("nbJours", joursUtilises.size());
            request.setAttribute("nbSalles", sallesUtilisees.size());
            request.setAttribute("fileName", fileName);
            request.setAttribute("configJours", config.getNbJours());
            request.setAttribute("afficherErreur", false);
            
            request.getRequestDispatcher("/WEB-INF/views/planningResultat.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            List<String> erreursException = new ArrayList<>();
            erreursException.add("Erreur lors de la generation du planning: " + e.getMessage());
            request.setAttribute("erreurs", erreursException);
            request.setAttribute("afficherErreur", true);
            request.getRequestDispatcher("/WEB-INF/views/planningResultat.jsp").forward(request, response);
        }
    }
}