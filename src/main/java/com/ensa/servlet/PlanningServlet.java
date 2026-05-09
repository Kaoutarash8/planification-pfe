package com.ensa.servlet;

import com.ensa.dao.impl.ConfigurationExcelDao;
import com.ensa.dao.impl.EtudiantExcelDao;
import com.ensa.dao.impl.ProfesseurExcelDao;
import com.ensa.model.*;
import com.ensa.service.impl.SchedulerServiceImpl;
import com.ensa.util.PlanningExcelWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/planning")
@MultipartConfig
public class PlanningServlet extends HttpServlet {
    
    private EtudiantExcelDao etudiantDao;
    private ProfesseurExcelDao professeurDao;
    private ConfigurationExcelDao configurationDao;
    private SchedulerServiceImpl schedulerService;
    
    @Override
    public void init() throws ServletException {
        etudiantDao = new EtudiantExcelDao();
        professeurDao = new ProfesseurExcelDao();
        configurationDao = new ConfigurationExcelDao();
        schedulerService = new SchedulerServiceImpl();
        
        System.out.println("=== PLANNING SERVLET INITIALISEE ===");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/planning.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String uploadPath = (String) getServletContext().getAttribute("uploadPath");
            if (uploadPath == null) {
                String projectPath = System.getProperty("user.dir").replace('\\', '/');
                uploadPath = projectPath + "/src/main/webapp/uploads";
                getServletContext().setAttribute("uploadPath", uploadPath);
            }
            
            String annee = getAnneeUniversitaire();
            
            System.out.println("=== GENERATION PLANNING ===");
            
            // 1. Lire la configuration
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
            
            // 2. Lire les professeurs
            String professeursPath = uploadPath + "/professeurs.xlsx";
            List<Professeur> professeurs = new ArrayList<>();
            File profFile = new File(professeursPath);
            if (profFile.exists()) {
                professeurs = professeurDao.lireProfesseurs(professeursPath);
            }
            
            // 3. Lire les étudiants
            String etudiantsCompletPath = uploadPath + "/etudiants.xlsx";
            Map<String, Etudiant> etudiantParCNE = new HashMap<>();
            File etuFile = new File(etudiantsCompletPath);
            if (etuFile.exists()) {
                List<Etudiant> tousEtudiants = etudiantDao.lireEtudiants(etudiantsCompletPath);
                for (Etudiant e : tousEtudiants) {
                    if (e.getCne() != null && !e.getCne().isEmpty()) {
                        etudiantParCNE.put(e.getCne(), e);
                    }
                    String nomComplet = (e.getPrenom() + " " + e.getNom()).toLowerCase();
                    etudiantParCNE.put(nomComplet, e);
                }
            }
            
            // 4. Lire l'affectation
            String affectationPath = uploadPath + "/affectation_" + annee + ".xlsx";
            File affectationFile = new File(affectationPath);
            
            if (!affectationFile.exists()) {
                throw new Exception("Fichier affectation non trouvé: " + affectationPath);
            }
            
            Map<String, List<String>> affectationMap = etudiantDao.lireAffectation(affectationPath);
            
            // 5. Convertir en Map<Professeur, List<Etudiant>>
            Map<Professeur, List<Etudiant>> affectation = new LinkedHashMap<>();
            Map<String, Professeur> professeurParNom = new HashMap<>();
            
            for (Professeur p : professeurs) {
                String format1 = (p.getPrenom() + " " + p.getNom()).toLowerCase();
                String format2 = (p.getNom() + " " + p.getPrenom()).toLowerCase();
                professeurParNom.put(format1, p);
                professeurParNom.put(format2, p);
                professeurParNom.put(p.getNom().toLowerCase(), p);
            }
            
            for (Map.Entry<String, List<String>> entry : affectationMap.entrySet()) {
                String encadrantKey = entry.getKey();
                List<String> cneList = entry.getValue();
                
                Professeur encadrant = professeurParNom.get(encadrantKey.toLowerCase());
                if (encadrant == null) {
                    for (Professeur p : professeurs) {
                        if (encadrantKey.toLowerCase().contains(p.getNom().toLowerCase())) {
                            encadrant = p;
                            break;
                        }
                    }
                    if (encadrant == null) {
                        encadrant = new Professeur();
                        String[] parts = encadrantKey.split(" ");
                        if (parts.length >= 2) {
                            encadrant.setPrenom(parts[0]);
                            encadrant.setNom(parts[1]);
                        } else {
                            encadrant.setPrenom(encadrantKey);
                            encadrant.setNom(encadrantKey);
                        }
                        encadrant.setSpecialite("AUTRE");
                        professeurs.add(encadrant);
                        professeurParNom.put(encadrantKey.toLowerCase(), encadrant);
                    }
                }
                
                List<Etudiant> etudiants = new ArrayList<>();
                for (String identifiant : cneList) {
                    Etudiant e = etudiantParCNE.get(identifiant.trim());
                    if (e == null) {
                        String nomComplet = identifiant.trim().toLowerCase();
                        e = etudiantParCNE.get(nomComplet);
                    }
                    if (e == null) {
                        e = new Etudiant();
                        e.setCne(identifiant);
                        e.setPrenom(identifiant);
                        e.setNom("");
                        e.setFiliere("");
                    }
                    etudiants.add(e);
                }
                
                affectation.put(encadrant, etudiants);
            }
            
            // 6. Générer le planning
            ScheduleResult result = schedulerService.genererPlanning(
                affectation, professeurs, etudiantParCNE, config
            );
            
            // 7. Convertir en PlanningSoutenance
            List<PlanningSoutenance> planning = new ArrayList<>();
            
            if (result.getSoutenances() != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                
                for (ScheduledDefense defense : result.getSoutenances()) {
                    PlanningSoutenance ps = new PlanningSoutenance();
                    
                    if (defense.getEncadrant() != null) {
                        ps.setEncadrant(defense.getEncadrant().getPrenom() + " " + 
                                       defense.getEncadrant().getNom().toUpperCase());
                    }
                    
                    if (defense.getJurys() != null && defense.getJurys().size() >= 2) {
                        ps.setJury1(defense.getJurys().get(0).getPrenom() + " " + 
                                   defense.getJurys().get(0).getNom().toUpperCase());
                        ps.setJury2(defense.getJurys().get(1).getPrenom() + " " + 
                                   defense.getJurys().get(1).getNom().toUpperCase());
                    }
                    
                    TimeSlot slot = defense.getTimeSlot();
                    if (slot != null) {
                        ps.setDate(slot.getDate().format(dateFormatter));
                        ps.setHeure(slot.getHeureDebut().format(timeFormatter));
                        ps.setSalle(slot.getSalle());
                    }
                    
                    Etudiant e = defense.getEtudiant();
                    if (e != null) {
                        ps.setEtudiantPrenom(e.getPrenom());
                        ps.setEtudiantNom(e.getNom().toUpperCase());
                        ps.setEtudiantCNE(e.getCne());
                        ps.setFiliere(e.getFiliere() != null ? e.getFiliere() : "");
                    }
                    
                    planning.add(ps);
                }
                
                planning.sort(Comparator.comparing(PlanningSoutenance::getDate)
                        .thenComparing(PlanningSoutenance::getHeure));
            }
            
            // 8. Vérification SIMPLE du planning (sans classe externe)
            Map<String, Object> verification = new HashMap<>();
            verification.put("success", true);
            
            // Vérifier la présence d'au moins 2 INFO par soutenance
            int soutenancesSans2Info = 0;
            for (ScheduledDefense defense : result.getSoutenances()) {
                int infoCount = isInfo(defense.getEncadrant()) ? 1 : 0;
                for (Professeur jury : defense.getJurys()) {
                    if (isInfo(jury)) infoCount++;
                }
                if (infoCount < 2) soutenancesSans2Info++;
            }
            verification.put("soutenancesSans2Info", soutenancesSans2Info);
            if (soutenancesSans2Info > 0) verification.put("success", false);
            
            // Vérifier la charge des professeurs
            Map<String, Integer> chargeProfs = new HashMap<>();
            for (ScheduledDefense defense : result.getSoutenances()) {
                if (defense.getEncadrant() != null) {
                    chargeProfs.merge(defense.getEncadrant().getNom() + " " + defense.getEncadrant().getPrenom(), 1, Integer::sum);
                }
                for (Professeur jury : defense.getJurys()) {
                    if (jury != null) {
                        chargeProfs.merge(jury.getNom() + " " + jury.getPrenom(), 1, Integer::sum);
                    }
                }
            }
            
            int maxCharge = chargeProfs.values().stream().max(Integer::compare).orElse(0);
            int minCharge = chargeProfs.values().stream().min(Integer::compare).orElse(0);
            int ecartCharge = maxCharge - minCharge;
            verification.put("chargeMin", minCharge);
            verification.put("chargeMax", maxCharge);
            verification.put("chargeEcart", ecartCharge);
            
            // Vérifier les conflits de salle
            Map<String, Set<String>> salleParCreneau = new HashMap<>();
            int conflitsSalle = 0;
            for (ScheduledDefense defense : result.getSoutenances()) {
                String salle = defense.getTimeSlot().getSalle();
                String creneau = defense.getTimeSlot().getDate() + "_" + defense.getTimeSlot().getHeureDebut();
                if (salleParCreneau.containsKey(creneau) && salleParCreneau.get(creneau).contains(salle)) {
                    conflitsSalle++;
                }
                salleParCreneau.computeIfAbsent(creneau, k -> new HashSet<>()).add(salle);
            }
            verification.put("conflitsSalle", conflitsSalle);
            
            // Répartition par jour
            Map<String, Integer> soutenancesParJour = new LinkedHashMap<>();
            for (PlanningSoutenance ps : planning) {
                soutenancesParJour.merge(ps.getDate(), 1, Integer::sum);
            }
            
            // Statistiques
            Set<String> joursUtilises = new HashSet<>();
            Set<String> sallesUtilisees = new HashSet<>();
            for (PlanningSoutenance ps : planning) {
                joursUtilises.add(ps.getDate());
                sallesUtilisees.add(ps.getSalle());
            }
            
            // Stocker tout dans la requête
            request.setAttribute("totalSoutenances", planning.size());
            request.setAttribute("nbJours", joursUtilises.size());
            request.setAttribute("nbSalles", sallesUtilisees.size());
            request.setAttribute("fileName", PlanningExcelWriter.sauvegarderPlanning(planning, uploadPath, annee));
            request.setAttribute("success", result.isSuccess());
            request.setAttribute("soutenancesParJour", soutenancesParJour);
            request.setAttribute("executionTime", result.getExecutionTimeMs());
            
            // Résultats de vérification
            request.setAttribute("verification", verification);
            request.setAttribute("planning", planning);
            
            // Messages
            if (!(boolean) verification.get("success")) {
                request.setAttribute("error", "Planning invalide: " + soutenancesSans2Info + " soutenance(s) sans 2 INFO");
            } else if (ecartCharge > 4) {
                request.setAttribute("warning", "Planning OK mais déséquilibre de charge: écart de " + ecartCharge);
            } else {
                request.setAttribute("successMessage", "Planning généré et validé avec succès!");
            }
            
            request.getRequestDispatcher("/WEB-INF/views/planningResultat.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/planning.jsp").forward(request, response);
        }
    }
    
    private boolean isInfo(Professeur p) {
        if (p == null || p.getSpecialite() == null) return false;
        return p.getSpecialite().toLowerCase().contains("info") || 
               p.getSpecialite().toLowerCase().contains("informatique");
    }
    
    private String getAnneeUniversitaire() {
        Calendar cal = Calendar.getInstance();
        int annee = cal.get(Calendar.YEAR);
        int anneePrecedente = annee - 1;
        return anneePrecedente + "_" + annee;
    }
}