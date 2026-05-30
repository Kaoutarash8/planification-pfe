package com.ensa.dao.impl;

import com.ensa.dao.interfaces.ConfigurationDao;
import com.ensa.model.ConfigurationSoutenance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConfigurationExcelDao implements ConfigurationDao {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Liste des clés valides pour chaque paramètre
    private static final Set<String> KEYS_DUREE = new HashSet<>(Arrays.asList("duree_soutenance_minutes", "duree", "duree_soutenance"));
    private static final Set<String> KEYS_HEURE_DEBUT_MATIN = new HashSet<>(Arrays.asList("heure_debut_matin", "debut_matin", "heure_debut_matin", "hdm"));
    private static final Set<String> KEYS_HEURE_FIN_MATIN = new HashSet<>(Arrays.asList("heure_fin_matin", "fin_matin", "heure_fin_matin", "hfm"));
    private static final Set<String> KEYS_HEURE_DEBUT_APRES_MIDI = new HashSet<>(Arrays.asList("heure_debut_apres_midi", "debut_apres_midi", "hdm"));
    private static final Set<String> KEYS_HEURE_FIN_APRES_MIDI = new HashSet<>(Arrays.asList("heure_fin_apres_midi", "fin_apres_midi", "hfa"));
    private static final Set<String> KEYS_PAUSE = new HashSet<>(Arrays.asList("pause_max_sans_soutenance_heures", "pause_max", "pause"));
    private static final Set<String> KEYS_SALLES = new HashSet<>(Arrays.asList("salles", "salle"));
    private static final Set<String> KEYS_DATE = new HashSet<>(Arrays.asList("date_debut_soutenance", "date_debut", "date"));
    private static final Set<String> KEYS_JOURS = new HashSet<>(Arrays.asList("jours", "nb_jours", "nombre_jours"));
    private static final Set<String> KEYS_INFO_MIN = new HashSet<>(Arrays.asList("info_prof_min", "min_info", "info_min"));
    private static final Set<String> KEYS_MAX_PAR_JOUR = new HashSet<>(Arrays.asList("max_soutenances_par_jour_par_prof", "max_par_jour"));
    
    @Override
    public ConfigurationSoutenance lireConfiguration(String filePath) throws Exception {
        ConfigurationSoutenance config = new ConfigurationSoutenance();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.err.println("Sheet not found, using default values");
                return config;
            }
            
            // Trouver la ligne où commence la configuration (chercher "duree" ou "heure")
            int startRow = trouverLigneConfiguration(sheet);
            
            System.out.println("Configuration trouvee a partir de la ligne: " + startRow);
            
            // Parcourir les lignes de configuration
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell keyCell = row.getCell(0);
                if (keyCell == null) continue;
                
                String key = getCellStringValue(keyCell).toLowerCase().trim();
                if (key.isEmpty()) continue;
                
                // Ignorer les lignes qui contiennent des CNE ou autres données non config
                if (estDonneeEtudiant(key)) {
                    continue;
                }
                
                Cell valueCell = row.getCell(1);
                if (valueCell == null) {
                    valueCell = row.getCell(2);
                }
                if (valueCell == null) continue;
                
                String value = getCellStringValue(valueCell);
                if (value.isEmpty()) continue;
                
                System.out.println("Lecture config: " + key + " = " + value);
                
                // Traitement selon la clé
                if (KEYS_DUREE.contains(key)) {
                    int duree = extractInteger(value);
                    if (duree > 0) {
                        config.setDureeSoutenanceMinutes(duree);
                    }
                }
                else if (KEYS_HEURE_DEBUT_MATIN.contains(key)) {
                    String heure = extractTime(value);
                    if (heure != null) config.setHeureDebutMatin(heure);
                }
                else if (KEYS_HEURE_FIN_MATIN.contains(key)) {
                    String heure = extractTime(value);
                    if (heure != null) config.setHeureFinMatin(heure);
                }
                else if (KEYS_HEURE_DEBUT_APRES_MIDI.contains(key)) {
                    String heure = extractTime(value);
                    if (heure != null) config.setHeureDebutApresMidi(heure);
                }
                else if (KEYS_HEURE_FIN_APRES_MIDI.contains(key)) {
                    String heure = extractTime(value);
                    if (heure != null) config.setHeureFinApresMidi(heure);
                }
                else if (KEYS_PAUSE.contains(key)) {
                    int pause = extractInteger(value);
                    if (pause >= 0) {
                        config.setPauseMaxSansSoutenanceHeures(pause);
                    }
                }
                else if (KEYS_MAX_PAR_JOUR.contains(key)) {
                    int max = extractInteger(value);
                    if (max > 0) {
                        config.setMaxSoutenancesParJourParProf(max);
                    }
                }
                else if (KEYS_SALLES.contains(key)) {
                    List<String> salles = extractSalles(value);
                    if (!salles.isEmpty()) {
                        config.setSalles(salles);
                    }
                }
                else if (KEYS_DATE.contains(key)) {
                    String date = extractDate(value);
                    if (date != null) {
                        config.setDateDebutSoutenance(date);
                    }
                }
                else if (KEYS_JOURS.contains(key)) {
                    int jours = extractInteger(value);
                    if (jours > 0) {
                        config.setNbJours(jours);
                    }
                }
                else if (KEYS_INFO_MIN.contains(key)) {
                    int minInfo = extractInteger(value);
                    if (minInfo > 0) {
                        config.setMinInfoParJury(minInfo);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lecture fichier configuration: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Appliquer valeurs par défaut si nécessaires
        appliquerValeursParDefaut(config);
        
        return config;
    }
    
    private int trouverLigneConfiguration(Sheet sheet) {
        // Chercher la ligne qui contient "duree" ou "heure" dans les 20 premières lignes
        for (int i = 0; i <= 20 && i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            Cell cell = row.getCell(0);
            if (cell == null) continue;
            
            String value = getCellStringValue(cell).toLowerCase();
            if (value.contains("duree") || value.contains("heure") || value.contains("pause") || 
                value.contains("salle") || value.contains("date") || value.contains("jours")) {
                return i;
            }
        }
        return 0;
    }
    
    private boolean estDonneeEtudiant(String key) {
        // Vérifier si la clé ressemble à un CNE ou un nom d'étudiant
        if (key.length() > 8 && key.matches(".*[0-9].*")) {
            return true;
        }
        if (key.matches("^[a-z]{2}[0-9]{9}.*")) { // Format CNE
            return true;
        }
        return false;
    }
    
    private void appliquerValeursParDefaut(ConfigurationSoutenance config) {
        if (config.getDureeSoutenanceMinutes() <= 0) {
            config.setDureeSoutenanceMinutes(60);
        }
        if (config.getHeureDebutMatin() == null || config.getHeureDebutMatin().isEmpty()) {
            config.setHeureDebutMatin("09:00");
        }
        if (config.getHeureFinMatin() == null || config.getHeureFinMatin().isEmpty()) {
            config.setHeureFinMatin("12:00");
        }
        if (config.getHeureDebutApresMidi() == null || config.getHeureDebutApresMidi().isEmpty()) {
            config.setHeureDebutApresMidi("14:00");
        }
        if (config.getHeureFinApresMidi() == null || config.getHeureFinApresMidi().isEmpty()) {
            config.setHeureFinApresMidi("18:00");
        }
        if (config.getPauseMaxSansSoutenanceHeures() < 0) {
            config.setPauseMaxSansSoutenanceHeures(0);
        }
        if (config.getMaxSoutenancesParJourParProf() <= 0) {
            config.setMaxSoutenancesParJourParProf(4);
        }
        if (config.getSalles() == null || config.getSalles().isEmpty()) {
            List<String> defaultSalles = new ArrayList<>();
            defaultSalles.add("Salle 16");
            defaultSalles.add("Salle 17");
            defaultSalles.add("Amphi A");
            config.setSalles(defaultSalles);
        }
        if (config.getDateDebutSoutenance() == null || config.getDateDebutSoutenance().isEmpty()) {
            config.setDateDebutSoutenance("22/06/2026");
        }
        if (config.getMinInfoParJury() <= 0) {
            config.setMinInfoParJury(2);
        }
        
        // Afficher la configuration finale
        System.out.println("\n=== CONFIGURATION FINALE ===");
        System.out.println("  Duree: " + config.getDureeSoutenanceMinutes() + " min");
        System.out.println("  Matin: " + config.getHeureDebutMatin() + " - " + config.getHeureFinMatin());
        System.out.println("  Apres-midi: " + config.getHeureDebutApresMidi() + " - " + config.getHeureFinApresMidi());
        System.out.println("  Pause: " + config.getPauseMaxSansSoutenanceHeures() + " h");
        System.out.println("  Max par jour par prof: " + config.getMaxSoutenancesParJourParProf());
        System.out.println("  Min info par jury: " + config.getMinInfoParJury());
        System.out.println("  Salles: " + config.getSalles());
        System.out.println("  Nombre de salles: " + config.getSalles().size());
        System.out.println("  Date debut: " + config.getDateDebutSoutenance());
        System.out.println("  Jours: " + (config.getNbJours() > 0 ? config.getNbJours() : "auto"));
        System.out.println("  Capacite par jour: " + config.calculerCapaciteParJour());
    }
    
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(cell.getDateCellValue());
                }
                double d = cell.getNumericCellValue();
                if (d == (long) d) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case FORMULA:
                try {
                    return String.valueOf((long) cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }
    
    private int extractInteger(String value) {
        if (value == null || value.isEmpty()) return -1;
        try {
            String digits = value.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                return Integer.parseInt(digits);
            }
        } catch (Exception e) {}
        return -1;
    }
    
    private String extractTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            // Format HH:MM ou H:MM
            if (value.matches("\\d{1,2}:\\d{2}")) {
                String[] parts = value.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return String.format("%02d:%02d", hour, minute);
                }
            }
        } catch (Exception e) {}
        return null;
    }
    
    private List<String> extractSalles(String value) {
        List<String> salles = new ArrayList<>();
        if (value == null || value.isEmpty()) return salles;
        
        String[] parts = value.split(",");
        for (String part : parts) {
            String salle = part.trim();
            if (!salle.isEmpty()) {
                salles.add(salle);
            }
        }
        return salles;
    }
    
    private String extractDate(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            // Format MM/DD/YYYY
            if (value.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] parts = value.split("/");
                if (parts.length == 3) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    // Convertir en DD/MM/YYYY
                    return String.format("%02d/%02d/%d", day, month, year);
                }
            }
            // Format DD/MM/YYYY
            if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return value;
            }
        } catch (Exception e) {}
        return "22/06/2026";
    }
}