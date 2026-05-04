package com.ensa.service;

import com.ensa.model.ConfigurationSoutenance;
import com.ensa.model.Etudiant;
import com.ensa.model.PlanningSoutenance;
import com.ensa.model.Professeur;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanningService {
    
    private List<Etudiant> etudiantsAvecAffectation;
    private List<Professeur> professeurs;
    private ConfigurationSoutenance config;
    
    private Map<String, Integer> chargeProfesseurs = new HashMap<>();
    private Map<String, Map<String, Integer>> chargeParJour = new HashMap<>();
    
    private List<PlanningSoutenance> planningGlobal = new ArrayList<>();
    
    private Map<String, Short> couleurParProfesseur = new HashMap<>();
    private Map<String, Short> couleurParFiliere = new HashMap<>();
    
    private final short[] COULEURS_PROF = {
        IndexedColors.LIGHT_GREEN.getIndex(),
        IndexedColors.LIGHT_BLUE.getIndex(),
        IndexedColors.LIGHT_YELLOW.getIndex(),
        IndexedColors.LIGHT_ORANGE.getIndex(),
        IndexedColors.LIGHT_TURQUOISE.getIndex(),
        IndexedColors.ROSE.getIndex(),
        IndexedColors.LAVENDER.getIndex(),
        IndexedColors.TAN.getIndex(),
        IndexedColors.PALE_BLUE.getIndex(),
        IndexedColors.CORAL.getIndex(),
        IndexedColors.SEA_GREEN.getIndex()
    };
    
    private final short[] COULEURS_FILIERE = {
        IndexedColors.LIGHT_GREEN.getIndex(),
        IndexedColors.LIGHT_BLUE.getIndex(),
        IndexedColors.LIGHT_YELLOW.getIndex(),
        IndexedColors.LIGHT_ORANGE.getIndex(),
        IndexedColors.LIGHT_TURQUOISE.getIndex(),
        IndexedColors.ROSE.getIndex()
    };
    
    public PlanningService(List<Etudiant> etudiants, List<Professeur> professeurs, ConfigurationSoutenance config) {
        this.etudiantsAvecAffectation = etudiants;
        this.professeurs = professeurs;
        this.config = config;
        
        int profIndex = 0;
        for (Professeur p : professeurs) {
            String nom = p.getNomComplet();
            chargeProfesseurs.put(nom, 0);
            chargeParJour.put(nom, new HashMap<>());
            couleurParProfesseur.put(nom, COULEURS_PROF[profIndex % COULEURS_PROF.length]);
            profIndex++;
        }
        
        Set<String> filieres = new HashSet<>();
        for (Etudiant e : etudiantsAvecAffectation) {
            String filiere = e.getFiliere();
            if (filiere == null || filiere.isEmpty()) filiere = "INFO";
            filieres.add(filiere);
        }
        int filiereIndex = 0;
        for (String filiere : filieres) {
            couleurParFiliere.put(filiere, COULEURS_FILIERE[filiereIndex % COULEURS_FILIERE.length]);
            filiereIndex++;
        }
    }
    
    public List<PlanningSoutenance> genererPlanning() throws Exception {
        planningGlobal.clear();
        
        if (etudiantsAvecAffectation == null || etudiantsAvecAffectation.isEmpty()) {
            throw new Exception("Aucun étudiant avec affectation trouvé");
        }
        
        if (professeurs == null || professeurs.isEmpty()) {
            throw new Exception("Aucun professeur trouvé");
        }
        
        List<String> jours = config.getJours();
        if (jours == null || jours.isEmpty()) {
            throw new Exception("Aucun jour configuré");
        }
        
        List<String> salles = config.getSalles();
        if (salles == null || salles.isEmpty()) {
            throw new Exception("Aucune salle configurée");
        }
        
        Collections.sort(jours);
        
        List<String> tousCreneaux = new ArrayList<>();
        tousCreneaux.addAll(genererCreneaux(config.getHeureDebutMatin(), config.getHeureFinMatin()));
        tousCreneaux.addAll(genererCreneaux(config.getHeureDebutApresMidi(), config.getHeureFinApresMidi()));
        if (tousCreneaux.isEmpty()) {
            tousCreneaux = getDefaultCreneaux();
        }
        Collections.sort(tousCreneaux);
        
        // Calculer la répartition équilibrée par jour
        int totalEtudiants = etudiantsAvecAffectation.size();
        int nbJours = jours.size();
        int baseParJour = totalEtudiants / nbJours;
        int reste = totalEtudiants % nbJours;
        
        List<Integer> repartitionJours = new ArrayList<>();
        for (int i = 0; i < nbJours; i++) {
            repartitionJours.add(baseParJour + (i < reste ? 1 : 0));
        }
        
        // Mélanger les étudiants
        List<Etudiant> tousEtudiants = new ArrayList<>(etudiantsAvecAffectation);
        Collections.shuffle(tousEtudiants);
        
        // Répartir par jour
        List<List<Etudiant>> etudiantsParJour = new ArrayList<>();
        int idx = 0;
        for (int i = 0; i < nbJours; i++) {
            List<Etudiant> jourList = new ArrayList<>();
            for (int j = 0; j < repartitionJours.get(i) && idx < tousEtudiants.size(); j++) {
                jourList.add(tousEtudiants.get(idx++));
            }
            etudiantsParJour.add(jourList);
        }
        
        // Calcul de la charge cible par professeur
        int totalParticipations = totalEtudiants * 3;
        int nbProfesseurs = professeurs.size();
        int cibleParticipations = totalParticipations / nbProfesseurs;
        int resteParticipations = totalParticipations % nbProfesseurs;
        
        Map<String, Integer> cibleParProf = new HashMap<>();
        List<Professeur> profsList = new ArrayList<>(professeurs);
        for (int i = 0; i < profsList.size(); i++) {
            String nom = profsList.get(i).getNomComplet();
            int cible = cibleParticipations + (i < resteParticipations ? 1 : 0);
            cibleParProf.put(nom, cible);
        }
        
        System.out.println("=== OBJECTIF PAR PROFESSEUR ===");
        for (Map.Entry<String, Integer> entry : cibleParProf.entrySet()) {
            System.out.println("  " + entry.getKey() + " doit avoir " + entry.getValue() + " participations");
        }
        
        // Planification
        int planningIndex = 0;
        
        for (int jourIndex = 0; jourIndex < nbJours; jourIndex++) {
            String jour = jours.get(jourIndex);
            List<Etudiant> etudiantsJour = etudiantsParJour.get(jourIndex);
            
            int creneauIndex = 0;
            int salleIndex = 0;
            
            for (Etudiant etudiant : etudiantsJour) {
                String encadrant = etudiant.getEncadrantComplet();
                String creneau = tousCreneaux.get(creneauIndex % tousCreneaux.size());
                String salle = salles.get(salleIndex % salles.size());
                
                List<String> jurys = selectionnerJurysEquilibres(encadrant, cibleParProf, jour);
                
                PlanningSoutenance ps = new PlanningSoutenance();
                ps.setDate(jour);
                ps.setHeure(creneau);
                ps.setSalle(salle);
                ps.setEtudiantCNE(etudiant.getCne());  // ← LE VRAI CNE
                ps.setEtudiantNom(etudiant.getNom());
                ps.setEtudiantPrenom(etudiant.getPrenom());
                ps.setFiliere(etudiant.getFiliere());
                ps.setEncadrant(encadrant);
                ps.setJury1(jurys.get(0));
                ps.setJury2(jurys.get(1));
                
                planningGlobal.add(ps);
                
                incrementerCharge(encadrant, jour);
                incrementerCharge(jurys.get(0), jour);
                incrementerCharge(jurys.get(1), jour);
                
                creneauIndex++;
                salleIndex++;
                planningIndex++;
            }
        }
        
        // Trier par date et heure
        planningGlobal.sort((p1, p2) -> {
            int cmp = p1.getDate().compareTo(p2.getDate());
            if (cmp != 0) return cmp;
            return p1.getHeure().compareTo(p2.getHeure());
        });
        
        // Afficher le bilan final
        System.out.println("=== BILAN FINAL ===");
        int max = 0, min = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : chargeProfesseurs.entrySet()) {
            max = Math.max(max, entry.getValue());
            min = Math.min(min, entry.getValue());
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Écart max: " + (max - min));
        
        return planningGlobal;
    }
    
    private List<String> selectionnerJurysEquilibres(String encadrant, Map<String, Integer> cibleParProf, String jour) {
        List<String> jurys = new ArrayList<>();
        
        List<Professeur> disponibles = new ArrayList<>(professeurs);
        disponibles.removeIf(p -> p.getNomComplet().equals(encadrant));
        
        if (disponibles.isEmpty()) {
            jurys.add("Non disponible");
            jurys.add("Non disponible");
            return jurys;
        }
        
        // Trier par écart à la cible
        disponibles.sort((p1, p2) -> {
            String nom1 = p1.getNomComplet();
            String nom2 = p2.getNomComplet();
            int ecart1 = chargeProfesseurs.get(nom1) - cibleParProf.get(nom1);
            int ecart2 = chargeProfesseurs.get(nom2) - cibleParProf.get(nom2);
            return Integer.compare(ecart1, ecart2);
        });
        
        // Séparer INFO et autres
        List<Professeur> profsInfo = new ArrayList<>();
        List<Professeur> autresProfs = new ArrayList<>();
        
        for (Professeur p : disponibles) {
            String specialite = p.getSpecialite();
            if (specialite != null && specialite.toLowerCase().contains("info")) {
                profsInfo.add(p);
            } else {
                autresProfs.add(p);
            }
        }
        
        // Trier par charge du jour
        profsInfo.sort((p1, p2) -> {
            int charge1 = chargeParJour.get(p1.getNomComplet()).getOrDefault(jour, 0);
            int charge2 = chargeParJour.get(p2.getNomComplet()).getOrDefault(jour, 0);
            return Integer.compare(charge1, charge2);
        });
        
        autresProfs.sort((p1, p2) -> {
            int charge1 = chargeParJour.get(p1.getNomComplet()).getOrDefault(jour, 0);
            int charge2 = chargeParJour.get(p2.getNomComplet()).getOrDefault(jour, 0);
            return Integer.compare(charge1, charge2);
        });
        
        if (profsInfo.size() >= 2) {
            jurys.add(profsInfo.get(0).getNomComplet());
            jurys.add(profsInfo.get(1).getNomComplet());
        } else if (profsInfo.size() == 1) {
            jurys.add(profsInfo.get(0).getNomComplet());
            jurys.add(autresProfs.isEmpty() ? profsInfo.get(0).getNomComplet() : autresProfs.get(0).getNomComplet());
        } else {
            jurys.add(autresProfs.get(0).getNomComplet());
            jurys.add(autresProfs.size() > 1 ? autresProfs.get(1).getNomComplet() : autresProfs.get(0).getNomComplet());
        }
        
        return jurys;
    }
    
    private void incrementerCharge(String professeur, String jour) {
        chargeProfesseurs.put(professeur, chargeProfesseurs.getOrDefault(professeur, 0) + 1);
        Map<String, Integer> jourMap = chargeParJour.get(professeur);
        if (jourMap != null) {
            jourMap.put(jour, jourMap.getOrDefault(jour, 0) + 1);
        }
    }
    
    private List<String> getDefaultCreneaux() {
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
    
    private List<String> genererCreneaux(String debut, String fin) {
        List<String> creneaux = new ArrayList<>();
        if (debut == null || fin == null) return creneaux;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date debutDate = sdf.parse(debut);
            Date finDate = sdf.parse(fin);
            
            int dureeMinutes = config.getDureeSoutenanceMinutes();
            int pauseMinutes = config.getPauseEntreSoutenancesMinutes();
            
            long dureeMs = dureeMinutes * 60 * 1000L;
            long pauseMs = pauseMinutes * 60 * 1000L;
            
            Date current = debutDate;
            while (current.getTime() + dureeMs <= finDate.getTime()) {
                creneaux.add(sdf.format(current));
                current = new Date(current.getTime() + dureeMs + pauseMs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return creneaux;
    }
    
    // ==================== EXPORT EXCEL ====================
    
    public void sauvegarderPlanning(String outputPath) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet planningSheet = workbook.createSheet("Planning_Soutenances");
            createPlanningSheet(planningSheet, workbook);
            
            Sheet recapSheet = workbook.createSheet("Recap_par_Professeur");
            createRecapSheet(recapSheet, workbook);
            
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
    }
    
    private void createPlanningSheet(Sheet sheet, Workbook workbook) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle separatorStyle = createSeparatorStyle(workbook);
        
        createMergedCell(sheet, 0, 0, 10, "ECOLE NATIONALE DES SCIENCES APPLIQUÉES - AL HOCEIMA", titleStyle);
        createMergedCell(sheet, 1, 0, 10, "Département Mathématiques et Informatique", titleStyle);
        createMergedCell(sheet, 2, 0, 10, "PLANNING DES SOUTENANCES DES PROJETS DE FIN D'ÉTUDE", titleStyle);
        createMergedCell(sheet, 3, 0, 10, "Première Session - Année Universitaire 2024/2025", titleStyle);
        
        String dateDebut = config.getJours().get(0);
        String dateFin = config.getJours().get(config.getJours().size() - 1);
        createMergedCell(sheet, 4, 0, 10, "Période : " + dateDebut + " → " + dateFin, titleStyle);
        
        sheet.createRow(5);
        
        // En-têtes avec CNE
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
            // Ligne de séparation entre jours
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
            row.createCell(1).setCellValue(ps.getEtudiantCNE());  // ← CNE original
            row.createCell(2).setCellValue(ps.getDate());
            row.createCell(3).setCellValue(ps.getHeure());
            row.createCell(4).setCellValue(ps.getSalle());
            createColoredCell(row, 5, ps.getEncadrant(), workbook, true);
            createColoredCell(row, 6, ps.getJury1(), workbook, true);
            createColoredCell(row, 7, ps.getJury2(), workbook, true);
            row.createCell(8).setCellValue(ps.getEtudiantNom());
            row.createCell(9).setCellValue(ps.getEtudiantPrenom());
            createColoredCell(row, 10, ps.getFiliere(), workbook, false);
            
            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i).getCellStyle() == null) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
        }
    }
    
    private void createRecapSheet(Sheet sheet, Workbook workbook) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        createMergedCell(sheet, 0, 0, 4, "RÉCAPITULATIF DES SOUTENANCES PAR PROFESSEUR", titleStyle);
        sheet.createRow(1);
        
        Row headerRow = sheet.createRow(2);
        String[] headers = {"Professeur", "Spécialité", "Participations", "Détail par jour", "Étudiants"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        Map<String, List<PlanningSoutenance>> soutenancesParProf = new HashMap<>();
        for (PlanningSoutenance ps : planningGlobal) {
            addSoutenance(soutenancesParProf, ps.getEncadrant(), ps);
            addSoutenance(soutenancesParProf, ps.getJury1(), ps);
            addSoutenance(soutenancesParProf, ps.getJury2(), ps);
        }
        
        int rowNum = 3;
        for (Professeur prof : professeurs) {
            String nom = prof.getNomComplet();
            List<PlanningSoutenance> liste = soutenancesParProf.getOrDefault(nom, new ArrayList<>());
            
            Row row = sheet.createRow(rowNum++);
            
            createColoredCell(row, 0, nom, workbook, true);
            row.createCell(1).setCellValue(prof.getSpecialite());
            row.createCell(2).setCellValue(liste.size());
            
            Map<String, Integer> parJour = new LinkedHashMap<>();
            for (PlanningSoutenance ps : liste) {
                parJour.put(ps.getDate(), parJour.getOrDefault(ps.getDate(), 0) + 1);
            }
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : parJour.entrySet()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(entry.getKey()).append(": ").append(entry.getValue());
            }
            row.createCell(3).setCellValue(sb.toString());
            
            Set<String> etudiantsSet = new LinkedHashSet<>();
            for (PlanningSoutenance ps : liste) {
                etudiantsSet.add(ps.getEtudiantPrenom() + " " + ps.getEtudiantNom());
            }
            row.createCell(4).setCellValue(String.join(", ", etudiantsSet));
            
            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i).getCellStyle() == null) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
        }
    }
    
    private void addSoutenance(Map<String, List<PlanningSoutenance>> map, String prof, PlanningSoutenance ps) {
        if (prof == null || prof.isEmpty()) return;
        map.computeIfAbsent(prof, k -> new ArrayList<>()).add(ps);
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
}