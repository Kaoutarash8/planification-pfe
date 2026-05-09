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
            
            boolean hasDuree = false;
            boolean hasHeureDebutMatin = false;
            boolean hasHeureFinMatin = false;
            boolean hasHeureDebutApresMidi = false;
            boolean hasHeureFinApresMidi = false;
            boolean hasPause = false;
            boolean hasSalles = false;
            boolean hasDateDebut = false;
            boolean hasNbJours = false;
            boolean hasMaxParJour = false;
            boolean hasMinInfo = false;
            
            for (Row row : sheet) {
                Cell keyCell = row.getCell(0);
                if (keyCell == null) continue;
                
                String key = getCellValueAsString(keyCell).toLowerCase().trim();
                if (key.isEmpty()) continue;
                
                Cell valueCell = row.getCell(1);
                String value = valueCell != null ? getCellValueAsString(valueCell) : "";
                
                System.out.println("Lecture config: " + key + " = " + value);
                
                switch (key) {
                    case "duree_soutenance_minutes":
                    case "duree":
                        int duree = extractInteger(value);
                        if (duree > 0) {
                            config.setDureeSoutenanceMinutes(duree);
                            hasDuree = true;
                        }
                        break;
                        
                    case "heure_debut_matin":
                    case "debut_matin":
                        String hdm = extractTime(value);
                        if (hdm != null) {
                            config.setHeureDebutMatin(hdm);
                            hasHeureDebutMatin = true;
                        }
                        break;
                        
                    case "heure_fin_matin":
                    case "fin_matin":
                        String hfm = extractTime(value);
                        if (hfm != null) {
                            config.setHeureFinMatin(hfm);
                            hasHeureFinMatin = true;
                        }
                        break;
                        
                    case "heure_debut_apres_midi":
                    case "debut_apres_midi":
                        String hda = extractTime(value);
                        if (hda != null) {
                            config.setHeureDebutApresMidi(hda);
                            hasHeureDebutApresMidi = true;
                        }
                        break;
                        
                    case "heure_fin_apres_midi":
                    case "fin_apres_midi":
                        String hfa = extractTime(value);
                        if (hfa != null) {
                            config.setHeureFinApresMidi(hfa);
                            hasHeureFinApresMidi = true;
                        }
                        break;
                        
                    case "pause_max_sans_soutenance_heures":
                    case "pause_max":
                        int pause = extractInteger(value);
                        if (pause >= 0) {
                            config.setPauseMaxSansSoutenanceHeures(pause);
                            hasPause = true;
                        }
                        break;
                        
                    case "max_soutenances_par_jour_par_prof":
                    case "max_par_jour":
                        int maxParJour = extractInteger(value);
                        if (maxParJour > 0) {
                            config.setMaxSoutenancesParJourParProf(maxParJour);
                            hasMaxParJour = true;
                        }
                        break;
                        
                    case "salles":
                    case "salle":
                        List<String> salles = extractSalles(value);
                        if (!salles.isEmpty()) {
                            config.setSalles(salles);
                            hasSalles = true;
                        }
                        break;
                        
                    case "date_debut_soutenance":
                    case "date_debut":
                    case "date":
                        String date = extractDate(value, valueCell);
                        if (date != null) {
                            config.setDateDebutSoutenance(date);
                            hasDateDebut = true;
                        }
                        break;
                        
                    case "jours":
                    case "nb_jours":
                        int jours = extractInteger(value);
                        if (jours > 0) {
                            config.setNbJours(jours);
                            hasNbJours = true;
                        }
                        break;
                        
                    case "info_prof_min":
                    case "min_info":
                        int minInfo = extractInteger(value);
                        if (minInfo > 0) {
                            config.setMinInfoParJury(minInfo);
                            hasMinInfo = true;
                        }
                        break;
                }
            }
            
            // Appliquer les valeurs par defaut pour les champs manquants
            if (!hasDuree) {
                System.out.println("Valeur par defaut pour duree: 60 minutes");
                config.setDureeSoutenanceMinutes(60);
            }
            
            if (!hasHeureDebutMatin || !hasHeureFinMatin) {
                System.out.println("Valeur par defaut pour les heures du matin: 09:00 - 12:00");
                config.setHeureDebutMatin("09:00");
                config.setHeureFinMatin("12:00");
            }
            
            if (!hasHeureDebutApresMidi || !hasHeureFinApresMidi) {
                System.out.println("Valeur par defaut pour les heures de l'apres-midi: 14:00 - 18:00");
                config.setHeureDebutApresMidi("14:00");
                config.setHeureFinApresMidi("18:00");
            }
            
            if (!hasPause) {
                System.out.println("Valeur par defaut pour la pause: 2 heures");
                config.setPauseMaxSansSoutenanceHeures(2);
            }
            
            if (!hasMaxParJour) {
                System.out.println("Valeur par defaut pour max par jour par prof: 4");
                config.setMaxSoutenancesParJourParProf(4);
            }
            
            if (!hasSalles) {
                System.out.println("Valeur par defaut pour les salles: Salle 16, Salle 17, Amphi A");
                List<String> defaultSalles = new ArrayList<>();
                defaultSalles.add("Salle 16");
                defaultSalles.add("Salle 17");
                defaultSalles.add("Amphi A");
                config.setSalles(defaultSalles);
            }
            
            if (!hasDateDebut) {
                System.out.println("Valeur par defaut pour la date de debut: 22/06/2026");
                config.setDateDebutSoutenance("22/06/2026");
            }
            
            if (!hasMinInfo) {
                System.out.println("Valeur par defaut pour min informaticiens par jury: 2");
                config.setMinInfoParJury(2);
            }
            
            // ============================================================
            // CALCUL AUTOMATIQUE DU NOMBRE DE JOURS SI NON DEFINI
            // ============================================================
            if (!hasNbJours || config.getNbJours() <= 0) {
                System.out.println("\n=== CALCUL AUTOMATIQUE DU NOMBRE DE JOURS ===");
                System.out.println("Le nombre de jours n'est pas defini dans la configuration");
                System.out.println("Calcul en cours...");
                
                // Pour l'instant, on met une valeur par defaut
                // Le calcul reel se fera dans PlanningServlet avec le nombre d'etudiants
                config.setNbJours(3);
                System.out.println("Valeur par defaut: 3 jours");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lecture fichier configuration: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Validation et correction des heures
        validerEtCorrigerHeures(config);
        
        // Calculer les capacites
        int capaciteParJour = config.calculerCapaciteParJour();
        int capaciteTotale = config.calculerCapaciteTotale();
        
        System.out.println("\n=== CONFIGURATION FINALE ===");
        System.out.println("  Duree soutenance: " + config.getDureeSoutenanceMinutes() + " min");
        System.out.println("  Horaires matin: " + config.getHeureDebutMatin() + " -> " + config.getHeureFinMatin());
        System.out.println("  Horaires apres-midi: " + config.getHeureDebutApresMidi() + " -> " + config.getHeureFinApresMidi());
        System.out.println("  Pause entre soutenances: " + config.getPauseMaxSansSoutenanceHeures() + " h");
        System.out.println("  Max par jour par professeur: " + config.getMaxSoutenancesParJourParProf());
        System.out.println("  Minimum informaticiens par jury: " + config.getMinInfoParJury());
        System.out.println("  Salles: " + config.getSalles());
        System.out.println("  Nombre de salles: " + config.getSalles().size());
        System.out.println("  Date debut: " + config.getDateDebutSoutenance());
        System.out.println("  Nombre de jours: " + config.getNbJours());
        System.out.println("  Capacite par jour: " + capaciteParJour + " soutenances");
        System.out.println("  Capacite totale: " + capaciteTotale + " soutenances");
        
        return config;
    }
    
    private void validerEtCorrigerHeures(ConfigurationSoutenance config) {
        try {
            String[] hdm = config.getHeureDebutMatin().split(":");
            String[] hfm = config.getHeureFinMatin().split(":");
            int debutMatin = Integer.parseInt(hdm[0]) * 60 + Integer.parseInt(hdm[1]);
            int finMatin = Integer.parseInt(hfm[0]) * 60 + Integer.parseInt(hfm[1]);
            if (debutMatin >= finMatin) {
                System.out.println("Heures matin invalides, correction...");
                config.setHeureDebutMatin("09:00");
                config.setHeureFinMatin("12:00");
            }
        } catch(Exception e) {
            System.out.println("Format heures matin invalide, correction...");
            config.setHeureDebutMatin("09:00");
            config.setHeureFinMatin("12:00");
        }
        
        try {
            String[] hda = config.getHeureDebutApresMidi().split(":");
            String[] hfa = config.getHeureFinApresMidi().split(":");
            int debutApresMidi = Integer.parseInt(hda[0]) * 60 + Integer.parseInt(hda[1]);
            int finApresMidi = Integer.parseInt(hfa[0]) * 60 + Integer.parseInt(hfa[1]);
            if (debutApresMidi >= finApresMidi) {
                System.out.println("Heures apres-midi invalides, correction...");
                config.setHeureDebutApresMidi("14:00");
                config.setHeureFinApresMidi("18:00");
            }
        } catch(Exception e) {
            System.out.println("Format heures apres-midi invalide, correction...");
            config.setHeureDebutApresMidi("14:00");
            config.setHeureFinApresMidi("18:00");
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(date);
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case FORMULA:
                try {
                    return String.valueOf((long) cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
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
        } catch (Exception e) {
            System.err.println("Erreur extraction entier: " + value);
        }
        return -1;
    }
    
    private String extractTime(String value) {
        if (value == null || value.isEmpty()) return null;
        
        try {
            if (value.matches("\\d{1,2}:\\d{2}")) {
                String[] parts = value.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                    return String.format("%02d:%02d", hours, minutes);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur extraction heure: " + value);
        }
        return null;
    }
    
    private List<String> extractSalles(String value) {
        List<String> salles = new ArrayList<>();
        if (value == null || value.isEmpty()) return salles;
        
        System.out.println("Extraction des salles depuis: '" + value + "'");
        
        String[] parts = value.split(",");
        for (String part : parts) {
            String salle = part.trim();
            if (!salle.isEmpty()) {
                salles.add(salle);
                System.out.println("  Salle trouvee: '" + salle + "'");
            }
        }
        
        return salles;
    }
    
    private String extractDate(String value, Cell valueCell) {
        if (value == null || value.isEmpty()) return null;
        
        try {
            if (valueCell != null && valueCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(valueCell)) {
                Date date = valueCell.getDateCellValue();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                return sdf.format(date);
            }
            
            if (value.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] parts = value.split("/");
                if (parts.length == 3) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    return String.format("%02d/%02d/%d", day, month, year);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur extraction date: " + value);
        }
        return null;
    }
}