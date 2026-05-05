package com.ensa.service;

import com.ensa.model.ConfigurationSoutenance;
import com.ensa.model.Etudiant;
import com.ensa.model.PlanningSoutenance;
import com.ensa.model.Professeur;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class PlanningService {
    
    private List<Etudiant> etudiantsAvecAffectation;
    private List<Professeur> professeurs;
    private ConfigurationSoutenance config;
    
    private List<PlanningSoutenance> planningGlobal = new ArrayList<>();
    
    private Map<String, Set<String>> professeursOccupees = new HashMap<>();
    private Set<String> sallesOccupees = new HashSet<>();
    private Map<String, Integer> chargeProfesseurs = new HashMap<>();
    private Map<String, List<DetailSoutenance>> detailsParProfesseur = new HashMap<>();
    
    private List<Professeur> professeursInfo = new ArrayList<>();
    private List<Professeur> professeursNonInfo = new ArrayList<>();
    
    private Map<String, Short> couleurParProfesseur = new HashMap<>();
    private Map<String, Short> couleurParFiliere = new HashMap<>();
    
    private Random random = new Random();
    
    private static final int CHARGE_MAX_ECART_INFO = 2;
    private static final int ECART_MAX_INFO_NONINFO = 6;
    
    // Palette de couleurs étendue pour les professeurs (beaucoup de couleurs)
    private final short[] COULEURS_PROF = {
        IndexedColors.LIGHT_GREEN.getIndex(),      // Vert clair
        IndexedColors.LIGHT_BLUE.getIndex(),       // Bleu clair
        IndexedColors.LIGHT_YELLOW.getIndex(),     // Jaune clair
        IndexedColors.LIGHT_ORANGE.getIndex(),     // Orange clair
        IndexedColors.LIGHT_TURQUOISE.getIndex(),  // Turquoise clair
        IndexedColors.ROSE.getIndex(),             // Rose
        IndexedColors.LAVENDER.getIndex(),         // Lavande
        IndexedColors.TAN.getIndex(),              // Tan
        IndexedColors.PALE_BLUE.getIndex(),        // Bleu pâle
        IndexedColors.CORAL.getIndex(),            // Corail
        IndexedColors.SEA_GREEN.getIndex(),        // Vert mer
        IndexedColors.VIOLET.getIndex(),           // Violet
        IndexedColors.GOLD.getIndex(),             // Or
        IndexedColors.TEAL.getIndex(),             // Teal
        IndexedColors.LIME.getIndex(),             // Vert lime
        IndexedColors.LEMON_CHIFFON.getIndex(),    // Jaune citron
        IndexedColors.PLUM.getIndex(),             // Prune
        IndexedColors.ORCHID.getIndex(),           // Orchidée
        IndexedColors.SKY_BLUE.getIndex(),         // Bleu ciel
        IndexedColors.MAROON.getIndex(),           // Marron
        IndexedColors.DARK_RED.getIndex(),         // Rouge foncé
        IndexedColors.DARK_BLUE.getIndex(),        // Bleu foncé
        IndexedColors.DARK_GREEN.getIndex()        // Vert foncé
    };
    
    private final short[] COULEURS_FILIERE = {
        IndexedColors.LIGHT_GREEN.getIndex(),
        IndexedColors.LIGHT_BLUE.getIndex(),
        IndexedColors.LIGHT_YELLOW.getIndex(),
        IndexedColors.LIGHT_ORANGE.getIndex(),
        IndexedColors.LIGHT_TURQUOISE.getIndex(),
        IndexedColors.ROSE.getIndex(),
        IndexedColors.LAVENDER.getIndex(),
        IndexedColors.TAN.getIndex(),
        IndexedColors.CORAL.getIndex()
    };
    
    public PlanningService(List<Etudiant> etudiants, List<Professeur> professeurs, ConfigurationSoutenance config) {
        this.etudiantsAvecAffectation = etudiants;
        this.professeurs = professeurs;
        this.config = config;
        
        for (Professeur p : professeurs) {
            String specialite = p.getSpecialite();
            if (specialite != null && specialite.toLowerCase().contains("info")) {
                professeursInfo.add(p);
            } else {
                professeursNonInfo.add(p);
            }
        }
        
        // Assigner une couleur unique à chaque professeur
        int profIndex = 0;
        for (Professeur p : professeurs) {
            String nom = p.getNomComplet();
            chargeProfesseurs.put(nom, 0);
            professeursOccupees.put(nom, new HashSet<>());
            detailsParProfesseur.put(nom, new ArrayList<>());
            couleurParProfesseur.put(nom, COULEURS_PROF[profIndex % COULEURS_PROF.length]);
            profIndex++;
        }
        
        // Couleurs par filière
        Set<String> filieres = new HashSet<>();
        for (Etudiant e : etudiantsAvecAffectation) {
            String filiere = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "INFO" : e.getFiliere();
            filieres.add(filiere);
        }
        int filiereIndex = 0;
        for (String filiere : filieres) {
            couleurParFiliere.put(filiere, COULEURS_FILIERE[filiereIndex % COULEURS_FILIERE.length]);
            filiereIndex++;
        }
        
        System.out.println("=== INITIALISATION ===");
        System.out.println("Professeurs INFO: " + professeursInfo.size());
        System.out.println("Professeurs NON-INFO: " + professeursNonInfo.size());
    }
    
    public List<PlanningSoutenance> genererPlanning() throws Exception {
        planningGlobal.clear();
        
        List<String> jours = config.getJours();
        List<String> salles = config.getSalles();
        List<String> creneaux = getCreneauxHoraires();
        
        Collections.sort(jours);
        
        int totalEtudiants = etudiantsAvecAffectation.size();
        System.out.println("\n=== GÉNÉRATION DU PLANNING ===");
        System.out.println("Total étudiants: " + totalEtudiants);
        
        List<Etudiant> etudiantsMelanges = new ArrayList<>(etudiantsAvecAffectation);
        Collections.shuffle(etudiantsMelanges);
        
        List<Creneau> tousLesCreneaux = new ArrayList<>();
        for (String jour : jours) {
            for (String heure : creneaux) {
                for (String salle : salles) {
                    tousLesCreneaux.add(new Creneau(jour, heure, salle));
                }
            }
        }
        Collections.shuffle(tousLesCreneaux);
        
        int places = 0;
        
        for (Etudiant etudiant : etudiantsMelanges) {
            boolean place = false;
            String encadrant = etudiant.getEncadrantComplet();
            boolean encadrantEstInfo = estProfesseurInfo(encadrant);
            
            for (Creneau creneau : tousLesCreneaux) {
                if (place) break;
                
                String salleKey = creneau.jour + "_" + creneau.heure + "_" + creneau.salle;
                if (sallesOccupees.contains(salleKey)) continue;
                
                String key = creneau.jour + "_" + creneau.heure;
                
                if (professeursOccupees.get(encadrant).contains(key)) continue;
                if (!verifierPause(encadrant, creneau.jour, creneau.heure)) continue;
                
                List<String> jurys = selectionnerJurysAvecEquilibre(encadrant, encadrantEstInfo, key, creneau.jour, creneau.heure);
                
                if (jurys.size() < 2) continue;
                if (jurys.get(0).equals(jurys.get(1))) continue;
                
                boolean jurysOk = true;
                for (String jury : jurys) {
                    if (!verifierPause(jury, creneau.jour, creneau.heure)) {
                        jurysOk = false;
                        break;
                    }
                }
                if (!jurysOk) continue;
                
                int nbInfo = (encadrantEstInfo ? 1 : 0);
                if (estProfesseurInfo(jurys.get(0))) nbInfo++;
                if (estProfesseurInfo(jurys.get(1))) nbInfo++;
                if (nbInfo < 2) continue;
                
                PlanningSoutenance ps = new PlanningSoutenance();
                ps.setDate(creneau.jour);
                ps.setHeure(creneau.heure);
                ps.setSalle(creneau.salle);
                ps.setEtudiantCNE(etudiant.getCne());
                ps.setEtudiantNom(etudiant.getNom());
                ps.setEtudiantPrenom(etudiant.getPrenom());
                ps.setFiliere(etudiant.getFiliere());
                ps.setEncadrant(encadrant);
                ps.setJury1(jurys.get(0));
                ps.setJury2(jurys.get(1));
                
                planningGlobal.add(ps);
                
                sallesOccupees.add(salleKey);
                professeursOccupees.get(encadrant).add(key);
                professeursOccupees.get(jurys.get(0)).add(key);
                professeursOccupees.get(jurys.get(1)).add(key);
                
                chargeProfesseurs.put(encadrant, chargeProfesseurs.get(encadrant) + 1);
                chargeProfesseurs.put(jurys.get(0), chargeProfesseurs.get(jurys.get(0)) + 1);
                chargeProfesseurs.put(jurys.get(1), chargeProfesseurs.get(jurys.get(1)) + 1);
                
                detailsParProfesseur.get(encadrant).add(new DetailSoutenance(creneau.jour, creneau.heure, creneau.salle, etudiant.getPrenom(), etudiant.getNom()));
                detailsParProfesseur.get(jurys.get(0)).add(new DetailSoutenance(creneau.jour, creneau.heure, creneau.salle, etudiant.getPrenom(), etudiant.getNom()));
                detailsParProfesseur.get(jurys.get(1)).add(new DetailSoutenance(creneau.jour, creneau.heure, creneau.salle, etudiant.getPrenom(), etudiant.getNom()));
                
                place = true;
                places++;
                break;
            }
            
            if (!place) {
                System.err.println("⚠️ Impossible de placer: " + etudiant.getPrenom() + " " + etudiant.getNom());
            }
        }
        
        planningGlobal.sort((p1, p2) -> {
            int cmp = p1.getDate().compareTo(p2.getDate());
            if (cmp != 0) return cmp;
            return p1.getHeure().compareTo(p2.getHeure());
        });
        
        afficherStatistiques();
        validerToutesContraintes();
        
        System.out.println("\n✅ Planning généré: " + planningGlobal.size() + "/" + totalEtudiants + " soutenances");
        
        return planningGlobal;
    }
    
    private List<String> getCreneauxHoraires() {
        List<String> creneaux = new ArrayList<>();
        creneaux.add("09:00");
        creneaux.add("10:00");
        creneaux.add("11:00");
       
        creneaux.add("14:00");
        creneaux.add("15:00");
        creneaux.add("16:00");
        creneaux.add("17:00");
        
        return creneaux;
    }
    
    private List<String> selectionnerJurysAvecEquilibre(String encadrant, boolean encadrantEstInfo, 
                                                        String key, String jour, String heure) {
        List<String> jurys = new ArrayList<>();
        
        List<Professeur> disponiblesInfo = new ArrayList<>();
        List<Professeur> disponiblesNonInfo = new ArrayList<>();
        
        for (Professeur p : professeurs) {
            String nom = p.getNomComplet();
            if (!nom.equals(encadrant) && !professeursOccupees.get(nom).contains(key)) {
                if (estProfesseurInfo(nom)) {
                    disponiblesInfo.add(p);
                } else {
                    disponiblesNonInfo.add(p);
                }
            }
        }
        
        disponiblesInfo.sort((p1, p2) -> 
            Integer.compare(chargeProfesseurs.get(p1.getNomComplet()), chargeProfesseurs.get(p2.getNomComplet())));
        disponiblesNonInfo.sort((p1, p2) -> 
            Integer.compare(chargeProfesseurs.get(p1.getNomComplet()), chargeProfesseurs.get(p2.getNomComplet())));
        
        double moyenneInfo = calculerMoyenneCharge(professeursInfo);
        double moyenneNonInfo = calculerMoyenneCharge(professeursNonInfo);
        double ecartActuel = Math.abs(moyenneInfo - moyenneNonInfo);
        
        if (encadrantEstInfo) {
            if (disponiblesInfo.isEmpty()) {
                if (disponiblesNonInfo.size() >= 2) {
                    jurys.add(disponiblesNonInfo.get(0).getNomComplet());
                    jurys.add(disponiblesNonInfo.get(1).getNomComplet());
                }
                return jurys;
            }
            
            jurys.add(disponiblesInfo.get(0).getNomComplet());
            
            if (disponiblesInfo.size() >= 2 && ecartActuel < ECART_MAX_INFO_NONINFO - 2) {
                jurys.add(disponiblesInfo.get(1).getNomComplet());
            } else if (!disponiblesNonInfo.isEmpty() && ecartActuel > ECART_MAX_INFO_NONINFO - 3) {
                jurys.add(disponiblesNonInfo.get(0).getNomComplet());
            } else if (disponiblesInfo.size() >= 2) {
                jurys.add(disponiblesInfo.get(1).getNomComplet());
            } else if (!disponiblesNonInfo.isEmpty()) {
                jurys.add(disponiblesNonInfo.get(0).getNomComplet());
            } else {
                return new ArrayList<>();
            }
        } else {
            if (disponiblesInfo.size() < 2) {
                if (disponiblesInfo.size() == 1 && disponiblesNonInfo.size() >= 1) {
                    jurys.add(disponiblesInfo.get(0).getNomComplet());
                    jurys.add(disponiblesNonInfo.get(0).getNomComplet());
                } else if (disponiblesNonInfo.size() >= 2) {
                    jurys.add(disponiblesNonInfo.get(0).getNomComplet());
                    jurys.add(disponiblesNonInfo.get(1).getNomComplet());
                }
                return jurys;
            }
            
            jurys.add(disponiblesInfo.get(0).getNomComplet());
            jurys.add(disponiblesInfo.get(1).getNomComplet());
        }
        
        return jurys;
    }
    
    private double calculerMoyenneCharge(List<Professeur> profs) {
        if (profs.isEmpty()) return 0;
        int total = 0;
        for (Professeur p : profs) {
            total += chargeProfesseurs.get(p.getNomComplet());
        }
        return (double) total / profs.size();
    }
    
    private boolean verifierPause(String professeur, String jour, String heure) {
        Set<String> creneaux = professeursOccupees.get(professeur);
        if (creneaux == null || creneaux.isEmpty()) return true;
        
        int heureActuelle = Integer.parseInt(heure.split(":")[0]);
        
        for (String existing : creneaux) {
            String[] parts = existing.split("_");
            if (!parts[0].equals(jour)) continue;
            
            int heureExist = Integer.parseInt(parts[1].split(":")[0]);
            int diff = Math.abs(heureActuelle - heureExist);
            
            if (diff < 1) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean estProfesseurInfo(String nomComplet) {
        for (Professeur p : professeursInfo) {
            if (p.getNomComplet().equals(nomComplet)) {
                return true;
            }
        }
        return false;
    }
    
    private void validerToutesContraintes() {
        System.out.println("\n=== VALIDATION DES CONTRAINTES ===");
        
        boolean toutOk = true;
        
        int nbSoutenancesSans2Info = 0;
        for (PlanningSoutenance ps : planningGlobal) {
            int nbInfo = 0;
            if (estProfesseurInfo(ps.getEncadrant())) nbInfo++;
            if (estProfesseurInfo(ps.getJury1())) nbInfo++;
            if (estProfesseurInfo(ps.getJury2())) nbInfo++;
            if (nbInfo < 2) {
                nbSoutenancesSans2Info++;
            }
        }
        
        if (nbSoutenancesSans2Info == 0) {
            System.out.println("  ✅ Toutes les soutenances ont au moins 2 INFO");
        } else {
            System.out.println("  ⚠️ " + nbSoutenancesSans2Info + " soutenance(s) avec moins de 2 INFO");
            toutOk = false;
        }
        
        int totalInfo = 0;
        int totalNonInfo = 0;
        
        for (Professeur p : professeursInfo) {
            totalInfo += chargeProfesseurs.get(p.getNomComplet());
        }
        for (Professeur p : professeursNonInfo) {
            totalNonInfo += chargeProfesseurs.get(p.getNomComplet());
        }
        
        double moyenneInfo = professeursInfo.isEmpty() ? 0 : (double) totalInfo / professeursInfo.size();
        double moyenneNonInfo = professeursNonInfo.isEmpty() ? 0 : (double) totalNonInfo / professeursNonInfo.size();
        double ecart = Math.abs(moyenneInfo - moyenneNonInfo);
        
        System.out.println("\n--- ÉCART INFO / NON-INFO ---");
        System.out.println("  Moyenne INFO: " + String.format("%.2f", moyenneInfo));
        System.out.println("  Moyenne NON-INFO: " + String.format("%.2f", moyenneNonInfo));
        System.out.println("  Écart: " + String.format("%.2f", ecart));
        
        if (ecart <= ECART_MAX_INFO_NONINFO) {
            System.out.println("  ✅ Écart acceptable (≤ " + ECART_MAX_INFO_NONINFO + ")");
        } else {
            System.out.println("  ⚠️ Écart trop grand (> " + ECART_MAX_INFO_NONINFO + ")");
            toutOk = false;
        }
        
        if (!professeursInfo.isEmpty()) {
            int minInfo = Integer.MAX_VALUE, maxInfo = Integer.MIN_VALUE;
            for (Professeur p : professeursInfo) {
                int charge = chargeProfesseurs.get(p.getNomComplet());
                minInfo = Math.min(minInfo, charge);
                maxInfo = Math.max(maxInfo, charge);
            }
            int ecartInfo = maxInfo - minInfo;
            System.out.println("\n--- ÉCART ENTRE PROFESSEURS INFO ---");
            System.out.println("  Min: " + minInfo + ", Max: " + maxInfo + ", Écart: " + ecartInfo);
            if (ecartInfo <= CHARGE_MAX_ECART_INFO) {
                System.out.println("  ✅ Écart acceptable (≤ " + CHARGE_MAX_ECART_INFO + ")");
            } else {
                System.out.println("  ⚠️ Écart trop grand (> " + CHARGE_MAX_ECART_INFO + ")");
                toutOk = false;
            }
        }
        
        Set<String> sallesVerif = new HashSet<>();
        Map<String, Set<String>> profsVerif = new HashMap<>();
        for (Professeur p : professeurs) {
            profsVerif.put(p.getNomComplet(), new HashSet<>());
        }
        
        int conflitsSalle = 0;
        int conflitsProf = 0;
        
        for (PlanningSoutenance ps : planningGlobal) {
            String salleKey = ps.getDate() + "_" + ps.getHeure() + "_" + ps.getSalle();
            if (sallesVerif.contains(salleKey)) conflitsSalle++;
            sallesVerif.add(salleKey);
            
            String key = ps.getDate() + "_" + ps.getHeure();
            for (String prof : Arrays.asList(ps.getEncadrant(), ps.getJury1(), ps.getJury2())) {
                if (profsVerif.get(prof).contains(key)) conflitsProf++;
                profsVerif.get(prof).add(key);
            }
        }
        
        System.out.println("\n--- CONFLITS ---");
        System.out.println("  Conflits de salle: " + conflitsSalle);
        System.out.println("  Conflits de professeur: " + conflitsProf);
        
        if (conflitsSalle == 0 && conflitsProf == 0) {
            System.out.println("  ✅ Aucun conflit détecté");
        } else {
            toutOk = false;
        }
        
        if (toutOk) {
            System.out.println("\n✅ TOUTES LES CONTRAINTES SONT RESPECTÉES !");
        } else {
            System.out.println("\n⚠️ Certaines contraintes ne sont pas respectées");
        }
    }
    
    private void afficherStatistiques() {
        System.out.println("\n=== STATISTIQUES DES CHARGES ===");
        
        System.out.println("\n--- Professeurs INFO ---");
        int minInfo = Integer.MAX_VALUE, maxInfo = Integer.MIN_VALUE;
        for (Professeur p : professeursInfo) {
            int charge = chargeProfesseurs.get(p.getNomComplet());
            minInfo = Math.min(minInfo, charge);
            maxInfo = Math.max(maxInfo, charge);
            System.out.println("  📘 " + p.getNomComplet() + ": " + charge);
        }
        System.out.println("  Écart INFO: " + (maxInfo - minInfo));
        
        System.out.println("\n--- Professeurs NON-INFO ---");
        for (Professeur p : professeursNonInfo) {
            int charge = chargeProfesseurs.get(p.getNomComplet());
            System.out.println("  📗 " + p.getNomComplet() + " (" + p.getSpecialite() + "): " + charge);
        }
    }
    
    // ==================== EXPORT EXCEL AVEC COULEURS ====================
    
    public void sauvegarderPlanning(String outputPath) throws Exception {
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet planningSheet = workbook.createSheet("Planning_Soutenances");
            createPlanningSheet(planningSheet, workbook);
            
            Sheet recapSheet = workbook.createSheet("Recap_par_Professeur");
            createRecapSheet(recapSheet, workbook);
            
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
        
        System.out.println("Planning sauvegardé: " + outputPath);
    }
    
    private void createPlanningSheet(Sheet sheet, Workbook workbook) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle separatorStyle = createSeparatorStyle(workbook);
        
        // En-têtes du document
        createMergedCell(sheet, 0, 0, 10, "ECOLE NATIONALE DES SCIENCES APPLIQUÉES - AL HOCEIMA", titleStyle);
        createMergedCell(sheet, 1, 0, 10, "Département Mathématiques et Informatique", titleStyle);
        createMergedCell(sheet, 2, 0, 10, "PLANNING DES SOUTENANCES DES PROJETS DE FIN D'ÉTUDE", titleStyle);
        createMergedCell(sheet, 3, 0, 10, "Première Session - Année Universitaire 2024/2025", titleStyle);
        
        String dateDebut = config.getJours().get(0);
        String dateFin = config.getJours().get(config.getJours().size() - 1);
        createMergedCell(sheet, 4, 0, 10, "Période : " + dateDebut + " → " + dateFin, titleStyle);
        
        sheet.createRow(5);
        
        // En-têtes du tableau
        Row headerRow = sheet.createRow(6);
        String[] headers = {"N°", "CNE", "Date", "Heure", "Salle", "Encadrant", "Jury 1", "Jury 2", "Nom", "Prenom", "Filière"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 7;
        int numero = 1;
        String currentDate = "";
        
        for (PlanningSoutenance ps : planningGlobal) {
            // Ligne de séparation entre les jours (bleu foncé)
            if (!ps.getDate().equals(currentDate) && !currentDate.isEmpty()) {
                Row sepRow = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = sepRow.createCell(i);
                    cell.setCellValue("");
                    cell.setCellStyle(separatorStyle);
                }
            }
            currentDate = ps.getDate();
            
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(numero++);
            row.createCell(1).setCellValue(ps.getEtudiantCNE());
            row.createCell(2).setCellValue(ps.getDate());
            row.createCell(3).setCellValue(ps.getHeure());
            row.createCell(4).setCellValue(ps.getSalle());
            
            // Encadrant - couleur spécifique au professeur
            createColoredCell(row, 5, ps.getEncadrant(), workbook, true);
            // Jury 1 - couleur spécifique au professeur
            createColoredCell(row, 6, ps.getJury1(), workbook, true);
            // Jury 2 - couleur spécifique au professeur
            createColoredCell(row, 7, ps.getJury2(), workbook, true);
            
            row.createCell(8).setCellValue(ps.getEtudiantNom().toUpperCase());
            row.createCell(9).setCellValue(ps.getEtudiantPrenom());
            
            // Filière - couleur par filière
            createColoredCell(row, 10, ps.getFiliere(), workbook, false);
            
            // Appliquer le style de base aux autres cellules
            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i).getCellStyle() == null) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }
        
        // Ajuster les colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 500, 255 * 256));
        }
    }
    
    private void createRecapSheet(Sheet sheet, Workbook workbook) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        createMergedCell(sheet, 0, 0, 3, "RÉCAPITULATIF DES SOUTENANCES PAR PROFESSEUR", titleStyle);
        sheet.createRow(1);
        
        Row headerRow = sheet.createRow(2);
        String[] headers = {"Professeur", "Spécialité", "Participations", "Détail des soutenances"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 3;
        for (Professeur prof : professeurs) {
            String nom = prof.getNomComplet();
            int charge = chargeProfesseurs.get(nom);
            List<DetailSoutenance> details = detailsParProfesseur.get(nom);
            
            Row row = sheet.createRow(rowNum++);
            
            // Professeur avec sa couleur
            createColoredCell(row, 0, nom, workbook, true);
            row.createCell(1).setCellValue(prof.getSpecialite());
            row.createCell(2).setCellValue(charge);
            
            StringBuilder sb = new StringBuilder();
            for (DetailSoutenance d : details) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(d.date).append(" ").append(d.heure).append(" - ").append(d.salle);
                sb.append(" → ").append(d.prenom).append(" ").append(d.nom);
            }
            row.createCell(3).setCellValue(sb.toString());
            
            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i) != null && row.getCell(i).getCellStyle() == null) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 500, 255 * 256));
        }
    }
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createSeparatorStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private void createMergedCell(Sheet sheet, int rowNum, int startCol, int endCol, String value, CellStyle style) {
        Row row = sheet.getRow(rowNum);
        if (row == null) row = sheet.createRow(rowNum);
        Cell cell = row.createCell(startCol);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startCol, endCol));
    }
    
    private void createColoredCell(Row row, int colIndex, String value, Workbook workbook, boolean isProfessor) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        
        if (isProfessor) {
            Short couleur = couleurParProfesseur.get(value);
            if (couleur != null) {
                style.setFillForegroundColor(couleur);
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
        } else {
            Short couleur = couleurParFiliere.get(value);
            if (couleur != null) {
                style.setFillForegroundColor(couleur);
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
        }
        
        cell.setCellStyle(style);
    }
    
    // ==================== CLASSES INTERNES ====================
    
    private static class Creneau {
        String jour, heure, salle;
        Creneau(String jour, String heure, String salle) {
            this.jour = jour; this.heure = heure; this.salle = salle;
        }
    }
    
    private static class DetailSoutenance {
        String date, heure, salle, prenom, nom;
        DetailSoutenance(String date, String heure, String salle, String prenom, String nom) {
            this.date = date; this.heure = heure; this.salle = salle;
            this.prenom = prenom; this.nom = nom;
        }
    }
}