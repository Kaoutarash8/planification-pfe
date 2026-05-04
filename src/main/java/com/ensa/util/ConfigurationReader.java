package com.ensa.util;

import com.ensa.model.ConfigurationSoutenance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ConfigurationReader {
    
    public static ConfigurationSoutenance lireConfiguration(String filePath) throws Exception {
        ConfigurationSoutenance config = new ConfigurationSoutenance();
        
        List<String> salles = new ArrayList<>();
        List<String> jours = new ArrayList<>();
        
        config.setDureeSoutenanceMinutes(60);
        config.setPauseEntreSoutenancesMinutes(15);
        config.setPauseMaxSansSoutenanceHeures(3);
        config.setHeureDebutMatin("09:00");
        config.setHeureFinMatin("12:00");
        config.setHeureDebutApresMidi("14:00");
        config.setHeureFinApresMidi("18:00");
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                Cell keyCell = row.getCell(0);
                if (keyCell == null) continue;
                
                String key = getCellValueAsString(keyCell);
                if (key == null || key.isEmpty()) continue;
                
                Cell valueCell = row.getCell(1);
                String value = "";
                if (valueCell != null) {
                    value = getCellValueAsString(valueCell);
                }
                
                if (value.isEmpty() && row.getCell(2) != null) {
                    value = getCellValueAsString(row.getCell(2));
                }
                
                switch (key.toLowerCase().trim()) {
                    case "duree_soutenance_minutes":
                        try {
                            config.setDureeSoutenanceMinutes(Integer.parseInt(value.replace("min", "").trim()));
                        } catch (NumberFormatException e) {}
                        break;
                    case "heure_debut_matin":
                        config.setHeureDebutMatin(extraireHeure(value));
                        break;
                    case "heure_fin_matin":
                        config.setHeureFinMatin(extraireHeure(value));
                        break;
                    case "heure_debut_apres_midi":
                        config.setHeureDebutApresMidi(extraireHeure(value));
                        break;
                    case "heure_fin_apres_midi":
                        config.setHeureFinApresMidi(extraireHeure(value));
                        break;
                    case "pause_max_sans_soutenance_heures":
                        try {
                            config.setPauseMaxSansSoutenanceHeures(Integer.parseInt(value.replace("h", "").trim()));
                        } catch (NumberFormatException e) {}
                        break;
                    case "salles":
                        for (String s : value.split(",")) {
                            String salle = s.trim();
                            if (!salle.isEmpty()) salles.add(salle);
                        }
                        break;
                    case "date debut soutnance":
                        config.setDateDebutSoutenance(extraireDate(value));
                        break;
                    case "jours":
                        try {
                            config.setNbJours(Integer.parseInt(value));
                        } catch (NumberFormatException e) {}
                        break;
                }
            }
        }
        
        if (!salles.isEmpty()) {
            config.setSalles(salles);
        } else {
            List<String> defaultSalles = new ArrayList<>();
            defaultSalles.add("Salle 16");
            defaultSalles.add("Salle 17");
            defaultSalles.add("Amphi A");
            config.setSalles(defaultSalles);
        }
        
        if (config.getDateDebutSoutenance() != null && !config.getDateDebutSoutenance().isEmpty()) {
            int nbJours = config.getNbJours();
            if (nbJours <= 0) nbJours = 3;
            jours = genererJours(config.getDateDebutSoutenance(), nbJours);
            config.setJours(jours);
        } else {
            List<String> defaultJours = new ArrayList<>();
            defaultJours.add("2026-06-22");
            defaultJours.add("2026-06-23");
            defaultJours.add("2026-06-24");
            config.setJours(defaultJours);
        }
        
        return config;
    }
    
    private static String extraireHeure(String value) {
        if (value == null || value.isEmpty()) return "09:00";
        if (value.contains("1899")) {
            String[] parts = value.split(" ");
            if (parts.length >= 2 && parts[1].length() >= 5) {
                return parts[1].substring(0, 5);
            }
        }
        if (value.matches("\\d{2}:\\d{2}")) return value;
        return "09:00";
    }
    
    private static String extraireDate(String value) {
        if (value == null || value.isEmpty()) return "2026-06-22";
        if (value.contains("2026")) {
            String[] parts = value.split(" ");
            if (parts.length >= 1 && parts[0].contains("-")) {
                return parts[0];
            }
        }
        return "2026-06-22";
    }
    
    private static List<String> genererJours(String dateDebut, int nbJours) {
        List<String> jours = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date debut = sdf.parse(dateDebut);
            Calendar cal = Calendar.getInstance();
            cal.setTime(debut);
            for (int i = 0; i < nbJours; i++) {
                jours.add(sdf.format(cal.getTime()));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            jours.add("2026-06-22");
            jours.add("2026-06-23");
            jours.add("2026-06-24");
        }
        return jours;
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return sdf.format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }
}